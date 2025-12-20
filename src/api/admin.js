// src/api/admin.js - 100%匹配你的Java后端接口
import request from '@/utils/request'

// ==================== 食谱管理（管理员） ====================
/**
 * 管理员查询所有食谱（多条件+分页）
 * 对接后端：GET /api/recipes/search/all
 * @param {Object} params
 * @param {Number} params.currentUserId - 当前登录用户ID（必传，判断管理员）
 * @param {Number} [params.id] - 食谱ID
 * @param {String} [params.title] - 菜名
 * @param {String} [params.category] - 分类
 * @param {String} [params.ingredient] - 食材
 * @param {String} [params.difficulty] - 难度
 * @param {Number} [params.page=0] - 页码（后端从0开始）
 * @param {Number} [params.size=10] - 每页条数
 * @returns {Promise}
 */
export const getAdminRecipeList = (params = {}) => {
    const queryParams = {
        currentUserId: params.currentUserId,
        id: params.id,
        title: params.title,
        category: params.category,
        ingredient: params.ingredient,
        difficulty: params.difficulty,
        page: params.page || 0,
        size: params.size || 10
    }
    return request({
        url: '/api/recipes/search/all',
        method: 'get',
        params: queryParams
    })
}

/**
 * 管理员删除食谱
 * 对接后端：DELETE /api/recipes/{id}
 * @param {Number|String} id - 食谱ID（路径参数）
 * @param {Number} currentUserId - 当前登录用户ID（必传）
 * @returns {Promise}
 */
export const deleteRecipe = (id, currentUserId) => {
    return request({
        url: `/api/recipes/${id}`,
        method: 'delete',
        params: { currentUserId }
    })
}

/**
 * 管理员查询单条食谱详情
 * 对接后端：GET /api/recipes/{id}
 * @param {Number|String} id - 食谱ID
 * @param {Number} currentUserId - 当前登录用户ID
 * @returns {Promise}
 */
export const getAdminRecipeDetail = (id, currentUserId) => {
    return request({
        url: `/api/recipes/${id}`,
        method: 'get',
        params: { currentUserId }
    })
}

// ==================== 用户管理（管理员） ====================
/**
 * 管理员查询所有用户
 * 对接后端：GET /api/users/admin/list
 * @param {Number} operatorId - 操作人ID（当前登录管理员ID，必传）
 * @param {Boolean} [isAdmin] - 筛选：是否仅查管理员
 * @returns {Promise}
 */
export const adminGetUserList = (operatorId, isAdmin) => {
    return request({
        url: '/api/users/admin/list',
        method: 'get',
        params: {
            operatorId,
            isAdmin // 可选：筛选管理员/普通用户
        }
    })
}

/**
 * 管理员修改用户权限（设置/取消管理员）
 * 对接后端：PUT /api/users/admin/{targetUserId}/status
 * @param {Number} operatorId - 操作人ID（当前登录管理员ID）
 * @param {Number} targetUserId - 被修改用户ID
 * @param {Boolean} isAdmin - 是否设为管理员
 * @returns {Promise}
 */
export const updateUserAdminStatus = (operatorId, targetUserId, isAdmin) => {
    return request({
        url: `/api/users/admin/${targetUserId}/status`,
        method: 'put',
        params: {
            operatorId,
            isAdmin
        }
    })
}

// ==================== 通用用户接口（登录/注册） ====================
/**
 * 用户登录
 * 对接后端：POST /api/users/login
 * @param {Object} data - {email, password}
 * @returns {Promise}
 */
export const userLogin = (data) => {
    return request({
        url: '/api/users/login',
        method: 'post',
        data
    })
}

/**
 * 用户注册
 * 对接后端：POST /api/users/register
 * @param {Object} data - 用户信息（username/email/password等）
 * @returns {Promise}
 */
export const userRegister = (data) => {
    return request({
        url: '/api/users/register',
        method: 'post',
        data
    })
}

/**
 * 获取用户信息
 * 对接后端：GET /api/users/{userId}
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export const getUserInfo = (userId) => {
    return request({
        url: `/api/users/${userId}`,
        method: 'get'
    })
}
