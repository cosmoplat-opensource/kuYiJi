/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.domin.MailFileUploadResult;
import com.cosmo.hhim.excel.ExcelUtil;
import com.cosmo.hhim.excel.ExcelUtilService;
import com.cosmo.hhim.mail.MailService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroEmailService;
import com.cosmo.hhim.micro.infrastructure.constant.MailConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Slf4j
@Service
public class MicroEmailServiceImpl implements IMicroEmailService {

    /** 附件上传失败标识 */
    private static final String UPLOAD_FAILURE_FLAG = "0";

    @Autowired
    private MailService mailService;

    @Autowired
    private ExcelUtilService excelUtilService;

    /**
     * 发送邮件
     *
     * @param mailSubject    邮件主题
     * @param mailTemplateId 平台邮件服务的模版id
     * @param receivedBy     邮件接收人
     * @param dataList       导出的数据
     * @param requiredType   导出对象class
     * @return
     */
    @Override
    public <T> String exportAndSendEmail(String mailSubject, String mailTemplateId, String receivedBy, List<T> dataList, Class<T> requiredType, Map<String, Object> placeholderMap) {
        mailSubject = getMailSubject(mailSubject);
        addAppNameForMailContent(placeholderMap);

        // 1. 构建输出文件和存放excel内容
        // 使用横杠替换冒号，避免Windows文件名非法字符问题
        String fileName = mailSubject + LocalDateTime.now().toString().replace(":", "-") + ".xlsx";
        File file = new File(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
            // 2.构造excel
            ExcelUtil<T> excelUtil = new ExcelUtil<>(requiredType);
            excelUtil.exportExcelWorkbook(out, dataList, mailTemplateId);
            fileOutputStream.write(out.toByteArray());
            this.uploadAndSend(mailSubject, mailTemplateId, receivedBy, placeholderMap, file);
        } catch (Exception e) {
            log.error("Failed to send email:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        } finally {
            // 删除创建的文件
            if (file.delete()) {
                log.debug("删除文件成功");
            }
        }

        return mailSubject;
    }

    /**
     * 发送邮件-easyPoi
     *
     * @param mailSubject    邮件主题
     * @param mailTemplateId 平台邮件服务的模版id
     * @param receivedBy     邮件接收人
     * @param dataList       导出的数据
     * @param requiredType   导出对象class
     * @return
     */
    @Override
    public <T> String exportAndSendEmailByEasyPoi(String mailSubject, String mailTemplateId, String receivedBy, List<T> dataList, Class<T> requiredType, Map<String, Object> placeholderMap) {
        mailSubject = getMailSubject(mailSubject);
        addAppNameForMailContent(placeholderMap);

        // 1. 构建输出文件和存放excel内容
        // 使用横杠替换冒号，避免Windows文件名非法字符问题
        String fileName = mailSubject + LocalDateTime.now().toString().replace(":", "-") + ".xlsx";
        File file = new File(fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
            // 2.构造excel
            ByteArrayOutputStream out = excelUtilService.exportExcel(dataList, requiredType, (String) placeholderMap.get(MailConstants.MAIL_REPORT_NAME));
            fileOutputStream.write(out.toByteArray());
            this.uploadAndSend(mailSubject, mailTemplateId, receivedBy, placeholderMap, file);
        } catch (Exception e) {
            log.error("Failed to send email:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        } finally {
            // 删除创建的文件
            if (file.delete()) {
                log.debug("删除文件成功");
            }
        }

        return mailSubject;
    }

    private void uploadAndSend(String mailSubject, String mailTemplateId, String receivedBy, Map<String, Object> placeholderMap, File file) throws Exception {
        // 发送邮件
        // 获取attachmentId
        MailFileUploadResult upload = mailService.upload(file);
        if (CheckObjectUtils.isEmpty(upload) || UPLOAD_FAILURE_FLAG.equals(upload.getFileFlag())) {
            throw new CustomException("附件上传失败");
        }
        List<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(Arrays.stream(upload.getAttachmentId()).collect(Collectors.joining(",")));
        // 调用平台邮件服务接口
        if (mailService.sendEmail(mailTemplateId, receivedBy, null, mailSubject, placeholderMap, attachmentIds)) {
            log.info("发送成功");
        } else {
            log.info("发送失败");
        }
    }

    @Override
    public String exportAndSendEmailByMultiSheet(String mailSubject, String mailTemplateId, String receivedBy, List<Map<String, Object>> multiSheetList, Map<String, Object> placeholderMap) {
        mailSubject = getMailSubject(mailSubject);
        addAppNameForMailContent(placeholderMap);

        // 1. 构建输出文件和存放excel内容
        // 使用横杠替换冒号，避免Windows文件名非法字符问题
        String fileName = mailSubject + LocalDateTime.now().toString().replace(":", "-") + ".xlsx";
        File file = new File(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            // 2.构造excel
            excelUtilService.exportExcelByMultiSheet(out, multiSheetList, fileName);
            fileOutputStream.write(out.toByteArray());
            // 3.发送邮件
            // 获取attachmentId
            this.uploadAndSend(mailSubject, mailTemplateId, receivedBy, placeholderMap, file);
        } catch (Exception e) {
            log.error("Failed to send email:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        } finally {
            // 删除创建的文件
            if (file.delete()) {
                log.debug("删除文件成功");
            }
        }
        return mailSubject;
    }

    /**
     * 邮件主题拼接应用标识
     *
     * @param mailSubject
     * @return
     */
    private String getMailSubject(String mailSubject) {
        String appSign = SecurityUtils.getApplicationSign();
        return mailSubject + "-" + CommonConstant.ApplicationSignEnum.getEnumDesc(appSign);
    }

    /**
     * 邮件内容占位符添加应用名称
     *
     * @param placeholderMap
     * @return
     */
    private void addAppNameForMailContent(Map<String, Object> placeholderMap) {
        String appSign = SecurityUtils.getApplicationSign();
        placeholderMap.put(MailConstants.MAIL_APP_NAME, CommonConstant.ApplicationSignEnum.getEnumDesc(appSign));
    }
}
