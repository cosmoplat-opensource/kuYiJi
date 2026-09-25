/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.minio;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 文件上传接口
 *
 * @author cosmo-hhim-open Team
 */
public interface ISysFileService
{
    String uploadFile(MultipartFile file,String folder) throws Exception;

    String uploadFileReturnObjectName(MultipartFile file,String folder) throws Exception;

    String uploadFileByName(MultipartFile file,String folder,String fileName) throws Exception;


    String uploadFileReturnObjectName(MultipartFile file,String folder,String fileName) throws Exception;



    InputStream  downloadFile(String bucketName, String objectName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException;

    void deleteFile(String fileName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException;

    Map<String,String> getPresignedPutUrl(String fileName) throws Exception;


    Map<String,String> getPresignedPutUrl(String fileName,String folder) throws Exception;

    Map<String,String> getPreSignedFilePutUrl(String fileName) ;

    String getPresignedGetUrl(String fileName) throws Exception;

    String getPresignedGetUrlNoExpiry(String fileName) throws Exception;

    public String getTemplateFilePath(String filePath);

}
