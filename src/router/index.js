import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 导入页面组件
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import RecipeList from '@/views/recipe/RecipeList.vue'
import RecipeCreate from '@/views/recipe/RecipeCreate.vue'
import RecipeDetail from '@/views/recipe/RecipeDetail.vue'
import RecipeSearch from '@/views/recipe/RecipeSearch.vue' // 保留新增的搜索组件

// 路由规则
const routes = [
    {
        path: '/',
        redirect: '/login' // 默认跳转到登录页
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: { title: '登录 - 食谱管理系统' }
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: { title: '首页 - 食谱管理系统', requireAuth: true }, // 需要登录
        children: [
            {
                path: 'recipe-list',
                name: 'RecipeList',
                component: RecipeList,
                meta: { title: '食谱列表 - 食谱管理系统' }
            },
            {
                path: 'recipe-create',
                name: 'RecipeCreate',
                component: RecipeCreate,
                meta: { title: '创建食谱 - 食谱管理系统' }
            },
            {
                path: 'recipe-detail/:id',
                name: 'RecipeDetail',
                component: RecipeDetail,
                meta: { title: '食谱详情 - 食谱管理系统' }
            },
            // ========== 新增：食谱搜索路由 ==========
            {
                path: 'recipe-search', // 路由路径（和其他食谱路由风格统一）
                name: 'RecipeSearch',  // 路由名称
                component: RecipeSearch, // 绑定搜索组件
                meta: {
                    title: '食谱搜索 - 食谱管理系统', // 页面标题（和原有格式一致）
                    requireAuth: true // 继承Home的登录权限，也可省略（父级已校验）
                }
            }
        ]
    }
]

// 创建路由实例
const router = createRouter({
    history: createWebHistory(), // HTML5历史模式
    routes
})

// 路由守卫（验证登录）
router.beforeEach((to, from, next) => {
    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title
    }

    // 验证登录状态
    const userStore = useUserStore()
    if (to.meta.requireAuth && !userStore.token) {
        // 未登录，跳转到登录页
        next('/login')
    } else {
        next()
    }
})

export default router
