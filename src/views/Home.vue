<template>
  <div class="home-container">
    <!-- 侧边栏 -->
    <el-container style="height: 100vh;">
      <el-aside width="200px" class="sidebar">
        <div class="sidebar-logo">
          <h2>食谱管理系统</h2>
        </div>
        <el-menu
            :default-active="route.fullPath"
            class="sidebar-menu"
            background-color="#2e3b4e"
            text-color="#fff"
            active-text-color="#ffd04b"
            router
        >
          <!-- 1. 食谱列表 -->
          <el-menu-item index="/home/recipe-list">
            <el-icon><Menu /></el-icon>
            <template #title>食谱列表</template>
          </el-menu-item>
          <!-- 2. 创建食谱 -->
          <el-menu-item index="/home/recipe-create">
            <el-icon><Plus /></el-icon>
            <template #title>创建食谱</template>
          </el-menu-item>
          <!-- 3. 我的食谱 -->
          <el-menu-item index="/home/my-recipe">
            <el-icon><UserFilled /></el-icon>
            <template #title>我的食谱</template>
          </el-menu-item>
          <!-- 4. 个人中心 -->
          <el-menu-item index="/home/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
          <!-- 5. 管理员面板（核心优化：手动跳转+指向子路由） -->
          <el-menu-item
              index="/home/admin/user-manage"
              v-if="!!userStore.userInfo?.isAdmin"
              @click="handleAdminClick"
          >
            <el-icon><Setting /></el-icon>
            <template #title>管理员面板</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-right">
            <el-dropdown>
              <span class="user-info">
                <el-icon><User /></el-icon>
                {{ userStore.userInfo?.username || '未知用户' }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 页面内容 -->
        <el-main class="main-content">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import { Menu, Plus, User, UserFilled, Setting, SwitchButton } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 新增：管理员面板手动跳转逻辑（绕过 el-menu 路由解析 bug）
const handleAdminClick = () => {
  router.push({
    path: '/home/admin/user-manage',
    replace: true // 替换历史记录，避免路由冗余
  }).catch(err => {
    // 仅捕获非重复导航的错误
    if (err.name !== 'NavigationDuplicated') {
      console.error('管理员面板跳转失败：', err)
    }
  })
}
</script>

<style scoped lang="scss">
.home-container {
  .sidebar {
    color: #333;
    background-color: #2e3b4e;

    .sidebar-logo {
      padding: 20px;
      text-align: center;
      border-bottom: 1px solid #404e60;

      h2 {
        color: #fff;
        font-size: 18px;
        margin: 0;
      }
    }

    .sidebar-menu {
      border-right: none;
      height: calc(100% - 68px);
    }
  }

  .header {
    text-align: right;
    font-size: 12px;
    background-color: #fff;
    border-bottom: 1px solid #e6e6e6;
    padding: 0 20px;

    .header-right {
      line-height: 60px;

      .user-info {
        cursor: pointer;
        color: #666;
        margin-left: 10px;
      }
    }
  }

  .main-content {
    padding: 20px;
    background-color: #f5f7fa;
  }

  :deep(.el-menu-item) {
    color: #fff;
    &:hover {
      background-color: #1f2d3d !important;
    }
    &.is-active {
      background-color: #409eff !important;
      color: #fff !important;
    }
  }
}
</style>
