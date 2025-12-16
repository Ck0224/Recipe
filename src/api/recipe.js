import service from './index'

// ========== 核心：多条件查询食谱（保留不变，功能正常） ==========
export function getRecipeList(params) {
    return service({
        url: '/api/recipes',
        method: 'get',
        params // 支持：id/title/category/ingredient/difficulty/page/size
    })
}

// ========== 兼容原有/search接口（保留不变） ==========
export function searchRecipeList(params) {
    // 将keyword映射为title参数，复用多条件查询接口
    return getRecipeList({
        title: params.keyword, // 关键词→菜名
        page: params.page,
        size: params.size
    })
}

// ========== 关键修复：详情接口（仅改此处，适配后端） ==========
// 根据ID查询食谱
// 根据ID查询食谱
export function getRecipeDetail(id, currentUserId) {
    // 1. 强制转换+兜底，确保ID为有效数字（核心修复）
    const recipeId = Number(id) || 0; // 转换失败则赋值0，后续由业务层提示
    // 2. 兜底currentUserId，避免NaN（核心修复）
    const userId = Number(currentUserId) || 1; // 无有效用户ID则默认1（或根据业务调整）

    // 可选：恢复校验逻辑，提前拦截无效参数（避免请求发送）
    if (isNaN(recipeId) || recipeId <= 0) {
        return Promise.reject(new Error('食谱ID必须为正整数'));
    }
    if (isNaN(userId) || userId <= 0) {
        return Promise.reject(new Error('用户ID必须为正整数'));
    }

    return service({
        // 3. 移除重复的/api（核心修复），baseURL已配置/api
        url: `/api/recipes/${recipeId}`,
        method: 'get',
        params: { currentUserId: userId }
    })
}


// ========== 创建食谱（保留不变，功能正常） ==========
export function createRecipe(data, userId) {
    return service({
        url: '/api/recipes',
        method: 'post',
        params: { userId },
        data
    })
}

// ========== 优化：更新食谱接口（保留不变） ==========
// 版本1：兼容原有参数格式（id, data, userId）
export function updateRecipe(id, data, userId) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'put',
        params: { userId },
        data
    })
}

// 版本2：新增适配编辑页的调用格式（保留不变）
export function updateRecipeV2(submitData) {
    return service({
        url: `/api/recipes/${submitData.id}`,
        method: 'put',
        params: { userId: submitData.userId },
        data: {
            title: submitData.title,
            category: submitData.category,
            difficulty: submitData.difficulty,
            cookTime: submitData.cookTime,
            ingredients: submitData.ingredients,
            steps: submitData.steps,
            isPrivate: submitData.isPrivate
        }
    })
}

// ========== 删除食谱（保留不变） ==========
export function deleteRecipe(id, userId) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'delete',
        params: { currentUserId:userId }
    })
}

// ========== 我的食谱专属接口（保留不变） ==========
// 查询当前用户的所有食谱
export function getMyRecipeList(params) {
    return service({
        url: '/api/recipes/my',
        method: 'get',
        params // 支持：title/category/isPrivate/page/size
    })
}

// ========== 图片上传（保留不变） ==========
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return service({
        url: '/api/upload/image',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
