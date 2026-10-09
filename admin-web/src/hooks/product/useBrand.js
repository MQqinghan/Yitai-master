import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useApp } from '@/pinia/modules/app'
import { GetBrandPageList, SaveBrand, UpdateBrand, DeleteBrand } from '@/api/brand.js'
// 引入通用工具函数
import { getUploadHeaders, validateBrandForm, handleUploadSuccess } from '@/utils/brandHelper.js'

/**
 * 品牌管理核心业务逻辑Hook
 * 封装所有响应式数据和业务方法
 */
export function useBrand() {
  // -------------------------- 响应式数据 --------------------------
  // 上传请求头（从工具函数获取）
  const headers = ref(getUploadHeaders())
  // 品牌表单数据
  const defaultForm = { id: '', name: '', logo: '' }
  const brand = ref({ ...defaultForm })
  // 弹窗显隐
  const dialogVisible = ref(false)
  // 表格数据
  const list = ref([])
  // 分页数据
  const total = ref(0)
  const pageParams = ref({ page: 1, limit: 10 })

  // -------------------------- 业务方法 --------------------------
  /**
   * 显示添加弹窗
   */
  const addShow = () => {
    brand.value = { ...defaultForm }
    dialogVisible.value = true
  }

  /**
   * 显示修改弹窗
   * @param {Object} row - 表格行数据
   */
  const editShow = (row) => {
    brand.value = { ...row } // 深拷贝避免修改原数据
    dialogVisible.value = true
  }

  /**
   * 图片上传成功回调（调用工具函数）
   * @param {Object} response - 上传接口返回值
   */
  const handleAvatarSuccess = (response) => {
    handleUploadSuccess(response, brand)
  }

  /**
   * 保存/修改品牌
   */
  const saveOrUpdate = async () => {
    // 调用工具函数校验表单
    const validateResult = validateBrandForm(brand.value)
    if (!validateResult.valid) {
      ElMessage.warning(validateResult.message)
      return
    }

    try {
      if (!brand.value.id) {
        await SaveBrand(brand.value)
        ElMessage.success('添加成功')
      } else {
        await UpdateBrand(brand.value)
        ElMessage.success('修改成功')
      }
      dialogVisible.value = false
      fetchData() // 刷新列表
    } catch (error) {
      ElMessage.error(`操作失败：${error.message || '网络异常'}`)
    }
  }

  /**
   * 删除品牌
   * @param {Number|String} id - 品牌ID
   */
  const deleteBrand = async (id) => {
    try {
      await ElMessageBox.confirm('确定删除该品牌？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await DeleteBrand(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error){
      ElMessage.info('已取消删除')
    }
  }

  /**
   * 分页查询品牌列表
   */
  const fetchData = async () => {
    try {
      const { code, message, data } = await GetBrandPageList(pageParams.value.page, pageParams.value.limit)
      if (code === 200) {
        list.value = data.list
        total.value = data.total
      } else {
        ElMessage.error(`查询失败：${message}`)
      }
    } catch (error) {
      ElMessage.error(`查询失败：${error.message}`)
    }
  }

  /**
   * 分页条数变化
   * @param {Number} size - 每页条数
   */
  const handleSizeChange = (size) => {
    pageParams.value.limit = size
    fetchData()
  }

  /**
   * 页码变化
   * @param {Number} number - 当前页码
   */
  const handleCurrentChange = (number) => {
    pageParams.value.page = number
    fetchData()
  }

  // -------------------------- 暴露变量/方法 --------------------------
  return {
    brand,
    dialogVisible,
    headers,
    list,
    total,
    pageParams,
    addShow,
    editShow,
    handleAvatarSuccess,
    saveOrUpdate,
    deleteBrand,
    handleSizeChange,
    handleCurrentChange,
    fetchData
  }
}