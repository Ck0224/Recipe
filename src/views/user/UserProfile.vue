<template>
  <div class="user-profile">
    <el-card shadow="hover">
      <!-- 头部：用户基础信息 -->
      <div class="profile-header" style="display: flex; align-items: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #e6e6e6;">
        <!-- 头像区域（移除删除按钮） -->
        <div class="avatar-wrapper" style="position: relative; margin-right: 30px;">
          <el-avatar
              :size="120"
              :src="userStore.userInfo.avatarUrl || defaultAvatar"
              style="border: 2px solid #f0f2f5;"
          >
            {{ userStore.userInfo.username?.charAt(0) || '用户' }}
          </el-avatar>

          <!-- 头像上传按钮 -->
          <el-button
              type="primary"
              size="small"
              :icon="Upload"
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
          <h2 style="margin: 0 0 10px 0;">{{ userStore.userInfo.username }}</h2>
          <el-descriptions :column="2" border style="width: 400px;">
            <el-descriptions-item label="用户ID">{{ userStore.userInfo.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="userStore.userInfo.isAdmin ? 'danger' : 'success'">
                {{ userStore.userInfo.isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userStore.userInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">
              {{ formatTime(userStore.userInfo.createdAt) || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 内容区域：修改密码 + 基本信息修改（完全保留原有功能） -->
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

        <!-- 基本信息修改标签页 -->
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
import { Upload } from '@element-plus/icons-vue' // 移除Close图标
import { useUserStore } from '@/stores/user.js'
// 仅导入真实存在的接口
import {
  getUserProfile,
  updatePassword,
  updateUserInfo,
  uploadImage
} from '@/api/user.js'

// 初始化用户状态
const userStore = useUserStore()
const avatarUploadRef = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 响应式数据
const activeTab = ref('password')
const passwordLoading = ref(false)
const infoLoading = ref(false)

// 密码修改表单（完全保留原有）
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码校验规则（完全保留原有）
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

// 基本信息表单（初始化从userStore取值）
const infoForm = reactive({
  username: userStore.userInfo.username || '',
  email: userStore.userInfo.email || ''
})

// 基本信息校验规则（完全保留原有）
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
 * 时间格式化（完全保留原有）
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
 * 加载用户信息（修复：逐个更新userInfo属性）
 */
const loadUserProfile = async () => {
  if (!userStore.userInfo?.id) {
    return ElMessage.error('用户信息异常，请重新登录')
  }
  try {
    const res = await getUserProfile(userStore.userInfo.id)
    const newUserInfo = res.data || {}

    // 逐个更新属性，避免整体赋值
    userStore.userInfo.id = newUserInfo.id || userStore.userInfo.id
    userStore.userInfo.username = newUserInfo.username || userStore.userInfo.username
    userStore.userInfo.email = newUserInfo.email || userStore.userInfo.email
    userStore.userInfo.isAdmin = newUserInfo.isAdmin || userStore.userInfo.isAdmin
    userStore.userInfo.createdAt = newUserInfo.createdAt || userStore.userInfo.createdAt
    userStore.userInfo.avatarUrl = newUserInfo.avatarUrl || userStore.userInfo.avatarUrl

    // 初始化表单
    infoForm.username = userStore.userInfo.username || ''
    infoForm.email = userStore.userInfo.email || ''
  } catch (error) {
    console.error('加载用户信息失败', error)
    ElMessage.error('加载用户信息失败：' + (error.message || '网络异常'))
  }
}

/**
 * 图片上传前校验
 */
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

/**
 * 提交密码修改（完全保留原有）
 */
const submitPassword = async () => {
  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return ElMessage.warning('请完善表单信息后提交')
  }

  if (!userStore.userInfo?.id) {
    return ElMessage.error('用户信息异常，请重新登录')
  }

  try {
    passwordLoading.value = true
    await updatePassword({
      userId: userStore.userInfo.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    userStore.logout()
    window.location.href = '/#/login'
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
  try {
    await infoFormRef.value.validate()
  } catch (error) {
    return ElMessage.warning('请完善表单信息后提交')
  }

  if (!userStore.userInfo?.id) {
    return ElMessage.error('用户信息异常，请重新登录')
  }

  try {
    infoLoading.value = true
    await updateUserInfo({
      userId: userStore.userInfo.id,
      username: infoForm.username
    })
    userStore.userInfo.username = infoForm.username
    ElMessage.success('用户名修改成功')
    await loadUserProfile()
  } catch (error) {
    console.error('修改用户信息失败', error)
    const errMsg = error.response?.data?.message || '修改失败'
    ElMessage.error(`修改用户信息失败：${errMsg}`)
  } finally {
    infoLoading.value = false
  }
}

/**
 * 处理头像上传（优化返回值处理，解决URL获取失败问题）
 */
const handleAvatarUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 上传前校验
  if (!beforeUpload(file)) {
    return
  }

  try {
    // 调用上传接口
    const res = await uploadImage(file)
    // 兼容不同的返回格式（res.data.url / res.data / res）
    let imageUrl = ''
    if (res.data?.url) {
      imageUrl = res.data.url // 常见格式1：{ url: 'xxx' }
    } else if (res.data) {
      imageUrl = res.data     // 常见格式2：直接返回URL字符串
    } else if (res) {
      imageUrl = res          // 兜底：返回整个响应
    }

    // 调试：打印返回值，方便排查问题
    console.log('上传接口返回值：', res)
    console.log('提取的图片URL：', imageUrl)

    if (!imageUrl || imageUrl.trim() === '') {
      return ElMessage.warning('上传成功，但未返回图片URL（请检查后端接口返回格式）')
    }

    // 拼接完整URL
    const fullImageUrl = imageUrl.startsWith('http')
        ? imageUrl
        : `${import.meta.env.VITE_IMAGE_BASE_URL || 'http://localhost:8080'}${imageUrl}`

    // 更新store和本地存储
    userStore.userInfo.avatarUrl = fullImageUrl
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))

    ElMessage.success('头像上传成功')
    // 清空上传控件
    avatarUploadRef.value.value = ''
  } catch (error) {
    console.error('上传头像失败', error)
    // 更详细的错误提示
    const errMsg = error.response?.data?.message
        || error.message
        || '上传失败：请检查接口地址或网络'
    ElMessage.error(`头像上传失败：${errMsg}`)
  }
}

// 页面挂载时加载用户信息
onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录！')
    window.location.href = '/#/login'
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
