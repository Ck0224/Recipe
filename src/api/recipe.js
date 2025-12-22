import service from './index'

// ========== 核心：多条件查询食谱（保留不变，恢复/api前缀） ==========
export function getRecipeList(params) {
    return service({
        url: '/api/recipes', // 恢复/api前缀
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

// ========== 关键修复：详情接口（仅保留参数校验，恢复/api前缀） ==========
// 根据ID查询食谱
export function getRecipeDetail(id, currentUserId) {
    // 1. 强制转换+兜底，确保ID为有效数字
    const recipeId = Number(id) || 0;
    // 2. 兜底currentUserId，避免NaN
    const userId = Number(currentUserId) || 1;

    // 提前拦截无效参数
    if (isNaN(recipeId) || recipeId <= 0) {
        return Promise.reject(new Error('食谱ID必须为正整数'));
    }
    if (isNaN(userId) || userId <= 0) {
        return Promise.reject(new Error('用户ID必须为正整数'));
    }

    return service({
        url: `/api/recipes/${recipeId}`, // 恢复/api前缀
        method: 'get',
        params: { currentUserId: userId }
    })
}

// ========== 创建食谱（优化参数处理，恢复/api前缀） ==========
export function createRecipe(data) {
    // 从 data 中提取 userId，兼容老调用方式
    let currentUserId = Number(data.userId);
    if (isNaN(currentUserId) && arguments.length > 1) {
        currentUserId = Number(arguments[1]);
    }

    if (isNaN(currentUserId) || currentUserId <= 0) {
        return Promise.reject(new Error('用户ID必须为正整数'));
    }

    // ========== 完全对齐 updateRecipe 的数据清洗逻辑 ==========
    const submitData = {
        // 基础字段：和 updateRecipe 保持一致的处理方式
        title: data.title?.trim() || '',
        description: data.description?.trim() || '', // 仅trim，不做额外处理（和update一致）
        coverImage: data.coverImage?.trim() || '',  // 补充coverImage，和update一致
        prepTime: Number(data.prepTime) || 0,       // 补充prepTime，和update一致
        cookTime: Number(data.cookTime) || 0,
        servings: Number(data.servings) || 1,       // 默认为1，和update一致
        difficulty: data.difficulty || '',
        category: data.category?.trim() || '',
        // 数组字段：完全复用 updateRecipe 的处理逻辑
        tagList: Array.isArray(data.tagList) ? data.tagList.map(tag => tag.trim()).filter(Boolean) : [],
        isPrivate: data.isPrivate === true || data.isPrivate === 'true',
        ingredients: Array.isArray(data.ingredients) ? data.ingredients.map(item => ({
            name: item.name?.trim() || '',
            quantity: Number(item.quantity) || 0,
            unit: item.unit?.trim() || '',
            note: item.note?.trim() || '',
            sortOrder: Number(item.sortOrder) || 0
        })) : [],
        steps: Array.isArray(data.steps) ? data.steps.map(item => ({
            stepNumber: Number(item.stepNumber) || 0,
            description: item.description?.trim() || '',
            imageUrl: item.imageUrl?.trim() || '',
            timerMinutes: Number(item.timerMinutes) || 0,
            sortOrder: Number(item.sortOrder) || 0
        })) : []
    };

    return service({
        url: '/api/recipes',
        method: 'post',
        params: { userId: currentUserId },
        data: submitData,
        headers: {
            'Content-Type': 'application/json;charset=UTF-8'
        }
    })
}



// ========== 优化：更新食谱接口（合并版本1+版本2，恢复/api前缀） ==========
// ========== 优化：更新食谱接口（匹配创建页的格式） ==========
export function updateRecipe(submitData) {
    // 参数兼容处理
    let recipeId, userId, data;

    if (typeof submitData === 'number' || typeof submitData === 'string') {
        // 老调用：updateRecipe(id, data, userId)
        recipeId = Number(submitData);
        data = arguments[1];
        userId = arguments[2];
    } else {
        // 新调用：updateRecipe(submitData) - 从对象中提取
        recipeId = Number(submitData.id);
        userId = Number(submitData.userId);
        data = submitData;
    }

    // 参数校验
    if (isNaN(recipeId) || recipeId <= 0) {
        return Promise.reject(new Error('食谱ID必须为正整数'));
    }
    if (isNaN(userId) || userId <= 0) {
        return Promise.reject(new Error('用户ID必须为正整数'));
    }

    // 数据结构转换 - 与创建页面保持一致
    const updateData = {
        title: data.title?.trim() || '',
        description: data.description?.trim() || '',
        coverImage: data.coverImage?.trim() || '',
        prepTime: Number(data.prepTime) || 0,
        cookTime: Number(data.cookTime) || 0,
        servings: Number(data.servings) || 1,
        difficulty: data.difficulty || '',
        category: data.category?.trim() || '',
        tagList: Array.isArray(data.tagList) ? data.tagList.map(tag => tag.trim()).filter(Boolean) : [],
        isPrivate: data.isPrivate === true || data.isPrivate === 'true',
        // 确保这些字段存在
        ingredients: Array.isArray(data.ingredients) ? data.ingredients.map(item => ({
            name: item.name?.trim() || '',
            quantity: Number(item.quantity) || 0,
            unit: item.unit?.trim() || '',
            note: item.note?.trim() || '',
            sortOrder: Number(item.sortOrder) || 0
        })) : [],
        steps: Array.isArray(data.steps) ? data.steps.map(item => ({
            stepNumber: Number(item.stepNumber) || 0,
            description: item.description?.trim() || '',
            imageUrl: item.imageUrl?.trim() || '',
            timerMinutes: Number(item.timerMinutes) || 0,
            sortOrder: Number(item.sortOrder) || 0
        })) : []
    };

    return service({
        url: `/api/recipes/${recipeId}`,
        method: 'put',
        params: { currentUserId: userId }, // 使用 params 传递 userId
        data: updateData,
        headers: {
            'Content-Type': 'application/json;charset=UTF-8'
        }
    })
}

// ========== 删除食谱（保留参数校验，恢复/api前缀） ==========
export function deleteRecipe(id, userId) {
    const recipeId = Number(id) || 0;
    if (isNaN(recipeId) || recipeId <= 0) {
        return Promise.reject(new Error('食谱ID必须为正整数'));
    }
    return service({
        url: `/api/recipes/${recipeId}`, // 恢复/api前缀
        method: 'delete',
        params: { currentUserId: userId }
    })
}

// ========== 我的食谱专属接口（恢复/api前缀） ==========
// 查询当前用户的所有食谱
export function getMyRecipeList(params) {
    return service({
        url: '/api/recipes/my', // 恢复/api前缀
        method: 'get',
        params // 支持：title/category/isPrivate/page/size
    })
}

// ========== 图片上传（保留不变，恢复/api前缀） ==========
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return service({
        url: '/api/upload/image', // 恢复/api前缀
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
