import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
    login as userLogin,
    register as userRegister,
    getUserInfo,
    updateUserInfo as apiUpdateUserInfo,
    uploadAvatar as apiUploadAvatar
} from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
    // ========== 核心状态（新增isAdmin/avatarUrl等） ==========
    const token = ref(localStorage.getItem('token') || '')
    // 用户信息：扩展字段（isAdmin/avatarUrl/username/email等）
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

    // ========== 登录方法（优化错误处理/状态同步） ==========
    const login = async (loginForm) => {
        try {
            const res = await userLogin(loginForm)
            // 存储Token和用户信息（包含isAdmin/avatarUrl）
            token.value = res.token
            userInfo.value = res.user || {}

            // 持久化存储（避免刷新丢失）
            localStorage.setItem('token', res.token)
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

            ElMessage.success('登录成功')
            return true
        } catch (error) {
            // 清空错误状态
            token.value = ''
            userInfo.value = {}
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')

            ElMessage.error(error.message || '登录失败：账号或密码错误')
            return false
        }
    }

    // ========== 注册方法（保持原有逻辑） ==========
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
            ElMessage.error(error.message || '注册失败：该邮箱已被注册')
            return false
        }
    }

    // ========== 退出登录（优化清空逻辑） ==========
    const logout = () => {
        token.value = ''
        // 重置用户信息（保留结构，避免页面报错）
        userInfo.value = {
            id: '',
            username: '',
            email: '',
            avatarUrl: '',
            isAdmin: false,
            createdAt: ''
        }
        // 清空本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.success('退出登录成功')
    }

    // ========== 新增：刷新用户信息（用于个人中心更新后同步） ==========
    const refreshUserInfo = async () => {
        if (!token.value) return
        try {
            const res = await getUserInfo()
            userInfo.value = res.data || {}
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        } catch (error) {
            console.error('刷新用户信息失败：', error)
        }
    }

    // ========== 新增：更新用户基本信息（同步Store） ==========
    const updateUserInfo = async (infoForm) => {
        try {
            await apiUpdateUserInfo({
                userId: userInfo.value.id,
                username: infoForm.username
            })
            // 同步更新Store和本地存储
            userInfo.value.username = infoForm.username
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
            ElMessage.success('用户信息修改成功')
            return true
        } catch (error) {
            ElMessage.error(error.message || '修改失败')
            return false
        }
    }

    // ========== 新增：更新头像（同步Store） ==========
    const updateAvatar = async (formData) => {
        try {
            const res = await apiUploadAvatar(formData)
            // 同步更新头像URL
            userInfo.value.avatarUrl = res.data.avatarUrl
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
            ElMessage.success('头像上传成功')
            return res.data.avatarUrl
        } catch (error) {
            ElMessage.error(error.message || '头像上传失败')
            return null
        }
    }

    // ========== 新增：判断是否为管理员（便捷方法） ==========
    const isAdmin = () => {
        return !!userInfo.value.isAdmin
    }

    // ========== 新增：清空Token（用于密码修改后） ==========
    const clearToken = () => {
        token.value = ''
        localStorage.removeItem('token')
    }

    return {
        token,
        userInfo,
        login,
        register,
        logout,
        refreshUserInfo, // 刷新用户信息
        updateUserInfo,  // 更新用户信息
        updateAvatar,    // 更新头像
        isAdmin,         // 判断是否为管理员
        clearToken       // 清空Token
    }
})
