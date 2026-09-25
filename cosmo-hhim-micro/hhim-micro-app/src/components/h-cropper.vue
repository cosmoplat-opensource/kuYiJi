<template>
	<view v-if="url" class="h-cropper">
		<!-- #ifdef MP-WEIXIN -->
		<canvas type="2d" class="h-cropper__canvas" :style="{ width: target.width + 'px', height: target.height + 'px' }"></canvas>
		<!-- #endif -->
		<!-- #ifdef APP-PLUS || H5 -->
		<canvas :canvas-id="canvasId" class="h-cropper__canvas" :style="{ width: target.width + 'px', height: target.height + 'px' }"></canvas>
		<!-- #endif -->
		<view class="h-cropper__panel">
			<view
				class="h-cropper__body"
				@touchstart.stop.prevent="onBodyTouchStart"
				@touchmove.stop.prevent="onBodyTouchMove"
				@touchend.stop.prevent="onBodyTouchEnd"
				@touchcancel.stop.prevent="onBodyTouchEnd"
			>
				<view class="h-cropper__image-wrap" :class="{ transit: transit }" :style="imageWrapStyle">
					<image class="h-cropper__image" :class="{ transit: transit }" :style="imageInnerStyle" :src="url" @load="onImageLoad" />
				</view>
				<view class="h-cropper__mask"></view>
				<view class="h-cropper__frame" :class="{ transit: transit }" :style="frameStyle">
					<view class="h-cropper__rect">
						<view class="h-cropper__image-rect" :class="{ transit: transit }" :style="imageRectStyle">
							<image class="h-cropper__image" :class="{ transit: transit }" :style="imageInnerStyle" :src="url" />
						</view>
					</view>
					<view class="h-cropper__line h-cropper__line-h1"></view>
					<view class="h-cropper__line h-cropper__line-h2"></view>
					<view class="h-cropper__line h-cropper__line-v1"></view>
					<view class="h-cropper__line h-cropper__line-v2"></view>
					<view class="h-cropper__corner h-cropper__corner-lt" data-corner="lt" @touchstart.stop.prevent="onCornerTouchStart" @touchmove.stop.prevent="onCornerTouchMove" @touchend.stop.prevent="onCornerTouchEnd"></view>
					<view class="h-cropper__corner h-cropper__corner-lb" data-corner="lb" @touchstart.stop.prevent="onCornerTouchStart" @touchmove.stop.prevent="onCornerTouchMove" @touchend.stop.prevent="onCornerTouchEnd"></view>
					<view class="h-cropper__corner h-cropper__corner-rt" data-corner="rt" @touchstart.stop.prevent="onCornerTouchStart" @touchmove.stop.prevent="onCornerTouchMove" @touchend.stop.prevent="onCornerTouchEnd"></view>
					<view class="h-cropper__corner h-cropper__corner-rb" data-corner="rb" @touchstart.stop.prevent="onCornerTouchStart" @touchmove.stop.prevent="onCornerTouchMove" @touchend.stop.prevent="onCornerTouchEnd"></view>
				</view>
			</view>
			<view class="h-cropper__toolbar">
				<view class="h-cropper__btn h-cropper__btn-cancel" @tap="onCancel">取消</view>
				<view class="h-cropper__btn h-cropper__btn-rotate" @tap="onRotate">旋转</view>
				<view class="h-cropper__btn h-cropper__btn-ok" @tap="onOk">确定</view>
			</view>
		</view>
	</view>
</template>

<script>
/**
 * h-cropper —— 图片裁剪组件(自研,MIT)
 *
 * 兼容原 ksp-cropper 的 props 与事件:
 *   mode: fixed | ratio | free(默认 free,不限制宽高比)
 *   url: 图片路径(非空时显示裁剪界面)
 *   width / height / maxWidth / maxHeight: 输出尺寸
 *   @cancel / @ok({ path, base64? })
 *
 * 手势:单指移动图片、双指缩放、四角拖拽缩放裁剪框、旋转(每次 -90°)
 * 输出:按裁剪框区域(或固定尺寸)绘制到离屏 canvas 并导出临时文件
 */
export default {
	name: 'h-cropper',
	props: {
		mode: {
			type: String,
			default: 'free'
		},
		url: {
			type: String,
			default: ''
		},
		width: {
			type: Number,
			default: 200
		},
		height: {
			type: Number,
			default: 200
		},
		maxWidth: {
			type: Number,
			default: 1024
		},
		maxHeight: {
			type: Number,
			default: 1024
		}
	},
	data() {
		return {
			canvasId: 'h-cropper-' + Math.random().toString(36).slice(-6),
			real: { width: 100, height: 100 },
			target: { width: 100, height: 100 },
			body: { width: 100, height: 100 },
			frame: { left: 50, top: 50, width: 200, height: 300 },
			image: { left: 20, top: 20, width: 300, height: 400 },
			rotate: 0,
			transit: false,
			touchType: '',
			touches: [],
			startFrame: { left: 0, top: 0, width: 0, height: 0 },
			startImage: { left: 0, top: 0, width: 0, height: 0 }
		}
	},
	computed: {
		frameStyle() {
			return {
				left: this.frame.left + 'px',
				top: this.frame.top + 'px',
				width: this.frame.width + 'px',
				height: this.frame.height + 'px'
			}
		},
		imageWrapStyle() {
			return {
				left: this.image.left + 'px',
				top: this.image.top + 'px',
				width: this.image.width + 'px',
				height: this.image.height + 'px'
			}
		},
		/** 框内预览层:相对裁剪框定位(图片相对框的偏移),拖动/缩放/旋转时框内内容实时跟随 */
		imageRectStyle() {
			return {
				left: this.image.left - this.frame.left + 'px',
				top: this.image.top - this.frame.top + 'px',
				width: this.image.width + 'px',
				height: this.image.height + 'px'
			}
		},
		imageInnerStyle() {
			let left = 0
			let top = 0
			let width = this.image.width
			let height = this.image.height
			if (this.rotate % 180 !== 0) {
				width = this.image.height
				height = this.image.width
				top = width / 2 - height / 2
				left = height / 2 - width / 2
			}
			return {
				left: left + 'px',
				top: top + 'px',
				width: width + 'px',
				height: height + 'px',
				transform: 'rotate(' + this.rotate + 'deg)'
			}
		}
	},
	methods: {
		onImageLoad() {
			uni.getImageInfo({
				src: this.url,
				success: (rst) => {
					this.real.width = rst.width
					this.real.height = rst.height
					uni.createSelectorQuery()
						.in(this)
						.select('.h-cropper__body')
						.boundingClientRect((data) => {
							this.body.width = data.width
							this.body.height = data.height
							this.initLayout()
						})
						.exec()
				}
			})
		},
		/** 初始布局:裁剪框为画布 70% 且保持 width/height 比例;图片按原图比例铺满裁剪框并居中 */
		initLayout() {
			this.rotate = 0
			const rate = this.width / this.height
			let width = this.body.width * 0.7
			let height = this.body.height * 0.7
			if (width / height > rate) {
				width = height * rate
			} else {
				height = width / rate
			}
			const left = (this.body.width - width) / 2
			const top = (this.body.height - height) / 2
			this.frame = { left, top, width, height }
			const imgRate = this.real.width / this.real.height
			width = this.frame.width
			height = this.frame.height
			if (width / height > imgRate) {
				height = width / imgRate
			} else {
				width = height * imgRate
			}
			this.image = {
				left: (this.frame.width - width) / 2 + this.frame.left,
				top: (this.frame.height - height) / 2 + this.frame.top,
				width,
				height
			}
			// 图片与裁剪框同比例(等大)时,边界约束会锁死拖动;默认放大 1.2 倍保证可拖动余地
			if (Math.abs(this.image.width - this.frame.width) < 1 && Math.abs(this.image.height - this.frame.height) < 1) {
				const scale = 1.2
				const w = this.image.width * scale
				const h = this.image.height * scale
				this.image = {
					left: this.frame.left - (w - this.frame.width) / 2,
					top: this.frame.top - (h - this.frame.height) / 2,
					width: w,
					height: h
				}
			}
		},
		flashTransit() {
			this.transit = true
			setTimeout(() => {
				this.transit = false
			}, 300)
		},
		// ---------- 手势:图片移动/双指缩放 ----------
		onBodyTouchStart(e) {
			this.touchType = 'body'
			this.touches = e.touches
			this.saveStart()
		},
		onBodyTouchMove(e) {
			const touches = e.touches
			if (this.touches.length === 1 && touches.length === 1) {
				this.moveImage(this.touches[0], touches[0])
			} else if (this.touches.length === 2 && touches.length === 2) {
				let ta = this.touches[0]
				let tb = this.touches[1]
				let tc = touches[0]
				let td = touches[1]
				if (ta.identifier !== tc.identifier) {
					const temp = tc
					tc = td
					td = temp
				}
				this.scaleImage(ta, tb, tc, td)
			}
		},
		onBodyTouchEnd() {
			this.touches = []
		},
		moveImage(ta, tb) {
			const ax = tb.clientX - ta.clientX
			const ay = tb.clientY - ta.clientY
			let left = this.startImage.left + ax
			let top = this.startImage.top + ay
			const f = this.startFrame
			if (left > f.left) left = f.left
			if (top > f.top) top = f.top
			if (left + this.startImage.width < f.left + f.width) left = f.left + f.width - this.startImage.width
			if (top + this.startImage.height < f.top + f.height) top = f.top + f.height - this.startImage.height
			this.image = Object.assign({}, this.startImage, { left, top })
		},
		scaleImage(ta, tb, tc, td) {
			const ol = Math.sqrt(Math.pow(ta.clientX - tb.clientX, 2) + Math.pow(ta.clientY - tb.clientY, 2))
			const el = Math.sqrt(Math.pow(tc.clientX - td.clientX, 2) + Math.pow(tc.clientY - td.clientY, 2))
			const ocx = (ta.clientX + tb.clientX) / 2
			const ocy = (ta.clientY + tb.clientY) / 2
			const ecx = (tc.clientX + td.clientX) / 2
			const ecy = (tc.clientY + td.clientY) / 2
			const ax = ecx - ocx
			const ay = ecy - ocy
			let scale = el / ol
			const f = this.startFrame
			if (this.startImage.width * scale < f.width) scale = f.width / this.startImage.width
			if (this.startImage.height * scale < f.height) scale = f.height / this.startImage.height
			const width = this.startImage.width * scale
			const height = this.startImage.height * scale
			let left = this.startImage.left + ax - (ocx - this.startImage.left) * (scale - 1)
			let top = this.startImage.top + ay - (ocy - this.startImage.top) * (scale - 1)
			if (left > f.left) left = f.left
			if (top > f.top) top = f.top
			if (left + width < f.left + f.width) left = f.left + f.width - width
			if (top + height < f.top + f.height) top = f.top + f.height - height
			this.image = { left, top, width, height }
		},
		// ---------- 手势:四角拖拽缩放裁剪框 ----------
		onCornerTouchStart(e) {
			this.touchType = e.currentTarget.dataset.corner
			this.touches = e.touches
			this.saveStart()
		},
		onCornerTouchMove(e) {
			if (this.touches.length !== 1) return
			this.scaleFrame(this.touches[0], e.touches[0])
		},
		onCornerTouchEnd() {
			this.touches = []
		},
		scaleFrame(ta, tb) {
			const ax = tb.clientX - ta.clientX
			const ay = tb.clientY - ta.clientY
			const f = this.startFrame
			let x1 = f.left
			let y1 = f.top
			let x2 = f.left + f.width
			let y2 = f.top + f.height
			const img = this.startImage
			let cx1 = false
			let cy1 = false
			let cx2 = false
			let cy2 = false
			const mix = 30
			const rate = this.frame.width / this.frame.height
			switch (this.touchType) {
				case 'lt':
					x1 += ax
					y1 += ay
					cx1 = true
					cy1 = true
					break
				case 'lb':
					x1 += ax
					y2 += ay
					cx1 = true
					cy2 = true
					break
				case 'rt':
					x2 += ax
					y1 += ay
					cx2 = true
					cy1 = true
					break
				default: // rb
					x2 += ax
					y2 += ay
					cx2 = true
					cy2 = true
					break
			}
			if (x1 < img.left) x1 = img.left
			if (y1 < img.top) y1 = img.top
			if (x2 > img.left + img.width) x2 = img.left + img.width
			if (y2 > img.top + img.height) y2 = img.top + img.height
			if (cx1 && x1 > x2 - mix) x1 = x2 - mix
			if (cy1 && y1 > y2 - mix) y1 = y2 - mix
			if (cx2 && x2 < x1 + mix) x2 = x1 + mix
			if (cy2 && y2 < y1 + mix) y2 = y1 + mix
			// fixed / ratio 模式保持裁剪框宽高比
			if (this.mode !== 'free') {
				if (cx1 && x1 < x2 - rate * (y2 - y1)) x1 = x2 - rate * (y2 - y1)
				if (cy1 && y1 < y2 - (x2 - x1) / rate) y1 = y2 - (x2 - x1) / rate
				if (cx2 && x2 > rate * (y2 - y1) + x1) x2 = rate * (y2 - y1) + x1
				if (cy2 && y2 > (x2 - x1) / rate + y1) y2 = (x2 - x1) / rate + y1
			}
			this.frame = { left: x1, top: y1, width: x2 - x1, height: y2 - y1 }
		},
		saveStart() {
			this.startFrame = Object.assign({}, this.frame)
			this.startImage = Object.assign({}, this.image)
		},
		// ---------- 旋转 ----------
		onRotate() {
			this.rotate -= 90
			const f = this.frame
			let width = this.image.height
			let height = this.image.width
			let left = this.image.left
			let top = this.image.top
			const rate = width / height
			if (width < f.width) {
				width = f.width
				height = width / rate
			}
			if (height < f.height) {
				height = f.height
				width = height * rate
			}
			if (left > f.left) left = f.left
			if (top > f.top) top = f.top
			if (left + width < f.left + f.width) left = f.left + f.width - width
			if (top + height < f.top + f.height) top = f.top + f.height - height
			this.image = { left, top, width, height }
			this.flashTransit()
		},
		// ---------- 按钮 ----------
		onCancel() {
			this.$emit('cancel')
		},
		onOk() {
			// #ifdef MP-WEIXIN
			this.cropWithNode()
			// #endif
			// #ifdef APP-PLUS || H5
			this.cropWithContext()
			// #endif
		},
		// ---------- 裁剪矩阵(输出尺寸与源图区域换算) ----------
		computeMatrix() {
			let width = this.width
			let height = this.height
			let mul = this.image.width / this.real.width
			if (this.rotate % 180 !== 0) {
				mul = this.image.height / this.real.width
			}
			if (this.mode !== 'fixed') {
				width = this.frame.width / mul
				height = this.frame.height / mul
			}
			const rate = width / height
			if (width > this.maxWidth) {
				width = this.maxWidth
				height = width / rate
			}
			if (height > this.maxHeight) {
				height = this.maxHeight
				width = height * rate
			}
			let sx = (this.frame.left - this.image.left) / mul
			let sy = (this.frame.top - this.image.top) / mul
			let sw = this.frame.width / mul
			let sh = this.frame.height / mul
			let ox = sx + sw / 2
			let oy = sy + sh / 2
			if (this.rotate % 180 !== 0) {
				const temp = sw
				sw = sh
				sh = temp
			}
			const angle = ((this.rotate % 360) + 360) % 360
			if (angle === 270) {
				const x = this.real.width - oy
				const y = ox
				ox = x
				oy = y
			}
			if (angle === 180) {
				const x = this.real.width - ox
				const y = this.real.height - oy
				ox = x
				oy = y
			}
			if (angle === 90) {
				const x = oy
				const y = this.real.height - ox
				ox = x
				oy = y
			}
			sx = ox - sw / 2
			sy = oy - sh / 2
			const dr = this.parseRect({ x: 0, y: 0, w: width, h: height }, -this.rotate)
			return {
				tw: width,
				th: height,
				sx,
				sy,
				sw,
				sh,
				dx: dr.x,
				dy: dr.y,
				dw: dr.w,
				dh: dr.h
			}
		},
		parsePoint(point, angle) {
			const rad = (angle * Math.PI) / 180
			return {
				x: point.x * Math.cos(rad) - point.y * Math.sin(rad),
				y: point.y * Math.cos(rad) + point.x * Math.sin(rad)
			}
		},
		parseRect(rect, angle) {
			const p1 = this.parsePoint({ x: rect.x, y: rect.y }, angle)
			const p2 = this.parsePoint({ x: rect.x + rect.w, y: rect.y + rect.h }, angle)
			return {
				x: Math.min(p1.x, p2.x),
				y: Math.min(p1.y, p2.y),
				w: Math.abs(p2.x - p1.x),
				h: Math.abs(p2.y - p1.y)
			}
		},
		// ---------- 导出:微信小程序(2d node) ----------
		async cropWithNode() {
			const mx = this.computeMatrix()
			this.target = { width: mx.tw, height: mx.th }
			const node = await new Promise((resolve) => {
				uni.createSelectorQuery()
					.in(this)
					.select('.h-cropper__canvas')
					.fields({ node: true })
					.exec((rst) => {
						resolve(rst && rst[0] ? rst[0].node : null)
					})
			})
			if (!node) {
				console.error('canvas node 获取失败')
				return
			}
			node.width = mx.tw
			node.height = mx.th
			uni.showLoading({ title: '处理中' })
			await new Promise((resolve) => setTimeout(resolve, 200))
			const context = node.getContext('2d')
			const image = node.createImage()
			await new Promise((resolve, reject) => {
				image.onload = resolve
				image.onerror = reject
				image.src = this.url
			})
			context.save()
			context.rotate((this.rotate * Math.PI) / 180)
			context.drawImage(image, mx.sx, mx.sy, mx.sw, mx.sh, mx.dx, mx.dy, mx.dw, mx.dh)
			context.restore()
			wx.canvasToTempFilePath({
				canvas: node,
				success: (rst) => {
					this.$emit('ok', { path: rst.tempFilePath })
				},
				complete: () => {
					uni.hideLoading()
				}
			})
		},
		// ---------- 导出:App / H5(createCanvasContext) ----------
		async cropWithContext() {
			const mx = this.computeMatrix()
			this.target = { width: mx.tw, height: mx.th }
			uni.showLoading({ title: '处理中' })
			await new Promise((resolve) => setTimeout(resolve, 200))
			const context = uni.createCanvasContext(this.canvasId, this)
			context.save()
			context.rotate((this.rotate * Math.PI) / 180)
			context.drawImage(this.url, mx.sx, mx.sy, mx.sw, mx.sh, mx.dx, mx.dy, mx.dw, mx.dh)
			context.restore()
			await new Promise((resolve) => {
				context.draw(false, resolve)
			})
			uni.canvasToTempFilePath(
				{
					canvasId: this.canvasId,
					destWidth: mx.tw,
					destHeight: mx.th,
					success: (rst) => {
						const path = rst.tempFilePath
						// #ifdef H5
						this.$emit('ok', { path: this.parseBlob(path), base64: path })
						// #endif
						// #ifdef APP-PLUS
						this.$emit('ok', { path })
						// #endif
					},
					complete: () => {
						uni.hideLoading()
					}
				},
				this
			)
		},
		/** H5:base64(dataURL) 转 blob URL */
		parseBlob(base64) {
			const arr = base64.split(',')
			const mime = arr[0].match(/:(.*?);/)[1]
			const bstr = atob(arr[1])
			const n = bstr.length
			const u8arr = new Uint8Array(n)
			for (let i = 0; i < n; i++) {
				u8arr[i] = bstr.charCodeAt(i)
			}
			const url = URL || webkitURL
			return url.createObjectURL(new Blob([u8arr], { type: mime }))
		}
	}
}
</script>

<style scoped>
.h-cropper__panel {
	position: fixed;
	width: 100%;
	height: 100%;
	top: 0;
	bottom: 0;
	left: 0;
	z-index: 1000;
	overflow: hidden;
}
.h-cropper__canvas {
	position: absolute;
	top: 5000px;
	left: 5000px;
}
.h-cropper__toolbar {
	position: absolute;
	width: 100%;
	height: 100rpx;
	left: 0;
	bottom: 0;
	display: flex;
	justify-content: space-around;
	align-items: center;
}
.h-cropper__btn {
	font-size: 40rpx;
	font-weight: bold;
}
.h-cropper__btn-cancel,
.h-cropper__btn-rotate {
	color: #d5dfe5;
}
.h-cropper__btn-ok {
	color: #ffffff;
}
.h-cropper__body {
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	background: black;
	overflow: hidden;
}
.h-cropper__mask {
	position: absolute;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	background: black;
	opacity: 0.4;
}
.h-cropper__image-wrap {
	position: absolute;
}
.h-cropper__image-rect {
	position: absolute;
}
.h-cropper__image {
	position: absolute;
}
.h-cropper__frame {
	position: absolute;
}
.h-cropper__rect {
	position: absolute;
	left: -2px;
	top: -2px;
	width: 100%;
	height: 100%;
	border: 2px solid white;
	overflow: hidden;
	box-sizing: content-box;
}
.h-cropper__line {
	position: absolute;
	background: white;
}
.h-cropper__line-h1,
.h-cropper__line-h2 {
	width: 100%;
	height: 1px;
	left: 0;
}
.h-cropper__line-h1 {
	top: 33.3%;
}
.h-cropper__line-h2 {
	top: 66.7%;
}
.h-cropper__line-v1,
.h-cropper__line-v2 {
	width: 1px;
	height: 100%;
	top: 0;
}
.h-cropper__line-v1 {
	left: 33.3%;
}
.h-cropper__line-v2 {
	left: 66.7%;
}
.h-cropper__corner {
	position: absolute;
	width: 20px;
	height: 20px;
	box-sizing: content-box;
}
.h-cropper__corner-lt {
	left: -6px;
	top: -6px;
	border-left: 4px solid red;
	border-top: 4px solid red;
}
.h-cropper__corner-lb {
	left: -6px;
	bottom: -6px;
	border-left: 4px solid red;
	border-bottom: 4px solid red;
}
.h-cropper__corner-rt {
	right: -6px;
	top: -6px;
	border-right: 4px solid red;
	border-top: 4px solid red;
}
.h-cropper__corner-rb {
	right: -6px;
	bottom: -6px;
	border-right: 4px solid red;
	border-bottom: 4px solid red;
}
.transit {
	transition:
		width 0.3s,
		height 0.3s,
		left 0.3s,
		top 0.3s,
		transform 0.3s;
}
</style>
