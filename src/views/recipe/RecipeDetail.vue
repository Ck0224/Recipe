<template>
  <!-- 完全保留原有模板，不做任何修改 -->
  <div class="recipe-detail">
    <el-card v-loading="loading">
      <!-- 返回按钮 + 编辑按钮（完全保留原有逻辑） -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <el-button
            icon="ArrowLeft"
            @click="goBack"
        >
          返回列表
        </el-button>
        <el-button
            v-if="showEditBtn"
            type="warning"
            icon="Edit"
            @click="goToEdit"
        >
          编辑食谱
        </el-button>
      </div>

      <!-- 以下所有模板内容完全保留 -->
      <div class="basic-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="cover-image">
              <img
                  :src="recipeDetail.coverImage || '/src/assets/images/default-cover.png'"
                  alt="封面图片"
                  @error="handleImageError"
              >
            </div>
          </el-col>
          <el-col :span="18">
            <div class="info-content">
              <h1 class="recipe-title">{{ recipeDetail.title || '未命名食谱' }}</h1>
              <div class="recipe-meta">
                <el-tag>{{ recipeDetail.category || '未分类' }}</el-tag>
                <el-tag
                    :type="getDifficultyType(recipeDetail.difficulty)"
                >
                  {{ getDifficultyText(recipeDetail.difficulty) }}
                </el-tag>
                <span class="meta-item" v-if="recipeDetail.prepTime">
                  <el-icon><Timer /></el-icon>
                  准备时间：{{ recipeDetail.prepTime }}分钟
                </span>
                <span class="meta-item" v-if="recipeDetail.cookTime">
                  <el-icon><HotWater /></el-icon>
                  烹饪时间：{{ recipeDetail.cookTime }}分钟
                </span>
                <span class="meta-item" v-if="recipeDetail.servings">
                  <el-icon><User /></el-icon>
                  份数：{{ recipeDetail.servings }}份
                </span>
                <span class="meta-item" v-if="recipeDetail.user?.username">
                  <el-icon><UserFilled /></el-icon>
                  创建人：{{ recipeDetail.user.username }}
                </span>
                <span class="meta-item" v-if="recipeDetail.createdAt">
                  <el-icon><Calendar /></el-icon>
                  创建时间：{{ formatTime(recipeDetail.createdAt) }}
                </span>
                <el-tag v-if="recipeDetail.isPrivate" type="info" class="meta-item">
                  <el-icon><Lock /></el-icon>
                  私有食谱
                </el-tag>
              </div>
              <div class="recipe-desc" v-if="recipeDetail.description">
                <h3>食谱描述</h3>
                <p>{{ recipeDetail.description }}</p>
              </div>
              <div class="recipe-tags" v-if="recipeDetail.tagList && recipeDetail.tagList.length">
                <h3>标签</h3>
                <el-tag
                    v-for="tag in recipeDetail.tagList"
                    :key="tag"
                    class="tag-item"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="ingredients-section mt-4">
        <h3 class="section-title">
          <el-icon><ShoppingCart /></el-icon>
          食材列表
        </h3>
        <div v-if="recipeDetail.ingredients && recipeDetail.ingredients.length">
          <el-table
              :data="recipeDetail.ingredients"
              border
              stripe
              style="width: 100%"
          >
            <el-table-column prop="name" label="食材名称" width="150"></el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" align="center"></el-table-column>
            <el-table-column prop="unit" label="单位" width="100" align="center"></el-table-column>
            <el-table-column prop="note" label="备注" min-width="200"></el-table-column>
          </el-table>
        </div>
        <div v-else class="empty-section">
          <el-empty description="暂无食材信息"></el-empty>
        </div>
      </div>

      <div class="steps-section mt-4">
        <h3 class="section-title">
          <el-icon><List /></el-icon>
          烹饪步骤
        </h3>
        <div v-if="recipeDetail.steps && recipeDetail.steps.length" class="steps-list">
          <div
              class="step-item"
              v-for="(step, index) in recipeDetail.steps"
              :key="step.id || index"
          >
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <div class="step-desc">{{ step.description || '无步骤说明' }}</div>
              <div class="step-meta" v-if="step.timerMinutes > 0">
                <el-tag size="small">
                  <el-icon><Timer /></el-icon>
                  预计耗时：{{ step.timerMinutes }}分钟
                </el-tag>
              </div>
              <div class="step-image" v-if="step.imageUrl">
                <img
                    :src="step.imageUrl"
                    alt="步骤图片"
                    @error="handleImageError"
                >
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-section">
          <el-empty description="暂无烹饪步骤"></el-empty>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 完全保留原有导入和变量定义（包括编辑按钮权限判断）
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Timer, HotWater, User, UserFilled, Calendar,
  ShoppingCart, List, Edit, Lock
} from '@element-plus/icons-vue'
import { getRecipeDetail } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const recipeDetail = ref({
  ingredients: [],
  steps: [],
  tagList: []
})

// 完全保留原有编辑按钮权限判断（不做任何修改）
const showEditBtn = computed(() => {
  // 未登录/无食谱数据 → 不显示
  if (!userStore.token || !recipeDetail.value.id) return false
  // 管理员 → 显示
  if (userStore.userInfo.isAdmin) return true
  // 创建者 → 显示
  return recipeDetail.value.user?.id === userStore.userInfo.id
})

// 完全保留原有跳转逻辑（不做任何修改）
const goToEdit = () => {
  if (!recipeDetail.value.id) {
    ElMessage.warning('食谱ID异常，无法编辑！')
    return
  }
  router.push({
    name: 'RecipeEdit',
    params: { id: recipeDetail.value.id }
  })
}

// 完全保留原有工具函数（不做任何修改）
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    if (isNaN(date.getTime())) return '-'
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } catch (e) {
    return '-'
  }
}

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

const handleImageError = (e) => {
  e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZTBlMGUwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIyMCIgZmlsbD0iIzY2NiIgbGV0dGVyLXNwYWNpbmc9IjEiPuWFseWFseS6huWFseWFseS5iYWlkdW88L3RleHQ+PC9zdmc+'
  e.target.alt = '默认封面'
}

const goBack = () => {
  if (router.options.history.state.back) {
    router.go(-1)
  } else {
    router.push('/home/recipe-list')
  }
}

// ========== 仅修改此处：加载食谱详情的Data赋值逻辑（移除Result包装判断） ==========
const loadRecipeDetail = async () => {
  try {
    loading.value = true
    let recipeId = route.params.id
    if (!recipeId) {
      ElMessage.error('食谱ID不能为空！')
      goBack()
      return
    }
    recipeId = Number(recipeId)
    if (isNaN(recipeId) || recipeId <= 0) {
      ElMessage.error('食谱ID必须为正整数！')
      goBack()
      return
    }

    const currentUserId = userStore.userInfo.id || 1
    const res = await getRecipeDetail(recipeId, Number(currentUserId))

    // 核心修改：直接赋值原始数据，删除所有Result包装（success/data）的判断逻辑
    // 适配编辑后后端返回的原始食谱数据，不再解析success字段
    recipeDetail.value = {
      ingredients: [],
      steps: [],
      tagList: [],
      ...(res.data || res) // 直接展开原始数据，无任何包装解析
    };

  } catch (error) {
    // 完全保留原有错误处理逻辑
    console.error('加载食谱详情失败:', error);
    let errMsg = '加载食谱详情失败';
    if (error.response) {
      errMsg = error.response.data?.error || `错误码：${error.response.status}`;
      if (error.response.status === 400) {
        errMsg = '请求参数错误：缺少用户ID或食谱ID格式错误';
      } else if (error.response.status === 404) {
        errMsg = '该食谱不存在或已被删除';
      } else if (error.response.status === 401) {
        errMsg = '登录状态失效，请重新登录';
      }
    }
    ElMessage.error(errMsg);
    goBack();
  } finally {
    loading.value = false
  }
}

// 完全保留原有初始化逻辑
onMounted(() => {
  loadRecipeDetail()
})
</script>

<style scoped lang="scss">
/* 完全保留原有样式，不做任何修改 */
.recipe-detail {
  padding: 20px;

  .basic-info {
    .cover-image {
      img {
        width: 100%;
        height: 250px;
        object-fit: cover;
        border-radius: 8px;
      }
    }

    .info-content {
      .recipe-title {
        font-size: 24px;
        margin-bottom: 15px;
        color: #333;
        font-weight: 600;
      }

      .recipe-meta {
        margin-bottom: 20px;

        .meta-item {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          margin: 0 10px 10px 0;
          font-size: 14px;
          color: #666;
        }
      }

      .recipe-desc, .recipe-tags {
        margin-bottom: 20px;

        h3 {
          font-size: 16px;
          margin-bottom: 10px;
          color: #333;
          font-weight: 600;
        }

        p {
          line-height: 1.6;
          color: #666;
        }

        .tag-item {
          margin-right: 8px;
          margin-bottom: 8px;
        }
      }
    }
  }

  .section-title {
    font-size: 18px;
    margin-bottom: 15px;
    color: #333;
    font-weight: 600;
    padding-bottom: 5px;
    border-bottom: 2px solid #667eea;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .empty-section {
    padding: 40px 0;
    text-align: center;
  }

  .steps-list {
    .step-item {
      display: flex;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px dashed #e6e6e6;

      &:last-child {
        border-bottom: none;
      }

      .step-number {
        width: 40px;
        height: 40px;
        background-color: #667eea;
        color: #fff;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 18px;
        font-weight: bold;
        margin-right: 20px;
        flex-shrink: 0;
      }

      .step-content {
        flex: 1;

        .step-desc {
          font-size: 16px;
          line-height: 1.6;
          margin-bottom: 10px;
          color: #333;
        }

        .step-image {
          margin-top: 10px;

          img {
            max-width: 500px;
            border-radius: 8px;
            max-height: 300px;
            object-fit: cover;
          }
        }
      }
    }
  }
}
</style>
