<template>
  <div class="recipe-edit">
    <el-card shadow="hover">
      <!-- 页面头部 -->
      <div class="page-header" style="margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #e6e6e6;">
        <h2 style="margin: 0;">更新食谱</h2>
        <p style="color: #909399; margin: 5px 0 0 0;">
          修改食谱信息后点击「更新食谱」按钮保存
        </p>
      </div>

      <!-- 食谱表单 -->
      <el-form
          ref="recipeFormRef"
          :model="recipeForm"
          :rules="recipeRules"
          label-width="100px"
          style="max-width: 800px; margin: 0 auto;"
      >
        <!-- 基础信息 -->
        <el-form-item label="食谱名称" prop="title">
          <el-input
              v-model="recipeForm.title"
              placeholder="请输入食谱名称（如番茄炒蛋）"
              maxlength="50"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-input
              v-model="recipeForm.category"
              placeholder="请输入分类（如家常菜）"
              maxlength="20"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="recipeForm.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="EASY"></el-option>
            <el-option label="中等" value="MEDIUM"></el-option>
            <el-option label="困难" value="HARD"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="烹饪时间" prop="cookTime">
          <el-input
              v-model="recipeForm.cookTime"
              type="number"
              placeholder="请输入烹饪时间（分钟）"
              min="1"
              max="999"
          ></el-input>
        </el-form-item>

        <!-- 食材信息 -->
        <el-form-item label="主要食材" prop="ingredients">
          <el-input
              v-model="recipeForm.ingredients"
              type="textarea"
              placeholder="请输入主要食材（多个食材用逗号分隔）"
              rows="3"
              maxlength="200"
              show-word-limit
          ></el-input>
        </el-form-item>

        <!-- 步骤信息 -->
        <el-form-item label="烹饪步骤" prop="steps">
          <el-input
              v-model="recipeForm.steps"
              type="textarea"
              placeholder="请输入烹饪步骤（每步换行）"
              rows="6"
              maxlength="1000"
              show-word-limit
          ></el-input>
        </el-form-item>

        <!-- 隐私状态 -->
        <el-form-item label="隐私状态" prop="isPrivate">
          <el-radio-group v-model="recipeForm.isPrivate">
            <el-radio label="false">公开（所有人可见）</el-radio>
            <el-radio label="true">私有（仅自己可见）</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item style="text-align: center; margin-top: 30px;">
          <el-button
              type="primary"
              size="large"
              icon="Edit"
              @click="submitEdit"
              :loading="submitting"
          >
            更新食谱
          </el-button>
          <el-button
              size="large"
              icon="ArrowLeft"
              @click="goBack"
              style="margin-left: 20px;"
          >
            返回列表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecipeDetail, updateRecipe } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

// 初始化路由、路由参数、用户状态
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const recipeFormRef = ref(null) // 表单引用
const submitting = ref(false) // 提交加载状态
const recipeId = ref('') // 当前编辑的食谱ID

// 食谱表单（与创建页结构一致）
const recipeForm = reactive({
  title: '',
  category: '',
  difficulty: '',
  cookTime: '',
  ingredients: '',
  steps: '',
  isPrivate: 'false' // 默认公开
})

// 表单校验规则（与创建页一致）
const recipeRules = reactive({
  title: [
    { required: true, message: '请输入食谱名称', trigger: 'blur' },
    { min: 2, max: 50, message: '食谱名称长度在2-50个字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请输入分类', trigger: 'blur' },
    { min: 1, max: 20, message: '分类长度在1-20个字符之间', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请选择难度', trigger: 'change' }
  ],
  cookTime: [
    { required: true, message: '请输入烹饪时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '烹饪时间为1-999分钟', trigger: 'blur' }
  ],
  ingredients: [
    { required: true, message: '请输入主要食材', trigger: 'blur' },
    { min: 5, max: 200, message: '食材信息长度在5-200个字符之间', trigger: 'blur' }
  ],
  steps: [
    { required: true, message: '请输入烹饪步骤', trigger: 'blur' },
    { min: 10, max: 1000, message: '步骤信息长度在10-1000个字符之间', trigger: 'blur' }
  ]
})

/**
 * 加载食谱详情（核心：编辑页专属）
 */
const loadRecipeDetail = async () => {
  // 1. 获取路由参数中的食谱ID
  recipeId.value = route.params.id || ''
  if (!recipeId.value) {
    ElMessage.error('食谱ID异常，无法编辑！')
    goBack()
    return
  }

  // 2. 校验用户登录状态
  if (!userStore.token) {
    ElMessage.warning('登录状态已过期，请重新登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    submitting.value = true
    // 3. 调用详情接口加载数据
    const res = await getRecipeDetail(recipeId.value)
    const recipeData = res.data || res

    // 4. 校验权限（仅创建者/管理员可编辑）
    const isCreator = recipeData.user?.id === userStore.userInfo.id
    const isAdmin = userStore.userInfo.isAdmin
    if (!isCreator && !isAdmin) {
      ElMessageBox.alert('您无权编辑该食谱！', '权限不足', { type: 'error' })
      goBack()
      return
    }

    // 5. 填充表单数据（与接口返回字段映射）
    recipeForm.title = recipeData.title || ''
    recipeForm.category = recipeData.category || ''
    recipeForm.difficulty = recipeData.difficulty || ''
    recipeForm.cookTime = recipeData.cookTime || ''
    recipeForm.ingredients = recipeData.ingredients || ''
    recipeForm.steps = recipeData.steps || ''
    recipeForm.isPrivate = String(recipeData.isPrivate) || 'false' // 转为字符串适配单选框
  } catch (error) {
    console.error('加载食谱详情失败', error)
    let errMsg = '加载食谱详情失败'
    if (error.response) {
      errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
      if (error.response.status === 404) {
        errMsg = '食谱不存在或已被删除'
      } else if (error.response.status === 403) {
        errMsg = '您无权查看该食谱'
      }
    }
    ElMessage.error(errMsg)
    goBack()
  } finally {
    submitting.value = false
  }
}

/**
 * 提交更新食谱
 */
const submitEdit = async () => {
  // 1. 表单校验
  const validateResult = await recipeFormRef.value.validate()
  if (!validateResult) return

  // 2. 二次确认
  try {
    await ElMessageBox.confirm(
        '确认要更新该食谱吗？更新后将覆盖原有数据！',
        '更新确认',
        {
          confirmButtonText: '确认更新',
          cancelButtonText: '取消',
          type: 'warning',
          draggable: true
        }
    )
  } catch (error) {
    ElMessage.info('已取消更新')
    return
  }

  // 3. 构建提交数据
  const submitData = {
    id: Number(recipeId.value), // 食谱ID（必填）
    title: recipeForm.title.trim(),
    category: recipeForm.category.trim(),
    difficulty: recipeForm.difficulty,
    cookTime: Number(recipeForm.cookTime),
    ingredients: recipeForm.ingredients.trim(),
    steps: recipeForm.steps.trim(),
    isPrivate: recipeForm.isPrivate === 'true', // 转为布尔值
    userId: Number(userStore.userInfo.id) // 操作人ID（供后端校验）
  }

  try {
    submitting.value = true
    // 4. 调用更新接口
    await updateRecipe(submitData)
    ElMessage.success('食谱更新成功！')
    // 5. 跳转回我的食谱列表
    router.push('/home/my-recipe-list')
  } catch (error) {
    console.error('更新食谱失败', error)
    let errMsg = '更新食谱失败'
    if (error.response) {
      errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
      if (error.response.status === 400) {
        errMsg = `参数错误：${error.response.data?.message || '请检查输入内容'}`
      } else if (error.response.status === 403) {
        errMsg = '您无权更新该食谱'
      } else if (error.response.status === 404) {
        errMsg = '食谱不存在或已被删除'
      }
    }
    ElMessage.error(errMsg)
  } finally {
    submitting.value = false
  }
}

/**
 * 返回上一页/我的食谱列表
 */
const goBack = () => {
  // 优先返回上一页，否则跳转到我的食谱列表
  if (router.options.history.state.back) {
    router.go(-1)
  } else {
    router.push('/home/my-recipe-list')
  }
}

// 页面挂载时加载食谱详情
onMounted(() => {
  loadRecipeDetail()
})

// 页面卸载清理
onUnmounted(() => {
  submitting.value = false
  recipeFormRef.value?.resetFields()
})
</script>

<style scoped lang="scss">
.recipe-edit {
  padding: 20px;

  :deep(.el-card) {
    --el-card-border-radius: 8px;
    --el-card-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }

  .page-header {
    h2 {
      color: #303133;
      font-weight: 600;
    }
  }

  :deep(.el-form) {
    --el-form-item-label-color: #606266;

    .el-form-item__content {
      --el-input-border-radius: 4px;
      --el-input-hover-border-color: #409eff;
    }

    .el-textarea {
      --el-textarea-border-radius: 4px;
      --el-textarea-hover-border-color: #409eff;
    }
  }

  :deep(.el-button) {
    --el-button-border-radius: 4px;
  }
}
</style>
