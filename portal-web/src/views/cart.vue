<template>
  <div class="cart-page">
    <!-- 顶部导航栏（双端适配） -->
    <div class="cart-header">
      <h2 class="cart-title">我的购物车</h2>
      <!-- 移动端返回按钮（仅移动端显示） -->
      <el-button
          v-show="isMobile"
          class="back-btn"
          :icon="ArrowLeft"
          text
          @click="goBack"
      ></el-button>
    </div>

    <!-- 筛选/搜索区（双端差异化显示） -->
    <div class="cart-filter">
      <!-- PC端筛选区 -->
      <div class="pc-filter" v-show="!isMobile">
        <div class="filter-group">
          <!-- 商品类型筛选 -->
          <el-select v-model="typeFilter" placeholder="全部商品" class="filter-select">
            <el-option label="全部商品" value="all"></el-option>
            <el-option label="降价商品" value="discount"></el-option>
          </el-select>

          <!-- 商品分类筛选 -->
          <el-select v-model="categoryFilter" placeholder="商品分类" class="filter-select">
            <el-option label="全部分类" value="all"></el-option>
            <el-option label="数码产品" value="digital"></el-option>
            <el-option label="家居用品" value="home"></el-option>
            <el-option label="食品生鲜" value="food"></el-option>
          </el-select>

          <!-- 商品状态筛选 -->
          <el-select v-model="statusFilter" placeholder="商品状态" class="filter-select">
            <el-option label="全部状态" value="all"></el-option>
            <el-option label="失效商品" value="invalid"></el-option>
            <el-option label="即将售罄" value="stockout"></el-option>
          </el-select>
        </div>

        <!-- PC端搜索框（按钮内置） -->
        <div class="search-box pc-search">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索购物车商品"
              prefix-icon="Search"
              class="search-input"
          >
            <template #append>
              <el-button @click="searchGoods" class="search-btn">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 移动端筛选/搜索区 -->
      <div class="mobile-filter" v-show="isMobile">
        <!-- 移动端搜索框 -->
        <div class="search-box mobile-search">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索购物车商品"
              prefix-icon="Search"
              @keyup.enter="searchGoods"
              class="search-input"
          ></el-input>
        </div>

        <!-- 移动端商品类型切换 + 管理按钮 -->
        <div class="mobile-actions">
          <el-radio-group v-model="typeFilter" class="type-radio">
            <el-radio label="all">全部</el-radio>
            <el-radio label="discount">
              降价
              <el-badge :value="priceDropCount" :hidden="priceDropCount === 0" class="price-drop-badge-inline" />
            </el-radio>
          </el-radio-group>

          <el-button
              @click="toggleManageMode"
              class="manage-btn"
              :type="isManageMode ? 'primary' : 'default'"
              text
          >
            {{ isManageMode ? '完成' : '管理' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 购物车商品列表 -->
    <div class="cart-list">
      <!-- 空购物车提示 -->
      <div class="empty-cart" v-if="filteredGoods.length === 0">
        <p class="empty-text">购物车空空如也~</p>
        <el-button type="primary" @click="goToHome">去逛逛</el-button>
      </div>

      <!-- 商品列表 -->
      <div class="goods-list" v-else>
        <!-- 列表头部（全选） -->
        <div class="list-header">
          <el-checkbox
              v-model="isAllChecked"
              @change="handleAllCheck"
              class="all-check"
          >
            全选
          </el-checkbox>
          <span class="header-title" v-show="!isMobile">商品信息</span>
          <span class="header-price" v-show="!isMobile">单价</span>
          <span class="header-count" v-show="!isMobile">数量</span>
          <span class="header-total" v-show="!isMobile">小计</span>
          <span class="header-op" v-show="!isMobile">操作</span>
        </div>

        <!-- 商品项 -->
        <div
            class="goods-item"
            v-for="(goods, index) in filteredGoods"
            :key="goods.id"
        >
          <!-- 选择框 -->
          <el-checkbox
              v-model="goods.checked"
              @change="(val) => handleCheckChange(goods.id, val)"
              class="goods-check"
          ></el-checkbox>

          <!-- 商品信息 -->
          <div class="goods-info">
            <img :src="goods.imgUrl" alt="商品图片" class="goods-img">
            <div class="goods-desc">
              <div class="goods-header">
                <div class="goods-name">{{ goods.name }}</div>
                <div class="goods-count">×{{ goods.count }}</div>
              </div>
              <div class="goods-spec">{{ goods.spec || '默认规格' }}</div>
              <div class="goods-tags">
                <span class="tag" v-if="goods.discountPrice">满{{ Math.floor(goods.price / 100) * 100 }}减{{ (goods.price - goods.discountPrice).toFixed(0) }}</span>
                <span class="tag primary">先用后付</span>
              </div>
              <div class="goods-price-row">
                <span class="price-label">券后</span>
                <span class="price-current">¥{{ goods.discountPrice || goods.price }}</span>
                <span class="price-original" v-if="goods.discountPrice">¥{{ goods.price }}</span>
                <span class="price-discount" v-if="goods.discountPrice">比加购降{{ (goods.price - goods.discountPrice).toFixed(0) }}</span>
              </div>
              <div class="goods-footer">
                <span class="total-discount">优惠合计{{ goods.discountPrice ? (goods.price - goods.discountPrice).toFixed(0) : 0 }}</span>
                <span class="detail-btn" @click.stop="showDetail">明细 &gt;</span>
              </div>
            </div>
          </div>

          <!-- 单价 -->
          <div class="goods-price">
            <span class="original-price" v-if="goods.discountPrice">¥{{ goods.price }}</span>
            <span class="current-price">¥{{ goods.discountPrice || goods.price }}</span>
          </div>

          <!-- 数量控制 -->
          <div class="goods-count">
            <el-input-number
                v-model="goods.count"
                :min="1"
                @change="(newCount) => handleCountChange(goods.id, newCount)"
                class="count-input"
            ></el-input-number>
          </div>

          <!-- 小计 -->
          <div class="goods-subtotal">
            ¥{{ (goods.count * (goods.discountPrice || goods.price)).toFixed(2) }}
          </div>

          <!-- 操作区（仅PC端显示，移动端使用底部管理栏） -->
          <div class="goods-operations" v-show="!isMobile">
            <el-button
                @click="addToCollect(goods.id)"
                text
                type="text"
                class="collect-btn"
            >
              移入收藏
            </el-button>
            <el-button
                @click="deleteGoods(goods.id)"
                text
                type="text"
                class="delete-btn"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 结算栏（双端适配） -->
    <div class="cart-footer">
      <!-- PC端结算栏 -->
      <div class="pc-footer" v-show="!isMobile">
        <div class="footer-left">
          <el-checkbox
              v-model="isAllChecked"
              @change="handleAllCheck"
              class="footer-all-check"
          >
            全选
          </el-checkbox>
          <span class="selected-count">已选 {{ checkedCount }} 件商品</span>
        </div>

        <!-- 新增：右侧整体容器 -->
        <div class="footer-right-group">
          <!-- 中间：价格明细横向排列 -->
          <div class="footer-middle">
            <div class="price-row">
              <p class="price-item">
                商品总价：<span>¥{{ totalPrice.toFixed(2) }}</span>
              </p>
              <p class="price-item">
                优惠金额：<span>-¥{{ discountAmount.toFixed(2) }}</span>
              </p>
              <p class="price-item final-price">
                实付金额：<span>¥{{ finalPrice.toFixed(2) }}</span>
              </p>
            </div>
          </div>

          <div class="footer-right">
            <el-button
                type="primary"
                class="checkout-btn"
                :disabled="checkedCount === 0"
                @click="checkout"
            >
              结算
            </el-button>
          </div>
        </div>
      </div>

      <!-- 移动端结算栏 -->
      <div class="mobile-footer" v-show="isMobile">
        <!-- 管理模式 -->
        <div class="footer-left" v-if="isManageMode">
          <el-checkbox
              v-model="isAllChecked"
              @change="handleAllCheck"
              class="footer-all-check"
          >
            全选
          </el-checkbox>
          <div class="manage-actions">
            <el-button
                type="primary"
                size="small"
                plain
                @click="batchCollect"
            >
              移入收藏
            </el-button>
            <el-button
                type="danger"
                size="small"
                plain
                @click="batchDelete"
            >
              删除
            </el-button>
          </div>
        </div>

        <!-- 普通模式 -->
        <template v-else>
          <div class="footer-left">
            <el-checkbox
                v-model="isAllChecked"
                @change="handleAllCheck"
                class="footer-all-check"
            >
              全选
            </el-checkbox>
            <div class="price-text">
              <span>合计：</span>
              <span class="final-price">¥{{ finalPrice.toFixed(2) }}</span>
            </div>
          </div>

          <div class="footer-right">
            <el-button
                type="primary"
                class="checkout-btn"
                :disabled="checkedCount === 0"
                @click="checkout"
            >
              {{ checkedCount > 0 ? `结算(${checkedCount})` : '结算' }}
            </el-button>
          </div>

          <!-- 移动端结算明细（展开/收起） -->
          <div class="price-detail-mobile" v-show="showPriceDetail">
            <p>商品总价：<span>¥{{ totalPrice.toFixed(2) }}</span></p>
            <p>优惠金额：<span>-¥{{ discountAmount.toFixed(2) }}</span></p>
            <p class="detail-tip" @click="showPriceDetail = false">收起明细 ∧</p>
          </div>
          <div class="show-detail-btn" v-show="!showPriceDetail" @click="showPriceDetail = true">
            查看明细 ∨
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCartList, deleteCart, checkCart, allCheckCart, addToCart } from '@/api/cart'
import { collect } from '@/api/collect'  // 导入收藏 API
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Search } from '@element-plus/icons-vue';

const router = useRouter();

// 响应式判断是否为移动端
const isMobile = ref(false);
const checkDevice = () => {
  isMobile.value = window.innerWidth < 768;
};

// 初始化设备判断
onMounted(async () => {
  checkDevice();
  window.addEventListener('resize', checkDevice);
  await loadCartData();
});

// 监听窗口变化
watch(isMobile, () => {
  // 切换设备时重置管理模式（移动端）
  if (!isMobile.value) {
    isManageMode.value = false;
  }
});

// 购物车商品数据
const cartGoods = ref<any[]>([]);
const loading = ref(false);

// 价格提醒相关
const priceDropCount = ref(0); // 降价商品数量
const hasPriceDrops = computed(() => priceDropCount.value > 0);

// 加载购物车数据
const loadCartData = async () => {
  loading.value = true;
  try {
    const res = await getCartList();
    // 转换后端数据格式为前端需要的格式
    cartGoods.value = res.data.map((item: any) => ({
      id: item.skuId,
      name: item.skuName,
      spec: '',
      price: parseFloat(item.cartPrice) || 0,  // BigDecimal 转数字
      originalCartPrice: parseFloat(item.cartPrice) || 0, // 记录加入购物车时的价格
      discountPrice: 0,
      count: item.skuNum || 1,
      checked: item.isChecked === 1,
      status: 'normal',
      imgUrl: item.imgUrl,
      createTime: item.createTime,
      // 价格提醒相关
      hasPriceDrop: false,
      priceDropAmount: 0
    }));
    
    // 检查价格变化
    checkPriceDrops();
    calcTotalPrice();
  } catch (error: any) {
    console.error('加载购物车数据失败:', error.message);
    ElMessage.error('加载购物车数据失败');
    cartGoods.value = [];
  } finally {
    loading.value = false;
  }
};

// 检查降价商品
const checkPriceDrops = () => {
  priceDropCount.value = 0;
  
  cartGoods.value.forEach(goods => {
    // 如果当前价格低于加入购物车时的价格，说明降价了
    if (goods.discountPrice > 0 && goods.discountPrice < goods.originalCartPrice) {
      goods.hasPriceDrop = true;
      goods.priceDropAmount = goods.originalCartPrice - goods.discountPrice;
      priceDropCount.value++;
    } else {
      goods.hasPriceDrop = false;
      goods.priceDropAmount = 0;
    }
  });
  
  // 如果有降价商品，发送提醒通知
  if (priceDropCount.value > 0) {
    showPriceDropNotification();
  }
};

// 显示降价提醒通知
const showPriceDropNotification = () => {
  ElMessage({
    message: `🎉 购物车中有 ${priceDropCount.value} 件商品降价啦！快去看看吧~`,
    type: 'success',
    duration: 5000,
    showClose: true
  });
};

// 筛选条件
const typeFilter = ref('all'); // all/discount
const categoryFilter = ref('all'); // all/digital/home/food
const statusFilter = ref('all'); // all/invalid/stockout
const searchKeyword = ref('');
const isManageMode = ref(false); // 移动端管理模式
const showPriceDetail = ref(false); // 移动端结算明细展开状态

// 筛选后的商品列表
const filteredGoods = computed(() => {
  return cartGoods.value.filter(goods => {
    // 类型筛选（选择降价时显示所有降价商品）
    if (typeFilter.value === 'discount' && !goods.hasPriceDrop) return false;
    // 状态筛选
    if (statusFilter.value !== 'all' && goods.status !== statusFilter.value) return false;
    // 搜索筛选 - 匹配多个字段
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      // 定义可搜索的字段
      const searchableFields = [
        goods.name,              // 商品名称
        goods.spec,              // 商品规格
        getStatusText(goods.status), // 商品状态文本
        goods.price?.toString(), // 原价
        goods.discountPrice?.toString(), // 折扣价
        goods.count?.toString()  // 数量
      ].filter(Boolean) // 过滤掉空值
      
      // 只要有一个字段匹配就返回 true
      const hasMatch = searchableFields.some(field => 
        field.toLowerCase().includes(keyword)
      )
      if (!hasMatch) return false
    }
    return true;
  });
});

// 全选状态
const isAllChecked = computed({
  get() {
    return filteredGoods.value.length > 0 && filteredGoods.value.every(goods => goods.checked);
  },
  set(val) {
    filteredGoods.value.forEach(goods => {
      goods.checked = val;
    });
    calcTotalPrice();
  }
});

// 已选商品种类数（用于结算按钮显示）
const checkedCount = computed(() => {
  return filteredGoods.value.filter(goods => goods.checked).length;
});

// 已选商品总数量（用于其他计算）
const checkedTotalNum = computed(() => {
  return filteredGoods.value.filter(goods => goods.checked).reduce((sum, goods) => sum + goods.count, 0);
});

// 计算价格相关
const totalPrice = ref(0); // 商品总价
const discountAmount = ref(0); // 优惠金额
const finalPrice = ref(0); // 实付金额

const calcTotalPrice = () => {
  let total = 0;
  let discount = 0;

  filteredGoods.value.forEach(goods => {
    if (goods.checked) {
      const original = goods.price * goods.count;
      const current = (goods.discountPrice || goods.price) * goods.count;
      total += original;
      discount += original - current;
    }
  });

  totalPrice.value = total;
  discountAmount.value = discount;
  finalPrice.value = total - discount;
};

// 全选事件
const handleAllCheck = async (val: boolean) => {
  try {
    await allCheckCart(val ? 1 : 0);
    // 更新本地状态
    filteredGoods.value.forEach(goods => {
      goods.checked = val;
    });
    calcTotalPrice();
  } catch (error: any) {
    console.error('全选失败:', error.message);
    ElMessage.error('操作失败');
    // 恢复全选状态
    isAllChecked.value = !val;
  }
};

// 搜索商品
const searchGoods = () => {
  // 触发筛选，无额外逻辑
};

// 处理单个商品选中状态变化
const handleCheckChange = async (skuId: number, checked: boolean) => {
  try {
    await checkCart(skuId, checked ? 1 : 0);
    calcTotalPrice();
  } catch (error: any) {
    console.error('更新选中状态失败:', error.message);
    ElMessage.error('操作失败');
    // 恢复状态
    const goods = cartGoods.value.find(g => g.id === skuId);
    if (goods) {
      goods.checked = !checked;
    }
  }
};

// 处理商品数量变化
const handleCountChange = async (skuId: number, newCount: number) => {
  try {
    // 获取商品当前的skuNum
    const goods = cartGoods.value.find(g => g.id === skuId);
    if (!goods) return;
    
    // 如果数量减少，需要计算差值
    if (newCount < goods.count) {
      // 后端不支持直接减少数量，需要删除后重新添加
      await deleteCart(skuId);
      await addToCart(skuId, newCount);
      // 恢复选中状态
      await checkCart(skuId, goods.checked ? 1 : 0);
    } else {
      // 数量增加，直接添加差值
      const diff = newCount - goods.count;
      await addToCart(skuId, diff);
    }
    
    // 更新本地数据
    goods.count = newCount;
    calcTotalPrice();
  } catch (error: any) {
    console.error('更新数量失败:', error.message);
    ElMessage.error('更新数量失败');
    // 恢复原数量
    const goods = cartGoods.value.find(g => g.id === skuId);
    if (goods) {
      // 重新加载购物车数据
      await loadCartData();
    }
  }
};

// 移入收藏
const addToCollect = async (skuId: number) => {
  try {
    await collect(skuId)
    ElMessage.success('已移入收藏')
  } catch (error: any) {
    console.error('移入收藏失败:', error.message)
    ElMessage.error('移入收藏失败，请重试')
  }
};

// 批量移入收藏（管理模式）
const batchCollect = async () => {
  const checkedGoods = filteredGoods.value.filter(goods => goods.checked);
  if (checkedGoods.length === 0) {
    ElMessage.warning('请先勾选要收藏的商品');
    return;
  }
  ElMessageBox.confirm(
      `确定将选中的 ${checkedGoods.length} 件商品移入收藏吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      for (const goods of checkedGoods) {
        await collect(goods.id);
      }
      ElMessage.success('已批量移入收藏');
      isManageMode.value = false;
    } catch (error: any) {
      console.error('批量收藏失败:', error.message);
      ElMessage.error('批量收藏失败，请重试');
    }
  });
};

// 批量删除（管理模式）
const batchDelete = async () => {
  const checkedGoods = filteredGoods.value.filter(goods => goods.checked);
  if (checkedGoods.length === 0) {
    ElMessage.warning('请先勾选要删除的商品');
    return;
  }
  ElMessageBox.confirm(
      `确定删除选中的 ${checkedGoods.length} 件商品吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      for (const goods of checkedGoods) {
        await deleteCart(goods.id);
      }
      // 更新本地列表
      cartGoods.value = cartGoods.value.filter(goods => !checkedGoods.includes(goods));
      calcTotalPrice();
      ElMessage.success('删除成功');
      isManageMode.value = false;
    } catch (error: any) {
      console.error('批量删除失败:', error.message);
      ElMessage.error('批量删除失败，请重试');
      // 失败后重新加载数据以确保一致性
      await loadCartData();
    }
  });
};

// 删除商品
const deleteGoods = async (id: number) => {
  ElMessageBox.confirm(
      '确定要删除该商品吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      await deleteCart(id);
      cartGoods.value = cartGoods.value.filter(goods => goods.id !== id);
      calcTotalPrice();
      ElMessage.success('删除成功');
    } catch (error: any) {
      console.error('删除失败:', error.message);
      ElMessage.error('删除失败，请重试');
    }
  });
};

// 切换移动端管理模式
const toggleManageMode = () => {
  isManageMode.value = !isManageMode.value;
};

// 显示明细（点击效果，暂无跳转）
const showDetail = () => {
  // 后续可在此处展开详情弹窗
};

// 获取商品状态文本
const getStatusText = (status: string) => {
  switch(status) {
    case 'invalid': return '商品已失效';
    case 'stockout': return '即将售罄';
    default: return '';
  }
};

// 结算 → 跳转到订单确认页（核心修改）
const checkout = () => {
  if (checkedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  const checkedGoods = filteredGoods.value.filter(item => item.checked)

  // 组装商品数据，通过路由参数传递
  const orderGoods = checkedGoods.map(item => ({
    name: item.name,
    image: item.imgUrl,
    specs: { spec: item.spec },
    price: item.discountPrice || item.price,
    num: item.count
  }))

  // 跳转到订单确认页（从后端获取选中的商品）
  router.push({
    path: '/order/confirm'
  })
}

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 去首页
const goToHome = () => {
  router.push({ name: 'home' });
};
</script>

<style scoped>
@import "@/assets/css/cart.css";
</style>