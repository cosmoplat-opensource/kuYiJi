interface IState {
  user: IUserModules
  report: IReportType
  audit: IAuditState
  process: IProcessState
  staff: IStaffState
  guide: IGuideModules
  popup: IPopupModules
  experience: IExperienceModules
  analysis: IAnalysisModules
  mock: IMockModules
  tabBar: ITabBar
  stock: IStockState
  settlement: ISettlementState
  qualityTesting: IQualityTestingState
  setting: ISettingState
  batchReport: IBatchReportState
  service: IService
}
interface IService {
  chatList: []
}
interface IQualityTestingState {
  dataItem: Object
  repairItem: Object
}

interface ISettlementState {
  dataList: []
}

interface IStaffState {
  item: {}
}

interface ISettingState {
  submitInspectSwitch: String
  batchSubmitSwitch: String
  id: Number
}

interface IStockState {
  storageList: []
}

interface IPopupModules {
  popupContent: string
  popupData: Object
  popupRefList: Object
  popupImagePath: string
}

interface IGuideModules {
  step: Object
  stepIndex: number
  shareTimeLine: boolean
}

interface IUserModules {
  userInfo: IUserInfo
}

interface IAnalysisModules {
  dataItem: unknown
  params: unknown
}

interface IExperienceModules {
  isExperience: boolean
  step: unknown
  stepIndex: number
  guideCode: string
  stepList: unknown[]
}

interface IExperienceModules {
  isExperience: boolean
}

interface IMockModules {
  mockOpen: boolean
}

interface IBatchReportState {
  productList: []
  productItem: Object
  productIndex: Number
}

interface IUserState {
  userInfo: Partial<IUserInfo>
}

interface IUserInfo {
  id: Number
  userName: string
  nickName: string
  phonenumber: string
  phoneNumber: string
  sex: string
  remark: string
  token: string
  avatar: string
  wxCode: string
  userType: string
  phoneCode: string
  roleCode: string
  roleName: string
  tenantCode: string
  tenantName: string
  status: string
  validDate: string
  companyName: string
  personalRecommend: number
  microRoles: IMicroRoles[]
  microUserAppRes: IMicroUserAppRes[]
}

type IMicroRoles = {
  roleName: string
  roleCode: string
}

type IMicroUserAppRes = {
  validDate: string
}

interface IResponseNormal {
  code: number
  msg: string
}

type IReportType = {
  reportItem: Object
  standard: boolean
}

interface IAuditState {
  currentFilter: string
  filterData: IFilterData
  followList: []
  followChecked: Boolean
  crossPageData: { switchChange: string; ids: string }
}

type IFilterData = {
  productNameOrCode: string
  processNameOrCode: string
  submitNickName: string
  submitUserIds: string
  startDate: string
  remark: string
  endDate: string
}

interface ITabBar {
  tabList: TabItem[]
  tabActive: Partial<TabItem>
  tabShow: Boolean
}

type TabItem = {
  checked: false
  pagePath: string
  iconPath: string
  selectedIconPath: string
  text: string
}

interface IProcessState {
  item: Object
  stockMaps: Object
}

declare enum EReportType {
  INDEX_URL = '/analysis/index',

  SUBMIT_RECORD_RANK_URL = '/analysis/submitRecordRank',

  FINISHED_PRODUCT_STATISTICS_URL = '/analysis/finishedProductStatistics',

  STORAGE_RANK_URL = '/analysis/storageRank',

  STORAGE_PRODUCT_LIST_URL = '/storage/product/list',

  STORAGE_PRODUCT_PROCESS_LIST_URL = '/storage/condition/list',

  STORAGE_CHANGE_HISTORY_URL = '/storage/history',

  PRODUCTION_TOTAL_COUNT_URL = '/analysis/showTotalCount',

  PRODUCTION_QUALITY_TREND_URL = '/analysis/productionQualityTrend',

  COMPLETED_PRODUCT_INFORMATION_URL = '/submit/completedProductInformation',

  PASS_RATE_ANALYSIS_BY_PRODUCT_URL = '/analysis/passRateAnalysisByProduct',

  PASS_RATE_ANALYSIS_BY_PROCESS_URL = '/analysis/passRateAnalysisByProcess',

  PASS_RATE_ANALYSIS_BY_EMPLOYEE_URL = '/analysis/passRateAnalysisByEmployee',

  PASS_RATE_ANALYSIS_BY_PRODUCT_PROCESS_URL = '/submit/completedProductInformation/product',

  PASS_RATE_ANALYSIS_BY_PROCESS_PRODUCT_URL = '/submit/completedProductInformation/process'
}
