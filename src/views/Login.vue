<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h2>食谱管理系统</h2>
        <p>欢迎登录，开始你的美食创作之旅</p>
      </div>

      <!-- 登录/注册切换 -->
      <el-tabs v-model="activeTab" type="card" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
              :model="loginForm"
              :rules="loginRules"
              ref="loginFormRef"
              label-width="80px"
              class="login-form"
          >
            <el-form-item label="邮箱" prop="email">
              <el-input
                  v-model="loginForm.email"
                  placeholder="请输入注册的邮箱"
                  prefix-icon="User"
              ></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  show-password
              ></el-input>
            </el-form-item>
            <el-form-item class="login-btn-group">
              <el-button
                  type="primary"
                  @click="handleLogin"
                  class="login-btn"
                  :loading="loginLoading"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
              :model="registerForm"
              :rules="registerRules"
              ref="registerFormRef"
              label-width="80px"
              class="login-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  prefix-icon="UserFilled"
              ></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱"
                  prefix-icon="Message"
              ></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码（至少6位）"
                  prefix-icon="Lock"
                  show-password
              ></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  prefix-icon="Lock"
                  show-password
              ></el-input>
            </el-form-item>
            <el-form-item class="login-btn-group">
              <el-button
                  type="primary"
                  @click="handleRegister"
                  class="login-btn"
                  :loading="registerLoading"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 状态变量
const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 登录表单
const loginForm = reactive({
  email: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 登录表单校验规则
const loginRules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
})

// 注册表单校验规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20位之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 登录方法
const handleLogin = async () => {
  try {
    loginLoading.value = true
    // 表单校验
    await loginFormRef.value.validate()
    // 调用登录接口
    const success = await userStore.login(loginForm)
    if (success) {
      // 登录成功，跳转到食谱列表
      router.push('/home/recipe-list')
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loginLoading.value = false
  }
}

// 注册方法
const handleRegister = async () => {
  try {
    registerLoading.value = true
    // 表单校验
    await registerFormRef.value.validate()
    // 调用注册接口
    const success = await userStore.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })
    if (success) {
      // 注册成功，切换到登录标签
      activeTab.value = 'login'
      // 清空注册表单
      registerFormRef.value.resetFields()
    }
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  display: flex;
  justify-content: center;
  align-items: center;

  .login-card {
    width: 450px;
    padding: 30px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    border-radius: 10px;
    background-color: #fff;

    .login-header {
      text-align: center;
      margin-bottom: 20px;

      h2 {
        color: #667eea;
        margin-bottom: 5px;
      }

      p {
        color: #999;
        font-size: 14px;
      }
    }

    .login-tabs {
      .el-tabs__header {
        margin: 0 0 20px 0;
      }
    }

    .login-form {
      .login-btn-group {
        margin-top: 10px;

        .login-btn {
          width: 100%;
          height: 40px;
          background-color: #667eea;
          border: none;

          &:hover {
            background-color: #5a6edb;
          }
        }
      }
    }
  }
}
</style>
