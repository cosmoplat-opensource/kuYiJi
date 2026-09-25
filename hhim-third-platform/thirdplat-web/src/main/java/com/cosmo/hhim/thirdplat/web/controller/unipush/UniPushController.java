/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.unipush;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.api.unipush.constants.CommonEnum;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.AppPushMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.CreatePushMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.ListPushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.Message;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.QueryPushDetailMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.SingleBatchPushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.SinglePushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.StandardMessage;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushChannel;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushMessage;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.android.AndroidChannel;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.android.ThirdNotification;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.android.Ups;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios.Alert;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios.Aps;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios.IosChannel;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.AppPushResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.CreatePushMsgResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.ListPushCidResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.PushDetailResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.SingleBatchPushCidResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.SinglePushCidResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.core.config.ForestGlobalConfig;
import com.cosmo.hhim.thirdplat.modules.unipush.constants.UniPushConstant;
import com.cosmo.hhim.thirdplat.web.constants.Constant;
import com.cosmo.hhim.thirdplat.web.service.manager.IDeviceUserBindService;
import com.cosmo.hhim.thirdplat.web.service.unipush.IUniPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Slf4j
@RestController
@RequestMapping("/uniapp/push")
public class UniPushController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IUniPushService iUniPushService;
    @Autowired
    private IDeviceUserBindService iHyzzDeviceUserBindService;
    @Autowired
    private ForestGlobalConfig forestGlobalConfig;

    /**
     * cid单推
     *
     * @param message
     * @return
     */
    @PostMapping("/single")
    public APIResponse<SinglePushCidResult> singlePush(@RequestBody @Valid SinglePushCidMsg message) {
        if(null==message.getPushMessage()){
            throw new IllegalArgumentException("个推通道消息不允许为空");
        }
        return iUniPushService.singlePush(message);
    }

    /**
     * 批量cid单推
     *
     * @param message
     * @return
     */
    @PostMapping("/singlebatch")
    public APIResponse<SingleBatchPushCidResult> singleBatchPush(@RequestBody @Valid SingleBatchPushCidMsg message) {
        if(CollectionUtils.isEmpty(message.getMsgList())){
            throw new IllegalArgumentException("批量推送的消息列表不允许为空");
        }
        return iUniPushService.singleBatchPush(message);
    }

    /**
     * 创建消息
     *
     * @param message
     * @return
     */
    @PostMapping("/createmessage")
    public APIResponse<CreatePushMsgResult> createMessage(@RequestBody @Valid CreatePushMsg message) {
        if(null==message.getPushMessage()){
            throw new IllegalArgumentException("个推通道消息不允许为空");
        }
        return iUniPushService.createMessage(message);
    }

    /**
     * 批量推送
     *
     * @param message
     * @return
     */
    @PostMapping("/list")
    public APIResponse<ListPushCidResult> listPush(@RequestBody @Valid ListPushCidMsg message) {
        if(CollectionUtils.isEmpty(message.getCids())){
            throw new IllegalArgumentException("推送设备ID数组不允许为空");
        }
        return iUniPushService.listPush(message);
    }

    /**
     * 按照app群推
     *
     * @param message
     * @return
     */
    @PostMapping("/all")
    public APIResponse<AppPushResult> appPush(@RequestBody @Valid AppPushMsg message) {
        if(null==message.getPushMessage()){
            throw new IllegalArgumentException("个推通道消息不允许为空");
        }
        return iUniPushService.appPush(message);
    }

    /**
     * 查询推送结果
     *
     * @param message
     * @return
     */
    @GetMapping("/pushdetail")
    public APIResponse<PushDetailResult> queryPushDetail(@Valid QueryPushDetailMsg message) {
        return iUniPushService.queryPushDetail(message);
    }

    /**
     * 按照用户账号进行标准模版推送
     *
     * @param message
     * @return
     */
    @PostMapping("/standard")
    public APIResponse<Boolean> userStandardPush(@RequestBody @Valid StandardMessage message) {
        log.info(">>>标准模版消息推送<<<，requestParam:{}", JSON.toJSONString(message));
        List<String> cids = iHyzzDeviceUserBindService.selectDeviceIdsByUsernames(message.getToUsernames());
        if (CollectionUtils.isEmpty(cids)) {
            return APIResponse.fail("用户没有对应的设备ID，无法推送消息！", 500);
        }

        if (cids.size() > 1000) {
            return APIResponse.fail("推送设备数超过最大限制数量（1000）", 500);
        }

        // 个推渠道消息
        PushMessage pushMessage = new PushMessage();
        Message msg = new Message();
        msg.setTitle(message.getTitle());
        msg.setContent(message.getContent());
        msg.setPayload(message.getPayload());
        pushMessage.setTransmission(JSON.toJSONString(msg));

        // Android厂商渠道消息设置
        ThirdNotification notification = new ThirdNotification();
        notification.setTitle(message.getTitle());
        notification.setBody(message.getContent());
        notification.setClickType(CommonEnum.ClickTypeEnum.TYPE_INTENT.type);
        String intent = String.format(Constant.UNIPUSH_INTENT, forestGlobalConfig.getVariables().get(UniPushConstant.UNI_APPPACKAGE),
                message.getTitle(), message.getContent(), message.getPayload());
        notification.setIntent(intent);

        Ups ups = new Ups();
        ups.setNotification(notification);
        AndroidChannel androidChannel = new AndroidChannel();
        androidChannel.setUps(ups);

        // IOS厂商渠道消息设置
        Alert alert = new Alert();
        alert.setTitle(message.getTitle());
        alert.setBody(message.getContent());

        Aps aps = new Aps();
        aps.setAlert(alert);
        IosChannel iosChannel = new IosChannel();
        iosChannel.setAps(aps);

        PushChannel pushChannel = new PushChannel();
        pushChannel.setAndroid(androidChannel);
        pushChannel.setIos(iosChannel);

        if (cids.size() > 1) { //创建消息->批量cid推送
            CreatePushMsg createPushMsg = new CreatePushMsg();
            createPushMsg.setPushMessage(pushMessage);
            createPushMsg.setPushChannel(pushChannel);
            APIResponse<CreatePushMsgResult> createMessageResponse = iUniPushService.createMessage(createPushMsg);
            if (!createMessageResponse.isSuccess()) {
                return APIResponse.fail("消息创建失败：" + createMessageResponse.getMessage(), createMessageResponse.getCode());
            }
            String taskId = createMessageResponse.getData().getTaskId();

            ListPushCidMsg listPushCidMsg = new ListPushCidMsg();
            listPushCidMsg.setTaskId(taskId);
            listPushCidMsg.setCids(cids);
            listPushCidMsg.setAsync(true);
            iUniPushService.listPush(listPushCidMsg);
        } else if (cids.size() == 1) { //cid单推
            SinglePushCidMsg singlePushCidMsg = new SinglePushCidMsg();
            singlePushCidMsg.setCid(cids.get(0));
            singlePushCidMsg.setPushMessage(pushMessage);
            singlePushCidMsg.setPushChannel(pushChannel);
            iUniPushService.singlePush(singlePushCidMsg);
        }
        return APIResponse.success(true);
    }

}
