import { api } from './index'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  phone?: string
  email?: string
}

export interface LoginResponse {
  token: string
  user: {
    id: number
    username: string
    nickname: string
    role: number
  }
}

// 用户登录
export const login = (data: LoginRequest) => {
  return api.post<LoginResponse>('/auth/login', data)
}

// 用户注册
export const register = (data: RegisterRequest) => {
  console.log('注册API调用，数据:', data)
  return api.post('/auth/register', data)
}
