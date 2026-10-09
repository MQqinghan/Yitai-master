import { useApp } from '@/pinia/modules/app'
import { ElMessage } from 'element-plus'

/**
 * 获取文件上传请求头（带token）
 * @returns {Object} 上传请求头对象
 */
export function getUploadHeaders() {
  const appStore = useApp()
  return {
    token: appStore.authorization.token
  }
}

/**
 * 校验品牌表单数据
 * @param {Object} form - 品牌表单对象
 * @returns {Object} { valid: 布尔值, message: 提示语 }
 */
export function validateBrandForm(form) {
  // 品牌名称非空校验
  if (!form.name || form.name.trim() === '') {
    return { valid: false, message: '品牌名称不能为空' }
  }
  // 可选：添加名称长度校验
  if (form.name.length > 50) {
    return { valid: false, message: '品牌名称不能超过50个字符' }
  }
  return { valid: true, message: '' }
}

/**
 * 处理文件上传成功逻辑
 * @param {Object} response - 上传接口返回数据
 * @param {Ref} brandRef - 品牌响应式对象
 */
export function handleUploadSuccess(response, brandRef) {
  if (response && response.data) {
    brandRef.value.logo = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败：返回数据异常')
  }
}

/**
 * 重置品牌表单（可扩展）
 * @param {Ref} brandRef - 品牌响应式对象
 * @returns {Object} 重置后的默认表单
 */
export function resetBrandForm(brandRef) {
  const defaultForm = { id: '', name: '', logo: '' }
  brandRef.value = { ...defaultForm }
  return defaultForm
}