/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 *
 * @author cosmo-hhim-open Team
 */

export default {
	data() {
		return {
			
		}
	},
	created(){
		this.popup = this.getParent()
	},
	methods:{
		/**
		 * 获取父元素实例
		 */
		getParent(name = 'uniPopup') {
			let parent = this.$parent;
			let parentName = parent.$options.name;
			while (parentName !== name) {
				parent = parent.$parent;
				if (!parent) return false
				parentName = parent.$options.name;
			}
			return parent;
		},
	}
}
