import { ref, onMounted } from 'vue'
import { FindAllBrand } from '@/api/brand.js'
import { FindCategoryByParentId } from '@/api/category.js'
import { GetProductPageList, SaveProduct, UpdateProductById, GetProductById, DeleteProductById, UpdateProductStatus, UpdateProductAuditStatus } from '@/api/product.js'
import { FindBrandByCategoryId } from '@/api/categoryBrand.js'
import { FindAllProductUnit } from '@/api/productUnit.js'
import { FindAllProductSpec } from '@/api/productSpec.js'
import { useApp } from '@/pinia/modules/app'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useProduct() {
  // ----------------------------------------------------分页列表查询 start --------------------------------------------------------------------------
  // 品牌列表数据模型
  const brandList = ref([])

  // 分类列表
  const props = {
    lazy: true,
    value: 'id',
    label: 'name',
    leaf: 'leaf',
    checkStrictly: true,
    async lazyLoad(node, resolve) {   // 加载数据的方法
      if (typeof node.value == 'undefined') node.value = 0
      const { data } = await FindCategoryByParentId(node.value)
      data.forEach(function(item) {       //hasChildren判断是否有子节点
        item.leaf = !item.hasChildren
      })
      resolve(data)  // 返回数据
    }
  };
  const categoryProps = ref(props)

  // 选中的数据模型
  const queryDto = ref({ brandId: '', category1Id: '', category2Id: '', category3Id: '' })
  const searchCategoryIdList = ref([])

  //分页数据
  const pageParamsForm = {
    page: 1, // 页码
    limit: 10, // 每页记录数
  }
  const pageParams = ref(pageParamsForm)

  // 表格数据模型
  const list = ref([])

  // 总记录数数据模型
  const total = ref(0)

  // 钩子函数
  onMounted(async () => {
    const { data } = await FindAllBrand()
    brandList.value = data
    fetchData()
  })

  //页面变化
  const handleSizeChange = size => {
    pageParams.value.limit = size
    fetchData()
  }
  const handleCurrentChange = number => {
    pageParams.value.page = number
    fetchData()
  }

  //重置
  const resetData = () => {
    queryDto.value = {}
    searchCategoryIdList.value = []
    fetchData()
  }

  // 分页列表查询方法
  const fetchData = async () => {
    // 处理三级分类的id
    if (searchCategoryIdList.value.length == 1) {
      queryDto.value.category1Id = searchCategoryIdList.value[0]
    }

    if (searchCategoryIdList.value.length == 2) {
      queryDto.value.category1Id = searchCategoryIdList.value[0]
      queryDto.value.category2Id = searchCategoryIdList.value[1]
    }

    if (searchCategoryIdList.value.length == 3) {
      queryDto.value.category1Id = searchCategoryIdList.value[0]
      queryDto.value.category2Id = searchCategoryIdList.value[1]
      queryDto.value.category3Id = searchCategoryIdList.value[2]
    }

    // 请求后端接口
    const { data } = await GetProductPageList(pageParams.value.page, pageParams.value.limit, queryDto.value)
    data.list.forEach(item => {
      item.sliderUrls = item.sliderUrls.split(',')
    })

    // 将响应结果数据设置给对应的属性模型
    list.value = data.list
    total.value = data.total
  }

  // 控制对话框是否展示的数据模型
  const dialogVisible = ref(false)

  // 表单label对其方式
  const labelPosition = ref('top')

  // 对话框激活的面板
  const activeNames = ref(['productBaseInfo', 'skuInfo', 'productDetails', 'submit'])

  // 表单数据模型
  const defaultForm = {
    id: '',
    name: '',
    brandId: '',
    category1Id: '',
    category2Id: '',
    category3Id: '',
    unitName: '',
    sliderUrls: '',
    specValue: '',
    productSkuList: [],
    detailsImageUrls: '',
  }
  const product = ref(defaultForm)

  //进入添加
  const addShow = () => {
    props.checkStrictly = false
    dialogVisible.value = true
    //清空
    fileList.value = []
    categoryIdList.value = []
    detailsFileList.value = []
    sliderUrlList.value = []
    specValueList.value = []
    product.value = {}
    fetchProductUnit()
    fetchProductSpect()
  }

  // 分类级联选择器选中数据以后的数据模型
  const categoryIdList = ref([])

  // 分类级联选择器数据发送改变以后的事件处理函数
  const categoryChange = async () => {
    if (categoryIdList.value.length == 3) {
      product.value.category1Id = categoryIdList.value[0]
      product.value.category2Id = categoryIdList.value[1]
      product.value.category3Id = categoryIdList.value[2]
      const { data } = await FindBrandByCategoryId(categoryIdList.value[2])
      categoryBrandList.value = data
      if (product.value.id) {
        product.value.brandId = ''
      }
    }
  }

  // 定义品牌的数据模型
  const categoryBrandList = ref([])

  // 商品单元的数据模型
  const unitList = ref([])

  // 查询所有的商品单元
  const fetchProductUnit = async () => {
    const { data } = await FindAllProductUnit()
    unitList.value = data
  }

  // 商品规格数据数据模型
  const specList = ref([])

  // 查询产品规格数据
  const fetchProductSpect = async () => {
    const { data } = await FindAllProductSpec()
    specList.value = data
  }

  const saveOrUpdate = () => {
    console.log('submit!' + product.value)
    product.value.sliderUrls = sliderUrlList.value.join(',')
    product.value.detailsImageUrls = detailsImageUrlList.value.join(',')
    if (!product.value.id) {
      saveData()
    } else {
      updateData()
    }
  }

  // 新增
  const saveData = async () => {
    await SaveProduct(product.value)
    dialogVisible.value = false
    ElMessage.success('操作成功')
    fetchData()
  }

  // 修改
  const updateData = async () => {
    console.log(product.value)
    await UpdateProductById(product.value)
    dialogVisible.value = false
    ElMessage.success('操作成功')
    fetchData()
  }

  // 展示选择的规则数据
  const specValueList = ref([])
  const changeSpecValueList = () => {
    specValueList.value = JSON.parse(product.value.specValue)
    const specValueArr = []
    specValueList.value.forEach((item, index) => {
      specValueArr.push(item.valueList)
    })

    // last为上次运算的结果，current为数组中的当前元素
    var result = specValueArr.reduce((last, current) => {
      const array = [];
      last.forEach(par1 => {
        current.forEach(par2 => {
          array.push(par1 + " + " + par2);
        });
      });
      return array;
    });

    console.log(result)
    product.value.productSkuList = []
    result.forEach(function (item) {
      product.value.productSkuList.push({
        skuSpec: item,
        price: 0,
      })
    })
  }

  // 上传商品轮播图图片
  const sliderUrlList = ref([])
  const headers = {
    token: useApp().authorization.token     // 从pinia中获取token，在进行文件上传的时候将token设置到请求头中
  }
  const handleRemove = (uploadFile, uploadFiles) => {
    sliderUrlList.value.splice(sliderUrlList.value.indexOf(uploadFile.url), 1)
  }
  const fileList = ref([])
  const handleSliderSuccess = (response, uploadFile) => {
    sliderUrlList.value.push(response.data)
  }

  //sku图片
  const handleSkuSuccess = (response, uploadFile, fileList, row) => {
    row.thumbImg = response.data
  }

  // 上传商品详情图片
  const detailsFileList = ref([])
  const handleDetailsRemove = (uploadFile, uploadFiles) => {
    console.log(uploadFile, uploadFiles)
    detailsImageUrlList.value.splice(detailsImageUrlList.value.indexOf(uploadFile.url), 1)
    console.log(sliderUrlList.value)
  }
  const detailsImageUrlList = ref([])
  const handleDetailsSuccess = (response, uploadFile) => {
    console.log(response)
    detailsImageUrlList.value.push(response.data)
    console.log(detailsImageUrlList.value)
  }

  //进入修改
  const editShow = row => {
    getById(row.id)
    props.checkStrictly = false
    dialogVisible.value = true
  }

  const getById = async id => {
    const { data } = await GetProductById(id)
    product.value = data
    // eslint-disable-next-line no-debugger
    console.log(product.value)

    //分类赋值
    categoryIdList.value = [product.value.category1Id, product.value.category2Id, product.value.category3Id]

    //处理图片
    fileList.value = []
    sliderUrlList.value = product.value.sliderUrls.split(',')
    sliderUrlList.value.forEach(url => {
      fileList.value.push({ url: url })
    })

    // 处理规格数据
    specValueList.value = JSON.parse(product.value.specValue)

    //处理sku
    // product.value.productSkuList.forEach(sku => {
    //   sku.skuSpec = sku.skuSpec.split(',').join(' * ')
    //   sku.skuSpecList = sku.skuSpec.split(',')
    // })

    //处理详情图片
    detailsFileList.value = []
    detailsImageUrlList.value = product.value.detailsImageUrls.split(',')
    detailsImageUrlList.value.forEach(url => {
      detailsFileList.value.push({ url: url })
    })

    //加载分类品牌
    getBrand()
    fetchProductUnit()
    fetchProductSpect()
  }

  const getBrand = async () => {
    const { data } = await FindBrandByCategoryId(categoryIdList.value[2])
    categoryBrandList.value = data
  }

  //删除
  const remove = async id => {
    console.log('removeDataById:' + id)
    ElMessageBox.confirm('此操作将永久删除该记录, 是否继续?', 'Warning', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
      .then(async () => {
        await DeleteProductById(id)
        ElMessage.success('删除成功')
        fetchData()
      })
      .catch(() => {
        ElMessage.info('取消删除')
      })
  }

  const dialogAuditVisible = ref(false)
  const audit = async id => {
    const { data } = await GetProductById(id)
    product.value = data

    //分类赋值
    categoryIdList.value = [product.value.category1Id, product.value.category2Id, product.value.category3Id]

    //处理图片
    fileList.value = []
    sliderUrlList.value = product.value.sliderUrls.split(',')
    sliderUrlList.value.forEach(url => {
      fileList.value.push({ url: url })
    })

    // 处理规格数据
    specValueList.value = JSON.parse(product.value.specValue)

    //处理详情图片
    detailsFileList.value = []
    detailsImageUrlList.value = product.value.detailsImageUrls.split(',')
    detailsImageUrlList.value.forEach(url => {
      detailsFileList.value.push({ url: url })
    })

    //加载分类品牌
    getBrand()
    fetchProductUnit()
    fetchProductSpect()

    dialogAuditVisible.value = true
  }

  const updateAuditStatus = async (id, auditStatus) => {
    await UpdateProductAuditStatus(id, auditStatus)
    dialogAuditVisible.value = false
    ElMessage.success('操作成功')
    fetchData()
  }

  //上下架
  const updateStatus = async (id, status) => {
    await UpdateProductStatus(id, status)
    dialogAuditVisible.value = false
    ElMessage.success('操作成功')
    fetchData()
  }

  // 返回所有需要在模板中使用的变量和方法
  return {
    brandList,
    categoryProps,
    queryDto,
    searchCategoryIdList,
    pageParams,
    list,
    total,
    handleSizeChange,
    handleCurrentChange,
    resetData,
    fetchData,
    dialogVisible,
    labelPosition,
    activeNames,
    product,
    addShow,
    categoryIdList,
    categoryChange,
    categoryBrandList,
    unitList,
    specList,
    saveOrUpdate,
    specValueList,
    changeSpecValueList,
    sliderUrlList,
    headers,
    handleRemove,
    fileList,
    handleSliderSuccess,
    handleSkuSuccess,
    detailsFileList,
    handleDetailsRemove,
    detailsImageUrlList,
    handleDetailsSuccess,
    editShow,
    remove,
    dialogAuditVisible,
    audit,
    updateAuditStatus,
    updateStatus
  }
}