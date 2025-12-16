import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 导入页面组件
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import RecipeList from '@/views/recipe/RecipeList.vue'
import RecipeCreate from '@/views/recipe/RecipeCreate.vue'
import RecipeDetail from '@/views/recipe/RecipeDetail.vue'

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
