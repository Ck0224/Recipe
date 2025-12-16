import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn' // 中文语言包
// 新增：导入所有Element Plus图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from '@/router'
import App from './App.vue'
import '@/assets/styles/global.scss' // 全局样式

// 创建应用
const app = createApp(App)

// 新增：全局注册所有图标（解决图标不显示导致菜单渲染异常）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 安装插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
    locale: zhCn // 配置中文
})

// 挂载应用
app.mount('#app')
