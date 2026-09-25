/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSortEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @description: 文本计算工具
 * @date 2022/11/15 9:30 上午
 */
@Slf4j
public class TextCalculateUtil {

    /**
     * 计算两字符串相似度算法 Levenshtein Distance(LD)
     *
     * @param str
     * @param target
     * @return
     */
    public static float getSimilarityByLevenshteinDistance(String str, String target) {
        // 矩阵
        int[][] d;
        int n = str.length();
        int m = target.length();
        // 遍历str的
        int i;
        // 遍历target的
        int j;
        // str的
        char ch1;
        // target的
        char ch2;
        // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        int temp;
        if (n == 0 || m == 0) {
            throw new CustomException("要比较的字符串均不能为空");
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

        // 遍历str
        for (i = 1; i <= n; i++) {
            ch1 = str.charAt(i - 1);
            // 去匹配target
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);
                if (ch1 == ch2 || ch1 == ch2 + 32 || ch1 + 32 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + temp);
            }
        }
        return (1 - (float) d[n][m] / Math.max(str.length(), target.length())) * 100F;
    }

    /**
     * 动态规划 求最长公共子序列
     *
     * @param str1
     * @param str2
     * @return
     */
    public static float getSimilarityByLCS(String str1, String str2) { 

        char[] s1 = str1.toCharArray();
        char[] s2 = str2.toCharArray();
        // 此处的棋盘长度要比字符串长度多加1
        int[][] array = new int[str1.length() + 1][str2.length() + 1];

        // 第0行第j列全部赋值为0
        for (int j = 0; j < array[0].length; j++) {
            array[0][j] = 0;
        }
        // 第i行第0列全部赋值为0
        for (int i = 0; i < array.length; i++) {
            array[i][0] = 0;
        }

        // 利用动态规划将数组赋满值
        for (int m = 1; m < array.length; m++) {
            for (int n = 1; n < array[m].length; n++) {
                if (s1[m - 1] == s2[n - 1]) {
                    // 动态规划公式一
                    array[m][n] = array[m - 1][n - 1] + 1;
                } else {
                    // 动态规划公式二
                    array[m][n] = max(array[m - 1][n], array[m][n - 1]);
                }
            }
        }

        Stack<Character> stack = new Stack<Character>();
        int i = str1.length() - 1;
        int j = str2.length() - 1;

        while ((i >= 0) && (j >= 0)) {
            // 字符串从后开始遍历，如若相等，则存入栈中
            if (s1[i] == s2[j]) {
                stack.push(s1[i]);
                i--;
                j--;
            } else {
                // 如果字符串的字符不同，则在数组中找相同的字符
                // 注意：数组的行列要比字符串中字符的个数大1，因此i和j要各加1
                if (array[i + 1][j] > array[i][j + 1]) {
                    j--;
                } else {
                    i--;
                }
            }
        }

        // 打印输出栈正好是正向输出最大的公共子序列
        String result = "";
        while (!stack.isEmpty()) {
            result += stack.pop().toString();
        }
        log.info("最长公共子序列为:{}", result);

        int len = result.length();
        return ((float) len / str1.length() + (float) len / str2.length()) / 2 * 100F;
    }

    /**
     * @param a
     * @param b
     * @return
     * @description 比较(a, b)，输出大的值
     */
    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    /**
     * 获取字符串中的中文字符
     *
     * @param str
     * @return
     */
    public static String getChineseWords(String str) {
        // 存放中文字符
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char aChar : chars) {
            int len = String.valueOf(aChar).getBytes().length;
            // 中文字符有3个字节
            if (len == 3) {
                sb.append(aChar);
            }
        }
        return sb.toString();
    }

    /**
     * 获取英文字符串
     *
     * @param str
     * @return
     */
    public static String getEnglishWords(String str) {
        // 存放英文字符
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char aChar : chars) {
            if (String.valueOf(aChar).getBytes().length == 1) {
                sb.append(aChar);
            }
        }
        return sb.toString();
    }

    /**
     * 模糊匹配后对匹配值排序
     *
     * @param key          模糊查询的值
     * @param toSortList   要匹配的集合
     * @param calcFunction 参与计算的函数
     * @param <T>
     * @return
     */
    public static <T extends MicroSortEntity> List<T> sortBySimilarRadio(String key, List<T> toSortList, Function<T, Integer> calcFunction) {
        try {
            if (!StringUtils.isEmpty(key)) {
                toSortList.forEach(t -> t.setSort(calcFunction.apply(t) - key.length()));
                return toSortList.stream().sorted(Comparator.comparing(MicroSortEntity::getSort)).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("最优排序失败,返回原数据:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return toSortList;
        }
        return toSortList;
    }
}
