<template>
  <div class="recipe-audit">
    <!-- 搜索筛选栏 -->
    <div class="search-bar" style="margin-bottom: 20px; padding: 10px; background: #f8f9fa; border-radius: 8px;">
      <el-row :gutter="20" type="flex" justify="start" align="center">
        <el-col :span="4">
          <el-input
              v-model="searchForm.id"
              placeholder="食谱ID"
              type="number"
              prefix-icon="Key"
              @input="validateId"
              clearable
          ></el-input>
        </el-col>
        <el-col :span="6">
          <el-input
              v-model="searchForm.title"
              placeholder="食谱名称"
              prefix-icon="Document"
              @keyup.enter="loadRecipeList"
              clearable
          ></el-input>
        </el-col>
        <el-col :span="5">
          <el-input
              v-model="searchForm.username"
              placeholder="创建人"
              prefix-icon="User"
              @keyup.enter="loadRecipeList"
              clearable
          ></el-input>
        </el-col>
        <el-col :span="4">
          <el-select
              v-model="searchForm.isPrivate"
              placeholder="隐私状态"
              clearable
              @change="loadRecipeList"
              style="width: 100%;"
          >
            <el-option label="公开" value="false"></el-option>
            <el-option label="私有" value="true"></el-option>
          </el-select>
        </el-col>
        <el-col :span="5" style="text-align: right;">
          <el-button
              type="primary"
              icon="Search"
              @click="loadRecipeList"
              style="margin-right: 10px;"
          >
            搜索
          </el-button>
          <el-button
              icon="Refresh"
              @click="resetSearch"
          >
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 食谱列表（新增标签列） -->
    <el-table
        :data="recipeList"
        border
        stripe
        style="width: 100%; border-radius: 8px;"
        v-loading="loading"
        empty-text="暂无符合条件的食谱数据，请调整搜索条件"
        @row-click="handleRowClick"
        fit="false"
    >
      <el-table-column prop="id" label="食谱ID" width="80" align="center"></el-table-column>
      <el-table-column prop="title" label="食谱名称" min-width="200">
        <template #default="scope">
          <span style="cursor: pointer; color: #409eff;" @click="viewDetail(scope.row.id)">
            {{ scope.row.title }}
            <el-tag v-if="scope.row.isPrivate" size="small" type="info" style="margin-left: 8px;">
              私有
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120"></el-table-column>
      <!-- 新增：标签列 -->
      <el-table-column label="标签" min-width="150">
        <template #default="scope">
          <el-tag
              v-for="(tag, index) in (scope.row.tagList || [])"
              :key="index"
              size="small"
              type="primary"
              style="margin: 0 2px;"
          >
            {{ tag }}
          </el-tag>
          <span v-if="!(scope.row.tagList && scope.row.tagList.length)" style="color: #909399;">
            无
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="创建人" width="120">
        <template #default="scope">
          {{ scope.row.user?.username || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="isPrivate" label="隐私状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.isPrivate ? 'info' : 'success'">
            {{ scope.row.isPrivate ? '私有' : '公开' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="200">
        <template #default="scope">
          {{ formatTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <!-- 核心优化：缩小操作列宽度+紧凑布局 -->
      <el-table-column label="操作" min-width="220" align="center" fixed="right">
        <template #default="scope">
          <div style="display: flex; gap: 4px; align-items: center; flex-wrap: nowrap;">
            <el-button
                type="primary"
                size="small"
                icon="View"
                @click="viewDetail(scope.row.id)"
            >
              查看
            </el-button>
            <el-button
                type="warning"
                size="small"
                icon="Edit"
                @click="updateRecipe(scope.row.id)"
            >
              更新
            </el-button>
            <el-button
                type="danger"
                size="small"
                icon="Delete"
                @click="deleteRecipe(scope.row.id)"
                :loading="deleteLoading"
                style="display: inline-block !important;"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件 -->
    <div class="pagination mt-4" style="text-align: right; padding: 10px 0;">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="page + 1"
          :page-sizes="[10, 20, 50]"
          :page-size="size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          :disabled="total === 0"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
// 替换为通用食谱API，修复400错误
import { getRecipeList, deleteRecipe as deleteRecipeApi } from '@/api/recipe'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 全局获取ElMessage/ElMessageBox，避免undefined
const instance = getCurrentInstance()
const ElMessage = instance?.proxy?.$message
const ElMessageBox = instance?.proxy?.$msgbox

// 响应式数据
const recipeList = ref([])
const page = ref(0)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const deleteLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  id: '',
  title: '',
  username: '',
  isPrivate: ''
})

/**
 * 时间格式化（兼容空值/非标准格式）
 */
const formatTime = (time) => {
  if (!time || time === 'null' || time === 'undefined') return '-'
  try {
    const d = new Date(time)
    if (isNaN(d.getTime())) return '-'
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hour = String(d.getHours()).padStart(2, '0')
    const minute = String(d.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
  } catch (e) {
    return '-'
  }
}

/**
 * 校验管理员权限（前置拦截）
 */
const checkAdminPermission = () => {
  if (!userStore.token || !userStore.userInfo?.isAdmin) {
    ElMessage && ElMessage.error('非管理员无权访问食谱管理页面！')
    router.push('/home/recipe-list')
    return false
  }
  return true
}

/**
 * 校验食谱ID
 */
const validateId = () => {
  if (searchForm.id !== '' && (isNaN(searchForm.id) || searchForm.id <= 0)) {
    ElMessage && ElMessage.warning('食谱ID必须是正整数！')
    searchForm.id = ''
  }
}

/**
 * 加载所有食谱列表（核心修复：管理员添加userId参数，让后端识别权限）
 */
const loadRecipeList = async () => {
  if (!checkAdminPermission()) return

  if (searchForm.id !== '' && searchForm.id <= 0) {
    ElMessage && ElMessage.warning('食谱ID必须是正整数，请重新输入！')
    return
  }

  try {
    loading.value = true
    // 核心修复：管理员主动传递userId参数（自己的ID），让后端识别管理员权限
    const params = {
      page: Number(page.value),
      size: Number(size.value),
      id: searchForm.id ? Number(searchForm.id) : undefined,
      title: searchForm.title.trim() || undefined,
      username: searchForm.username.trim() || undefined,
      isPrivate: searchForm.isPrivate || undefined,
      // 新增：管理员传递自己的ID，后端判断权限后返回所有食谱（含私有）
      userId: Number(userStore.userInfo.id)
    }
    // 调用通用食谱接口
    const res = await getRecipeList(params)
    // 兼容多种后端返回格式
    const responseData = res.data || res
    recipeList.value = responseData.content || responseData.list || responseData || []
    total.value = responseData.totalElements || responseData.total || 0
  } catch (error) {
    console.error('加载食谱列表失败', error)
    let errMsg = '加载食谱列表失败'
    if (error.response) {
      errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
      if (error.response.status === 400) {
        errMsg = `参数错误：${error.response.data?.message || '管理员ID格式错误，请检查登录状态'}`
      } else if (error.response.status === 403) {
        errMsg = '权限不足：当前管理员无查看所有食谱的权限'
      } else if (error.response.status === 404) {
        errMsg = '接口不存在：请检查recipe.js中getRecipeList的url配置'
      }
    } else if (error.message.includes('Failed to fetch')) {
      errMsg = '网络错误：无法连接到服务器'
    }
    ElMessage && ElMessage.error(errMsg)
  } finally {
    loading.value = false
  }
}

/**
 * 重置搜索条件
 */
const resetSearch = () => {
  searchForm.id = ''
  searchForm.title = ''
  searchForm.username = ''
  searchForm.isPrivate = ''
  page.value = 0
  loadRecipeList()
}

/**
 * 分页-每页条数变化
 */
const handleSizeChange = (val) => {
  size.value = val
  page.value = 0
  loadRecipeList()
}

/**
 * 分页-当前页变化
 */
const handleCurrentChange = (val) => {
  page.value = val - 1
  loadRecipeList()
}

/**
 * 行点击事件（查看详情）
 */
const handleRowClick = (row) => {
  viewDetail(row.id)
}

/**
 * 查看食谱详情
 */
const viewDetail = (id) => {
  if (!id) {
    ElMessage && ElMessage.warning('食谱ID异常！')
    return
  }
  router.push({ path: `/home/recipe-detail/${id}` })
}

/**
 * 更新食谱（跳转编辑页）
 */
const updateRecipe = (id) => {
  if (!id) {
    ElMessage && ElMessage.warning('食谱ID异常！')
    return
  }
  router.push({ path: `/home/recipe-edit/${id}` })
}

/**
 * 删除食谱（修复参数传递：管理员删除需传递currentUserId）
 */
const deleteRecipe = async (id) => {
  if (!id || !checkAdminPermission()) return

  try {
    await ElMessageBox.confirm(
        '确定要删除该食谱吗？删除后将无法恢复，且会删除该食谱的所有关联数据！',
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'danger',
          draggable: true
        }
    )
    deleteLoading.value = true
    // 修复：管理员删除需要传递currentUserId参数，让后端验证权限
    await deleteRecipeApi(id, Number(userStore.userInfo.id))
    ElMessage && ElMessage.success('食谱删除成功！')
    loadRecipeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除食谱失败', error)
      let errMsg = '删除食谱失败'
      if (error.response) {
        errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
        if (error.response.status === 400) {
          errMsg = `参数错误：${error.response.data?.message || '管理员ID/食谱ID格式错误'}`
        } else if (error.response.status === 403) {
          errMsg = '权限不足：无法删除该食谱'
        } else if (error.response.status === 409) {
          errMsg = '删除失败：该食谱存在关联数据，无法直接删除'
        }
      }
      ElMessage && ElMessage.error(errMsg)
    }
  } finally {
    deleteLoading.value = false
  }
}

// 页面挂载/卸载
onMounted(() => {
  loadRecipeList()
})

onUnmounted(() => {
  loading.value = false
  deleteLoading.value = false
  recipeList.value = []
})
</script>

<style scoped lang="scss">
.recipe-audit {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  overflow: auto;

  .search-bar {
    :deep(.el-input) {
      --el-input-border-radius: 4px;
      --el-input-hover-border-color: #409eff;
      width: 100%;
    }

    :deep(.el-select) {
      --el-select-border-radius: 4px;
      --el-select-hover-border-color: #409eff;
    }
  }

  :deep(.el-table) {
    --el-table-header-text-color: #606266;
    --el-table-row-hover-bg-color: #f5f7fa;
    --el-table-border-color: #e6e6e6;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    width: 100%;
  }

  :deep(.el-table .el-tag) {
    --el-tag-border-radius: 4px;
  }

  :deep(.el-pagination) {
    --el-pagination-text-color: #606266;
    --el-pagination-active-color: #409eff;
    --el-pagination-button-bg-color: #fff;
  }

  // 缩小操作按钮内边距，进一步紧凑布局
  :deep(.el-table-column--operation .el-button) {
    display: inline-block !important;
    margin: 0 2px !important;
    padding: 5px 8px !important;
  }

  :deep(.el-table-cell span[style*="cursor: pointer"]) {
    &:hover {
      text-decoration: underline;
    }
  }

  // 新增：标签列样式优化
  :deep(.el-table-column--label .el-tag) {
    margin: 1px;
    white-space: nowrap;
  }
}
</style>
