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
              rows="3"
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
            <img
                v-if="recipeForm.coverImage"
                :src="recipeForm.coverImage"
                class="avatar"
            >
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

        <el-form-item label="标签">
          <el-tag-input
              v-model="recipeForm.tagList"
              placeholder="请输入标签，回车分隔"
              maxlength="10"
          ></el-tag-input>
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
                    rows="2"
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
                  <img
                      v-if="scope.row.imageUrl"
                      :src="scope.row.imageUrl"
                      class="step-image"
                  >
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
import { ElMessage } from 'element-plus'
import { Plus, Upload, Delete } from '@element-plus/icons-vue'
import { createRecipe, uploadImage } from '@/api/recipe'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 状态变量
const recipeFormRef = ref(null)
const submitting = ref(false)

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
  ]
})

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

// 上传封面图片
const uploadCoverImage = async (options) => {
  try {
    const res = await uploadImage(options.file)
    recipeForm.coverImage = res.url
    ElMessage.success('封面图片上传成功')
  } catch (error) {
    ElMessage.error('封面图片上传失败')
    console.error('上传失败', error)
  }
}

// 上传步骤图片
const uploadStepImage = async (index, options) => {
  try {
    const res = await uploadImage(options.file)
    recipeForm.steps[index].imageUrl = res.url
    ElMessage.success('步骤图片上传成功')
  } catch (error) {
    ElMessage.error('步骤图片上传失败')
    console.error('上传失败', error)
  }
}

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
  // 重新排序
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
  // 重新排序
  recipeForm.steps.forEach((item, i) => {
    item.stepNumber = i + 1
    item.sortOrder = i
  })
}

// 提交食谱
const submitRecipe = async () => {
  try {
    // 表单校验
    await recipeFormRef.value.validate()
    submitting.value = true

    // 调用创建接口
    await createRecipe(recipeForm, userStore.userInfo.id)
    ElMessage.success('食谱创建成功！')
    // 跳转到食谱列表
    router.push('/home/recipe-list')
  } catch (error) {
    console.error('创建食谱失败', error)
    ElMessage.error('创建食谱失败，请重试！')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  recipeFormRef.value.resetFields()
  // 重置数组
  recipeForm.ingredients = [{ name: '', quantity: 0, unit: '', note: '', sortOrder: 0 }]
  recipeForm.steps = [{ stepNumber: 1, description: '', imageUrl: '', timerMinutes: 0, sortOrder: 0 }]
  recipeForm.coverImage = ''
  recipeForm.tagList = []
  recipeForm.isPrivate = false
}

onMounted(() => {
  // 初始化
})
</script>

<style scoped lang="scss">
.recipe-create {
  .recipe-form {
    .avatar-uploader {
      .avatar {
        width: 150px;
        height: 150px;
        display: block;
        object-fit: cover;
        border-radius: 4px;
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
      .step-image {
        width: 80px;
        height: 80px;
        object-fit: cover;
        border-radius: 4px;
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
  }
}
</style>
