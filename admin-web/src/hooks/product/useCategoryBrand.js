// src/hooks/product/useCategoryBrand.js
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FindAllBrand } from '@/api/brand.js'
import { GetCategoryBrandPageList, SaveCategoryBrand, UpdateCategoryBrandById, DeleteCategoryBrandById } from '@/api/categoryBrand.js'
import { formatCategoryId } from '@/utils/categoryHelper.js' // 工具函数

export function useCategoryBrand() {
  // 所有响应式数据
  const brandList = ref([])
  const list = ref([])
  const total = ref(0)
  const queryDto = ref({ brandId: '', categoryId: '' })
  const searchCategoryIdList = ref([])
  const pageParams = ref({ page: 1, limit: 2 })
  const dialogVisible = ref(false)
  const categoryBrand = ref({ id: '', brandId: '', categoryId: '' })

  // 初始化品牌列表
  const selectAllBrandList = async () => {
    const { data } = await FindAllBrand()
    brandList.value = data
  }

  // 分页查询
  const fetchData = async () => {
    queryDto.value.categoryId = formatCategoryId(searchCategoryIdList.value)
    const { data } = await GetCategoryBrandPageList(pageParams.value.page, pageParams.value.limit, queryDto.value)
    list.value = data.list
    total.value = data.total
    if (brandList.value.length === 0) await selectAllBrandList()
  }

  // 重置查询
  const resetData = () => {
    queryDto.value = { brandId: '', categoryId: '' }
    searchCategoryIdList.value = []
    fetchData()
  }

  // 分页事件
  const handleSizeChange = (size) => {
    pageParams.value.limit = size
    fetchData()
  }
  const handleCurrentChange = (num) => {
    pageParams.value.page = num
    fetchData()
  }

  // 弹窗操作
  const addShow = () => {
    categoryBrand.value = {}
    dialogVisible.value = true
  }
  const editShow = (row) => {
    categoryBrand.value = { ...row }
    dialogVisible.value = true
  }

  // 保存/更新
  const saveOrUpdate = async () => {
    if (!categoryBrand.value.brandId) {
      ElMessage.info('请选择品牌')
      return
    }
    const categoryId = formatCategoryId(categoryBrand.value.categoryId)
    if (!categoryId) {
      ElMessage.info('请选择分类')
      return
    }
    categoryBrand.value.categoryId = categoryId

    try {
      if (!categoryBrand.value.id) {
        await SaveCategoryBrand(categoryBrand.value)
        ElMessage.success('新增成功')
      } else {
        await UpdateCategoryBrandById(categoryBrand.value)
        ElMessage.success('修改成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }

  // 删除
  const remove = async (id) => {
    try {
      await ElMessageBox.confirm('确认删除?', '警告', { type: 'warning' })
      await DeleteCategoryBrandById(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.info('已取消')
    }
  }

  // 暴露给组件使用
  return {
    brandList, list, total, queryDto, searchCategoryIdList, pageParams,
    categoryBrand, dialogVisible,
    fetchData, resetData, handleSizeChange, handleCurrentChange,
    addShow, editShow, saveOrUpdate, remove
  }
}