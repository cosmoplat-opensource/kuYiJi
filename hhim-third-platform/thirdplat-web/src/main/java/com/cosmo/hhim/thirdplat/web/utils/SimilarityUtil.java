/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.utils;

/**
 * [相似度算法]
 * 目前采用的是编辑距离法(有顺序区分),
 * 且只符合对本项目业务需求
 * 如有其他需求,可用余弦相似法求得最优解
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-05
 */
public class SimilarityUtil {
    private static int compare(String source, String target) {
        int[][] d;
        int n = source.length();
        int m = target.length();
        int i;
        int j;
        char c1;
        char c2;
        int temp;
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];
        // 初始化第一列
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        // 初始化第一行
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++) {
            // 遍历source
            c1 = source.charAt(i - 1);
            // 去匹配target
            for (j = 1; j <= m; j++) {
                c2 = target.charAt(j - 1);
                if (c1 == c2 || c1 == c2 + 32 || c1 + 32 == c2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }


    /**
     * 获取最小的值
     */
    private static int min(int one, int two, int three) {
        return (one = Math.min(one, two)) < three ? one : three;
    }

    /**
     * 获取两字符串的相似度：1-（编辑距离）/字符串最大长度
     */
    public static double getSimilarityRatio(String source, String target) {
        int max = Math.max(source.length(), target.length());
        return 1 - (double) compare(source, target) / max;
    }
}