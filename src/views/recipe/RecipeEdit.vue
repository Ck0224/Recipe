<template>
  <div class="recipe-edit">
    <el-card>
      <el-form
          :model="recipeForm"
          :rules="recipeRules"
          ref="recipeFormRef"
          label-width="100px"
          class="recipe-form"
      >
        <!-- 基础信息（和创建页一致） -->
        <el-form-item label="食谱名称" prop="title">
          <el-input
              v-model="recipeForm.title"
              placeholder="请输入食谱名称（如番茄炒蛋）"
              maxlength="50"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="食谱描述" prop="description">
          <el-input
              v-model="recipeForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入食谱的详细描述"
              maxlength="500"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="封面图片">
          <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="uploadCoverImage"
          >
            <!-- 封面图预览 + 删除按钮 -->
            <div class="preview-wrapper" v-if="recipeForm.coverImage">
              <img
                  :src="getFullImageUrl(recipeForm.coverImage)"
                  class="avatar"
              >
              <el-button
                  class="delete-btn"
                  icon="Close"
                  type="danger"
                  size="small"
                  circle
                  @click.stop="removeCoverImage"
              ></el-button>
            </div>
            <div v-else class="upload-icon">
              <el-icon><Plus /></el-icon>
              <div class="text">上传封面图片</div>
            </div>
          </el-upload>
          <div class="upload-tip">支持jpg/png格式，大小不超过2MB</div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="准备时间(分钟)" prop="prepTime">
              <el-input
                  v-model.number="recipeForm.prepTime"
                  type="number"
                  min="0"
                  placeholder="请输入准备时间"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="烹饪时间(分钟)" prop="cookTime">
              <el-input
                  v-model.number="recipeForm.cookTime"
                  type="number"
                  min="1"
                  placeholder="请输入烹饪时间"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="份数" prop="servings">
              <el-input
                  v-model.number="recipeForm.servings"
                  type="number"
                  min="1"
                  placeholder="请输入份数"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="recipeForm.difficulty" placeholder="请选择难度">
                <el-option label="简单" value="EASY"></el-option>
                <el-option label="中等" value="MEDIUM"></el-option>
                <el-option label="困难" value="HARD"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input
                  v-model="recipeForm.category"
                  placeholder="请输入分类（如家常菜、西餐）"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 标签输入项（和创建页一致） -->
        <el-form-item label="标签" prop="tagList">
          <el-input
              v-model="tagInputValue"
              placeholder="请输入标签（回车添加）"
              maxlength="10"
              @keyup.enter="addTag"
              style="width: 100%; margin-bottom: 8px;"
          ></el-input>
          <div>
            <el-tag
                v-for="(tag, index) in recipeForm.tagList"
                :key="index"
                closable
                @close="removeTag(index)"
                style="margin: 0 4px 4px 0;"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="tag-tip" style="margin-top: 4px; font-size: 12px; color: #909399;">
            最多支持5个标签，每个标签不超过10个字
          </div>
        </el-form-item>

        <el-form-item label="是否私有">
          <el-switch
              v-model="recipeForm.isPrivate"
              active-text="私有（仅自己可见）"
              inactive-text="公开（所有人可见）"
          ></el-switch>
        </el-form-item>

        <!-- 食材列表（和创建页一致） -->
        <el-form-item label="食材列表">
          <el-table
              :data="recipeForm.ingredients"
              border
              style="width: 100%"
              :row-class-name="(row) => row.id ? '' : 'new-row'"
          >
            <el-table-column label="食材名称" min-width="150">
              <template #default="scope">
                <el-input
                    v-model="scope.row.name"
                    placeholder="请输入食材名称（如番茄）"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="scope">
                <el-input
                    v-model.number="scope.row.quantity"
                    type="number"
                    min="0"
                    step="0.5"
                    placeholder="数量"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="100">
              <template #default="scope">
                <el-input
                    v-model="scope.row.unit"
                    placeholder="如个、克"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="150">
              <template #default="scope">
                <el-input
                    v-model="scope.row.note"
                    placeholder="如去皮、切丁"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button
                    type="danger"
                    size="small"
                    icon="Delete"
                    @click="removeIngredient(scope.$index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button
              type="primary"
              size="small"
              icon="Plus"
              @click="addIngredient"
              class="mt-2"
          >
            添加食材
          </el-button>
        </el-form-item>

        <!-- 步骤列表（和创建页一致） -->
        <el-form-item label="步骤列表">
          <el-table
              :data="recipeForm.steps"
              border
              style="width: 100%"
          >
            <el-table-column label="步骤编号" width="100" align="center">
              <template #default="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column label="步骤描述" min-width="300">
              <template #default="scope">
                <el-input
                    v-model="scope.row.description"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入详细的步骤描述"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="计时(分钟)" width="100">
              <template #default="scope">
                <el-input
                    v-model.number="scope.row.timerMinutes"
                    type="number"
                    min="0"
                    placeholder="可选"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="步骤图片" width="180">
              <template #default="scope">
                <el-upload
                    class="step-upload"
                    action="#"
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    :http-request="(options) => uploadStepImage(scope.$index, options)"
                >
                  <!-- 步骤图预览 + 删除按钮 -->
                  <div class="preview-wrapper" v-if="scope.row.imageUrl">
                    <img
                        :src="getFullImageUrl(scope.row.imageUrl)"
                        class="step-image"
                    >
                    <el-button
                        class="delete-btn"
                        icon="Close"
                        type="danger"
                        size="small"
                        circle
                        @click.stop="removeStepImage(scope.$index)"
                    ></el-button>
                  </div>
                  <div v-else class="upload-btn">
                    <el-button size="small" type="primary" icon="Upload">
                      上传图片
                    </el-button>
                  </div>
                </el-upload>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button
                    type="danger"
                    size="small"
                    icon="Delete"
                    @click="removeStep(scope.$index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button
              type="primary"
              size="small"
              icon="Plus"
              @click="addStep"
              class="mt-2"
          >
            添加步骤
          </el-button>
        </el-form-item>

        <!-- 提交按钮（修改文案） -->
        <el-form-item class="form-submit">
          <el-button
              type="primary"
              size="large"
              @click="submitRecipe"
              :loading="submitting"
          >
            更新食谱
          </el-button>
          <el-button
              size="large"
              @click="resetForm"
              style="margin-left: 20px;"
          >
            恢复原始数据
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Delete, Close } from '@element-plus/icons-vue' // 新增Close图标
import { updateRecipe, getRecipeDetail, uploadImage } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 状态变量
const recipeFormRef = ref(null)
const submitting = ref(false)
const tagInputValue = ref('')
const loading = ref(false)
const recipeId = ref('')

// 食谱表单（和创建页一致的初始结构）
const recipeForm = reactive({
  title: '',
  description: '',
  coverImage: '',
  prepTime: 0,
  cookTime: 0,
  servings: 1,
  difficulty: '',
  category: '',
  tagList: [],
  isPrivate: false,
  ingredients: [
    { name: '', quantity: 0, unit: '', note: '', sortOrder: 0 }
  ],
  steps: [
    {
      stepNumber: 1,
      description: '',
      imageUrl: '',
      timerMinutes: 0,
      sortOrder: 0
    }
  ]
})

// 表单校验规则（和创建页一致）
const recipeRules = reactive({
  title: [
    { required: true, message: '请输入食谱名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在2-50位之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入食谱描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在10-500位之间', trigger: 'blur' }
  ],
  cookTime: [
    { required: true, message: '请输入烹饪时间', trigger: 'blur' },
    { type: 'number', min: 1, message: '烹饪时间不能小于1分钟', trigger: 'blur' }
  ],
  servings: [
    { required: true, message: '请输入份数', trigger: 'blur' },
    { type: 'number', min: 1, message: '份数不能小于1', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请选择难度', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请输入分类', trigger: 'blur' }
  ],
  tagList: [
    {
      validator: (rule, value, callback) => {
        if (value.length > 5) {
          callback(new Error('最多只能添加5个标签'))
        } else {
          const overLength = value.some(tag => tag.length > 10)
          if (overLength) {
            callback(new Error('每个标签长度不能超过10个字'))
          } else {
            callback()
          }
        }
      },
      trigger: 'change'
    }
  ]
})

// 核心修复：定义模板中使用的 getFullImageUrl 函数
const getFullImageUrl = (imageUrl) => {
  // 空值兜底
  if (!imageUrl || imageUrl.trim() === '') {
    return ''; // 无图片时返回空
  }
  // 已拼接完整URL则直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  // 未拼接则补充服务器地址
  const baseUrl = 'http://localhost:8080';
  return `${baseUrl}${imageUrl}`;
};

// 新增：删除封面图片
const removeCoverImage = async () => {
  try {
    await ElMessageBox.confirm('确定要删除封面图片吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    });
    recipeForm.coverImage = '';
    ElMessage.success('封面图片已删除');
  } catch (error) {
    ElMessage.info('已取消删除');
  }
};

// 新增：删除步骤图片
const removeStepImage = async (index) => {
  try {
    await ElMessageBox.confirm(`确定要删除步骤${index+1}的图片吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    });
    recipeForm.steps[index].imageUrl = '';
    ElMessage.success(`步骤${index+1}图片已删除`);
  } catch (error) {
    ElMessage.info('已取消删除');
  }
};

// 加载食谱详情（编辑页专属逻辑）
const loadRecipeDetail = async () => {
  try {
    loading.value = true
    recipeId.value = route.params.id
    if (!recipeId.value) {
      ElMessage.error('食谱ID异常，无法编辑！')
      router.go(-1)
      return
    }
    const currentUserId = userStore.userInfo.id || 1
    const res = await getRecipeDetail(Number(recipeId.value), Number(currentUserId))
    const data = res.data || res

    // 赋值到表单（和创建页字段对齐）
    recipeForm.title = data.title || ''
    recipeForm.description = data.description || ''
    // 修复：详情页图片URL也拼接完整路径
    recipeForm.coverImage = getFullImageUrl(data.coverImage || '')
    recipeForm.prepTime = Number(data.prepTime) || 0
    recipeForm.cookTime = Number(data.cookTime) || 0
    recipeForm.servings = Number(data.servings) || 1
    recipeForm.difficulty = data.difficulty || ''
    recipeForm.category = data.category || ''
    recipeForm.tagList = Array.isArray(data.tagList) ? data.tagList : []
    recipeForm.isPrivate = data.isPrivate || false
    recipeForm.ingredients = Array.isArray(data.ingredients) && data.ingredients.length
        ? data.ingredients.map(item => ({
          name: item.name || '',
          quantity: Number(item.quantity) || 0,
          unit: item.unit || '',
          note: item.note || '',
          sortOrder: item.sortOrder || 0
        }))
        : [{ name: '', quantity: 0, unit: '', note: '', sortOrder: 0 }]
    recipeForm.steps = Array.isArray(data.steps) && data.steps.length
        ? data.steps.map((item, index) => ({
          stepNumber: index + 1,
          description: item.description || '',
          // 修复：步骤图片URL拼接完整路径
          imageUrl: getFullImageUrl(item.imageUrl || ''),
          timerMinutes: Number(item.timerMinutes) || 0,
          sortOrder: item.sortOrder || 0
        }))
        : [{ stepNumber: 1, description: '', imageUrl: '', timerMinutes: 0, sortOrder: 0 }]
  } catch (error) {
    console.error('加载食谱详情失败', error)
    ElMessage.error('加载食谱详情失败，请返回重试')
    router.go(-1)
  } finally {
    loading.value = false
  }
}

// 以下方法完全复用创建页的逻辑
const addTag = () => {
  const tag = tagInputValue.value.trim()
  if (!tag) return ElMessage.warning('标签不能为空！')
  if (recipeForm.tagList.length >= 5) return ElMessage.warning('最多只能添加5个标签！')
  if (recipeForm.tagList.includes(tag)) return ElMessage.warning('该标签已存在！')
  if (tag.length > 10) return ElMessage.warning('标签长度不能超过10个字！')
  recipeForm.tagList.push(tag)
  tagInputValue.value = ''
}

const removeTag = (index) => {
  recipeForm.tagList.splice(index, 1)
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片格式（jpg/png）！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB！')
    return false
  }
  return true
}

// 修复封面图片上传方法
const uploadCoverImage = async (options) => {
  try {
    const res = await uploadImage(options.file);
    // 适配后端返回格式
    const rawImageUrl = res?.data || res || '';
    // 拼接完整URL
    const fullImageUrl = getFullImageUrl(rawImageUrl);

    recipeForm.coverImage = fullImageUrl;
    ElMessage.success('封面图片上传成功');
    console.log('封面图片完整URL：', fullImageUrl);
  } catch (error) {
    ElMessage.error('封面图片上传失败');
    console.error('上传失败', error);
    recipeForm.coverImage = '';
  }
};

// 修复步骤图片上传方法（核心：赋值到对应步骤的imageUrl，而非coverImage）
const uploadStepImage = async (index, options) => {
  try {
    const res = await uploadImage(options.file);
    const rawImageUrl = res?.data || res || '';
    const fullImageUrl = getFullImageUrl(rawImageUrl);

    // 赋值到对应步骤的imageUrl，而非封面图片
    recipeForm.steps[index].imageUrl = fullImageUrl;
    ElMessage.success('步骤图片上传成功');
    console.log(`步骤${index+1}图片完整URL：`, fullImageUrl);
  } catch (error) {
    ElMessage.error('步骤图片上传失败');
    console.error('上传失败', error);
    recipeForm.steps[index].imageUrl = '';
  }
};

const addIngredient = () => {
  recipeForm.ingredients.push({
    name: '',
    quantity: 0,
    unit: '',
    note: '',
    sortOrder: recipeForm.ingredients.length
  })
}

const removeIngredient = (index) => {
  if (recipeForm.ingredients.length <= 1) {
    ElMessage.warning('至少保留一个食材')
    return
  }
  recipeForm.ingredients.splice(index, 1)
  recipeForm.ingredients.forEach((item, i) => {
    item.sortOrder = i
  })
}

const addStep = () => {
  recipeForm.steps.push({
    stepNumber: recipeForm.steps.length + 1,
    description: '',
    imageUrl: '',
    timerMinutes: 0,
    sortOrder: recipeForm.steps.length
  })
}

const removeStep = (index) => {
  if (recipeForm.steps.length <= 1) {
    ElMessage.warning('至少保留一个步骤')
    return
  }
  recipeForm.steps.splice(index, 1)
  recipeForm.steps.forEach((item, i) => {
    item.stepNumber = i + 1
    item.sortOrder = i
  })
}

// 提交更新（替换为updateRecipe接口）
const submitRecipe = async () => {
  try {
    await recipeFormRef.value.validate()
    submitting.value = true

    // 数据清洗（和创建页完全一致）
    const finalTagList = recipeForm.tagList
        .map(tag => tag.trim())
        .filter(tag => tag)
        .slice(0, 5)

    const finalIngredients = recipeForm.ingredients
        .filter(item => item.name.trim())
        .map((item, index) => ({
          name: item.name.trim(),
          quantity: Number(item.quantity) || 0,
          unit: item.unit.trim() || '',
          note: item.note.trim() || '',
          sortOrder: index
        }))

    const finalSteps = recipeForm.steps
        .filter(item => item.description.trim())
        .map((item, index) => ({
          stepNumber: index + 1,
          description: item.description.trim(),
          imageUrl: item.imageUrl.trim() || '',
          timerMinutes: Number(item.timerMinutes) || 0,
          sortOrder: index
        }))

    // 构造提交数据 - 与创建页面保持完全一致的格式
    const submitData = {
      id: Number(recipeId.value), // 更新页面特有字段
      title: recipeForm.title.trim(),
      description: recipeForm.description.trim(),
      coverImage: recipeForm.coverImage?.trim() || '',
      prepTime: Number(recipeForm.prepTime) || 0,
      cookTime: Number(recipeForm.cookTime) || 1,
      servings: Number(recipeForm.servings) || 1,
      difficulty: recipeForm.difficulty,
      category: recipeForm.category.trim(),
      tagList: finalTagList,
      isPrivate: recipeForm.isPrivate,
      ingredients: finalIngredients,
      steps: finalSteps,
      userId: userStore.userInfo.id
    }

    // 详细的调试信息
    console.log('📤 更新页面提交数据详情:', JSON.stringify({
      基本信息: {
        recipeId: recipeId.value,
        userId: userStore.userInfo.id
      },
      表单数据: {
        标题: recipeForm.title,
        描述: recipeForm.description,
        分类: recipeForm.category,
        难度: recipeForm.difficulty
      },
      数据结构: {
        ingredients类型: typeof finalIngredients,
        ingredients是数组: Array.isArray(finalIngredients),
        ingredients数量: finalIngredients.length,
        ingredients内容: finalIngredients,
        steps类型: typeof finalSteps,
        steps是数组: Array.isArray(finalSteps),
        steps数量: finalSteps.length
      }
    }, null, 2))

    await ElMessageBox.confirm(
        '确认更新该食谱吗？更新后将覆盖原有数据！',
        '更新确认',
        { type: 'warning' }
    )

    if (!userStore.userInfo.id) {
      throw new Error('用户未登录，请重新登录！')
    }

    console.log('🚀 开始调用 updateRecipe API...')
    const response = await updateRecipe(submitData)
    console.log('✅ updateRecipe 响应:', response)

    ElMessage.success('食谱更新成功！')
    router.push(`/home/recipe-detail/${recipeId.value}`)
  } catch (error) {
    if (error === 'cancel') return

    console.error('❌ 更新食谱失败详情:', {
      错误信息: error.message,
      错误代码: error.code,
      状态码: error.response?.status,
      状态文本: error.response?.statusText,
      响应数据: error.response?.data,
      请求URL: error.config?.url,
      请求方法: error.config?.method,
      请求参数: error.config?.params,
      请求数据: error.config?.data
    })

    let errMsg = '更新食谱失败：服务器异常，请重试！'
    if (error.message) {
      errMsg = `更新食谱失败：${error.message}`
    }
    // 特别处理 400 错误
    if (error.response?.status === 400) {
      const serverError = error.response?.data?.message || error.response?.data?.error
      errMsg = `数据格式错误：${serverError || '请检查数据格式是否符合要求'}`
    }

    ElMessage.error(errMsg)
  } finally {
    submitting.value = false
  }
}

// 重置表单（恢复原始数据）
const resetForm = () => {
  ElMessageBox.confirm(
      '确认要恢复原始数据吗？所有修改将被丢弃！',
      '重置确认',
      { type: 'warning' }
  ).then(() => {
    loadRecipeDetail() // 重新加载原始数据
    tagInputValue.value = ''
    ElMessage.success('已恢复原始数据')
  }).catch(() => {
    ElMessage.info('已取消重置')
  })
}

// 挂载时加载详情
onMounted(() => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录！')
    router.push('/login')
    return
  }
  loadRecipeDetail()
})
</script>

<style scoped lang="scss">
.recipe-edit { // 仅修改根类名
  padding: 20px;

  .recipe-form {
    .avatar-uploader {
      // 封面图预览容器
      .preview-wrapper {
        position: relative;
        display: inline-block;

        .avatar {
          width: 150px;
          height: 150px;
          display: block;
          object-fit: cover;
          border-radius: 4px;
        }

        // 封面图删除按钮
        .delete-btn {
          position: absolute;
          top: -8px;
          right: -8px;
          background: rgba(255, 255, 255, 0.9);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          z-index: 10;
          cursor: pointer;
        }
      }

      .upload-icon {
        width: 150px;
        height: 150px;
        border: 1px dashed #d9d9d9;
        border-radius: 4px;
        background-color: #fafafa;
        text-align: center;
        cursor: pointer;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;

        .text {
          margin-top: 8px;
          font-size: 14px;
          color: #999;
        }
      }
    }

    .upload-tip {
      margin-top: 8px;
      font-size: 12px;
      color: #999;
    }

    .step-upload {
      // 步骤图预览容器
      .preview-wrapper {
        position: relative;
        display: inline-block;

        .step-image {
          width: 80px;
          height: 80px;
          object-fit: cover;
          border-radius: 4px;
        }

        // 步骤图删除按钮
        .delete-btn {
          position: absolute;
          top: -8px;
          right: -8px;
          background: rgba(255, 255, 255, 0.9);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          z-index: 10;
          cursor: pointer;
        }
      }

      .upload-btn {
        width: 80px;
        height: 80px;
        display: flex;
        justify-content: center;
        align-items: center;
        border: 1px dashed #d9d9d9;
        border-radius: 4px;
        background-color: #fafafa;
      }
    }

    .new-row {
      background-color: #f9f9f9;
    }

    .form-submit {
      text-align: center;
      margin-top: 20px;
    }

    .tag-tip {
      line-height: 1.2;
    }
  }
}
</style>
