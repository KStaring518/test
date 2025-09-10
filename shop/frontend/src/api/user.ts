import { api } from './index'

export interface UserProfile {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  avatarUrl?: string
  role: string
}

export interface UpdateProfilePayload {
  nickname?: string
  phone?: string
  email?: string
  avatarUrl?: string
}

export const getProfile = () => api.get<UserProfile>('/user/profile')
export const updateProfile = (payload: UpdateProfilePayload) => api.put<UserProfile>('/user/profile', payload)

export const changePassword = (oldPassword: string, newPassword: string) => api.put('/user/password', { oldPassword, newPassword })

export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post<string>('/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}


