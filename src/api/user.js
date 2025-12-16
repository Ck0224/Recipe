import service from './index'

// 登录接口（✅ 正确路径：/api/user/login）
export function login(data) {
    return service({
        url: '/api/user/login',
        method: 'post',
        data
    })
}

// 注册接口（✅ 正确路径：/api/user/register）
export function register(data) {
    return service({
        url: '/api/user/register',
        method: 'post',
        data
    })
}
