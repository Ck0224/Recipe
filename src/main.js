import { createApp } from 'vue'
import { createPinia } from 'pinia'
// ========== 核心修复：分开导入 ElementPlus 核心和组件 ==========
import ElementPlus from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from '@/router'
import App from './App.vue'
import '@/assets/styles/global.scss'

// 1. 创建应用实例
const app = createApp(App)

// 2. 全局注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 3. 注册Element Plus核心（带中文语言包）
app.use(ElementPlus, {
    locale: zhCn
})

// 4. 注册Pinia和路由
app.use(createPinia())
app.use(router)

// ========== 核心修复：挂载独立导出的组件 ==========
// 错误写法：ElementPlus.ElMessage（ElementPlus对象下无此属性）
// 正确写法：直接挂载导入的 ElMessage/ElMessageBox
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$msgbox = ElMessageBox
app.config.globalProperties.$alert = ElMessageBox.alert
app.config.globalProperties.$confirm = ElMessageBox.confirm

// 5. 最后挂载应用
app.mount('#app')
