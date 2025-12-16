import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as userLogin, register as userRegister } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

    // 登录方法（✅ 适配后端返回的 {token, user} 结构）
    const login = async (loginForm) => {
        try {
            // 后端返回的data是 LoginResponse { token: "", user: {...} }
            const res = await userLogin(loginForm)
            token.value = res.token
            userInfo.value = res.user // 对应 UserSimpleDTO

            localStorage.setItem('token', res.token)
            localStorage.setItem('userInfo', JSON.stringify(res.user))
            ElMessage.success('登录成功')
            return true
        } catch (error) {
            ElMessage.error(error.message || '登录失败')
            return false
        }
    }

    // 注册方法（✅ 适配后端返回的 UserSimpleDTO）
    const register = async (registerForm) => {
        try {
            await userRegister({
                username: registerForm.username,
                email: registerForm.email,
                password: registerForm.password
            })
            ElMessage.success('注册成功，请登录')
            return true
        } catch (error) {
            ElMessage.error(error.message || '注册失败')
            return false
        }
    }

    const logout = () => {
        token.value = ''
        userInfo.value = null
        localStorage.clear()
        ElMessage.success('退出登录成功')
    }

    return { token, userInfo, login, register, logout }
})
