<template>
  <div class="admin-panel">
    <el-card shadow="hover">
      <!-- 管理员面板头部 -->
      <div class="admin-header" style="margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #e6e6e6;">
        <h2 style="margin: 0;">管理员控制面板</h2>
        <p style="color: #909399; margin: 5px 0 0 0;">
          您可以管理所有用户、审核/修改/删除所有食谱
        </p>
      </div>

      <!-- 管理员功能标签页（优化高度+加载状态） -->
      <el-tabs
          v-model="activeTab"
          type="card"
          style="height: calc(100vh - 220px);"
          @tab-change="handleTabChange"
      >
        <!-- 用户管理标签页（增加加载状态） -->
        <el-tab-pane label="用户管理" name="user-manage">
          <div v-loading="userManageLoading" style="min-height: 500px;">
            <UserManage />
          </div>
        </el-tab-pane>
        <!-- 食谱管理标签页（增加加载状态） -->
        <el-tab-pane label="食谱管理" name="recipe-audit">
          <div v-loading="recipeAuditLoading" style="min-height: 500px;">
            <RecipeAudit />
          </div>
        </el-tab-pane>
        <!-- 可选：新增系统日志标签页（扩展） -->
        <el-tab-pane label="系统日志" name="system-log" disabled>
          <div style="display: flex; align-items: center; justify-content: center; height: 100%;">
            <el-empty description="系统日志功能待开发"></el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
// 懒加载组件（优化性能，避免首次加载过慢）
const UserManage = () => import('./UserManage.vue')
const RecipeAudit = () => import('./RecipeAudit.vue')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const activeTab = ref('user-manage') // 默认激活用户管理
const userManageLoading = ref(false) // 用户管理加载状态
const recipeAuditLoading = ref(false) // 食谱管理加载状态
let permissionCheckTimer = null // 权限检查定时器

/**
 * 标签页切换事件（加载对应组件时显示loading）
 */
const handleTabChange = (tabName) => {
  if (tabName === 'user-manage') {
    userManageLoading.value = true
    // 模拟加载延迟（实际由子组件控制）
    setTimeout(() => {
      userManageLoading.value = false
    }, 500)
  } else if (tabName === 'recipe-audit') {
    recipeAuditLoading.value = true
    setTimeout(() => {
      recipeAuditLoading.value = false
    }, 500)
  }
}

/**
 * 校验管理员权限（核心：双重校验+实时检查）
 */
const checkAdminPermission = () => {
  // 1. 未登录校验
  if (!userStore.token) {
    ElMessage.warning('登录状态已过期，请重新登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  // 2. 非管理员校验
  if (!userStore.userInfo?.isAdmin) {
    ElMessageBox.alert('您不是管理员，无权访问该页面！', '权限不足', {
      type: 'error',
      confirmButtonText: '确定'
    }).then(() => {
      router.push({ path: '/home/recipe-list' })
    })
    return false
  }
  return true
}

/**
 * 监听路由变化（防止手动修改URL访问）
 */
const handleRouteChange = () => {
  checkAdminPermission()
}

// 页面挂载时初始化
onMounted(() => {
  // 首次权限校验
  const hasPermission = checkAdminPermission()
  if (!hasPermission) return

  // 监听路由变化（防止通过URL直接访问）
  router.beforeEach(handleRouteChange)

  // 定时检查权限（防止登录态失效/管理员权限被撤销）
  permissionCheckTimer = setInterval(() => {
    checkAdminPermission()
  }, 30000) // 每30秒检查一次

  // 初始化标签页加载状态
  handleTabChange(activeTab.value)

  ElMessage.success('欢迎进入管理员控制面板！')
})

// 页面卸载时清理
onUnmounted(() => {
  // 清除定时器
  if (permissionCheckTimer) {
    clearInterval(permissionCheckTimer)
  }
  // 移除路由监听
  router.afterEach(() => {})
})
</script>

<style scoped lang="scss">
.admin-panel {
  padding: 20px;
  height: 100vh;
  box-sizing: border-box;
  overflow: hidden;

  :deep(.el-card) {
    --el-card-border-radius: 8px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .admin-header {
    flex-shrink: 0;
  }

  :deep(.el-tabs) {
    --el-tabs-card-header-background-color: #f8f9fa;
    --el-tabs-active-color: #409eff;
    --el-tabs-content-padding: 20px 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100%;

    .el-tabs__header {
      flex-shrink: 0;
      margin: 0;
    }

    .el-tabs__content {
      flex: 1;
      height: calc(100% - 40px);
      overflow: auto;
      padding: 10px;
    }
  }

  // 加载状态样式优化
  :deep(.el-loading-mask) {
    background-color: rgba(255, 255, 255, 0.8);
  }
}
</style>
