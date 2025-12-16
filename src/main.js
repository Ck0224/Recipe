import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn' // 中文语言包
import router from '@/router'
import App from './App.vue'
import '@/assets/styles/global.scss' // 全局样式

// 创建应用
const app = createApp(App)

// 安装插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
    locale: zhCn // 配置中文
})

// 挂载应用
app.mount('#app')
