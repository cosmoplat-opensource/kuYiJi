/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.domin;


import java.io.Serializable;
import java.util.Map;


public class SendEmailDTO implements Serializable {

	private String accessKey;
	private String tempId;
	private String receiver;
	private String emailCarbonCopy;
	private String mailTitle;
	private Map<String, Object> params;
	private String attachmentId;

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getTempId() {
		return tempId;
	}

	public void setTempId(String tempId) {
		this.tempId = tempId;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getEmailCarbonCopy() {
		return emailCarbonCopy;
	}

	public void setEmailCarbonCopy(String emailCarbonCopy) {
		this.emailCarbonCopy = emailCarbonCopy;
	}

	public String getMailTitle() {
		return mailTitle;
	}

	public void setMailTitle(String mailTitle) {
		this.mailTitle = mailTitle;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
}
