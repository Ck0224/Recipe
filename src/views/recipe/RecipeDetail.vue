<template>
  <div class="recipe-detail">
    <el-card v-loading="loading">
      <!-- 返回按钮 -->
      <el-button
          icon="ArrowLeft"
          @click="goBack"
          style="margin-bottom: 20px;"
      >
        返回列表
      </el-button>

      <!-- 食谱基础信息 -->
      <div class="basic-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="cover-image">
              <img
                  :src="recipeDetail.coverImage || '@/assets/images/default-cover.png'"
                  alt="封面图片"
              >
            </div>
          </el-col>
          <el-col :span="18">
            <div class="info-content">
              <h1 class="recipe-title">{{ recipeDetail.title }}</h1>
              <div class="recipe-meta">
                <el-tag>{{ recipeDetail.category }}</el-tag>
                <el-tag
                    :type="recipeDetail.difficulty === 'EASY' ? 'success' : recipeDetail.difficulty === 'MEDIUM' ? 'warning' : 'danger'"
                >
                  {{ recipeDetail.difficulty === 'EASY' ? '简单' : recipeDetail.difficulty === 'MEDIUM' ? '中等' : '困难' }}
                </el-tag>
                <span class="meta-item">
                  <el-icon><Timer /></el-icon>
                  准备时间：{{ recipeDetail.prepTime }}分钟
                </span>
                <span class="meta-item">
                  <el-icon><HotWater /></el-icon>
                  烹饪时间：{{ recipeDetail.cookTime }}分钟
                </span>
                <span class="meta-item">
                  <el-icon><User /></el-icon>
                  份数：{{ recipeDetail.servings }}份
                </span>
                <span class="meta-item">
                  <el-icon><UserFilled /></el-icon>
                  创建人：{{ recipeDetail.user?.username }}
                </span>
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  创建时间：{{ formatTime(recipeDetail.createdAt) }}
                </span>
              </div>
              <div class="recipe-desc">
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

      <!-- 食材列表 -->
      <div class="ingredients-section mt-4">
        <h3 class="section-title">
          <el-icon><ShoppingCart /></el-icon>
          食材列表
        </h3>
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

      <!-- 步骤列表 -->
      <div class="steps-section mt-4">
        <h3 class="section-title">
          <el-icon><List /></el-icon>
          烹饪步骤
        </h3>
        <div class="steps-list">
          <div
              class="step-item"
              v-for="(step, index) in recipeDetail.steps"
              :key="step.id || index"
          >
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <div class="step-desc">{{ step.description }}</div>
              <div class="step-meta" v-if="step.timerMinutes > 0">
                <el-tag size="small">
                  <el-icon><Timer /></el-icon>
                  预计耗时：{{ step.timerMinutes }}分钟
                </el-tag>
              </div>
              <div class="step-image" v-if="step.imageUrl">
                <img :src="step.imageUrl" alt="步骤图片">
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Timer, HotWater, User, UserFilled, Calendar,
  ShoppingCart, List
} from '@element-plus/icons-vue'
import { getRecipeDetail } from '@/api/recipe'

const router = useRouter()
const route = useRoute()

// 状态变量
const loading = ref(false)
const recipeDetail = ref({
  ingredients: [],
  steps: [],
  tagList: []
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 返回列表
const goBack = () => {
  router.push('/home/recipe-list')
}

// 加载食谱详情
const loadRecipeDetail = async () => {
  try {
    loading.value = true
    const id = route.params.id
    const res = await getRecipeDetail(id)
    recipeDetail.value = res
  } catch (error) {
    console.error('加载食谱详情失败', error)
    ElMessage.error('加载食谱详情失败')
    goBack()
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadRecipeDetail()
})
</script>

<style scoped lang="scss">
.recipe-detail {
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
      }

      .recipe-meta {
        margin-bottom: 20px;

        .meta-item {
          display: inline-block;
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
          }
        }
      }
    }
  }
}
</style>
