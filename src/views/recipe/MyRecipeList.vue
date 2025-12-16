<template>
  <div class="my-recipe-list">
    <el-card>
      <!-- 顶部操作栏 -->
      <div class="header-actions" style="margin-bottom: 20px;">
        <h3 style="margin: 0; display: inline-block;">我的食谱</h3>
        <el-button
            type="primary"
            icon="Plus"
            @click="goToCreate"
            style="float: right;"
        >
          创建新食谱
        </el-button>
      </div>

      <!-- 搜索筛选栏 -->
      <div class="search-bar">
        <el-row :gutter="20" type="flex" justify="center">
          <!-- 食谱名称搜索 -->
          <el-col :span="6">
            <el-input
                v-model="searchForm.title"
                placeholder="请输入食谱名称"
                prefix-icon="Document"
                @keyup.enter="loadMyRecipeList"
            ></el-input>
          </el-col>
          <!-- 分类搜索 -->
          <el-col :span="6">
            <el-input
                v-model="searchForm.category"
                placeholder="请输入分类"
                prefix-icon="Menu"
                @keyup.enter="loadMyRecipeList"
            ></el-input>
          </el-col>
          <!-- 隐私状态筛选 -->
          <el-col :span="6">
            <el-select
                v-model="searchForm.isPrivate"
                placeholder="隐私状态"
                clearable
                @change="loadMyRecipeList"
            >
              <el-option label="公开食谱" value="false"></el-option>
              <el-option label="私有食谱" value="true"></el-option>
            </el-select>
          </el-col>
          <!-- 操作按钮 -->
          <el-col :span="6" class="btn-group">
            <el-button
                type="primary"
                icon="Search"
                @click="loadMyRecipeList"
                class="search-btn"
            >
              搜索
            </el-button>
            <el-button
                icon="Refresh"
                @click="resetSearch"
                class="reset-btn"
            >
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 我的食谱列表 -->
      <div class="list-content">
        <el-table
            :data="myRecipeList"
            border
            stripe
            style="width: 100%"
            v-loading="loading"
            empty-text="暂无已创建的食谱数据"
        >
          <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
          <el-table-column prop="title" label="食谱名称" min-width="200">
            <template #default="scope">
              <span>
                {{ scope.row.title }}
                <el-tag v-if="scope.row.isPrivate" size="small" type="info" style="margin-left: 8px;">
                  私有
                </el-tag>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="120"></el-table-column>
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="scope">
              <el-tag
                  :type="scope.row.difficulty === 'EASY' ? 'success' : scope.row.difficulty === 'MEDIUM' ? 'warning' : 'danger'"
              >
                {{ scope.row.difficulty === 'EASY' ? '简单' : scope.row.difficulty === 'MEDIUM' ? '中等' : '困难' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="cookTime" label="烹饪时间(分钟)" width="120" align="center"></el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="200"></el-table-column>
          <el-table-column label="操作" width="280" align="center">
            <template #default="scope">
              <el-button
                  type="primary"
                  size="small"
                  icon="View"
                  @click="viewDetail(scope.row.id)"
              >
                查看
              </el-button>
              <!-- 编辑→更新，跳转独立的编辑页面路由 -->
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
                  :loading="deleteLoading"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页控件 -->
      <div class="pagination mt-4">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page + 1"
            :page-sizes="[10, 20, 50]"
            :page-size="size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            background
        >
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyRecipeList, deleteRecipe as deleteRecipeApi } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

// 初始化路由和用户状态
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const myRecipeList = ref([])
const page = ref(0)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const deleteLoading = ref(false) // 新增：删除按钮加载状态

// 搜索表单
const searchForm = reactive({
  title: '',
  category: '',
  isPrivate: ''
})

/**
 * 加载我的食谱列表（保留原有逻辑）
 */
const loadMyRecipeList = async () => {
  // 前置校验：确保用户ID存在
  if (!userStore.userInfo?.id) {
    ElMessage.error('用户信息异常，请重新登录！')
    router.push('/login')
    return
  }

  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      userId: userStore.userInfo.id,
      title: searchForm.title.trim() || undefined,
      category: searchForm.category.trim() || undefined,
      isPrivate: searchForm.isPrivate || undefined
    }
    const res = await getMyRecipeList(params)
    // 兼容不同的后端返回格式
    myRecipeList.value = res.content || res.data?.list || res.list || []
    total.value = res.totalElements || res.data?.total || res.total || 0
  } catch (error) {
    console.error('加载我的食谱列表失败', error)
    // 精准提示错误原因
    const errMsg = error.response?.data?.message || error.message || '网络异常/接口不存在'
    ElMessage.error(`加载我的食谱失败：${errMsg}`)
    // 若接口404，检查接口是否定义
    if (error.code === 'ERR_BAD_REQUEST' || error.code === 'ERR_NOT_FOUND') {
      ElMessage.warning('请确认getMyRecipeList接口已在api/recipe.js中定义！')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 重置搜索条件
 */
const resetSearch = () => {
  searchForm.title = ''
  searchForm.category = ''
  searchForm.isPrivate = ''
  page.value = 0
  loadMyRecipeList()
}

/**
 * 分页-每页条数变化
 */
const handleSizeChange = (val) => {
  size.value = val
  page.value = 0
  loadMyRecipeList()
}

/**
 * 分页-当前页变化
 */
const handleCurrentChange = (val) => {
  page.value = val - 1
  loadMyRecipeList()
}

/**
 * 查看食谱详情
 */
const viewDetail = (id) => {
  if (!id) {
    ElMessage.warning('食谱ID异常！')
    return
  }
  // 强制使用完整路由路径，避免嵌套路由问题
  router.push({ path: `/home/recipe-detail/${id}` })
}

/**
 * 更新食谱
 */
const updateRecipe = (id) => {
  if (!id) {
    ElMessage.warning('食谱ID异常！')
    return
  }
  // 跳转到独立的食谱编辑页面
  router.push({ path: `/home/recipe-edit/${id}` })
}

/**
 * 删除食谱（仅修复错误，保留原有功能）
 */
const deleteRecipe = async (recipeId) => {
  // 防重复点击
  if (deleteLoading.value) return

  try {
    deleteLoading.value = true
    // 1. 确保userId是有效数字
    const userId = Number(userStore.userInfo.id) || 0;
    if (isNaN(userId) || userId <= 0) {
      ElMessage.error('用户ID异常，无法删除！');
      return;
    }
    // 2. 校验食谱ID
    if (isNaN(Number(recipeId)) || Number(recipeId) <= 0) {
      ElMessage.warning('食谱ID异常！');
      return;
    }
    // 3. 二次确认删除
    await ElMessageBox.confirm(
        '确定要删除该食谱吗？删除后无法恢复！',
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    // 4. 调用删除接口
    await deleteRecipeApi(recipeId, userId);
    ElMessage.success('删除食谱成功！');
    // 5. 重新加载列表（修复函数名：loadMyRecipeList）
    loadMyRecipeList();
  } catch (error) {
    // 过滤取消确认的错误
    if (error.message.includes('cancel')) {
      deleteLoading.value = false
      return
    }
    console.error('删除食谱失败:', error);
    // 精准提示错误原因
    let errMsg = '删除食谱失败';
    if (error.response?.status === 400) {
      errMsg = '参数错误：食谱ID/用户ID无效';
    } else if (error.response?.status === 403) {
      errMsg = '无权限删除该食谱';
    } else if (error.response?.status === 404) {
      errMsg = '食谱不存在';
    }
    ElMessage.error(errMsg);
  } finally {
    deleteLoading.value = false
  }
};

/**
 * 跳转到创建食谱页面
 */
const goToCreate = () => {
  router.push({ path: '/home/recipe-create' })
}

// 页面挂载时加载数据
onMounted(() => {
  // 1. 校验token
  if (!userStore.token) {
    ElMessage.warning('请先登录后再操作')
    router.push({ path: '/login' })
    return
  }
  // 2. 校验用户ID
  if (!userStore.userInfo?.id) {
    ElMessage.error('用户信息不完整，请重新登录')
    userStore.logout()
    router.push({ path: '/login' })
    return
  }
  // 3. 加载数据
  loadMyRecipeList()
})
</script>

<style scoped lang="scss">
.my-recipe-list {
  padding: 20px;

  .search-bar {
    padding: 10px 0;
    border-bottom: 1px solid #e6e6e6;

    :deep(.el-input__prefix) {
      color: #909399;
    }

    :deep(.el-select) {
      width: 100%;
    }

    .btn-group {
      display: flex;
      gap: 10px;
      justify-content: center;
      align-items: flex-end;
      margin-top: 24px;

      .search-btn, .reset-btn {
        flex: 1;
        width: 100%;
        white-space: nowrap;
      }
    }
  }

  .list-content {
    :deep(.el-table) {
      --el-table-header-text-color: #303133;
      --el-table-row-hover-bg-color: #f5f7fa;
    }

    :deep(.el-tag--info) {
      background-color: #e8f4f8;
      color: #409eff;
    }
  }

  .pagination {
    text-align: right;

    :deep(.el-pagination) {
      --el-pagination-button-bg-color: #fff;
    }
  }
}
</style>
