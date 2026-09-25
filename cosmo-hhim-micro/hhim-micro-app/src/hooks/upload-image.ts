import { server, api_pre, uuc } from '@/utils/common'
import { computed, getCurrentInstance, ref } from 'vue'
import compress from '@/components/w-compress/compress.js'

const HooksUploadImage = (count = 1) => {
  // 已选图片集合
  const uploadImageList = ref([])
  // 选择图片
  function choseImage(wCompress?, options = { width: 800, height: 800, zip: false }, sourceType?) {
    // 如果已经选够指定数量的图片，就不再选择图片
    if (count === uploadImageList.value.length) return
    return new Promise((resolve, reject) => {
      uni.chooseImage({
        count: count - uploadImageList.value.length,
        sourceType: sourceType || ['album', 'camera'],
        sizeType: 'compressed',
        success: async (res) => {
          if (options.zip) {
            const arr = []
            const temp = Array.isArray(res.tempFilePaths) ? res.tempFilePaths : [res.tempFilePaths]
            temp.forEach((item, idx) => {
              arr.push(compress(item, idx + 1, wCompress, options))
            })
            Promise.all(arr).then((res) => {
              if (count === 1) {
                uploadImageList.value = res
              } else {
                uploadImageList.value.push(...res)
              }
              resolve(res)
            })
          } else {
            resolve(res.tempFilePaths)
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }
  // 上传图片
  function uploadImage(hideLoading = false) {
    if (!uploadImageList.value.length) {
      return new Promise((resolve, reject) => {
        resolve('')
      })
    }
    const arrReq = []
    const header = {
      type: uuc ? 'microMobileApp' : 'wechatMiniApp',
      application_sign: 'micro_process',
      Authorization: 'Bearer ' + uni.getStorageSync('micro_token'),
      username: uni.getStorageSync('micro_user')
    }
    const result = []
    !hideLoading &&
      uni.showLoading({
        title: '图片上传中...'
      })
    uploadImageList.value.forEach((item) => {
      // 已是完整 http(s) 图片地址（已上传到对象存储的）则跳过重新上传；本地临时文件（blob:/wxfile:/file: 等）才需要上传
      if (!/^https?:\/\//.test(item)) {
        arrReq.push(
          new Promise((resolve, reject) => {
            uni.uploadFile({
              url: `${server}${api_pre}/upload/picture`,
              filePath: item,
              name: 'file',
              header,
              success: async (res) => {
                !hideLoading && uni.hideLoading()
                resolve(JSON.parse(res.data))
              }
            })
          })
        )
      } else {
        result.push(item)
      }
    })

    return new Promise((resolve, reject) => {
      if (arrReq.length === 0) {
        !hideLoading && uni.hideLoading()
      }
      Promise.all(arrReq).then((res) => {
        result.push(...res.map((item) => item.msg))
        resolve(result)
      })
    })
  }
  // 设置是否已有图片集合
  function setImageList(list) {
    uploadImageList.value = list.filter((item) => !!item)
  }
  // 根据下标索引删除图片
  function removeImage(index) {
    uploadImageList.value.splice(index, 1)
  }
  function _compressImage(path) {
    return new Promise((resolve, reject) => {
      if (path.includes('.jpg')) {
        uni.compressImage({
          src: path,
          quality: 50,
          success: (res) => {
            resolve(res.tempFilePath)
          }
        })
      } else {
        resolve(path)
      }
    })
  }
  function previewImg(tempFilePaths, current = 0) {
    uni.previewImage({
      urls: tempFilePaths,
      current,
      longPressActions: {
        itemList: ['发送给朋友', '保存图片', '收藏'],
        success: function (data) {},
        fail: function (err) {
        }
      }
    })
  }
  // 已选图片数量
  const uploadsCount = computed(() => uploadImageList.value.length)

  return {
    uploadImageList,
    setImageList,
    removeImage,
    uploadsCount,
    choseImage,
    uploadImage,
    previewImg
  }
}

export default HooksUploadImage
