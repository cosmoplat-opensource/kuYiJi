<template>
	<view class="h-echart" :style="customStyle">
		<!-- #ifdef H5 -->
		<view ref="boxRef" class="h-echart__box"></view>
		<!-- #endif -->
		<!-- #ifdef MP-WEIXIN -->
		<!-- 触摸事件绑外层 view(避免 block wx:if 守卫);canvas 用显式像素尺寸(微信同层渲染要求明确宽高) -->
		<view
			class="h-echart__wrap"
			@touchstart="onTouchStart"
			@touchmove="onTouchMove"
			@touchend="onTouchEnd"
			@touchcancel="onTouchEnd"
		>
			<canvas type="2d" class="h-echart__box" :style="canvasBoxStyle" />
		</view>
		<!-- #endif -->
		<!-- #ifdef MP-ALIPAY || MP-TOUTIAO || APP-PLUS -->
		<view
			class="h-echart__wrap"
			@touchstart="onTouchStart"
			@touchmove="onTouchMove"
			@touchend="onTouchEnd"
			@touchcancel="onTouchEnd"
		>
			<canvas type="2d" class="h-echart__box" :style="canvasBoxStyle" />
		</view>
		<!-- #endif -->
	</view>
</template>

<script>
/**
 * h-echart —— echarts 图表容器组件(自研,MIT)
 *
 * 兼容原 lime-echart 的调用方式:
 *   <h-echart ref="chart" class="w-100 h-500" @finished="init" />
 *   chart.value.init(echarts, (chart) => { chart.setOption(option) })
 *
 * 平台策略:
 *   H5   : 原生 <view> 容器 + echarts.init(dom),鼠标/触摸交互由 echarts 原生处理
 *   小程序/App: canvas type="2d"(或旧 canvas) + echarts.init(node),触摸事件转发给 zrender
 */
function sleep(ms) {
	return new Promise((resolve) => setTimeout(resolve, ms))
}
function compareVersion(v1, v2) {
	const a = String(v1).split('.').map(Number)
	const b = String(v2).split('.').map(Number)
	for (let i = 0; i < Math.max(a.length, b.length); i++) {
		const x = a[i] || 0
		const y = b[i] || 0
		if (x !== y) return x > y ? 1 : -1
	}
	return 0
}
// #ifndef H5
// 小程序/App 端使用 echarts 完整版(npm dist,标准 UMD,含 setCanvasCreator/zrender 小程序适配,
// 与官方 ec-canvas 同源用法);H5 端仍用页面传入的 npm echarts(core)
import echartsFull from 'echarts/dist/echarts.js'
// #endif
export default {
	name: 'h-echart',
	// 小程序端需要外部高度/布局样式作用于组件根节点(删除会导致图表不显示),
	// 保留 apply-shared;悬浮问题与它无关(详见排查记录)
	options: {
		styleIsolation: 'apply-shared'
	},
	props: {
		customStyle: {
			type: String,
			default: ''
		},
		isDisableScroll: {
			type: Boolean,
			default: false
		},
		// 微信/头条/支付宝小程序 2d canvas 开关
		type: {
			type: String,
			default: '2d'
		},
		beforeDelay: {
			type: Number,
			default: 30
		}
	},
	data() {
		return {
			canvasId: 'h-echart-' + Math.random().toString(36).slice(-6),
			// 初始即 true:避免 canvas 条件渲染(wx:if 初始 false 导致动态创建、同层渲染失效)
			use2d: true,
			chart: null,
			canvasNode: null,
			canvasRect: null,
			width: 0,
			height: 0,
			isDown: false,
			startX: 0,
			startY: 0,
			startT: 0,
			endTimer: null,
			resizeHandler: null
		}
	},
	computed: {
		// 小程序端 canvas 2d 需显式像素宽高(百分比可能回退到原生浮层)
		canvasBoxStyle() {
			return this.width && this.height
				? { width: Math.round(this.width) + 'px', height: Math.round(this.height) + 'px' }
				: ''
		}
	},
	created() {
		// #ifndef H5
		// 小程序/App 环境可能无 requestAnimationFrame,zrender 渲染循环会依赖它,补 polyfill
		const g = typeof globalThis !== 'undefined' ? globalThis : typeof global !== 'undefined' ? global : this
		if (typeof g.requestAnimationFrame !== 'function') {
			g.requestAnimationFrame = function (cb) {
				return setTimeout(function () {
					cb(Date.now())
				}, 16)
			}
			g.cancelAnimationFrame = function (id) {
				clearTimeout(id)
			}
		}
		// #endif
		// #ifdef MP-WEIXIN
		try {
			const info = uni.getSystemInfoSync()
			this.use2d = this.type === '2d' && compareVersion(info.SDKVersion, '2.9.2') >= 0
		} catch (e) {
			this.use2d = true
		}
		// #endif
		// #ifdef MP-ALIPAY
		try {
			this.use2d = this.type === '2d' && compareVersion(my.SDKVersion, '2.7.0') >= 0
		} catch (e) {
			this.use2d = true
		}
		// #endif
		// #ifdef MP-TOUTIAO
		try {
			const info = uni.getSystemInfoSync()
			this.use2d = this.type === '2d' && compareVersion(info.SDKVersion, '1.78.0') >= 0
		} catch (e) {
			this.use2d = true
		}
		// #endif
		// #ifdef APP-PLUS
		this.use2d = true
		// #endif
	},
	mounted() {
		this.$nextTick(() => {
			this.$emit('finished')
		})
	},
	beforeUnmount() {
		this.dispose()
		if (this.resizeHandler) {
			// #ifdef H5
			window.removeEventListener('resize', this.resizeHandler)
			// #endif
			this.resizeHandler = null
		}
	},
	methods: {
		/**
		 * 初始化图表
		 * 兼容 lime-echart: init(echarts, theme?, opts?, callback)
		 */
		async init(echarts, ...args) {
			// 幂等:重复 init 先释放旧实例(页面数据刷新/就绪后重新初始化绘制)
			if (this.chart) {
				this.chart.dispose()
				this.chart = null
			}
			if (args.length < 1) {
				console.error('缺少参数：init(echarts, theme?, opts?, callback)')
				return
			}
			let theme = null
			let opts = {}
			let callback = null
			args.forEach((item) => {
				if (typeof item === 'function') {
					callback = item
				} else if (typeof item === 'string') {
					theme = item
				} else if (item && typeof item === 'object') {
					opts = item
				}
			})
			if (typeof callback !== 'function') {
				console.error('callback 非 function')
				return
			}
			if (this.beforeDelay) {
				await sleep(this.beforeDelay)
			}
			// #ifdef H5
			const box = await this.getH5Node()
			if (!box) {
				console.error('容器未就绪，请确认组件已挂载')
				return
			}
			this.width = box.clientWidth || 300
			this.height = box.clientHeight || 300
			this.chart = echarts.init(box, theme, opts)
			callback(this.chart)
			if (!this.resizeHandler) {
				this.resizeHandler = () => {
					if (this.chart) {
						this.chart.resize()
					}
				}
				window.addEventListener('resize', this.resizeHandler)
			}
			// #endif
			// #ifndef H5
			const config = await this.getCanvasConfig()
			if (!config.canvas) {
				console.error('canvas 获取失败')
				return
			}
			// 小程序/App 端使用 echarts 完整版(忽略页面传入的 npm echarts/core,页面代码零改动);
			// UMD 经 vite 转换后内容在 .default 上
			const engine = (echartsFull && echartsFull.default) || echartsFull
			// 官方 ec-canvas 方案:setCanvasCreator 注册画布创建器 + init(canvas node)
			this.registerMpPlatform(engine, config)
			this.chart = engine.init(
				config.canvas,
				theme,
				Object.assign({}, { width: config.width, height: config.height, devicePixelRatio: config.devicePixelRatio }, opts)
			)
			callback(this.chart)
			// #endif
		},
		setOption() {
			if (!this.chart || !this.chart.setOption) {
				console.warn('组件还未初始化，请先使用 init')
				return
			}
			this.chart.setOption(...arguments)
		},
		showLoading() {
			if (this.chart) {
				this.chart.showLoading(...arguments)
			}
		},
		hideLoading() {
			if (this.chart) {
				this.chart.hideLoading()
			}
		},
		clear() {
			if (this.chart) {
				this.chart.clear()
			}
		},
		dispose() {
			if (this.chart) {
				this.chart.dispose()
				this.chart = null
			}
		},
		// #ifdef H5
		/** H5:获取容器真实 DOM 节点。uni-app H5 中 <view ref> 拿到的是组件实例,真实 DOM 在其 $el 上 */
		getH5Node() {
			const box = this.$refs.boxRef
			const dom = box ? box.$el || box : null
			if (dom && typeof dom.querySelector === 'function' && typeof dom.getBoundingClientRect === 'function') {
				return Promise.resolve(dom)
			}
			// 兜底:SelectorQuery
			return new Promise((resolve) => {
				uni.createSelectorQuery()
					.in(this)
					.select('.h-echart__box')
					.fields({ node: true, size: true })
					.exec((res) => {
						const info = res && res[0]
						resolve(info && info.node ? info.node : null)
					})
			})
		},
		resize(size) {
			if (size && size.width && size.height) {
				this.width = size.width
				this.height = size.height
				if (this.chart) {
					this.chart.resize(size)
				}
				return
			}
			this.$nextTick(() => {
				this.getH5Node().then((box) => {
					if (box && this.chart) {
						this.width = box.clientWidth || 300
						this.height = box.clientHeight || 300
						this.chart.resize({ width: this.width, height: this.height })
					}
				})
			})
		},
		// #endif
		// #ifndef H5
		resize(size) {
			if (size && size.width && size.height) {
				this.width = size.width
				this.height = size.height
				if (this.chart) {
					this.chart.resize(size)
				}
				return
			}
			this.$nextTick(() => {
				uni.createSelectorQuery()
					.in(this)
					.select('.' + 'h-echart__box')
					.boundingClientRect((res) => {
						if (res && this.chart) {
							const width = res.width || 300
							const height = res.height || 300
							this.width = width
							this.height = height
							this.chart.resize({ width, height })
						}
					})
					.exec()
			})
		},
		// #endif
		canvasToTempFilePath(args = {}) {
			// #ifndef H5
			const { use2d, canvasId, canvasNode } = this
			return new Promise((resolve, reject) => {
				const copyArgs = Object.assign(
					{
						canvasId,
						success: resolve,
						fail: reject
					},
					args
				)
				if (use2d) {
					delete copyArgs.canvasId
					// 使用真实 canvas node(包装对象仅供 zrender 使用,导出需原始节点)
					copyArgs.canvas = this.realCanvasNode || canvasNode
				}
				uni.canvasToTempFilePath(copyArgs, this)
			})
			// #endif
			// #ifdef H5
			return new Promise((resolve) => {
				if (this.chart) {
					resolve({ tempFilePath: this.chart.getDataURL({ pixelRatio: 1 }) })
				} else {
					reject({ error: 'chart 未初始化' })
				}
			})
			// #endif
		},
		// #ifndef H5
		/**
		 * 官方小程序适配:setPlatformAPI({ createCanvas }) 注册画布创建器,
		 * createCanvas 每次返回新包装实例(zrender 多图层互不干扰),包装转发到真实 canvas node
		 */
		registerMpPlatform(engine, config) {
			if (this._platformRegistered) {
				return
			}
			this._platformRegistered = true
			const factory = config.wrapperFactory
			if (typeof engine.setPlatformAPI === 'function') {
				engine.setPlatformAPI({ createCanvas: () => factory() })
			} else if (typeof engine.setCanvasCreator === 'function') {
				engine.setCanvasCreator(() => factory())
			}
		},
		getCanvasConfig() {
			return new Promise((resolve) => {
				this.queryCanvasNode(0, resolve)
			})
		},
		/** 查询 2d canvas node;动态创建/隐藏(v-show)时 node 可能未就绪或尺寸为 0,延迟重试等待可见 */
		queryCanvasNode(retry, resolve) {
			uni.createSelectorQuery()
				.in(this)
				.select('.h-echart__box')
				.fields({ node: true, size: true })
				.exec((res) => {
					const info = res && res[0]
					if (!info || !info.node || !info.width || !info.height) {
						if (retry < 30) {
							setTimeout(() => this.queryCanvasNode(retry + 1, resolve), 200)
						} else {
							console.error('canvas node 获取失败')
							resolve({ canvas: null, wrapperFactory: null, width: 300, height: 300, devicePixelRatio: 1 })
						}
						return
					}
					const dpr = (uni.getSystemInfoSync().pixelRatio) || 1
					let width = info.width || 0
					let height = info.height || 0
					const node = info.node
					// 尺寸兜底:fields size 在组件刚挂载时可能拿不到高度,用容器 boundingClientRect 实测
					const finishSetup = () => {
						if (this.use2d) {
							node.width = width * dpr
							node.height = height * dpr
						}
						// zrender 按 DOM canvas 接口操作(style/width/getContext/addEventListener 等),
						// 微信 canvas 2d node 缺这些接口,构造兼容包装对象转发到真实 node
						const makeWrapper = () => ({
							nodeName: 'CANVAS',
							_node: node,
							style: {
								position: 'absolute',
								left: '0',
								top: '0',
								width: width + 'px',
								height: height + 'px'
							},
							getContext: function (type) {
								return node.getContext(type)
							},
							addEventListener: function () {},
							removeEventListener: function () {},
							get width() {
								return node.width
							},
							set width(v) {
								node.width = v
							},
							get height() {
								return node.height
							},
							set height(v) {
								node.height = v
							},
							get clientWidth() {
								return width
							},
							get clientHeight() {
								return height
							},
							getBoundingClientRect: function () {
								return { left: 0, top: 0, width, height }
							}
						})
						this.canvasNode = makeWrapper()
						this.realCanvasNode = node
						this.width = width
						this.height = height
						resolve({ canvas: makeWrapper(), wrapperFactory: makeWrapper, realNode: node, width, height, devicePixelRatio: dpr })
					}
					const measureRect = () => {
						uni.createSelectorQuery()
							.in(this)
							.select('.h-echart__box')
							.boundingClientRect((rect) => {
								this.canvasRect = rect
								if (!width && rect && rect.width) width = rect.width
								if (!height && rect && rect.height) height = rect.height
								if (!width) width = 300
								if (!height) height = 300
								finishSetup()
							})
							.exec()
					}
					measureRect()
				})
		},
		getTouch(e) {
			const touch = e.touches && e.touches[0]
			if (!touch) {
				return { x: 0, y: 0 }
			}
			const rect = this.canvasRect || { left: 0, top: 0 }
			return {
				x: (touch.clientX || touch.pageX || 0) - (rect.left || 0),
				y: (touch.clientY || touch.pageY || 0) - (rect.top || 0)
			}
		},
		wrapTouch(e) {
			const map = (list) =>
				Array.from(list || []).map((t) => ({
					x: t.clientX || t.pageX || 0,
					y: t.clientY || t.pageY || 0,
					identifier: t.identifier
				}))
			return {
				touches: map(e.touches),
				changedTouches: map(e.changedTouches),
				timeStamp: e.timeStamp,
				// 微信小程序 touch 事件没有 preventDefault/stopPropagation,zrender 缩放/手势处理会调用,
				// 补成空函数避免 "preventDefault is not a function" 崩溃
				preventDefault() {},
				stopPropagation() {}
			}
		},
		// 生成 zrender 指针事件:带 preventDefault/stopPropagation 空函数,避免微信 touch 事件对象缺方法导致
		// zrender 在 mousedown/mousemove/mouseup 等处理里调用 preventDefault 时崩溃("preventDefault is not a function")
		makePointEvent(x, y) {
			return {
				zrX: x,
				zrY: y,
				preventDefault() {},
				stopPropagation() {}
			}
		},
		onTouchStart(e) {
			if (!this.chart) return
			this.isDown = true
			this.startT = new Date().getTime()
			const touch = this.getTouch(e)
			this.startX = touch.x
			this.startY = touch.y
			const handler = this.chart.getZr().handler
			handler.dispatch('mousedown', this.makePointEvent(touch.x, touch.y))
			handler.dispatch('mousemove', this.makePointEvent(touch.x, touch.y))
			handler.processGesture(this.wrapTouch(e), 'start')
			clearTimeout(this.endTimer)
		},
		onTouchMove(e) {
			if (!this.chart || !this.isDown) return
			const touch = this.getTouch(e)
			const handler = this.chart.getZr().handler
			handler.dispatch('mousemove', this.makePointEvent(touch.x, touch.y))
			handler.processGesture(this.wrapTouch(e), 'change')
		},
		onTouchEnd(e) {
			if (!this.chart) return
			this.isDown = false
			const touch = this.getTouch(e)
			const handler = this.chart.getZr().handler
			const isClick = Math.abs(touch.x - this.startX) < 10 && new Date().getTime() - this.startT < 200
			handler.dispatch('mouseup', this.makePointEvent(touch.x, touch.y))
			handler.processGesture(this.wrapTouch(e), 'end')
			if (isClick) {
				handler.dispatch('click', this.makePointEvent(touch.x, touch.y))
			} else {
				this.endTimer = setTimeout(() => {
					handler.dispatch('mousemove', this.makePointEvent(999999999, 999999999))
					handler.dispatch('mouseup', this.makePointEvent(999999999, 999999999))
				}, 50)
			}
		}
		// #endif
	}
}
</script>

<style scoped>
.h-echart {
	position: relative;
	width: 100%;
	/* 高度由外部传入的高度类(w-100 h-500 / h-600)或内联 custom-style 控制,此处不设置,
	   避免 scoped 高优先级样式覆盖外部类导致容器塌缩 */
}
.h-echart__wrap {
	position: relative;
	width: 100%;
	height: 100%;
}
.h-echart__box {
	/* zrender 会给 canvas 设 position:absolute,容器必须是定位元素,否则图表相对页面定位(悬浮不随卡片滚动) */
	position: relative;
	width: 100%;
	height: 100%;
	/* #ifdef H5 */
	overflow: hidden;
	/* #endif */
}
</style>
