/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.integration;

import com.cosmo.hhim.micro.application.dto.integration.MicroCustomerSuggestionDTO;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroFileDomain;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFiles;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestion;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public class MicroIntegrationAssembler {

    public static MicroCustomerSuggestionDTO toSuggestionDTO(HyzzPortalSuggestion suggestion) {
        MicroCustomerSuggestionDTO suggestionDTO = new MicroCustomerSuggestionDTO();
        suggestionDTO.setContent(suggestion.getContent());
        suggestionDTO.setAttachFiles(convertMicroFile(suggestion.getHyzzFiles()));
        suggestionDTO.setCreateTime(suggestion.getCreateTime());
        suggestionDTO.setUpdateTime(suggestion.getUpdateTime());
        suggestionDTO.setContactNumber(suggestion.getContactPhone());
        suggestionDTO.setDealResult(suggestion.getDealResult());
        suggestionDTO.setStatus(convertSuggestionStatus(suggestion.getStatus()));
        return suggestionDTO;
    }

    /**
     * 未处理	10
     * 处理中	20
     * 已处理	30
     */
    private static String convertSuggestionStatus(String status) {
        String nonProcessed = "未处理";
        String processing = "处理中";
        String done = "已处理";
        switch (status) {
            case "10":
                return nonProcessed;
            case "20":
                return processing;
            case "30":
                return done;
            default:
                return "";
        }
    }


    private static List<MicroFileDomain> convertMicroFile(List<HyzzFiles> hyzzFiles) {
        if (CollectionUtils.isEmpty(hyzzFiles)) {
            return Collections.emptyList();
        }

        MicroFileDomain fileDomain;
        List<MicroFileDomain> result = new ArrayList<>();
        for (HyzzFiles hyzzFile : hyzzFiles) {
            fileDomain = new MicroFileDomain();
            fileDomain.setFileName(hyzzFile.getFileName());
            fileDomain.setFileType(hyzzFile.getFileType());
            fileDomain.setFileSize(hyzzFile.getFileSize());
            fileDomain.setFilePath(hyzzFile.getFilePath());
            result.add(fileDomain);
        }
        return result;
    }

}
