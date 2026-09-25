/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.mail;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.domin.MailFileUploadResult;
import com.cosmo.hhim.domin.SendEmailDTO;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class MailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    @Value("${mail.access-key:please_set_mail_access_key}")
    private String mailAccessKey;

    @Value("${mail.upload-url:please_set_mail_upload_url}")
    private String mailUploadUrl;

    @Value("${mail.send-url:please_set_mail_send_url}")
    private String mailSendUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * @return {"fileFlag":"1","attachmentId":["582E9D31-B7B0-4D7E"],"message":"上传成功"}
     */
    public MailFileUploadResult upload(File file) {
        MediaType type = MediaType.parseMediaType("multipart/form-data");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
        FileSystemResource fileSystemResource =new FileSystemResource(file);
        form.add("file", fileSystemResource);
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(mailUploadUrl, files, String.class);//发送
        return JSONObject.parseObject(responseEntity.getBody(),MailFileUploadResult.class) ;
    }
    /**
     * 发送邮件（带附件）
     * @param templateId 邮件的模板id
     * @param receivedBy 接收人
     * @param emailCarbonCopy 邮件抄送人
     * @param mailTitle 邮件主题
     * @param params 邮件内容
     * @param attachmentIds 附件id
     */
    public boolean sendEmail(String templateId, String receivedBy, String emailCarbonCopy, String mailTitle, Map<String, Object> params, List<String> attachmentIds) throws Exception {
        boolean result = false;
        SendEmailDTO emailDTO = new SendEmailDTO();
        emailDTO.setAccessKey(mailAccessKey);
        emailDTO.setReceiver(receivedBy);
        emailDTO.setEmailCarbonCopy(emailCarbonCopy);
        emailDTO.setMailTitle(mailTitle);
        emailDTO.setTempId(templateId);
        emailDTO.setParams(params);
        if(!CollectionUtils.isEmpty(attachmentIds)) {
            emailDTO.setAttachmentId(Joiner.on(",").skipNulls().join(attachmentIds));
        }
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(emailDTO), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(mailSendUrl, request, String.class);
        JSONObject jsonObject =  JSONObject.parseObject(responseEntity.getBody());
        if ("0".equals(jsonObject.getString("code"))) {
            result = true;
            LOGGER.info("发送邮件通知给  {} 成功", receivedBy);
        } else if ("1".equals(jsonObject.getString("code"))) {
            LOGGER.info("发送邮件通知给  {} 失败", receivedBy);
        }

        return result;
    }


    /**
     * 发送邮件
     * @param templateId 邮件的模板id
     * @param receivedBy 接收人
     * @param emailCarbonCopy 邮件抄送人
     * @param mailTitle 邮件主题
     * @param params 邮件内容
     */
    public boolean sendEmail(String templateId, String receivedBy, String emailCarbonCopy, String mailTitle, Map<String, Object> params) throws Exception {
        return sendEmail(templateId, receivedBy, emailCarbonCopy, mailTitle, params, null);
    }
}
