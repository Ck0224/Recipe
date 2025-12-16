<template>
  <div class="recipe-search-container">
    <!-- 搜索栏 -->
    <div class="search-box">
      <el-input
          v-model="searchForm.keyword"
          placeholder="请输入食谱名称/描述（如番茄炒蛋）"
          prefix-icon="el-icon-search"
          clearable
          @keyup.enter="fetchSearchList"
      ></el-input>
      <el-button type="primary" @click="fetchSearchList" class="search-btn">
        搜索
      </el-button>
    </div>

    <!-- 结果列表 -->
    <el-table
        v-loading="loading"
        :data="recipeList"
        border
        stripe
        style="width: 100%; margin-top: 20px"
        @row-click="goToDetail"
    >
      <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
      <el-table-column prop="title" label="食谱名称" min-width="200"></el-table-column>
      <el-table-column prop="category" label="分类" width="120"></el-table-column>
      <el-table-column prop="difficulty" label="难度" width="100">
        <template #default="scope">
          <el-tag :type="getDifficultyType(scope.row.difficulty)">
            {{ getDifficultyText(scope.row.difficulty) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="cookTime" label="烹饪时间(分钟)" width="120" align="center"></el-table-column>
      <el-table-column label="创建人" width="120">
        <template #default="scope">
          {{ scope.row.user?.username || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="200">
        <template #default="scope">
          {{ formatTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="scope">
          <el-button
              type="primary"
              size="small"
              icon="el-icon-view"
              @click="goToDetail(scope.row)"
          >
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 无结果提示 -->
    <el-empty
        v-if="!loading && recipeList.length === 0 && searchForm.keyword.trim()"
        description="暂无匹配的食谱，请更换关键词重试"
        style="margin-top: 40px"
    ></el-empty>

    <!-- 分页 -->
    <el-pagination
        v-if="total > 0"
        style="margin-top: 20px; text-align: right"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="searchForm.page + 1"
        :page-sizes="[10, 20, 50]"
        :page-size="searchForm.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
    >
    </el-pagination>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 核心修改：替换为多条件查询接口（兼容原有逻辑）
import { getRecipeList } from '@/api/recipe'

const router = useRouter()
const loading = ref(false)
const recipeList = ref([])
const total = ref(0)

// 搜索表单（page从0开始，和原有接口一致）
const searchForm = reactive({
  keyword: '',
  page: 0,
  size: 10
})

// 格式化时间（适配后端LocalDateTime）
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } catch (e) {
    return timeStr
  }
}

// 难度转换
const getDifficultyType = (difficulty) => {
  switch (difficulty) {
    case 'EASY': return 'success'
    case 'MEDIUM': return 'warning'
    case 'HARD': return 'danger'
    default: return 'info'
  }
}
const getDifficultyText = (difficulty) => {
  switch (difficulty) {
    case 'EASY': return '简单'
    case 'MEDIUM': return '中等'
    case 'HARD': return '困难'
    default: return '未知'
  }
}

// 跳转到详情页
const goToDetail = (row) => {
  router.push(`/recipe/detail/${row.id}`)
}

// 核心：调用多条件查询接口（替代原有searchRecipeList）
const fetchSearchList = async () => {
  if (!searchForm.keyword.trim()) {
    return ElMessage.warning('请输入搜索关键词')
  }

  try {
    loading.value = true
    // 调用多条件查询接口，仅传递title参数（等效原有关键词搜索）
    const pageData = await getRecipeList({
      title: searchForm.keyword.trim(), // 关键词→菜名
      page: searchForm.page,
      size: searchForm.size
    })
    // 解析分页数据（和原有逻辑完全一致）
    recipeList.value = pageData.content || []
    total.value = pageData.totalElements || 0
  } catch (error) {
    ElMessage.error(`搜索失败：${error.message || '未知错误'}`)
    recipeList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 分页事件
const handleSizeChange = (val) => {
  searchForm.size = val
  searchForm.page = 0
  fetchSearchList()
}
const handleCurrentChange = (val) => {
  searchForm.page = val - 1
  fetchSearchList()
}

// 初始化：路由带关键词自动搜索
onMounted(() => {
  const keyword = router.currentRoute.query.keyword
  if (keyword) {
    searchForm.keyword = keyword
    fetchSearchList()
  }
})
</script>

<style scoped>
.recipe-search-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}
.search-box {
  display: flex;
  gap: 10px;
  align-items: center;
}
.search-box .el-input {
  flex: 1;
  max-width: 500px;
}
.search-btn {
  white-space: nowrap;
}
</style>
