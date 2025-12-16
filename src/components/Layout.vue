<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false) // 侧边栏是否折叠

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 检查是否为管理员（空值保护）
const isAdmin = () => {
  return !!userStore.userInfo?.isAdmin
}
</script>

<template>
  <div class="layout-container" style="height: 100vh; display: flex; flex-direction: column;">
    <!-- 顶部导航栏 -->
    <el-header style="text-align: right; font-size: 12px; border-bottom: 1px solid #e6e6e6;">
      <div style="float: left; font-size: 20px; font-weight: bold; color: #409eff;">
        食谱管理系统
      </div>
      <el-dropdown>
        <i class="el-icon-setting" style="margin-right: 15px"></i>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">
              <i class="el-icon-switch-button"></i> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <span>{{ userStore.userInfo?.username || '用户' }}</span>
    </el-header>

    <!-- 主体内容：侧边栏 + 主内容区 -->
    <div style="display: flex; flex: 1; overflow: hidden;">
      <!-- 侧边栏导航 -->
      <el-aside width="200px" style="background-color: #2e3b4e; color: #fff;">
        <el-menu
            default-active="/home/recipe-list"
            class="el-menu-vertical-demo"
            background-color="#2e3b4e"
            text-color="#fff"
            active-text-color="#ffd04b"
            :collapse="isCollapse"
            @select="(key) => router.push(key)"
        >
          <!-- 折叠按钮 -->
          <div style="text-align: center; padding: 10px 0;">
            <el-button
                icon="el-icon-s-fold"
                circle
                size="small"
                style="background-color: #409eff; color: #fff;"
                @click="isCollapse = !isCollapse"
            ></el-button>
          </div>

          <!-- 硬编码所有菜单（强制显示，绕过循环问题） -->
          <!-- 食谱列表 -->
          <el-menu-item index="/home/recipe-list">
            <i class="el-icon-menu"></i>
            <template #title>食谱列表</template>
          </el-menu-item>
          <!-- 创建食谱 -->
          <el-menu-item index="/home/recipe-create">
            <i class="el-icon-plus"></i>
            <template #title>创建食谱</template>
          </el-menu-item>
          <!-- 食谱搜索 -->
          <el-menu-item index="/home/recipe-search">
            <i class="el-icon-search"></i>
            <template #title>食谱搜索</template>
          </el-menu-item>
          <!-- 我的食谱 -->
          <el-menu-item index="/home/my-recipe">
            <i class="el-icon-user-filled"></i>
            <template #title>我的食谱</template>
          </el-menu-item>
          <!-- 个人中心 -->
          <el-menu-item index="/home/profile">
            <i class="el-icon-user"></i>
            <template #title>个人中心</template>
          </el-menu-item>
          <!-- 管理员面板（仅管理员显示） -->
          <el-menu-item index="/home/admin" v-if="isAdmin()">
            <i class="el-icon-setting"></i>
            <template #title>管理员面板</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main style="padding: 20px; overflow: auto; background-color: #f5f7fa;">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<style scoped>
/* 全局样式重置 */
:deep(.el-header) {
  --el-header-height: 60px;
  line-height: 60px;
}

:deep(.el-aside) {
  color: #333;
}

/* 侧边栏菜单样式优化 */
:deep(.el-menu-vertical-demo) {
  border-right: none;
  height: 100%;
}

:deep(.el-menu-item) {
  color: #fff;
  &:hover {
    background-color: #1f2d3d !important;
  }
  &.is-active {
    background-color: #1f2d3d !important;
    color: #ffd04b !important;
  }
}

/* 图标样式 */
:deep(.el-menu-item i) {
  margin-right: 8px;
}

/* 主内容区滚动条优化 */
.el-main::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.el-main::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}
.el-main::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>
