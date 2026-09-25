/**
 * 图片压缩
 * @param {String} imgUrl  需要压缩的图片路径
 * @param {Object} self	必传，当前组件对象
 * @param {Object} options 压缩参数
 * 		width: 传入的最大宽度，即画布宽度，必传 按比例压缩到多宽
 * 		height: 传入的最大高度，即画布高度，必传 按比例压缩到多高
 * 		pixels: 压缩图片的最大分辨率，默认二百万
 * 		quality: 压缩质量，默认0.8
 * 		type: 获取的base64类型，默认jpg
 * 		base64: 是否返回base64，默认true(非H5有效)
 * @return {Promise}
 * 		reject
 * 			code
 * 				-1: 获取图片信息错误
 * 				-2: 极大可能创建图片对象出错(h5会出现，出现概率无限接近0)
 * 				-3: canvas转图片错误(小程序会出现)
 * 				-4: 图片转base64错误(小程序会出现)
 */
import { getCurrentInstance } from 'vue'

// 图片分辨率压缩
const calcImageSize = (res, pixels) => {
  let imgW, imgH
  imgW = res.width
  imgH = res.height

  let ratio
  if ((ratio = (imgW * imgH) / pixels) > 1) {
    ratio = Math.sqrt(ratio)
    imgW = parseInt(imgW / ratio)
    imgH = parseInt(imgH / ratio)
  } else {
    ratio = 1
  }

  return { imgW, imgH }
}



const compress = (imgUrl, idx, slef, options = {}) => {
  /*************** 参数默认值 ***************/
  const MAX_PIXELS = 2000000 // 最大分辨率，宽 * 高 的值
  const MAX_QUALITY = 1 // 压缩质量
  const IMG_TYPE = 'jpg'
  const CANVAS_ID = 'compress_canvas' + idx
  const BASE_64 = false

  return new Promise((resolve, reject) => {
    uni.getImageInfo({
      src: imgUrl,
      success: (res) => {
        let pixels = options.pixels || MAX_PIXELS
        let quality = options.quality || MAX_QUALITY
        let type = options.type || IMG_TYPE
        let canvasId = options.canvasId || CANVAS_ID
        let isBase64 = options.base64 || BASE_64

        let { imgW, imgH } = calcImageSize(res, pixels)
        let w = 0
        let h = 0
        //  计算要画的图片宽高，即输出的画布宽高，为了保持比例进行相应计算
        if (imgW > options.width && imgH <= options.height) {
          w = options.width
          h = (w * imgH) / imgW
        } else if (imgH > options.height && imgW <= options.width) {
          h = options.height
          w = (imgW * h) / imgH
        } else if (imgW > options.width && imgH > options.height) {
          if (imgW >= imgH) {
            w = options.width
            h = (w * imgH) / imgW
          } else {
            h = options.height
            w = (imgW * h) / imgH
          }
        } else {
          w = imgW
          h = imgH
        }
        // #ifndef H5
        type = type == 'png' ? 'png' : 'jpg'
        // #endif

        // slef.height = h
        // slef.width = w
        setTimeout(() => {
          let canvas = null
          if (!canvas) {
            canvas = uni.createCanvasContext(canvasId, slef)
          }
          canvas.drawImage(res.path, 0, 0, w, h)
          canvas.save()
          canvas.draw(false, () => {
            uni.canvasToTempFilePath(
              {
                x: 0,
                y: 0,
                width: w,
                height: h,
                destWidth: w,
                destHeight: h,
                canvasId,
                quality: quality,
                success: (file) => {
                  resolve(file.tempFilePath)
                },
                fail: (e) => {
                  reject({
                    code: -3,
                    msg: 'canvas转图片错误',
                    data: e
                  })
                }
              },
              slef
            )
          })
        }, 200)
      }
    })
  })
}

export default compress
