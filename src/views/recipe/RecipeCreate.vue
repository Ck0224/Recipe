<template>
  <div class="recipe-create">
    <el-card>
      <el-form
          :model="recipeForm"
          :rules="recipeRules"
          ref="recipeFormRef"
          label-width="100px"
          class="recipe-form"
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
            <!-- 封面图预览 + 删除按钮（和edit一致） -->
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

        <!-- 标签输入项 -->
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

        <!-- 食材列表 -->
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

        <!-- 步骤列表 -->
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
                  <!-- 步骤图预览 + 删除按钮（和edit一致） -->
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

        <!-- 提交按钮 -->
        <el-form-item class="form-submit">
          <el-button
              type="primary"
              size="large"
              @click="submitRecipe"
              :loading="submitting"
          >
            提交食谱
          </el-button>
          <el-button
              size="large"
              @click="resetForm"
              style="margin-left: 20px;"
          >
            重置表单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Delete, Close } from '@element-plus/icons-vue'
import { createRecipe, uploadImage } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 状态变量
const recipeFormRef = ref(null)
const submitting = ref(false)
const tagInputValue = ref('')

// 食谱表单
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

// 表单校验规则
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

// ========== 和edit完全一致的图片URL拼接方法 ==========
const getFullImageUrl = (imageUrl) => {
  // 空值兜底
  if (!imageUrl || imageUrl.trim() === '') {
    return '';
  }
  // 已拼接完整URL则直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  // 未拼接则补充服务器地址
  const baseUrl = 'http://localhost:8080';
  return `${baseUrl}${imageUrl}`;
};

// ========== 和edit完全一致的图片删除方法 ==========
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

// 添加标签
const addTag = () => {
  const tag = tagInputValue.value.trim()
  if (!tag) return ElMessage.warning('标签不能为空！')
  if (recipeForm.tagList.length >= 5) return ElMessage.warning('最多只能添加5个标签！')
  if (recipeForm.tagList.includes(tag)) return ElMessage.warning('该标签已存在！')
  if (tag.length > 10) return ElMessage.warning('标签长度不能超过10个字！')
  recipeForm.tagList.push(tag)
  tagInputValue.value = ''
}

// 删除标签
const removeTag = (index) => {
  recipeForm.tagList.splice(index, 1)
}

// 图片上传前校验
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

// ========== 和edit完全一致的图片上传方法 ==========
const uploadCoverImage = async (options) => {
  try {
    const res = await uploadImage(options.file);
    const rawImageUrl = res?.data || res || '';
    const fullImageUrl = getFullImageUrl(rawImageUrl);
    recipeForm.coverImage = fullImageUrl;
    ElMessage.success('封面图片上传成功');
  } catch (error) {
    ElMessage.error('封面图片上传失败');
    console.error('上传失败', error);
    recipeForm.coverImage = '';
  }
};

const uploadStepImage = async (index, options) => {
  try {
    const res = await uploadImage(options.file);
    const rawImageUrl = res?.data || res || '';
    const fullImageUrl = getFullImageUrl(rawImageUrl);
    recipeForm.steps[index].imageUrl = fullImageUrl;
    ElMessage.success('步骤图片上传成功');
  } catch (error) {
    ElMessage.error('步骤图片上传失败');
    console.error('上传失败', error);
    recipeForm.steps[index].imageUrl = '';
  }
};

// 添加食材
const addIngredient = () => {
  recipeForm.ingredients.push({
    name: '',
    quantity: 0,
    unit: '',
    note: '',
    sortOrder: recipeForm.ingredients.length
  })
}

// 删除食材
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

// 添加步骤
const addStep = () => {
  recipeForm.steps.push({
    stepNumber: recipeForm.steps.length + 1,
    description: '',
    imageUrl: '',
    timerMinutes: 0,
    sortOrder: recipeForm.steps.length
  })
}

// 删除步骤
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

// 提交食谱（最终修复版）
const submitRecipe = async () => {
  try {
    // 1. 表单基础校验
    await recipeFormRef.value.validate()
    submitting.value = true

    // 2. 数据清洗（核心：确保无多余字段、类型正确）
    // 标签清洗
    const finalTagList = recipeForm.tagList
        .map(tag => tag.trim())
        .filter(tag => tag)
        .slice(0, 5)

    // 食材清洗：仅保留必要字段，移除所有可能的多余字段（如recipe）
    const finalIngredients = recipeForm.ingredients
        .filter(item => item.name.trim()) // 过滤空食材
        .map((item, index) => {
          // 只保留后端需要的字段，避免多余字段干扰
          return {
            name: item.name.trim(),
            quantity: item.quantity ? Number(item.quantity) : 0, // 强制转为数字
            unit: item.unit.trim() || '',
            note: item.note.trim() || '',
            sortOrder: index // 重新排序
          }
        })

    // 步骤清洗：仅保留必要字段
    const finalSteps = recipeForm.steps
        .filter(item => item.description.trim()) // 过滤空步骤
        .map((item, index) => {
          return {
            stepNumber: index + 1, // 重新编号
            description: item.description.trim(),
            imageUrl: item.imageUrl.trim() || '',
            timerMinutes: item.timerMinutes ? Number(item.timerMinutes) : 0,
            sortOrder: index
          }
        })

    // 3. 构造最终提交数据（严格匹配后端实体）
    const submitData = {
      title: recipeForm.title.trim(),
      description: recipeForm.description.trim(),
      coverImage: recipeForm.coverImage.trim() || '',
      prepTime: Number(recipeForm.prepTime) || 0,
      cookTime: Number(recipeForm.cookTime) || 1,
      servings: Number(recipeForm.servings) || 1,
      difficulty: recipeForm.difficulty,
      category: recipeForm.category.trim(),
      tagList: finalTagList,
      isPrivate: recipeForm.isPrivate,
      ingredients: finalIngredients, // 仅传递清洗后的数组
      steps: finalSteps              // 仅传递清洗后的数组
    }

    // 调试日志（关键：确认无recipe字段）
    console.log('🚀 最终提交给后端的完整数据：', submitData)
    console.log('🥗 提交的食材（去空后）：', finalIngredients)
    console.log('📝 提交的步骤（去空后）：', finalSteps)

    // 4. 确认提交
    await ElMessageBox.confirm(
        `确认提交该食谱吗？\n- 食材数：${finalIngredients.length}条\n- 步骤数：${finalSteps.length}条`,
        '提交确认',
        { type: 'warning' }
    )

    // 5. 调用接口（确保传递userId）
    if (!userStore.userInfo.id) {
      throw new Error('用户未登录，请重新登录！')
    }
    const res = await createRecipe(submitData, userStore.userInfo.id)

    // 6. 提交成功处理
    ElMessage.success('食谱创建成功！')
    if (res?.id) {
      router.push(`/home/recipe-detail/${res.id}`)
    } else {
      router.push('/home/recipe-list')
    }

  } catch (error) {
    // 取消提交不提示错误
    if (error === 'cancel') return
    console.error('创建食谱失败', error)

    // 友好的错误提示
    let errMsg = '创建食谱失败：服务器异常，请重试！'
    if (error.message) {
      errMsg = `创建食谱失败：${error.message}`
    } else if (error.response?.data?.error) {
      errMsg = `创建食谱失败：${error.response.data.error}`
    }
    ElMessage.error(errMsg)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (recipeFormRef.value) {
    recipeFormRef.value.resetFields()
  }
  // 重置表单数据
  Object.assign(recipeForm, {
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
    ingredients: [{ name: '', quantity: 0, unit: '', note: '', sortOrder: 0 }],
    steps: [{ stepNumber: 1, description: '', imageUrl: '', timerMinutes: 0, sortOrder: 0 }]
  })
  tagInputValue.value = ''
}

onMounted(() => {
  // 初始化：确保用户已登录
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录！')
    router.push('/login')
  }
})
</script>

<style scoped lang="scss">
.recipe-create {
  padding: 20px;

  .recipe-form {
    .avatar-uploader {
      // ========== 和edit完全一致的封面图样式 ==========
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
      // ========== 和edit完全一致的步骤图样式 ==========
      .preview-wrapper {
        position: relative;
        display: inline-block;

        .step-image {
          width: 80px;
          height: 80px;
          object-fit: cover;
          border-radius: 4px;
        }

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
