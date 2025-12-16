import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 懒加载页面组件
const Login = () => import('@/views/Login.vue')
const Home = () => import('@/views/Home.vue')
// 食谱相关
const RecipeList = () => import('@/views/recipe/RecipeList.vue')
const RecipeCreate = () => import('@/views/recipe/RecipeCreate.vue')
const RecipeDetail = () => import('@/views/recipe/RecipeDetail.vue')
const RecipeSearch = () => import('@/views/recipe/RecipeSearch.vue')
const MyRecipeList = () => import('@/views/recipe/MyRecipeList.vue') // 我的食谱
const RecipeEdit = () => import('@/views/recipe/RecipeEdit.vue') // 新增：食谱编辑
// 个人中心
const UserProfile = () => import('@/views/user/UserProfile.vue') // 个人中心
// 管理员面板
const AdminPanel = () => import('@/views/admin/AdminPanel.vue') // 管理员面板
const UserManage = () => import('@/views/admin/UserManage.vue') // 用户管理
const RecipeAudit = () => import('@/views/admin/RecipeAudit.vue') // 食谱审核

// 路由规则
const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: { title: '登录 - 食谱管理系统', requireAuth: false }
    },
    {
        path: '/home',
        component: Home,
        meta: { title: '首页 - 食谱管理系统', requireAuth: true },
        children: [
            // 首页默认子路由
            {
                path: '',
                name: 'Home',
                redirect: 'recipe-list'
            },

            // 食谱基础路由
            {
                path: 'recipe-list',
                name: 'RecipeList',
                component: RecipeList,
                meta: { title: '食谱列表 - 食谱管理系统', requireAuth: true }
            },
            {
                path: 'recipe-create',
                name: 'RecipeCreate',
                component: RecipeCreate,
                meta: { title: '创建食谱 - 食谱管理系统', requireAuth: true }
            },
            {
                path: 'recipe-detail/:id',
                name: 'RecipeDetail',
                component: RecipeDetail,
                meta: { title: '食谱详情 - 食谱管理系统', requireAuth: true },
                props: true
            },
            {
                path: 'recipe-edit/:id', // 新增：食谱编辑路由（带ID参数）
                name: 'RecipeEdit',
                component: RecipeEdit,
                meta: {
                    title: '更新食谱 - 食谱管理系统',
                    requireAuth: true // 仅登录可见（创建者/管理员可编辑由页面内校验）
                },
                props: true // 开启props传参，方便页面接收ID
            },
            {
                path: 'recipe-search',
                name: 'RecipeSearch',
                component: RecipeSearch,
                meta: { title: '食谱搜索 - 食谱管理系统', requireAuth: true }
            },

            // 我的食谱
            {
                path: 'my-recipe',
                name: 'MyRecipeList',
                component: MyRecipeList,
                meta: {
                    title: '我的食谱 - 食谱管理系统',
                    requireAuth: true
                }
            },

            // 个人中心
            {
                path: 'profile',
                name: 'UserProfile',
                component: UserProfile,
                meta: {
                    title: '个人中心 - 食谱管理系统',
                    requireAuth: true
                }
            },

            // 管理员面板（嵌套子路由）
            {
                path: 'admin',
                component: AdminPanel,
                meta: {
                    title: '管理员面板 - 食谱管理系统',
                    requireAuth: true,
                    requireAdmin: true
                },
                children: [
                    {
                        path: '',
                        name: 'AdminPanel',
                        redirect: 'user-manage'
                    },
                    {
                        path: 'user-manage',
                        name: 'UserManage',
                        component: UserManage,
                        meta: {
                            title: '用户管理 - 管理员面板',
                            requireAuth: true,
                            requireAdmin: true
                        }
                    },
                    {
                        path: 'recipe-audit',
                        name: 'RecipeAudit',
                        component: RecipeAudit,
                        meta: {
                            title: '食谱审核 - 管理员面板',
                            requireAuth: true,
                            requireAdmin: true
                        }
                    }
                ]
            }
        ]
    },
    // 404路由
    {
        path: '/:pathMatch(.*)*',
        redirect: '/home/recipe-list',
        meta: { requireAuth: true }
    }
]

// 创建路由实例
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior() {
        return { top: 0 }
    }
})

// 路由守卫（保持原有逻辑不变）
router.beforeEach(async (to, from, next) => {
    // 1. 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title
    }

    // 2. 获取用户状态
    const userStore = useUserStore()
    const requireAuth = to.meta.requireAuth ?? true
    const requireAdmin = to.meta.requireAdmin ?? false
    const isLogin = !!userStore.token
    const isAdmin = !!userStore.userInfo?.isAdmin

    // 3. 基础登录校验
    if (requireAuth && !isLogin) {
        ElMessage.warning('请先登录后再操作')
        next('/login')
        return
    }

    // 4. 已登录访问登录页：跳首页
    if (!requireAuth && isLogin && to.path === '/login') {
        next('/home')
        return
    }

    // 5. 管理员权限校验
    if (requireAdmin) {
        if (!isLogin) {
            ElMessage.warning('请先登录后再操作')
            next('/login')
            return
        }
        if (!isAdmin) {
            ElMessage.error('无管理员权限，无法访问该页面')
            next('/home/recipe-list')
            return
        }
    }

    // 6. 正常放行
    next()
})

export default router
