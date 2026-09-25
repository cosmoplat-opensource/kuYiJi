import { createStore } from 'vuex'
import audit from '@/store/audit'
import copyThis from '@/store/copy-this'
import user from '@/store/user'
import report from '@/store/report'
import process from '@/store/process'
import staff from '@/store/staff'
import popup from '@/store/popup'
import experience from '@/store/experience'
import analysis from '@/store/analysis'
import productToProcess from '@/store/product-to-process'
import guide from '@/store/guide'
import mock from '@/store/mock'
import stock from '@/store/stock'
import tabBar from '@/store/tab-bar'
import settlement from '@/store/settlement'
import qualityTesting from '@/store/quality-testing'
import setting from '@/store/setting'
import batchReport from '@/store/batch-report'
import outProductStorage from '@/store/out-product-storage'
import service from '@/store/service'

export default createStore<IState>({
  modules: {
    audit,
    copyThis,
    user,
    report,
    process,
    popup,
    staff,
    experience,
    analysis,
    guide,
    mock,
    productToProcess,
    stock,
    tabBar,
    settlement,
    qualityTesting,
    setting,
    batchReport,
    outProductStorage,
    service
  },
  getters: {
    isExperience: (state) => state.experience.isExperience,
    currentFilter: (state) => state.audit.currentFilter
  }
})
