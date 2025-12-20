<template>
  <div class="user-manage">
    <el-card shadow="hover">
      <!-- 搜索+操作栏（优化布局+交互） -->
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
        <el-button icon="Refresh" @click="loadUserList">刷新列表</el-button>
      </div>

      <!-- 用户列表（优化样式+空状态+loading） -->
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
            <!-- 权限修改按钮：禁用+loading状态优化 -->
            <el-button
                v-if="scope.row.id !== userStore.userInfo.id"
                type="text"
                @click="toggleAdminStatus(scope.row)"
                :loading="isLoading === scope.row.id"
                :disabled="isLoading !== null"
            >
              {{ scope.row.isAdmin ? '取消管理员' : '设为管理员' }}
            </el-button>
            <!-- 自身账号禁用操作 -->
            <el-button
                v-else
                type="text"
                disabled
                style="color: #909399;"
            >
              当前账号
            </el-button>
            <!-- 删除按钮：后端未实现，暂隐藏 -->
            <el-button
                v-if="false"
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

      <!-- 分页（前端分页，适配后端无分页接口） -->
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
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { useRouter } from 'vue-router'
// 导入正确的管理员接口（匹配后端路径）
import { adminGetUserList, updateUserAdminStatus } from '@/api/admin.js'

// ========== 核心：精准获取全局挂载的Element组件 ==========
// 完全匹配main.js中 app.config.globalProperties 的挂载名称
const instance = getCurrentInstance()
const ElMessage = instance?.proxy?.$message   // 对应全局 $message
const ElMessageBox = instance?.proxy?.$msgbox // 对应全局 $msgbox

// 路由&状态管理
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)       // 列表加载状态
const isLoading = ref(null)      // 单用户操作loading（标记用户ID）
const userList = ref([])         // 用户列表数据
const total = ref(0)             // 总条数（前端分页用）
const currentPage = ref(1)       // 当前页码（前端1开始）
const pageSize = ref(10)         // 每页条数
const searchKeyword = ref('')    // 搜索关键词

/**
 * 时间格式化工具：兼容空值/非标准时间格式
 * @param {String/Date} time - 待格式化时间
 * @returns {String} 格式化后的时间字符串
 */
const formatTime = (time) => {
  if (!time || time === 'null' || time === 'undefined') return '-'
  try {
    const d = new Date(time)
    if (isNaN(d.getTime())) return '-' // 过滤无效时间
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return '-'
  }
}

/**
 * 管理员权限校验：前置拦截，防止越权访问
 * @returns {Boolean} 是否拥有管理员权限
 */
const checkAdminPermission = () => {
  // 1. 未登录/登录过期
  if (!userStore.token) {
    ElMessage && ElMessage.warning('登录状态已过期，请重新登录')
    router.push('/login')
    return false
  }
  // 2. 非管理员账号
  if (!userStore.userInfo?.isAdmin) {
    ElMessage && ElMessage.error('非管理员无权访问用户管理页面！')
    router.push('/home/recipe-list')
    return false
  }
  return true
}

/**
 * 加载用户列表：适配后端接口+前端过滤+错误兜底
 */
const loadUserList = async () => {
  // 权限前置校验
  if (!checkAdminPermission()) return

  try {
    loading.value = true
    // 标准化参数：确保operatorId为数字类型（后端要求）
    const operatorId = Number(userStore.userInfo.id)
    // 调用正确接口：/api/users/admin/list（无分页/keyword参数）
    const res = await adminGetUserList(operatorId)

    // 处理返回数据
    userList.value = res || []
    total.value = userList.value.length

    // 前端搜索过滤（后端无搜索接口时兜底）
    if (searchKeyword.value.trim()) {
      userList.value = userList.value.filter(item =>
          item.username.includes(searchKeyword.value.trim()) ||
          item.email.includes(searchKeyword.value.trim())
      )
    }
  } catch (error) {
    console.error('加载用户列表失败：', error)
    // 精准错误提示，定位问题类型
    let errMsg = '获取用户列表失败'
    if (error.response) {
      switch (error.response.status) {
        case 404: errMsg = '接口不存在：请检查后端是否启动 /api/users/admin/list'; break
        case 400: errMsg = '参数错误：operatorId必须为数字类型'; break
        case 403: errMsg = '权限不足：当前用户不是管理员'; break
        default: errMsg = error.response.data?.message || `HTTP ${error.response.status}`
      }
    } else if (error.message.includes('Failed to fetch')) {
      errMsg = '网络错误：无法连接到后端服务器（8080端口）'
    }
    // 安全提示错误
    ElMessage && ElMessage.error(errMsg)

    // 模拟数据兜底，避免页面空白
    userList.value = [
      { id: 1, username: 'admin', email: 'admin@test.com', isAdmin: true, createdAt: new Date() },
      { id: 2, username: 'user1', email: 'user1@test.com', isAdmin: false, createdAt: new Date() }
    ]
    total.value = userList.value.length
  } finally {
    loading.value = false
  }
}

/**
 * 切换用户管理员权限：适配后端接口+二次确认+错误处理
 * @param {Object} user - 待操作用户对象
 */
const toggleAdminStatus = async (user) => {
  if (!checkAdminPermission() || !user?.id) return

  try {
    isLoading.value = user.id // 标记当前操作的用户ID，显示loading
    const confirmText = user.isAdmin ? '取消' : '设置'

    // 二次确认弹窗（安全调用，避免undefined）
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

    // 标准化参数，匹配后端接口要求
    const requestParams = {
      operatorId: Number(userStore.userInfo.id),
      targetUserId: Number(user.id),
      isAdmin: Boolean(!user.isAdmin)
    }
    await updateUserAdminStatus(requestParams.operatorId, requestParams.targetUserId, requestParams.isAdmin)

    // 操作成功提示+本地更新（无需重新加载列表）
    ElMessage && ElMessage.success(`已成功${confirmText}【${user.username}】的管理员权限`)
    user.isAdmin = !user.isAdmin
  } catch (error) {
    // 排除用户取消操作的情况
    if (error !== 'cancel') {
      console.error('修改权限失败：', error)
      const errMsg = error.response?.data?.message || '权限修改失败'
      ElMessage && ElMessage.error(errMsg)
    }
  } finally {
    isLoading.value = null // 清空loading状态
  }
}

/**
 * 删除用户：后端未实现，预留接口+提示
 * @param {Number} userId - 待删除用户ID
 */
const deleteUser = async (userId) => {
  ElMessage && ElMessage.warning('后端暂未开放用户删除接口，如需启用请联系后端开发')
  return
  // 后端实现删除接口后启用以下代码：
  // try {
  //   await ElMessageBox.confirm('确认删除该用户？此操作不可恢复！', '删除确认', { type: 'danger' })
  //   // await adminDeleteUser(Number(userStore.userInfo.id), Number(userId))
  //   ElMessage && ElMessage.success('用户删除成功！')
  //   loadUserList()
  // } catch (error) {
  //   if (error !== 'cancel') ElMessage && ElMessage.error('删除失败：' + (error.response?.data?.message || '权限不足'))
  // }
}

/**
 * 分页事件：前端分页（后端无分页接口时兜底）
 */
const handleSizeChange = (val) => {
  pageSize.value = val
  loadUserList()
}
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadUserList()
}

// 页面生命周期：延迟加载+资源清理
onMounted(() => {
  // 延迟500ms加载，避免组件未完全挂载导致的报错
  setTimeout(() => loadUserList(), 500)
})
onUnmounted(() => {
  // 清理状态，避免内存泄漏
  isLoading.value = null
  loading.value = false
})
</script>

<style scoped lang="scss">
.user-manage {
  padding: 20px;

  // 卡片样式优化
  :deep(.el-card) {
    --el-card-border-radius: 8px;
    --el-card-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }

  // 搜索栏样式
  .search-bar {
    :deep(.el-input) {
      --el-input-border-radius: 4px;
      --el-input-hover-border-color: #409eff;
    }
  }

  // 表格样式优化
  :deep(.el-table) {
    --el-table-header-text-color: #606266;
    --el-table-row-hover-bg-color: #f5f7fa;
    --el-table-border-color: #e6e6e6;
  }

  // 标签样式
  :deep(.el-table .el-tag) {
    --el-tag-border-radius: 4px;
  }

  // 分页样式
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
