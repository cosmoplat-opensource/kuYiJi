
/**
 * Copyright 2013-2015 JueYue (qrb.jueyue@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * Modifications by 海尔卡奥斯物联科技有限公司:
 *   2026 - Added @Slf4j, replaced e.printStackTrace() with log.error(), fixed Javadoc
 *
 * Original: https://github.com/jueyue/easypoi
 */
package com.cosmo.hhim.excel;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.cosmo.hhim.easypoi.ExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DownloadTemplate {

    public static void test() {

        try {

            List beanList = new ArrayList();
            ExcelExportEntity entity = new ExcelExportEntity("序号", "name");
            entity.setMergeVertical(true);
            ExcelExportEntity entity1 = new ExcelExportEntity("物料编码", "name1");
            entity.setMergeVertical(true);
            ExcelExportEntity entity2 = new ExcelExportEntity("物料名称", "name2");
            entity.setMergeVertical(true);

            ExcelExportEntity entity3 = new ExcelExportEntity("工艺分组", "name3");
            List<ExcelExportEntity> list5 = new ArrayList<>();
            list5.add(new ExcelExportEntity("一级分组", "as"));
            list5.add(new ExcelExportEntity("二级分组", "as1"));
            entity3.setList(list5);

            ExcelExportEntity entity4 = new ExcelExportEntity("Gx001工序1-生产投料", "name4");
            List<ExcelExportEntity> list7 = new ArrayList<>();
            list7.add(new ExcelExportEntity("原材料1", "asa1"));
            list7.add(new ExcelExportEntity("原材料2", "asa11"));
            list7.add(new ExcelExportEntity("原材N", "asaa11"));
            entity4.setList(list7);
            ExcelExportEntity entity5=new ExcelExportEntity("Gx001工序1-技术参数", "asa1");
            List<ExcelExportEntity> list8 = new ArrayList<>();
//            list8.add(new ExcelExportEntity("技术参数", "asaa22"));
//            list8.add(new ExcelExportEntity("技术参数2", "asaa33"));
            entity5.setList(list8);
//            ExcelExportEntity entity5 = new ExcelExportEntity("Gx001工序2", "name5");
//            List<ExcelExportEntity> list10 = new ArrayList<>();
//            list10.add(new ExcelExportEntity("原材料2", "asaa"));
//            list10.add(new ExcelExportEntity("原材料3", "asaa1"));
//            list10.add(new ExcelExportEntity("原材N", "asaaa1"));
//            list10.add(new ExcelExportEntity("技术参数", "asaaa2"));
//            list10.add(new ExcelExportEntity("技术参数2", "asaaa3"));
//            entity5.setList(list10);
            beanList.add(entity);
            beanList.add(entity1);
            beanList.add(entity2);
            beanList.add(entity3);
            beanList.add(entity4);
            beanList.add(entity5);
            List list = new ArrayList<>();
            Map map = new HashMap<>();
            list.add(map);
            Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("测试", "测试"), beanList, list);
            FileOutputStream fos = new FileOutputStream("D:/excel/ExcelExportForMap.tt.xls");
            workbook.write(fos);
            fos.close();

        } catch (FileNotFoundException e) {

            log.error("下载模板文件未找到", e);

        } catch (IOException e) {

            log.error("下载模板IO异常", e);

        }

    }

    public static void main(String[] args) {

        DownloadTemplate.test();

    }


}
