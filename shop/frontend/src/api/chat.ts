import { api } from './index'

export interface ChatRequest {
  message: string
  sessionId: string
  userId?: number
  userType: string
  context?: string
}

export interface ChatResponse {
  messageId: string
  content: string
  sessionId: string
  confidence?: number
  isHandledByHuman?: boolean
  suggestedActions?: string
  timestamp: string
  status: string
}

// 发送聊天消息
export const sendChatMessage = (data: ChatRequest) => {
  return api.post<ChatResponse>('/chat/send', data)
}

// 创建聊天会话
export const createChatSession = (userId: number, userType: string) => {
  return api.post<string>('/chat/session', null, {
    params: { userId, userType }
  })
}

// 获取聊天历史
export const getChatHistory = (sessionId: string) => {
  return api.get<ChatResponse[]>(`/chat/history/${sessionId}`)
}

// 关闭聊天会话
export const closeChatSession = (sessionId: string) => {
  return api.delete(`/chat/session/${sessionId}`)
}

// 转人工客服
export const transferToHuman = (sessionId: string) => {
  return api.post<ChatResponse>(`/chat/transfer/${sessionId}`)
}

// 获取快捷问题
export const getQuickQuestions = () => {
  return api.get<string[]>('/chat/quick-questions')
}
