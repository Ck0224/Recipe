<template>
  <div class="recipe-audit">
    <!-- 搜索筛选栏（优化布局+样式） -->
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

    <!-- 食谱列表（优化样式+空状态） -->
    <el-table
        :data="recipeList"
        border
        stripe
        style="width: 100%; border-radius: 8px; overflow: hidden;"
        v-loading="loading"
        empty-text="暂无符合条件的食谱数据，请调整搜索条件"
        @row-click="handleRowClick"
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
      <el-table-column label="操作" width="300" align="center">
        <template #default="scope">
          <el-button
              type="primary"
              size="small"
              icon="View"
              @click="viewDetail(scope.row.id)"
          >
            查看
          </el-button>
          <!-- 编辑→更新，跳转独立的编辑页面 -->
          <el-button
              type="warning"
              size="small"
              icon="Edit"
              @click="updateRecipe(scope.row.id)"
              style="margin-left: 10px;"
          >
            更新
          </el-button>
          <el-button
              type="danger"
              size="small"
              icon="Delete"
              @click="deleteRecipe(scope.row.id)"
              style="margin-left: 10px;"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件（优化样式+位置） -->
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminRecipeList, deleteRecipe as deleteRecipeApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const recipeList = ref([])
const page = ref(0)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

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
    ElMessage.error('非管理员无权访问食谱管理页面！')
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
    ElMessage.warning('食谱ID必须是正整数！')
    searchForm.id = ''
  }
}

/**
 * 加载所有食谱列表（管理员权限）
 */
const loadRecipeList = async () => {
  // 前置权限校验
  if (!checkAdminPermission()) return

  if (searchForm.id !== '' && searchForm.id <= 0) {
    ElMessage.warning('食谱ID必须是正整数，请重新输入！')
    return
  }

  try {
    loading.value = true
    // 标准化请求参数（数字类型+空值处理）
    const params = {
      page: Number(page.value),
      size: Number(size.value),
      id: searchForm.id ? Number(searchForm.id) : undefined,
      title: searchForm.title.trim() || undefined,
      username: searchForm.username.trim() || undefined,
      isPrivate: searchForm.isPrivate || undefined,
      operatorId: Number(userStore.userInfo.id) // 管理员ID，供后端校验
    }
    const res = await getAdminRecipeList(params)
    // 兼容多种后端返回格式
    const responseData = res.data || res
    recipeList.value = responseData.content || responseData.list || responseData || []
    total.value = responseData.totalElements || responseData.total || 0
  } catch (error) {
    console.error('加载食谱列表失败', error)
    // 精准错误提示
    let errMsg = '加载食谱列表失败'
    if (error.response) {
      errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
      if (error.response.status === 400) {
        errMsg = `参数错误：${error.response.data?.message || '管理员ID/食谱ID格式错误'}`
      } else if (error.response.status === 403) {
        errMsg = '权限不足：当前管理员无查看所有食谱的权限'
      } else if (error.response.status === 404) {
        errMsg = '接口不存在：请检查getAdminRecipeList接口路径'
      }
    } else if (error.message.includes('Failed to fetch')) {
      errMsg = '网络错误：无法连接到服务器'
    }
    ElMessage.error(errMsg)
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
    ElMessage.warning('食谱ID异常！')
    return
  }
  router.push({ path: `/home/recipe-detail/${id}` })
}

/**
 * 更新食谱（跳转独立的编辑页面）
 */
const updateRecipe = (id) => {
  if (!id) {
    ElMessage.warning('食谱ID异常！')
    return
  }
  // 跳转到独立的编辑页面（后续需配置路由）
  router.push({ path: `/home/recipe-edit/${id}` })
}

/**
 * 删除食谱（管理员可删除任意食谱）
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
    // 标准化参数
    await deleteRecipeApi(id, Number(userStore.userInfo.id))
    ElMessage.success('食谱删除成功！')
    loadRecipeList() // 重新加载列表
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
      ElMessage.error(errMsg)
    }
  }
}

// 页面挂载时加载数据
onMounted(() => {
  // 首次加载列表
  loadRecipeList()
})

// 页面卸载清理
onUnmounted(() => {
  loading.value = false
  recipeList.value = []
})
</script>

<style scoped lang="scss">
.recipe-audit {
  padding: 20px;

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
  }

  :deep(.el-table .el-tag) {
    --el-tag-border-radius: 4px;
  }

  :deep(.el-pagination) {
    --el-pagination-text-color: #606266;
    --el-pagination-active-color: #409eff;
    --el-pagination-button-bg-color: #fff;
  }

  // 可点击的食谱名称样式
  :deep(.el-table-cell span[style*="cursor: pointer"]) {
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
