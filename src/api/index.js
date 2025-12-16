import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 5000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
})

// 请求拦截器
service.interceptors.request.use(
    (config) => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器（✅ 适配后端 ResponseEntity<Result> 结构）
service.interceptors.response.use(
    (response) => {
        // 后端返回的完整响应：response.data 是 Result 对象
        const res = response.data
        if (res.code === 200) {
            return res.data // 返回Result中的data字段（token+userInfo）
        } else {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
    },
    (error) => {
        const status = error.response?.status
        const userStore = useUserStore()

        if (status === 401) {
            userStore.logout()
            router.push('/login')
            ElMessage.error('登录已过期，请重新登录')
        } else if (status === 404) {
            ElMessage.error('接口不存在：' + error.response.config.url)
        } else {
            ElMessage.error(error.message || '服务器错误')
        }
        return Promise.reject(error)
    }
)

export default service
