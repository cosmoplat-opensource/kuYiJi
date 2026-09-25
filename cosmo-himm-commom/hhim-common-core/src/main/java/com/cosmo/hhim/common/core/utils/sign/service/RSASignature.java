/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.sign.service;

import com.cosmo.hhim.common.core.enums.SignatureStrategyEnum;
import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import com.cosmo.hhim.common.core.utils.sign.RSASignUtil;
import com.cosmo.hhim.common.core.utils.sign.SignStreamTool;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureService;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureStrategyFactory;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

@Component
public class RSASignature extends SignatureService {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public ThirdInterfaceTenant createSecret(SignatureStrategyEnum strategy) throws Exception {
        RSASignUtil rsaSignUtilIsv = new RSASignUtil(RSASignUtil.KeyFormat.PKCS8, RSASignUtil.KeyLength.LENGTH_1024);
        RSASignUtil.KeyStore keyStoreIsv = rsaSignUtilIsv.createKeys();
        ThirdInterfaceTenant tenant = new ThirdInterfaceTenant();
        tenant.setPublicKey(keyStoreIsv.getPublicKey());
        tenant.setPrivateKey(keyStoreIsv.getPrivateKey());
        return tenant;
    }

    /**
     * RSA加签操作
     *
     * @param params     请求参数
     * @param privateKey 秘钥
     * @return 签名字符串
     */
    @Override
    public String encryptSign(Map<String, String> params, String privateKey) throws Exception {
        String signContent = buildSignContent(params);
        return rsa256Sign(signContent, privateKey);
    }

    @Override
    public boolean verifySign(Map<String, String> params, String publicKey) {
        String sign = params.get("sign");
        String contentV2 = buildVerifySignContent(params);
        return rsa256CheckContent(contentV2, sign, publicKey, StandardCharsets.UTF_8.name());
    }


    private boolean rsa256CheckContent(String content, String sign, String publicKey,
                                       String charset) {
        try {
            PublicKey pubKey = getPublicKeyFromX509("RSA",
                    new ByteArrayInputStream(publicKey.getBytes()));

            Signature signature = Signature
                    .getInstance("SHA256WithRSA");

            signature.initVerify(pubKey);

            if (StringUtils.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            return signature.verify(Base64.decodeBase64(sign.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException();
//            throw ErrorEnum.ISV_INVALID_SIGNATURE.getErrorMeta().getException(e);
        }
    }

    private PublicKey getPublicKeyFromX509(String algorithm,
                                           InputStream ins) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        StringWriter writer = new StringWriter();
        SignStreamTool.io(new InputStreamReader(ins), writer);

        byte[] encodedKey = writer.toString().getBytes();

        encodedKey = Base64.decodeBase64(encodedKey);

        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    /**
     * sha256WithRsa 加签
     *
     * @param content
     * @param privateKey
     * @return
     * @throws Exception
     */
    private String rsa256Sign(String content, String privateKey) throws Exception {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(priKey);
            if (isEmpty(StandardCharsets.UTF_8.name())) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(StandardCharsets.UTF_8));
            }
            byte[] signed = signature.sign();
            return new String(Base64.encodeBase64(signed));
        } catch (Exception e) {
            throw new Exception("RSAcontent = " + content + "; charset = " + StandardCharsets.UTF_8, e);
        }

    }

    private PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
        if (ins == null || isEmpty(algorithm)) {
            return null;
        }

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        byte[] encodedKey = readText(ins).getBytes();

        encodedKey = Base64.decodeBase64(encodedKey);

        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }


    private String readText(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        StringWriter writer = new StringWriter();
        io(reader, writer);
        return writer.toString();
    }

    private void io(Reader in, Writer out) throws IOException {
        int bufferSize = DEFAULT_BUFFER_SIZE >> 1;
        char[] buffer = new char[bufferSize];
        int amount;
        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    @Override
    public void afterPropertiesSet() {
        SignatureStrategyFactory.register("RSA2", this);
    }
}
