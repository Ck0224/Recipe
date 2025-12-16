<template>
  <div class="user-manage">
    <el-card shadow="hover">
      <!-- 搜索+操作栏（优化布局） -->
      <div class="search-bar" style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <el-input
            v-model="searchKeyword"
            placeholder="请输入用户名/邮箱搜索"
            style="width: 300px;"
            @keyup.enter="loadUserList"
        >
          <template #append>
            <el-button @click="loadUserList" icon="Search"></el-button>
          </template>
        </el-input>
        <!-- 新增：刷新按钮 -->
        <el-button icon="Refresh" @click="loadUserList">刷新列表</el-button>
      </div>

      <!-- 用户列表（优化样式+空状态） -->
      <el-table
          v-loading="loading"
          :data="userList"
          border
          stripe
          style="width: 100%;"
          empty-text="暂无用户数据，请检查搜索条件或联系管理员"
      >
        <el-table-column prop="id" label="用户ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column
            prop="isAdmin"
            label="角色"
            width="100"
            align="center"
        >
          <template #default="scope">
            <el-tag :type="scope.row.isAdmin ? 'danger' : 'success'">
              {{ scope.row.isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            prop="createdAt"
            label="注册时间"
            min-width="180"
            align="center"
        >
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <!-- 权限修改按钮：禁用状态优化 -->
            <el-button
                v-if="scope.row.id !== userStore.userInfo.id"
                type="text"
                @click="toggleAdminStatus(scope.row)"
                :loading="isLoading === scope.row.id"
                :disabled="isLoading !== null"
            >
              {{ scope.row.isAdmin ? '取消管理员' : '设为管理员' }}
            </el-button>
            <!-- 自己的账号显示“当前账号” -->
            <el-button
                v-else
                type="text"
                disabled
                style="color: #909399;"
            >
              当前账号
            </el-button>
            <!-- 删除按钮：增加加载禁用 -->
            <el-button
                v-if="scope.row.id !== userStore.userInfo.id"
                type="text"
                danger
                @click="deleteUser(scope.row.id)"
                :disabled="isLoading !== null"
                style="margin-left: 10px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页（优化显示逻辑） -->
      <el-pagination
          v-if="total > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top: 20px; text-align: right;"
          background
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user.js'
import { useRouter } from 'vue-router'
import { adminGetUserList, adminUpdateUserAdminStatus, adminDeleteUser } from '@/api/user.js'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false) // 列表加载状态
const isLoading = ref(null) // 权限修改/删除加载状态（标记当前操作的用户ID）
const userList = ref([]) // 用户列表
const total = ref(0) // 总条数
const currentPage = ref(1) // 当前页
const pageSize = ref(10) // 每页条数
const searchKeyword = ref('') // 搜索关键词

/**
 * 时间格式化（兼容空值/非标准时间格式）
 */
const formatTime = (time) => {
  if (!time || time === 'null' || time === 'undefined') return '-'
  try {
    const d = new Date(time)
    if (isNaN(d.getTime())) return '-' // 无效时间
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hour = String(d.getHours()).padStart(2, '0')
    const minute = String(d.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
  } catch (e) {
    return '-'
  }
}

/**
 * 校验管理员权限（前置拦截）
 */
const checkAdminPermission = () => {
  if (!userStore.token || !userStore.userInfo?.isAdmin) {
    ElMessage.error('非管理员无权访问用户管理页面！')
    router.push('/home/recipe-list')
    return false
  }
  return true
}

/**
 * 加载用户列表（核心：解决400错误+参数标准化）
 */
const loadUserList = async () => {
  // 前置权限校验
  if (!checkAdminPermission()) return

  try {
    loading.value = true
    // 标准化请求参数（解决400核心：参数类型/格式匹配后端）
    const params = {
      operatorId: Number(userStore.userInfo.id), // 确保是数字类型
      isAdmin: undefined,
      page: Number(currentPage.value - 1), // 后端分页通常从0开始
      size: Number(pageSize.value),
      keyword: searchKeyword.value.trim() || undefined
    }
    const res = await adminGetUserList(params)
    // 兼容多种后端返回格式
    const responseData = res.data || res
    userList.value = responseData.list || responseData.content || responseData || []
    total.value = responseData.total || responseData.totalElements || userList.value.length
  } catch (error) {
    console.error('加载用户列表失败', error)
    // 精准定位400错误原因
    let errMsg = '获取用户列表失败'
    if (error.response) {
      errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
      // 常见400错误提示
      if (error.response.status === 400) {
        errMsg = `参数错误：${error.response.data?.message || '请检查管理员ID是否有效'}`
      } else if (error.response.status === 403) {
        errMsg = '权限不足：当前管理员账号无操作权限'
      } else if (error.response.status === 404) {
        errMsg = '接口不存在：请检查adminGetUserList接口路径'
      }
    } else if (error.message.includes('Failed to fetch')) {
      errMsg = '网络错误：无法连接到服务器'
    }
    ElMessage.error(errMsg)
  } finally {
    loading.value = false
  }
}

/**
 * 切换用户管理员权限（解决400错误+强化校验）
 */
const toggleAdminStatus = async (user) => {
  // 前置校验
  if (!checkAdminPermission() || !user?.id) return

  try {
    isLoading.value = user.id // 标记当前操作的用户ID
    // 二次确认（强化交互）
    const confirmText = user.isAdmin ? '取消' : '设置'
    await ElMessageBox.confirm(
        `确认要${confirmText}【${user.username}】的管理员权限吗？`,
        '权限修改确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          draggable: true
        }
    )
    // 标准化参数（解决400核心）
    const requestData = {
      operatorId: Number(userStore.userInfo.id), // 数字类型
      targetUserId: Number(user.id), // 数字类型
      isAdmin: Boolean(!user.isAdmin) // 布尔类型
    }
    await adminUpdateUserAdminStatus(requestData)
    ElMessage.success(`已成功${confirmText}【${user.username}】的管理员权限`)
    // 本地更新（无需重新加载列表）
    user.isAdmin = !user.isAdmin
  } catch (error) {
    if (error !== 'cancel') { // 排除取消操作
      console.error('修改权限失败', error)
      let errMsg = '权限修改失败'
      if (error.response) {
        errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
        if (error.response.status === 400) {
          errMsg = `参数错误：${error.response.data?.message || '管理员ID/用户ID格式错误'}`
        } else if (error.response.status === 403) {
          errMsg = '权限不足：无法修改该用户的管理员权限'
        }
      }
      ElMessage.error(errMsg)
    }
  } finally {
    isLoading.value = null // 清空加载状态
  }
}

/**
 * 删除用户（解决400错误+强化校验）
 */
const deleteUser = async (userId) => {
  // 前置校验
  if (!checkAdminPermission() || !userId) return

  try {
    await ElMessageBox.confirm(
        '确认要删除该用户吗？此操作不可恢复，且会删除该用户的所有食谱数据！',
        '删除用户确认',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'danger',
          draggable: true
        }
    )
    // 标准化参数
    const requestData = {
      operatorId: Number(userStore.userInfo.id),
      targetUserId: Number(userId)
    }
    await adminDeleteUser(requestData)
    ElMessage.success('用户删除成功！')
    loadUserList() // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败', error)
      let errMsg = '用户删除失败'
      if (error.response) {
        errMsg = error.response.data?.message || `HTTP ${error.response.status}：${error.response.statusText}`
        if (error.response.status === 400) {
          errMsg = `参数错误：${error.response.data?.message || '管理员ID/用户ID格式错误'}`
        } else if (error.response.status === 403) {
          errMsg = '权限不足：无法删除该用户'
        } else if (error.response.status === 409) {
          errMsg = '删除失败：该用户有关联的食谱数据，请先删除食谱'
        }
      }
      ElMessage.error(errMsg)
    }
  }
}

// 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val
  loadUserList()
}
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadUserList()
}

// 页面挂载初始化
onMounted(() => {
  // 首次加载列表
  loadUserList()
})

// 页面卸载清理
onUnmounted(() => {
  isLoading.value = null
  loading.value = false
})
</script>

<style scoped lang="scss">
.user-manage {
  padding: 20px;

  :deep(.el-card) {
    --el-card-border-radius: 8px;
    --el-card-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }

  .search-bar {
    :deep(.el-input) {
      --el-input-border-radius: 4px;
      --el-input-hover-border-color: #409eff;
    }
  }

  :deep(.el-table) {
    --el-table-header-text-color: #606266;
    --el-table-row-hover-bg-color: #f5f7fa;
    --el-table-border-color: #e6e6e6;
  }

  :deep(.el-table .el-tag) {
    --el-tag-border-radius: 4px;
  }

  :deep(.el-pagination) {
    --el-pagination-text-color: #606266;
    --el-pagination-active-color: #409eff;
    --el-pagination-button-bg-color: #fff;
  }

  // 禁用按钮样式优化
  :deep(.el-button.is-disabled) {
    color: #c0c4cc !important;
    background-color: #f5f7fa !important;
    border-color: #e6e6e6 !important;
  }
}
</style>
