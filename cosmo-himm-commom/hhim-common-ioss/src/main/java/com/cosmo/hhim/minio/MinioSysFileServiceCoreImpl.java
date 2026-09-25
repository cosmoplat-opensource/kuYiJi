/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.minio;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.*;
import com.cosmo.hhim.common.core.utils.file.MimeTypeUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Minio 文件存储
 *
 * @author cosmo-hhim-open Team
 */
@Primary
@Service
public class MinioSysFileServiceCoreImpl implements ISysFileService {
    @Autowired(required = false)
    private MinioConfig minioConfig;

    @Autowired(required = false)
    private MinioClient client;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Value("${minio.fileFolder}")
    private String fileFolder;
    @Value("${spring.profiles.active}")
    private String active;
    @Value("${minio.url}")
    private String minioUrl;

    /**
     * fileName 文件名重新生成 时间
     *
     * @param file
     * @param folder
     * @return
     * @throws Exception
     */
    @Override
    public String uploadFile(MultipartFile file, @NotNull String folder) throws Exception {
        return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + uploadFileReturnObjectName(file, folder);
    }

    /**
     * fileName 文件名重新生成 时间
     *
     * @param file
     * @param folder
     * @return
     * @throws Exception
     */
    @Override
    public String uploadFileReturnObjectName(MultipartFile file, String folder) throws Exception {
        // 文件类型白名单校验：仅允许常规图片/文档/音视频类型，防止上传可执行脚本等危险文件
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        String fileName = FileUploadUtils.extractFilename(file);
        if(StringUtils.isEmpty(folder)){
            throw new RuntimeException("folder不能为空");
        }
        String customer=null;
        if(ThreadContext.get(Constants.TARGET_CUSTOMER)!=null){
            customer=ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        }else{
            customer= ServletUtils.getRequest().getHeader(Constants.TARGET_CUSTOMER);
        }
        fileName = customer + "/" + fileFolder + "/" + folder + "/" + fileName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build();
        client.putObject(args);
        return fileName;
    }

    /**
     * fileName 文件名相同会覆盖源文件
     *
     * @param file
     * @param folder
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public String uploadFileByName(MultipartFile file,String folder, String fileName) throws Exception {
        if(StringUtils.isEmpty(folder)){
            throw new RuntimeException("folder不能为空");
        }
        return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + uploadFileReturnObjectName(file, folder, fileName);
    }

    /**
     * fileName 文件名相同会覆盖源文件
     *
     * @param file
     * @param folder
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public String uploadFileReturnObjectName(MultipartFile file,  String folder, String fileName) throws Exception {
        if(StringUtils.isEmpty(folder)){
            throw new RuntimeException("folder不能为空");
        }
        // 文件类型白名单校验：仅允许常规图片/文档/音视频类型，防止上传可执行脚本等危险文件
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        fileName = DateUtils.datePath() + "/" + fileName;
        String customer=null;
        if(ThreadContext.get(Constants.TARGET_CUSTOMER)!=null){
            customer=ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        }else{
            customer= ServletUtils.getRequest().getHeader(Constants.TARGET_CUSTOMER);
        }
        fileName = customer + "/" + fileFolder + "/" + folder + "/" + fileName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build();
        client.putObject(args);
        return fileName;
    }

    @Override
    public InputStream downloadFile(String bucketName, String objectName) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        if (objectName.contains("http")) {//全路径
            URL url = new URL(objectName);
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            return inStream;
        }
        return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    @Override
    public void deleteFile(String url) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        String fileName = url.replace(minioConfig.getUrl() + "/", "")
                .replace(minioConfig.getBucketName() + "/", "");
        client.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName).build());
    }

    /**
     * 预签名上传重写文件名 uuid
     *
     * @param fileName
     * @return
     */
    @Override
    public Map<String, String> getPresignedPutUrl(String fileName) throws Exception {
        return getPresignedPutUrl(fileName, "file-upload");
    }

    @Override
    public Map<String, String> getPresignedPutUrl( String fileName,  String folder) throws Exception {

        // 预签名直传同样校验文件扩展名，防止绕过服务端白名单上传危险文件
        String extension = FilenameUtils.getExtension(fileName);
        if (!FileUploadUtils.isAllowedExtension(extension, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION)) {
            throw new CustomException("不支持的文件类型：" + fileName);
        }
        Map<String, String> result = new HashMap<String, String>();
        String customer=null;
        if(ThreadContext.get(Constants.TARGET_CUSTOMER)!=null){
            customer=ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        }else{
            customer= ServletUtils.getRequest().getHeader(Constants.TARGET_CUSTOMER);
        }
        String objectName = customer + "/" + fileFolder + "/" + folder + "/" + DateUtils.datePath() + "/" + FileNameUtil.getRandomFileName(fileName);
        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put("response-content-type", "application/json");
        String url =
                client.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.PUT)
                                .bucket(bucketName)
                                .object(objectName)
                                .expiry(1, TimeUnit.HOURS)
                                .build());
        result.put("putUrl", url);
        result.put("objectName", objectName);
        return result;
    }

    /**
     * 预签名上传保留文件名
     *
     * @param fileName
     * @return
     */
    @Override
    public Map<String, String> getPreSignedFilePutUrl(String fileName) {
        if (CheckObjectUtils.isEmpty(fileName)) {
            throw new CustomException("文件名不能为空！！");
        }
        // 预签名直传同样校验文件扩展名，防止绕过服务端白名单上传危险文件
        String extension = FilenameUtils.getExtension(fileName);
        if (!FileUploadUtils.isAllowedExtension(extension, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION)) {
            throw new CustomException("不支持的文件类型：" + fileName);
        }
        Map<String, String> result = new HashMap<String, String>(2);
        try {
            String customer=null;
            if(ThreadContext.get(Constants.TARGET_CUSTOMER)!=null){
                customer=ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
            }else{
                customer= ServletUtils.getRequest().getHeader(Constants.TARGET_CUSTOMER);
            }
            String objectName = customer + "/" + fileFolder + "/file-upload/" + DateUtils.datePath() + "/" + FileNameUtil.getRandomFileName(fileName);
            Map<String, String> reqParams = new HashMap<String, String>(1);
            reqParams.put("response-content-type", "application/json");
            String url =
                    client.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.PUT)
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .expiry(1, TimeUnit.HOURS)
                                    .build());
            result.put("putUrl", url);
            result.put("objectName", objectName);
        } catch (Exception e) {
            throw new CustomException("文件上传失败！！");
        }
        return result;
    }

    /**
     * 获取文件下载地址（预签名）
     *
     * @param objectName
     * @return
     * @throws Exception
     */
    @Override
    public String getPresignedGetUrl(String objectName) throws Exception {
        if(objectName.startsWith("/")){
            objectName=objectName.replaceFirst("/","");
        }
        String url =
                client.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucketName)
                                .object(objectName)
                                .expiry(1, TimeUnit.HOURS)
                                .build());
        return url;
    }


    /**
     * 获取文件下载地址（预签名）(无过期时间)
     *
     * @param objectName
     * @return
     * @throws Exception
     */
    @Override
    public String getPresignedGetUrlNoExpiry(String objectName) throws Exception {
        if(objectName.startsWith("/")){
            objectName=objectName.replaceFirst("/","");
        }
        String url =minioUrl+"/"+bucketName+"/"+objectName;

        return url;
    }
    /**
     * 拼接下载模板路径
     *
     * @param filePath
     * @return
     */
    @Override
    public String getTemplateFilePath(String filePath) {
        if (!filePath.startsWith("http")) {
            String templateBucket = "hyzz-site-test";
            if (active.equals(Constants.PROFILES_ACTIVE_PRE) || active.equals(Constants.PROFILES_ACTIVE_PROD)) {
                templateBucket = "hyzz-site";
            }
            return minioUrl + "/" + templateBucket + "/template/" + active + filePath;

        }
        return filePath;
    }
}
