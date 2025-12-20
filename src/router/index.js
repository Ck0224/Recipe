import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
// 关键：延迟导入Pinia，避免提前实例化
let userStore = null

// 懒加载页面组件
const Login = () => import('@/views/Login.vue')
const Home = () => import('@/views/Home.vue')
// 食谱相关
const RecipeList = () => import('@/views/recipe/RecipeList.vue')
const RecipeCreate = () => import('@/views/recipe/RecipeCreate.vue')
const RecipeDetail = () => import('@/views/recipe/RecipeDetail.vue')
const RecipeSearch = () => import('@/views/recipe/RecipeSearch.vue')
const MyRecipeList = () => import('@/views/recipe/MyRecipeList.vue')
const RecipeEdit = () => import('@/views/recipe/RecipeEdit.vue')
// 个人中心
const UserProfile = () => import('@/views/user/UserProfile.vue')
// 管理员面板
const AdminPanel = () => import('@/views/admin/AdminPanel.vue')
const UserManage = () => import('@/views/admin/UserManage.vue')
const RecipeAudit = () => import('@/views/admin/RecipeAudit.vue')

// 路由规则（保留原有结构，删除requireAdmin元信息）
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
                path: 'recipe-edit/:id',
                name: 'RecipeEdit',
                component: RecipeEdit,
                meta: {
                    title: '更新食谱 - 食谱管理系统',
                    requireAuth: true
                },
                props: true
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
            // 管理员面板（嵌套子路由，删除requireAdmin）
            {
                path: 'admin',
                component: AdminPanel,
                meta: {
                    title: '管理员面板 - 食谱管理系统',
                    requireAuth: true // 仅保留登录校验，删除管理员校验
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
                            requireAuth: true
                        }
                    },
                    {
                        path: 'recipe-audit',
                        name: 'RecipeAudit',
                        component: RecipeAudit,
                        meta: {
                            title: '食谱审核 - 管理员面板',
                            requireAuth: true
                        }
                    }
                ]
            }
        ]
    },
    // 404路由：仅对未登录用户重定向，避免管理员面板被拦截
    {
        path: '/:pathMatch(.*)*',
        redirect: (to) => {
            // 已登录用户404 → 跳首页；未登录 → 跳登录
            const isLogin = userStore?.token
            return isLogin ? '/home/recipe-list' : '/login'
        },
        meta: { requireAuth: false }
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

// 修复后的路由守卫（仅保留登录校验，删除管理员权限校验）
router.beforeEach(async (to, from, next) => {
    // 1. 延迟导入并实例化Pinia（关键修复）
    if (!userStore) {
        const { useUserStore } = await import('@/stores/user')
        userStore = useUserStore()
    }

    // 2. 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title
    }

    // 3. 获取用户状态（加兜底，避免undefined）
    const requireAuth = to.meta.requireAuth ?? true
    const isLogin = !!userStore?.token

    // 4. 基础登录校验（加兜底，避免空值）
    if (requireAuth && !isLogin) {
        ElMessage.warning('请先登录后再操作')
        next('/login')
        return
    }

    // 5. 已登录访问登录页：跳首页
    if (!requireAuth && isLogin && to.path === '/login') {
        next('/home')
        return
    }

    // 6. 直接放行（删除所有管理员权限校验逻辑）
    next()
})

export default router
