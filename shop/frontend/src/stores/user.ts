import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import type { LoginRequest, RegisterRequest } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<any>(null)

  // 登录
  const login = async (loginData: LoginRequest) => {
    try {
      const response = await loginApi(loginData)
      const { token: newToken, user: userData } = response.data
      
      token.value = newToken
      user.value = userData
      localStorage.setItem('token', newToken)
      if (userData) {
        localStorage.setItem('user', JSON.stringify(userData))
      }
      
      // 确保用户信息正确设置
      console.log('登录成功，用户信息:', userData)
      
      return response
    } catch (error) {
      throw error
    }
  }

  // 注册
  const register = async (registerData: RegisterRequest) => {
    try {
      console.log('发送注册请求:', registerData)
      const response = await registerApi(registerData)
      console.log('注册API响应:', response)
      return response
    } catch (error) {
      console.error('注册API错误:', error)
      throw error
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 检查是否已登录
  const isLoggedIn = () => {
    return !!token.value && !!user.value
  }

  // 获取用户角色
  const getUserRole = () => {
    return user.value?.role || null
  }

  // 初始化：从本地恢复登录状态
  const initUser = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    if (savedToken) token.value = savedToken
    if (savedUser) {
      try { user.value = JSON.parse(savedUser) } catch { user.value = null }
    }
  }

  return {
    token,
    user,
    login,
    register,
    logout,
    isLoggedIn,
    getUserRole,
    initUser
  }
})