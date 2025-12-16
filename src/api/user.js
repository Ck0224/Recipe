import service from './index'

// ===================== 基础用户接口 =====================
// 登录接口
export function login(data) {
    return service({
        url: '/api/users/login',
        method: 'post',
        data
    })
}

// 注册接口
export function register(data) {
    return service({
        url: '/api/users/register',
        method: 'post',
        data
    })
}

// 获取当前用户信息（登录后获取）
export function getUserInfo() {
    return service({
        url: '/api/users/info',
        method: 'get'
    })
}

// ===================== 个人中心接口 =====================
// 获取用户个人详情（匹配后端：GET /api/users/{userId}）
export function getUserProfile(userId) {
    return service({
        url: `/api/users/${userId}`,
        method: 'get'
    })
}

// 修改密码（匹配后端：PUT /api/users/{userId}/password）
export function updatePassword(data) {
    return service({
        url: `/api/users/${data.userId}/password`,
        method: 'put',
        params: { // 后端@RequestParam接收，放params里
            oldPassword: data.oldPassword,
            newPassword: data.newPassword
        }
    })
}

// 修改用户基本信息（匹配后端：PUT /api/users/{userId}）
export function updateUserInfo(data) {
    return service({
        url: `/api/users/${data.userId}`,
        method: 'put',
        data: { // 后端@RequestBody接收，放data里
            username: data.username
            // 如需修改邮箱，需后端支持：email: data.email
        }
    })
}

// 上传头像（匹配后端：PUT /api/users/{userId}/avatar）
export function uploadAvatar(formData) {
    return service({
        url: `/api/users/${formData.get('userId')}/avatar`,
        method: 'put',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// ===================== 管理员专属接口 =====================
// 管理员查询所有用户列表（匹配后端：GET /api/admin/users?operatorId={adminId}&isAdmin={boolean}）
export function adminGetUserList(params) {
    return service({
        url: '/api/admin/users', // 修正路径：去掉/list，匹配后端实际路由
        method: 'get',
        params: {
            operatorId: params.operatorId, // 管理员ID（必填，后端校验权限）
            isAdmin: params.isAdmin || false // 可选：筛选管理员/普通用户
        }
    })
}

// 管理员修改用户管理员权限（匹配后端：PUT /api/admin/users/{targetUserId}/status）
export function adminUpdateUserAdminStatus(data) {
    return service({
        url: `/api/admin/users/${data.targetUserId}/status`, // 修正路径
        method: 'put', // 改为PUT（匹配后端Restful规范）
        params: { // 后端@RequestParam接收，放params里
            operatorId: data.operatorId, // 操作的管理员ID
            isAdmin: data.isAdmin // 是否设为管理员
        }
    })
}

// 管理员删除用户（匹配后端：DELETE /api/admin/users/{targetUserId}）
export function adminDeleteUser(data) {
    return service({
        url: `/api/admin/users/${data.targetUserId}`, // 修正路径
        method: 'delete',
        params: {
            operatorId: data.operatorId // 操作的管理员ID（必填）
        }
    })
}
