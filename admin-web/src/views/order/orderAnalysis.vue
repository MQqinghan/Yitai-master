<template>
  <div class="order-analysis-container">
    <!-- 搜索区域 -->
    <div class="search-div">
      <el-form label-width="90px" size="small">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="createTimes"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="截止日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="排行数量">
              <el-input-number
                v-model="limit"
                :min="5"
                :max="50"
                placeholder="默认10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" size="small" @click="handleSearch">
              搜索
            </el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 热销商品排行榜 -->
    <el-card class="chart-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">🔥 热销商品排行榜</span>
        </div>
      </template>
      <div ref="hotProductChart" class="chart-container"></div>
    </el-card>

    <!-- 品类销售分析 -->
    <el-card class="chart-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">📊 品类销售分析</span>
        </div>
      </template>
      <div class="category-analysis-wrapper">
        <div ref="categorySalesChart" class="chart-container"></div>
        <div class="category-table">
          <el-table :data="categorySalesList" border stripe style="width: 100%">
            <el-table-column prop="categoryName" label="品类名称" width="150" />
            <el-table-column prop="totalSales" label="总销量" width="120" align="center">
              <template #default="{ row }">
                <el-tag type="success">{{ row.totalSales }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="销售金额" width="150" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatAmount(row.totalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="salesRatio" label="销售占比" align="right">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.salesRatio"
                  :color="getProgressColor(row.salesRatio)"
                  :stroke-width="18"
                  :text-inside="true"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, ref, nextTick } from 'vue'
import { GetHotProductList, GetCategorySalesList } from '@/api/orderInfo.js'

const hotProductChart = ref(null)
const categorySalesChart = ref(null)
const createTimes = ref([])
const limit = ref(10)
const categorySalesList = ref([])

const searchParams = ref({
  createTimeBegin: '',
  createTimeEnd: '',
  limit: 10,
})

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

const handleResize = () => {
  const hotChart = echarts.getInstanceByDom(hotProductChart.value)
  const categoryChart = echarts.getInstanceByDom(categorySalesChart.value)
  hotChart?.resize()
  categoryChart?.resize()
}

const handleSearch = () => {
  if (createTimes.value.length === 2) {
    searchParams.value.createTimeBegin = createTimes.value[0]
    searchParams.value.createTimeEnd = createTimes.value[1]
  } else {
    searchParams.value.createTimeBegin = ''
    searchParams.value.createTimeEnd = ''
  }
  searchParams.value.limit = limit.value || 10
  fetchData()
}

const handleReset = () => {
  createTimes.value = []
  limit.value = 10
  searchParams.value = {
    createTimeBegin: '',
    createTimeEnd: '',
    limit: 10,
  }
  fetchData()
}

const fetchData = async () => {
  await Promise.all([
    fetchHotProductList(),
    fetchCategorySalesList(),
  ])
}

const fetchHotProductList = async () => {
  try {
    const { data } = await GetHotProductList(searchParams.value)
    setHotProductChartOption(data)
  } catch (error) {
    console.error('获取热销商品数据失败:', error)
  }
}

const fetchCategorySalesList = async () => {
  try {
    const { data } = await GetCategorySalesList({
      createTimeBegin: searchParams.value.createTimeBegin,
      createTimeEnd: searchParams.value.createTimeEnd,
    })
    categorySalesList.value = data
    await nextTick()
    setCategorySalesChartOption(data)
  } catch (error) {
    console.error('获取品类销售数据失败:', error)
  }
}

const setHotProductChartOption = (data) => {
  const myChart = echarts.init(hotProductChart.value)

  const productNames = data.map(item => item.productName).reverse()
  const salesData = data.map(item => item.totalSales).reverse()
  const amountData = data.map(item => item.totalAmount).reverse()
  const reversedData = data.slice().reverse()

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      formatter: (params) => {
        const item = reversedData[params[0].dataIndex]
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 5px;">${item.productName}</div>
            <div>排名: 第${item.rank}名</div>
            <div>销量: ${item.totalSales} 件</div>
            <div>销售额: ¥${formatAmount(item.totalAmount)}</div>
          </div>
        `
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      name: '销量',
      axisLabel: {
        formatter: '{value}',
      },
    },
    yAxis: {
      type: 'category',
      data: productNames,
      axisLabel: {
        interval: 0,
        formatter: (value) => {
          return value.length > 10 ? value.substring(0, 10) + '...' : value
        },
      },
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        data: salesData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#ff6b6b' },
            { offset: 1, color: '#ffa502' },
          ]),
        },
        label: {
          show: true,
          position: 'right',
          formatter: (params) => {
            return `¥${formatAmount(amountData[params.dataIndex])}`
          },
        },
        barWidth: '60%',
      },
    ],
  }

  myChart.setOption(option)
}

const setCategorySalesChartOption = (data) => {
  const myChart = echarts.init(categorySalesChart.value)

  const categoryNames = data.map(item => item.categoryName)
  const salesData = data.map(item => ({
    value: item.totalAmount,
    name: item.categoryName,
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 5px;">${params.name}</div>
            <div>销售额: ¥${formatAmount(params.value)}</div>
            <div>占比: ${params.percent}%</div>
          </div>
        `
      },
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
    },
    series: [
      {
        name: '品类销售',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
        },
        data: salesData,
      },
    ],
  }

  myChart.setOption(option)
}

const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return Number(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

const getProgressColor = (percentage) => {
  if (percentage >= 40) return '#f56c6c'
  if (percentage >= 20) return '#e6a23c'
  return '#67c23a'
}
</script>

<style scoped lang="scss">
.order-analysis-container {
  padding: 20px;

  .search-div {
    margin-bottom: 20px;
    padding: 20px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background-color: #fff;
  }

  .chart-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: bold;
      }
    }

    .chart-container {
      width: 100%;
      height: 500px;
    }

    .category-analysis-wrapper {
      display: flex;
      gap: 20px;

      .chart-container {
        flex: 1;
        min-height: 400px;
      }

      .category-table {
        flex: 1;
        max-height: 400px;
        overflow-y: auto;

        .amount-text {
          color: #f56c6c;
          font-weight: bold;
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .order-analysis-container {
    .chart-card {
      .category-analysis-wrapper {
        flex-direction: column;
      }
    }
  }
}
</style>
