import { createSSRApp } from 'vue'
import App from './App.vue'
import Store from './store'
import HStatusFooter from '@/components/h-status-footer.vue'
import HStatusHeader from '@/components/h-status-header.vue'
import HUserInfo from '@/components/h-user-info.vue'
import HPageVideo from '@/components/h-page-video.vue'
import HButton from '@/components/h-button.vue'
import HCheckBox from '@/components/h-checkbox.vue'
import HPopup from '@/components/h-popup.vue'
import HTextDisplay from '@/components/h-text-display.vue'
import PreviewPath from '@/components/preview-path.vue'
import WordIcon from '@/components/word-icon.vue'
import HSearch from '@/components/h-search.vue'
import HShare from '@/components/h-share.vue'

export function createApp() {
  const Vue = createSSRApp(App)
  Vue.use(Store)
  Vue.component('HStatusHeader', HStatusHeader)
  Vue.component('HCheckBox', HCheckBox)
  Vue.component('HPopup', HPopup)
  Vue.component('HTextDisplay', HTextDisplay)
  Vue.component('HPageVideo', HPageVideo)
  Vue.component('HSearch', HSearch)
  Vue.component('HStatusFooter', HStatusFooter)
  Vue.component('HButton', HButton)
  Vue.component('HShare', HShare)
  Vue.component('HUserInfo', HUserInfo)
  Vue.component('PreviewPath', PreviewPath)
  Vue.component('WordIcon', WordIcon)
  return {
    app: Vue
  }
}
