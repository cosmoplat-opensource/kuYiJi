/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class WxMpMenu {

	public WxMpMenu() {
	}

	public WxMpMenu(String name, String type, String key){
		this.name = name;
		this.type = type;
		this.key = key;
	}

	public WxMpMenu(String name, String type, String url, String key) {
		this.name = name;
		this.type = type;
		this.url = url;
		this.key = key;
	}

	public WxMpMenu(String name, String type, String url, String key, String appid, String pagepath) {
		this.name = name;
		this.type = type;
		this.url = url;
		this.key = key;
		this.appid = appid;
		this.pagepath = pagepath;
	}

	// 二级菜单数组，个数应为1~5个
	// 选填
	private List<WxMpMenu> sub_button;

	// 菜单的响应动作类型，view表示网页类型，click表示点击类型，miniprogram表示小程序类型
	// 必填
	@NotBlank(message = "菜单响应动作类型不允许为空！")
	private String type;

	// 菜单标题，不超过16个字节，子菜单不超过60个字节
	// 必填
	@NotBlank(message = "菜单标题不允许为空！")
	private String name;

	// 菜单KEY值，用于消息接口推送，不超过128字节
	// click等点击类型必须
	private String key;

	// 网页 链接，用户点击菜单可打开链接，不超过1024字节。 type为miniprogram时，不支持小程序的老版本客户端将打开本url。
	// view、miniprogram类型必填
	private String url;

	// 调用新增永久素材接口返回的合法media_id
	// media_id类型和view_limited类型必须
	private String media_id;

	// 小程序的appid（仅认证公众号可配置）
	// miniprogram类型必须
	private String appid;

	// 小程序的页面路径
	// miniprogram类型必须
	private String pagepath;

	// 发布后获得的合法 article_id
	// article_id类型和article_view_limited类型必须
	private String article_id;

}
