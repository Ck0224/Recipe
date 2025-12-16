import service from './index'

// ========== 核心：多条件查询食谱（兼容所有搜索场景） ==========
export function getRecipeList(params) {
    return service({
        url: '/api/recipes',
        method: 'get',
        params // 支持：id/title/category/ingredient/difficulty/page/size
    })
}

// ========== 兼容原有/search接口（内部调用多条件查询） ==========
export function searchRecipeList(params) {
    // 将keyword映射为title参数，复用多条件查询接口
    return getRecipeList({
        title: params.keyword, // 关键词→菜名
        page: params.page,
        size: params.size
    })
}

// ========== 原有基础接口（保留不变） ==========
// 根据ID查询食谱
export function getRecipeDetail(id) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'get'
    })
}

// 创建食谱
export function createRecipe(data, userId) {
    return service({
        url: '/api/recipes',
        method: 'post',
        params: { userId },
        data
    })
}

// 更新食谱
export function updateRecipe(id, data, userId) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'put',
        params: { userId },
        data
    })
}

// 删除食谱
export function deleteRecipe(id, userId) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'delete',
        params: { userId }
    })
}

// 图片上传
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
