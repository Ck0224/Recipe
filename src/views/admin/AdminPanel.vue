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

      <!-- 标签页（删除系统日志，保留用户管理/食谱管理） -->
      <el-tabs
          v-model="activeTab"
          type="card"
          @tab-change="handleTabChange"
          style="margin-bottom: 15px;"
          :disabled="loading"
      >
        <el-tab-pane label="用户管理" name="user-manage"></el-tab-pane>
        <el-tab-pane label="食谱管理" name="recipe-audit"></el-tab-pane>
      </el-tabs>

      <!-- 嵌套路由出口 -->
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
 * 路由监听：仅同步用户管理/食谱管理标签页
 */
watch(
    () => route.fullPath,
    async (newPath) => {
      await nextTick()
      // 仅匹配保留的两个标签页
      if (newPath.includes('/home/admin/user-manage')) {
        activeTab.value = 'user-manage'
      } else if (newPath.includes('/home/admin/recipe-audit')) {
        activeTab.value = 'recipe-audit'
      }
      // 路由切换时的loading逻辑不变
      if (newPath.startsWith('/home/admin/') && !loading.value) {
        loading.value = true
        setTimeout(() => loading.value = false, 300)
      }
    },
    { immediate: true, deep: true }
)

/**
 * 标签页切换：仅处理用户管理/食谱管理，无需判断系统日志
 */
const handleTabChange = (tabName) => {
  if (loading.value) return // 移除system-log的判断

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
 * 挂载/卸载逻辑不变
 */
onMounted(() => {
  setTimeout(() => {
    ElMessage.success('欢迎进入管理员控制面板！', { duration: 2000 })
  }, 1000)
})

onUnmounted(() => {
  if (tabChangeTimer) clearTimeout(tabChangeTimer)
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

  // loading样式兜底
  :deep(.el-loading-mask) {
    background-color: rgba(255, 255, 255, 0.8);
    min-height: 500px;
  }
}
</style>
