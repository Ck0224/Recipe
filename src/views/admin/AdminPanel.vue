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

      <!-- 标签页（loading联动+禁用状态） -->
      <el-tabs
          v-model="activeTab"
          type="card"
          @tab-change="handleTabChange"
          style="margin-bottom: 15px;"
          :disabled="loading"
      >
        <el-tab-pane label="用户管理" name="user-manage"></el-tab-pane>
        <el-tab-pane label="食谱管理" name="recipe-audit"></el-tab-pane>
        <el-tab-pane label="系统日志" name="system-log" disabled></el-tab-pane>
      </el-tabs>

      <!-- 嵌套路由出口（强制刷新+样式兜底） -->
      <div style="min-height: 500px; width: 100%;" v-loading="loading">
        <router-view :key="route.fullPath"></router-view>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 响应式数据
const activeTab = ref('user-manage')
const loading = ref(false)
let tabChangeTimer = null

/**
 * 路由监听：精准同步标签页，避免闪烁
 */
watch(
    () => route.fullPath,
    async (newPath) => {
      await nextTick()
      // 精准匹配子路由，同步标签页状态
      if (newPath.includes('/home/admin/user-manage')) {
        activeTab.value = 'user-manage'
      } else if (newPath.includes('/home/admin/recipe-audit')) {
        activeTab.value = 'recipe-audit'
      }
      // 仅路由切换时触发loading，排除重复触发
      if (newPath.startsWith('/home/admin/') && !loading.value) {
        loading.value = true
        setTimeout(() => loading.value = false, 300)
      }
    },
    { immediate: true, deep: true }
)

/**
 * 标签页切换：防抖+防重复跳转+安全错误提示
 */
const handleTabChange = (tabName) => {
  if (tabName === 'system-log' || loading.value) return

  clearTimeout(tabChangeTimer)
  tabChangeTimer = setTimeout(() => {
    const targetPath = `/home/admin/${tabName}`
    if (route.fullPath !== targetPath) {
      router.push({ path: targetPath, replace: true }).catch(err => {
        if (err.name !== 'NavigationDuplicated') {
          console.error('标签页跳转失败：', err)
          ElMessage.error('页面切换失败，请重试')
        }
      })
    }
  }, 50)
}

/**
 * 挂载/卸载：仅保留基础逻辑，删除权限校验
 */
onMounted(() => {
  // 仅显示欢迎提示，无权限校验
  setTimeout(() => {
    ElMessage.success('欢迎进入管理员控制面板！', { duration: 2000 })
  }, 1000)
})

onUnmounted(() => {
  // 清理定时器
  if (tabChangeTimer) clearTimeout(tabChangeTimer)
  // 重置响应式数据
  loading.value = false
  activeTab.value = 'user-manage'
})
</script>

<style scoped lang="scss">
.admin-panel {
  padding: 20px;
  height: 100vh;
  box-sizing: border-box;
  overflow: auto;
  background-color: #f5f7fa;

  :deep(.el-card) {
    --el-card-border-radius: 8px;
    height: 100%;
    display: flex;
    flex-direction: column;
    border: none;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }

  .admin-header {
    flex-shrink: 0;
  }

  :deep(.el-tabs) {
    flex-shrink: 0;
    :deep(.el-tabs__header) {
      margin: 0 0 10px 0;
      :deep(.el-tabs__nav-wrap) {
        border-bottom: 1px solid #e6e6e6;
      }
    }
    :deep(.el-tabs__content) {
      display: none; // 隐藏默认内容，使用router-view
    }
  }

  // loading样式兜底，避免布局错乱
  :deep(.el-loading-mask) {
    background-color: rgba(255, 255, 255, 0.8);
    min-height: 500px;
  }
}
</style>
