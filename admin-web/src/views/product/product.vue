<template>
  <div class="search-div">
    <el-form label-width="70px" size="small">
      <el-row>
        <el-col :span="12">
          <el-form-item label="品牌">
            <el-select
              class="m-2"
              v-model="queryDto.brandId"
              placeholder="选择品牌"
              size="small"
              style="width: 100%"
            >
              <el-option
                v-for="item in brandList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类">
            <el-cascader
              :props="categoryProps"
              style="width: 100%"
              v-model="searchCategoryIdList"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row style="display:flex">
        <el-button type="primary" size="small" @click="fetchData">
          搜索
        </el-button>
        <el-button size="small" @click="resetData">重置</el-button>
      </el-row>
    </el-form>
  </div>

  <div class="tools-div">
    <el-button type="success" size="small" @click="addShow">添 加</el-button>
  </div>

  <el-table :data="list" style="width: 100%">
    <el-table-column
      prop="sliderUrls"
      label="轮播图"
      #default="scope"
      width="200"
    >
      <div style="height: 50px;float: left;">
        <img
          v-for="(item, index) in scope.row.sliderUrls"
          :key="index"
          :src="item"
          width="50"
        />
      </div>
    </el-table-column>
    <el-table-column prop="name" label="商品名称" width="160" />
    <el-table-column prop="brandName" label="品牌" />
    <el-table-column prop="category1Name" label="一级分类" />
    <el-table-column prop="category2Name" label="二级分类" />
    <el-table-column prop="category3Name" label="三级分类" />
    <el-table-column prop="unitName" label="计量单位" />
    <el-table-column prop="status" label="状态" #default="scope">
      {{
        scope.row.status == 0
          ? '未上架'
          : scope.row.status == 1
            ? '上架'
            : '下架'
      }}
    </el-table-column>
    <el-table-column prop="auditStatus" label="审核状态" #default="scope">
      {{
        scope.row.auditStatus == 0
          ? '未审核'
          : scope.row.auditStatus == 1
            ? '通过'
            : '驳回'
      }}
    </el-table-column>
    <el-table-column label="操作" align="center" width="200" #default="scope" >
      <el-button type="primary" size="small" @click="editShow(scope.row)" style="padding: 5px; margin-left: 5px;">
        修改
      </el-button>
      <el-button type="danger" size="small" @click="remove(scope.row.id)" style="padding: 5px; margin-left: 5px;">
        删除
      </el-button>
      <el-button
        v-if="scope.row.auditStatus == 0 || scope.row.auditStatus == -1"
        type="success"
        size="small"
        @click="audit(scope.row.id)"
        style="padding: 5px; margin-left: 5px;"
      >
        审批
      </el-button>
      <el-button
        v-if="scope.row.auditStatus == 1 && (scope.row.status == 0 || scope.row.status == -1)
        && scope.row.status == 0 "
        type="warning"
        size="small"
        @click="updateStatus(scope.row.id, 1)"
        style="padding: 5px; margin-left: 5px;"
      >
        上架
      </el-button>
      <el-button
        v-if="
          scope.row.auditStatus == 1 &&
            (scope.row.status == 0 || scope.row.status == 1) && scope.row.status == 1
        "
        type="warning"
        plain
        size="small"
        @click="updateStatus(scope.row.id, -1)"
        style="padding: 5px; margin-left: 5px;"
      >
        下架
      </el-button>
    </el-table-column>
  </el-table>

  <el-pagination
    v-model:current-page="pageParams.page"
    v-model:page-size="pageParams.limit"
    :page-sizes="[10, 20, 50, 100]"
    layout="total, sizes, prev, pager, next"
    :total="total"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
  />

  <el-dialog v-model="dialogVisible" title="添加或修改" width="80%">
    <el-form label-width="120px" style="margin-top: 10px;" :label-position="labelPosition">
      <el-collapse v-model="activeNames">
        <el-collapse-item title="商品基本信息" name="productBaseInfo">
          <el-form-item label="商品名称">
            <el-input v-model="product.name"/>
          </el-form-item>
          <el-form-item label="分类">
            <el-cascader
              style="width: 100%;"
              :props="categoryProps"
              v-model="categoryIdList"
              @change="categoryChange"
            />
          </el-form-item>
          <el-form-item label="品牌">
            <el-select class="m-2" placeholder="选择品牌" v-model="product.brandId" style="width: 100%;">
              <el-option
                v-for="item in categoryBrandList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="商品单元">
            <el-select
              v-model="product.unitName"
              style="width: 100%;"
              class="m-2"
              placeholder="商品单元"
            >
              <el-option
                v-for="item in unitList"
                :key="item.id"
                :label="item.name"
                :value="item.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="轮播图">
            <el-upload
              v-model:file-list="fileList"
              action="http://localhost:8501/admin/system/fileUpload"
              list-type="picture-card"
              multiple
              :on-success="handleSliderSuccess"
              :on-remove="handleRemove"
              :headers="headers"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-collapse-item>

        <el-collapse-item title="商品SKU信息" name="skuInfo">
          <el-form-item label="选择规格">
            <el-select
              :disabled="product.id != '' && product.id != undefined "
              v-model="product.specValue"
              style="width: 100%;"
              class="m-2"
              placeholder="选择规格"
              size="default"
              @change="changeSpecValueList"
            >
              <el-option
                v-for="item in specList"
                :key="item.specValue"
                :label="item.specName"
                :value="item.specValue"
              />
            </el-select>
          </el-form-item>

          <div class="specValue" v-for="(item , index) in specValueList" :key="index">
            <span class="tag-group__title">{{item.key}}</span>
            <el-tag class="tag-group__title" type="success" effect="dark" v-for="(specValue , index) in item.valueList" :key="index">
              {{specValue}}
            </el-tag>
          </div>

          <el-form-item label="商品SKU">
            <el-table :data="product.productSkuList" border style="width: 100%">
              <el-table-column prop="skuSpec" label="规格" width="180" />
              <el-table-column label="图片" #default="scope" width="80">
                <el-upload
                  class="avatar-uploader"
                  action="http://localhost:8501/admin/system/fileUpload"
                  :show-file-list="false"
                  :on-success="
                    (response, uploadFile, fileList) => handleSkuSuccess(response, uploadFile, fileList, scope.row)
                  "
                  :headers="headers"
                >
                  <img
                    v-if="scope.row.thumbImg"
                    :src="scope.row.thumbImg"
                    class="avatar"
                  />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-table-column>
              <el-table-column label="售价" #default="scope">
                <el-input v-model="scope.row.salePrice" />
              </el-table-column>
              <el-table-column label="市场价" #default="scope">
                <el-input v-model="scope.row.marketPrice" />
              </el-table-column>
              <el-table-column label="成本价" #default="scope">
                <el-input v-model="scope.row.costPrice" />
              </el-table-column>
              <el-table-column label="库存数" #default="scope">
                <el-input v-model="scope.row.stockNum" />
              </el-table-column>
              <el-table-column label="重量" #default="scope">
                <el-input v-model="scope.row.weight" />
              </el-table-column>
              <el-table-column label="体积" #default="scope">
                <el-input v-model="scope.row.volume" />
              </el-table-column>
            </el-table>
          </el-form-item>
        </el-collapse-item>
        <el-collapse-item title="商品详情信息" name="productDetails">
          <el-form-item label="详情图片">
            <el-upload
              v-model:file-list="detailsFileList"
              action="http://localhost:8501/admin/system/fileUpload"
              list-type="picture-card"
              multiple
              :on-success="handleDetailsSuccess"
              :on-remove="handleDetailsRemove"
              :headers="headers"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-collapse-item>
        <el-collapse-item title="保存修改数据" name="submit" :disabled="true">
          <el-form-item>
            <el-button type="primary" @click="saveOrUpdate">提交</el-button>
            <el-button @click="dialogVisible = false">取消</el-button>
          </el-form-item>
        </el-collapse-item>
      </el-collapse>
    </el-form>
  </el-dialog>

  <el-dialog v-model="dialogAuditVisible" title="审批" width="80%">
    <el-form :model="product" label-width="120px" style="margin-top: 10px;">
      <el-divider />
      <span style="margin-bottom: 5px;">商品基本信息</span>
      <el-row>
        <el-col :span="12">
          <el-form-item label="商品名称">
            {{ product.name }}
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类">
            {{ product.category1Name }} => {{ product.category2Name }} =>
            {{ product.category3Name }}
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="品牌">
            {{ product.brandName }}
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计量单位">
            {{ product.unitName }}
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="创建时间">
            {{ product.createTime }}
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            {{
              product.auditStatus == 0
                ? '未审核'
                : product.auditStatus == 1
                  ? '通过'
                  : '驳回'
            }}
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="轮播图">
            <img
              v-for="(item, index) in sliderUrlList"
              :key="index"
              :src="item"
              style="width: 50px;"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider />
      <span style="margin-bottom: 5px;">商品SKU信息</span>
      <el-form-item label="商品SKU">
        <el-table :data="product.productSkuList" border style="width: 100%">
          <el-table-column prop="skuSpec" label="规格" width="180" />
          <el-table-column label="图片" #default="scope" width="80">
            <img :src="scope.row.thumbImg" style="width: 80px;" />
          </el-table-column>
          <el-table-column label="售价" prop="salePrice" />
          <el-table-column label="市场价" prop="marketPrice" />
          <el-table-column label="成本价" prop="costPrice" />
          <el-table-column label="库存数" prop="stockNum" />
          <el-table-column label="重量" prop="weight" />
          <el-table-column label="体积" prop="volume" />
        </el-table>
      </el-form-item>

      <el-divider />
      <span style="margin-bottom: 5px;">商品详情信息</span>
      <el-form-item label="详情图片">
        <img
          v-for="(item, index) in detailsImageUrlList"
          :key="index"
          :src="item"
          style="width: 80px;"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="success"
          size="large"
          @click="updateAuditStatus(product.id, 1)"
        >
          通过
        </el-button>
        <el-button
          type="success"
          size="large"
          plain
          @click="updateAuditStatus(product.id, -1)"
        >
          驳回
        </el-button>
        <el-button size="large" @click="dialogAuditVisible = false">
          取消
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
import { useProduct } from '@/hooks/product/useProduct'
import '@/assets/css/product.css'

// 引入并解构useProduct中的所有变量和方法
const {
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
} = useProduct()
</script>