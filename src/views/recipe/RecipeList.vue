<template>
  <div class="recipe-list">
    <el-card>
      <!-- 搜索筛选栏 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
                v-model="searchForm.category"
                placeholder="请输入分类（如家常菜）"
                prefix-icon="Menu"
                @keyup.enter="loadRecipeList"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-select
                v-model="searchForm.difficulty"
                placeholder="请选择难度"
                clearable
            >
              <el-option label="简单" value="EASY"></el-option>
              <el-option label="中等" value="MEDIUM"></el-option>
              <el-option label="困难" value="HARD"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button
                type="primary"
                icon="Search"
                @click="loadRecipeList"
            >
              搜索
            </el-button>
            <el-button
                icon="Refresh"
                @click="resetSearch"
                style="margin-left: 10px;"
            >
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 食谱列表 -->
      <div class="list-content mt-4">
        <el-table
            :data="recipeList"
            border
            stripe
            style="width: 100%"
            @row-click="handleRowClick"
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecipeList, deleteRecipe as deleteRecipeApi } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 状态变量
const recipeList = ref([])
const page = ref(0)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  category: '',
  difficulty: ''
})

// 加载食谱列表
const loadRecipeList = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      category: searchForm.category || undefined,
      difficulty: searchForm.difficulty || undefined
    }
    const res = await getRecipeList(params)
    recipeList.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error('加载食谱列表失败', error)
    ElMessage.error('加载食谱列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.category = ''
  searchForm.difficulty = ''
  page.value = 0
  loadRecipeList()
}

// 页码变化
const handleCurrentChange = (val) => {
  page.value = val - 1 // 转换为后端页码（0开始）
  loadRecipeList()
}

// 每页条数变化
const handleSizeChange = (val) => {
  size.value = val
  page.value = 0 // 重置页码
  loadRecipeList()
}

// 行点击事件（查看详情）
const handleRowClick = (row) => {
  viewDetail(row.id)
}

// 查看详情
const viewDetail = (id) => {
  router.push(`/home/recipe-detail/${id}`)
}

// 删除食谱
const deleteRecipe = async (id) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该食谱吗？删除后将无法恢复！',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    // 👇 关键修改：deleteRecipe → deleteRecipeApi
    await deleteRecipeApi(id, userStore.userInfo.id)
    ElMessage.success('删除成功')
    loadRecipeList() // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除食谱失败', error)
      ElMessage.error('删除食谱失败')
    }
  }
}

// 初始化加载
onMounted(() => {
  loadRecipeList()
})
</script>

<style scoped lang="scss">
.recipe-list {
  .search-bar {
    padding: 10px 0;
    border-bottom: 1px solid #e6e6e6;
  }

  .pagination {
    text-align: right;
  }
}
</style>
