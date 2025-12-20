// src/utils/request.js - 适配后端Result结构体
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建Axios实例（后端默认端口8080）
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    timeout: 5000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
})

// 请求拦截器：添加token（若后端用JWT校验）
service.interceptors.request.use(
    (config) => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers['Authorization'] = `Bearer ${userStore.token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器：解析后端Result格式
service.interceptors.response.use(
    (response) => {
        const res = response.data
        // 后端Result的code=200为成功
        if (res.code !== 200) {
            ElMessage.error(res.msg || '操作失败')
            return Promise.reject(res)
        }
        // 成功返回data字段（前端直接用数据）
        return res.data
    },
    (error) => {
        const errMsg = error.response?.data?.msg || '服务器错误'
        ElMessage.error(errMsg)
        return Promise.reject(error)
    }
)

export default service
