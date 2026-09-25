/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.domain;

import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;

import java.io.Serializable;
import java.util.Objects;

public class ThirdInterfaceEntity implements Serializable {
    private String messageKeys;
    private String messageId;
    private Integer reconsumeTimes;
    private ThirdInterfaceMessage interfaceMessage;
    private ThirdResponse response;
    private ThirdClientTenant clientTenant;

    public ThirdInterfaceEntity(String messageKeys, String messageId, Integer reconsumeTimes, ThirdInterfaceMessage interfaceMessage, ThirdClientTenant clientTenant) {
        this.messageKeys = messageKeys;
        this.messageId = messageId;
        this.reconsumeTimes = reconsumeTimes;
        this.interfaceMessage = interfaceMessage;
        this.clientTenant = clientTenant;
        this.response = ThirdResponse.success(null, null, null);
    }

    public String getMessageKeys() {
        return messageKeys;
    }

    public void setMessageKeys(String messageKeys) {
        this.messageKeys = messageKeys;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Integer getReconsumeTimes() {
        return reconsumeTimes;
    }

    public void setReconsumeTimes(Integer reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    public ThirdInterfaceMessage getInterfaceMessage() {
        return interfaceMessage;
    }

    public void setInterfaceMessage(ThirdInterfaceMessage interfaceMessage) {
        this.interfaceMessage = interfaceMessage;
    }

    public ThirdResponse getResponse() {
        return response;
    }

    public void setResponse(ThirdResponse response) {
        this.response = response;
    }

    public ThirdClientTenant getClientTenant() {
        return clientTenant;
    }

    public void setClientTenant(ThirdClientTenant clientTenant) {
        this.clientTenant = clientTenant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThirdInterfaceEntity that = (ThirdInterfaceEntity) o;
        return Objects.equals(messageKeys, that.messageKeys) && Objects.equals(messageId, that.messageId) && Objects.equals(reconsumeTimes, that.reconsumeTimes) && Objects.equals(interfaceMessage, that.interfaceMessage) && Objects.equals(response, that.response) && Objects.equals(clientTenant, that.clientTenant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageKeys, messageId, reconsumeTimes, interfaceMessage, response, clientTenant);
    }

    @Override
    public String toString() {
        return "ThirdInterfaceEntity{" +
                "messageKeys='" + messageKeys + '\'' +
                ", messageId='" + messageId + '\'' +
                ", reconsumeTimes=" + reconsumeTimes +
                ", interfaceMessage=" + interfaceMessage +
                ", response=" + response +
                ", clientTenant=" + clientTenant +
                '}';
    }
}
