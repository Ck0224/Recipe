<template>
  <div class="user-profile">
    <el-card shadow="hover">
      <!-- 头部：用户基础信息 -->
      <div class="profile-header" style="display: flex; align-items: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #e6e6e6;">
        <!-- 头像区域 -->
        <div class="avatar-wrapper" style="position: relative; margin-right: 30px;">
          <el-avatar
              :size="120"
              :src="userInfo.avatarUrl || defaultAvatar"
              style="border: 2px solid #f0f2f5;"
          >
            {{ userInfo.username?.charAt(0) || '用户' }}
          </el-avatar>
          <!-- 头像上传按钮 -->
          <el-button
              type="primary"
              size="small"
              icon="Upload"
              style="position: absolute; bottom: 0; right: 0; border-radius: 50%;"
              @click="avatarUploadRef.click()"
          ></el-button>
          <!-- 隐藏的文件上传控件 -->
          <input
              ref="avatarUploadRef"
              type="file"
              accept="image/png, image/jpeg, image/jpg"
              style="display: none;"
              @change="handleAvatarUpload"
          >
        </div>

        <!-- 用户信息 -->
        <div class="user-info">
          <h2 style="margin: 0 0 10px 0;">{{ userInfo.username }}</h2>
          <el-descriptions :column="2" border style="width: 400px;">
            <el-descriptions-item label="用户ID">{{ userInfo.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="userInfo.isAdmin ? 'danger' : 'success'">
                {{ userInfo.isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">
              {{ formatTime(userInfo.createdAt) || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 内容区域：修改密码 + 基本信息修改 -->
      <el-tabs v-model="activeTab" type="card" style="margin-top: 20px;">
        <!-- 密码修改标签页 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              style="max-width: 500px; margin: 0 auto; padding: 20px;"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入原密码"
                  show-password
              ></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码（至少6位）"
                  show-password
              ></el-input>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
              ></el-input>
            </el-form-item>
            <el-form-item style="text-align: center;">
              <el-button
                  type="primary"
                  @click="submitPassword"
                  :loading="passwordLoading"
              >
                提交修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 基本信息修改标签页（可选） -->
        <el-tab-pane label="基本信息" name="info">
          <el-form
              ref="infoFormRef"
              :model="infoForm"
              :rules="infoRules"
              label-width="100px"
              style="max-width: 500px; margin: 0 auto; padding: 20px;"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                  v-model="infoForm.username"
                  placeholder="请输入新的用户名"
              ></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input
                  v-model="infoForm.email"
                  type="email"
                  placeholder="请输入新的邮箱"
                  disabled
                  style="background-color: #f5f7fa;"
              ></el-input>
            </el-form-item>
            <el-form-item style="text-align: center;">
              <el-button
                  type="primary"
                  @click="submitInfo"
                  :loading="infoLoading"
              >
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user.js'
import { getUserProfile, updatePassword, updateUserInfo, uploadAvatar } from '@/api/user.js'

// 初始化用户状态
const userStore = useUserStore()
const avatarUploadRef = ref(null) // 头像上传DOM引用
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png' // 默认头像

// 响应式数据
const activeTab = ref('password') // 默认激活修改密码标签页
const userInfo = ref({}) // 用户详情信息
const passwordLoading = ref(false) // 密码修改加载状态
const infoLoading = ref(false) // 信息修改加载状态

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码修改表单校验规则
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 基本信息表单
const infoForm = reactive({
  username: '',
  email: ''
})

// 基本信息表单校验规则
const infoRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20位之间', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

// 表单引用
const passwordFormRef = ref(null)
const infoFormRef = ref(null)

/**
 * 时间格式化（内置实现，无需依赖外部工具）
 */
const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

/**
 * 加载用户个人信息
 */
const loadUserProfile = async () => {
  try {
    const res = await getUserProfile(userStore.userInfo.id)
    // 适配后端返回格式（res.data 是接口返回的核心数据）
    userInfo.value = res.data || res
    // 初始化基本信息表单
    infoForm.username = userInfo.value.username || ''
    infoForm.email = userInfo.value.email || ''
  } catch (error) {
    console.error('加载用户信息失败', error)
    ElMessage.error('加载用户信息失败：' + (error.message || '网络异常'))
  }
}

/**
 * 提交密码修改
 */
const submitPassword = async () => {
  // 表单校验
  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return ElMessage.warning('请完善表单信息后提交')
  }

  try {
    passwordLoading.value = true
    // 调用修改密码接口（适配新的API参数格式）
    await updatePassword({
      userId: userStore.userInfo.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    // 清空表单并退出登录
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    userStore.logout() // 调用store的logout方法（而非clearToken）
    window.location.href = '/#/login' // 适配Vue Router哈希模式
  } catch (error) {
    console.error('修改密码失败', error)
    const errMsg = error.response?.data?.message || '原密码错误或修改失败'
    ElMessage.error(`修改密码失败：${errMsg}`)
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 提交基本信息修改
 */
const submitInfo = async () => {
  // 表单校验
  try {
    await infoFormRef.value.validate()
  } catch (error) {
    return ElMessage.warning('请完善表单信息后提交')
  }

  try {
    infoLoading.value = true
    // 调用修改用户信息接口（适配新的API参数格式）
    await updateUserInfo({
      userId: userStore.userInfo.id,
      username: infoForm.username
      // email: infoForm.email // 邮箱如需修改需后端支持
    })
    ElMessage.success('用户名修改成功')
    // 更新本地用户信息
    userStore.userInfo.username = infoForm.username
    userInfo.value.username = infoForm.username
  } catch (error) {
    console.error('修改用户信息失败', error)
    const errMsg = error.response?.data?.message || '修改失败'
    ElMessage.error(`修改用户信息失败：${errMsg}`)
  } finally {
    infoLoading.value = false
  }
}

/**
 * 处理头像上传
 */
const handleAvatarUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 校验文件大小（限制2MB）
  if (file.size > 2 * 1024 * 1024) {
    return ElMessage.warning('头像文件大小不能超过2MB')
  }

  try {
    // 构建FormData
    const formData = new FormData()
    formData.append('file', file)
    formData.append('userId', userStore.userInfo.id)

    // 调用上传头像接口
    const res = await uploadAvatar(formData)
    const avatarUrl = res.data?.avatarUrl || res.avatarUrl

    // 更新本地头像信息
    userInfo.value.avatarUrl = avatarUrl
    userStore.userInfo.avatarUrl = avatarUrl
    ElMessage.success('头像上传成功')

    // 清空上传控件
    avatarUploadRef.value.value = ''
  } catch (error) {
    console.error('上传头像失败', error)
    ElMessage.error('上传头像失败：' + (error.message || '网络异常'))
  }
}

// 页面挂载时加载用户信息
onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录后再操作')
    window.location.href = '/#/login' // 适配Vue Router哈希模式
    return
  }
  loadUserProfile()
})
</script>

<style scoped lang="scss">
.user-profile {
  padding: 20px;

  :deep(.el-card) {
    --el-card-border-radius: 8px;
  }

  .profile-header {
    .avatar-wrapper {
      :deep(.el-avatar) {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: scale(1.05);
        }
      }
    }

    .user-info {
      :deep(.el-descriptions) {
        --el-descriptions-label-color: #606266;
        --el-descriptions-content-color: #303133;
      }
    }
  }

  :deep(.el-tabs) {
    --el-tabs-card-header-background-color: #f8f9fa;
    --el-tabs-active-color: #409eff;
  }

  :deep(.el-form) {
    --el-form-item-label-color: #606266;
  }
}
</style>
