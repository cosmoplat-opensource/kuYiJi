/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.domin;

public class MailFileUploadResult {

    private String fileFlag;
    private String message;
    private String[] attachmentId;

    public String getFileFlag() {
        return fileFlag;
    }

    public void setFileFlag(String fileFlag) {
        this.fileFlag = fileFlag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String[] attachmentId) {
        this.attachmentId = attachmentId;
    }
}
