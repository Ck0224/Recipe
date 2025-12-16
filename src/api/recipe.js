import service from './index'

// 分页查询食谱（✅ 正确路径：/api/recipes）
export function getRecipeList(params) {
    return service({
        url: '/api/recipes',
        method: 'get',
        params // page/size/category/difficulty
    })
}

// 根据ID查询食谱（✅ 正确路径：/api/recipes/{id}）
export function getRecipeDetail(id) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'get'
    })
}

// 创建食谱（✅ 正确路径：/api/recipes）
export function createRecipe(data, userId) {
    return service({
        url: '/api/recipes',
        method: 'post',
        params: { userId },
        data
    })
}

// 更新食谱（✅ 正确路径：/api/recipes/{id}）
export function updateRecipe(id, data, userId) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'put',
        params: { userId },
        data
    })
}

// 删除食谱（✅ 正确路径：/api/recipes/{id}）
export function deleteRecipe(id, userId) {
    return service({
        url: `/api/recipes/${id}`,
        method: 'delete',
        params: { userId }
    })
}

// 图片上传（需确认后端路径，示例：/api/upload/image）
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
