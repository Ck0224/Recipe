<template>
  <div class="recipe-list">
    <el-card>
      <!-- 搜索筛选栏-->
      <div class="search-bar">
        <el-row :gutter="20" type="flex" justify="center">
          <!-- 食谱ID搜索 -->
          <el-col :span="4">
            <el-input
                v-model="searchForm.id"
                placeholder="请输入食谱ID（正整数）"
                type="number"
                prefix-icon="Key"
                @keyup.enter="loadRecipeList"
                @input="validateId"
            ></el-input>
            <div class="input-tip" style="font-size: 12px; color: #999; margin-top: 4px;">
              仅支持正整数ID查询
            </div>
          </el-col>
          <!-- 食谱名称搜索 -->
          <el-col :span="5">
            <el-input
                v-model="searchForm.title"
                placeholder="请输入食谱名称（如番茄炒蛋）"
                prefix-icon="Document"
                @keyup.enter="loadRecipeList"
            ></el-input>
          </el-col>
          <!-- 分类搜索 -->
          <el-col :span="5">
            <el-input
                v-model="searchForm.category"
                placeholder="请输入分类（如家常菜）"
                prefix-icon="Menu"
                @keyup.enter="loadRecipeList"
            ></el-input>
          </el-col>
          <!-- 食材搜索 -->
          <el-col :span="5">
            <el-input
                v-model="searchForm.ingredient"
                placeholder="请输入食材（如番茄）"
                prefix-icon="Food"
                @keyup.enter="loadRecipeList"
            ></el-input>
          </el-col>
          <!-- 难度筛选 -->
          <el-col :span="4">
            <el-select
                v-model="searchForm.difficulty"
                placeholder="请选择难度"
                clearable
                @change="loadRecipeList"
            >
              <el-option label="简单" value="EASY"></el-option>
              <el-option label="中等" value="MEDIUM"></el-option>
              <el-option label="困难" value="HARD"></el-option>
            </el-select>
          </el-col>

          <!-- ========== 核心调整：按钮区域 ========== -->
          <el-col :span="4" class="btn-group">
            <el-button
                type="primary"
                icon="Search"
                @click="loadRecipeList"
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

      <!-- 食谱列表/分页（原有内容不变） -->
      <div class="list-content mt-4">
        <el-table
            :data="recipeList"
            border
            stripe
            style="width: 100%"
            @row-click="handleRowClick"
            v-loading="loading"
            empty-text="暂无符合条件的食谱数据"
        >
          <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
          <el-table-column prop="title" label="食谱名称" min-width="200"></el-table-column>
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
          <el-table-column prop="username" label="创建人" width="120">
            <template #default="scope">
              {{ scope.row.user?.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="200"></el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button
                  type="primary"
                  size="small"
                  icon="View"
                  @click="viewDetail(scope.row.id)"
              >
                查看
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
      </div>
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
// 原有script逻辑完全不变
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecipeList, deleteRecipe as deleteRecipeApi } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const recipeList = ref([])
const page = ref(0)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const searchForm = reactive({
  id: '',
  title: '',
  category: '',
  ingredient: '',
  difficulty: ''
})

const validateId = () => {
  if (searchForm.id !== '' && (isNaN(searchForm.id) || searchForm.id <= 0)) {
    ElMessage.warning('食谱ID必须是正整数！')
    searchForm.id = ''
  }
}

const loadRecipeList = async () => {
  if (searchForm.id !== '' && searchForm.id <= 0) {
    ElMessage.warning('食谱ID必须是正整数，请重新输入！')
    return
  }

  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      id: searchForm.id ? Number(searchForm.id) : undefined,
      title: searchForm.title.trim() || undefined,
      category: searchForm.category.trim() || undefined,
      ingredient: searchForm.ingredient.trim() || undefined,
      difficulty: searchForm.difficulty || undefined
    }
    const res = await getRecipeList(params)
    recipeList.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('加载食谱列表失败', error)
    ElMessage.error('加载食谱列表失败：' + (error.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.id = ''
  searchForm.title = ''
  searchForm.category = ''
  searchForm.ingredient = ''
  searchForm.difficulty = ''
  page.value = 0
  loadRecipeList()
}

const handleCurrentChange = (val) => {
  page.value = val - 1
  loadRecipeList()
}

const handleSizeChange = (val) => {
  size.value = val
  page.value = 0
  loadRecipeList()
}

const handleRowClick = (row) => {
  viewDetail(row.id)
}

const viewDetail = (id) => {
  router.push(`/home/recipe-detail/${id}`)
}

const deleteRecipe = async (id) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该食谱吗？删除后将无法恢复！',
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          draggable: true
        }
    )
    await deleteRecipeApi(id, userStore.userInfo?.id)
    ElMessage.success('食谱删除成功！')
    loadRecipeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除食谱失败', error)
      ElMessage.error('删除食谱失败：' + (error.message || '权限不足或网络异常'))
    }
  }
}

onMounted(() => {
  loadRecipeList()
})
</script>

<style scoped lang="scss">
.recipe-list {
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

    // ========== 核心：按钮组样式调整 ==========
    .btn-group {
      display: flex;
      gap: 10px; // 按钮之间的间距
      justify-content: center; // 按钮组内部居中
      align-items: flex-end; // 按钮向下对齐（和输入框底部齐平/略下）
      margin-top: 24px; // 向下偏移的距离（可按需调整，比如16px/20px）

      .search-btn, .reset-btn {
        flex: 1; // 两个按钮平分列宽
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
  }

  .pagination {
    text-align: right;

    :deep(.el-pagination) {
      --el-pagination-button-bg-color: #fff;
    }
  }
}
</style>
