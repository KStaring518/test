<template>
  <div class="chat-widget" v-show="isVisible" :style="widgetStyle">
    <!-- 拖拽区域 -->
    <div class="chat-header" @mousedown="startDrag" @touchstart="startDrag">
      <div class="chat-title">
        <span>智能客服助手</span>
      </div>
      <div class="chat-controls">
        <button class="control-btn minimize" @click="toggleMinimize" title="最小化">
          <span>─</span>
        </button>
        <button class="control-btn close" @click="closeChat" title="关闭">
          <span>×</span>
        </button>
      </div>
    </div>

    <!-- 调整大小手柄 -->
    <div class="resize-handle resize-handle-nw" @mousedown="startResize('nw')" @touchstart="startResize('nw')"></div>
    <div class="resize-handle resize-handle-ne" @mousedown="startResize('ne')" @touchstart="startResize('ne')"></div>
    <div class="resize-handle resize-handle-sw" @mousedown="startResize('sw')" @touchstart="startResize('sw')"></div>
    <div class="resize-handle resize-handle-se" @mousedown="startResize('se')" @touchstart="startResize('se')"></div>

    <!-- 聊天内容区域 -->
    <div class="chat-content" v-show="!isMinimized">
      <!-- 欢迎消息 -->
      <div class="welcome-message" v-if="messages.length === 0">
        <div class="bot-avatar">
          <span>🤖</span>
        </div>
        <div class="welcome-text">
          <p>您好！我是零食商城的智能客服助手</p>
          <p>可以帮您解答关于商品、订单、支付、退换货等问题</p>
        </div>
      </div>

      <!-- 快捷问题 -->
      <div class="quick-questions" v-if="messages.length === 0">
        <h4>常见问题:</h4>
        <div class="question-buttons">
          <button 
            v-for="question in quickQuestions" 
            :key="question"
            @click="sendQuickQuestion(question)"
            class="question-btn"
          >
            {{ question }}
          </button>
        </div>
      </div>

      <!-- 聊天消息 -->
      <div class="messages" ref="messagesContainer">
        <div 
          v-for="message in messages" 
          :key="message.id"
          :class="[
            'message', 
            message.userType === 'USER' ? 'user-message' : 'bot-message',
            message.isLoading ? 'loading' : '',
            message.isError ? 'error' : ''
          ]"
        >
          <div class="message-content" v-html="message.content"></div>
          <div class="message-time">{{ formatTime(message.createdAt) }}</div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <input 
          v-model="inputMessage" 
          @keyup.enter="sendMessage"
          placeholder="请输入您的问题..."
          class="message-input"
        />
        <button @click="sendMessage" class="send-btn">发送</button>
      </div>
    </div>

    <!-- 最小化状态显示 -->
    <div class="minimized-chat" v-show="isMinimized" @click="toggleMinimize">
      <span>💬</span>
    </div>
  </div>

  <!-- 悬浮按钮 -->
  <div class="chat-toggle" v-show="!isVisible" @click="openChat">
    <span>💬</span>
    <span class="toggle-text">智能客服</span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { createChatSession, sendChatMessage, getQuickQuestions } from '@/api/chat'
import { useUserStore } from '@/stores/user'

// 响应式数据
const isVisible = ref(false)
const isMinimized = ref(false)
const messages = ref<any[]>([])
const inputMessage = ref('')
const sessionId = ref<string | null>(null)
const quickQuestions = ref<string[]>([])
const messagesContainer = ref<HTMLElement>()

// 用户状态
const userStore = useUserStore()

// 拖拽和调整大小相关
const isDragging = ref(false)
const isResizing = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const resizeStart = ref({ x: 0, y: 0, width: 0, height: 0 })
const resizeDirection = ref('')
const widgetPosition = ref({ x: 20, y: 20 })
const widgetSize = ref({ width: 400, height: 600 })

// 计算样式
const widgetStyle = computed(() => ({
  left: `${widgetPosition.value.x}px`,
  top: `${widgetPosition.value.y}px`,
  width: `${widgetSize.value.width}px`,
  height: `${widgetSize.value.height}px`
}))

// 拖拽功能
const startDrag = (e: MouseEvent | TouchEvent) => {
  if (isResizing.value) return
  
  e.preventDefault()
  isDragging.value = true
  
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY
  
  dragStart.value = {
    x: clientX - widgetPosition.value.x,
    y: clientY - widgetPosition.value.y
  }
  
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.addEventListener('touchmove', onDragMove)
  document.addEventListener('touchend', onDragEnd)
}

const onDragMove = (e: MouseEvent | TouchEvent) => {
  if (!isDragging.value) return
  
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY
  
  const newX = clientX - dragStart.value.x
  const newY = clientY - dragStart.value.y
  
  // 限制在窗口范围内
  const maxX = window.innerWidth - widgetSize.value.width
  const maxY = window.innerHeight - widgetSize.value.height
  
  widgetPosition.value.x = Math.max(0, Math.min(newX, maxX))
  widgetPosition.value.y = Math.max(0, Math.min(newY, maxY))
}

const onDragEnd = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('touchmove', onDragMove)
  document.removeEventListener('touchend', onDragEnd)
}

// 调整大小功能
const startResize = (direction: string) => {
  isResizing.value = true
  resizeDirection.value = direction
  
  const handle = event?.target as HTMLElement
  if (handle) {
    const rect = handle.getBoundingClientRect()
    resizeStart.value = {
      x: rect.left,
      y: rect.top,
      width: widgetSize.value.width,
      height: widgetSize.value.height
    }
  }
  
  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', onResizeEnd)
  document.addEventListener('touchmove', onResizeMove)
  document.addEventListener('touchend', onResizeEnd)
}

const onResizeMove = (e: MouseEvent | TouchEvent) => {
  if (!isResizing.value) return
  
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY
  
  const deltaX = clientX - resizeStart.value.x
  const deltaY = clientY - resizeStart.value.y
  
  let newWidth = resizeStart.value.width
  let newHeight = resizeStart.value.height
  let newX = widgetPosition.value.x
  let newY = widgetPosition.value.y
  
  // 根据方向调整大小和位置
  if (resizeDirection.value.includes('e')) {
    newWidth = Math.max(300, resizeStart.value.width + deltaX)
  }
  if (resizeDirection.value.includes('w')) {
    const widthChange = Math.max(300, resizeStart.value.width - deltaX)
    newWidth = widthChange
    newX = widgetPosition.value.x + (resizeStart.value.width - widthChange)
  }
  if (resizeDirection.value.includes('s')) {
    newHeight = Math.max(400, resizeStart.value.height + deltaY)
  }
  if (resizeDirection.value.includes('n')) {
    const heightChange = Math.max(400, resizeStart.value.height - deltaY)
    newHeight = heightChange
    newY = widgetPosition.value.y + (resizeStart.value.height - heightChange)
  }
  
  // 限制最小和最大尺寸
  newWidth = Math.max(300, Math.min(newWidth, window.innerWidth - 40))
  newHeight = Math.max(400, Math.min(newHeight, window.innerHeight - 40))
  
  // 限制在窗口范围内
  if (newX < 0) newX = 0
  if (newY < 0) newY = 0
  if (newX + newWidth > window.innerWidth) newX = window.innerWidth - newWidth
  if (newY + newHeight > window.innerHeight) newY = window.innerHeight - newHeight
  
  widgetSize.value = { width: newWidth, height: newHeight }
  widgetPosition.value = { x: newX, y: newY }
}

const onResizeEnd = () => {
  isResizing.value = false
  resizeDirection.value = ''
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', onResizeEnd)
  document.removeEventListener('touchmove', onResizeMove)
  document.removeEventListener('touchend', onResizeEnd)
}

// 其他功能保持不变
const openChat = () => {
  isVisible.value = true
  isMinimized.value = false
  if (!sessionId.value) {
    createNewSession()
  }
}

const closeChat = () => {
  isVisible.value = false
}

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

const createNewSession = async () => {
  try {
    const userId = userStore.user?.id || 0
    const userType = userStore.user?.role || 'USER'
    const response = await createChatSession(userId, userType)
    sessionId.value = response.data
    loadQuickQuestions()
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

const loadQuickQuestions = async () => {
  try {
    const response = await getQuickQuestions()
    quickQuestions.value = response.data || []
  } catch (error) {
    // 使用100%贴合系统的默认问题
    quickQuestions.value = [
      '如何查看我的订单？',
      '购物车商品怎么修改数量？',
      '支付宝支付失败怎么办？',
      '运费怎么计算？',
      '如何添加收货地址？',
      '商品显示"售罄"怎么办？',
      '如何评价商品？',
      '退换货需要什么条件？',
      '可以收藏商品吗？',
      '如何查看物流信息？'
    ]
  }
}

const sendQuickQuestion = (question: string) => {
  inputMessage.value = question
  sendMessage()
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !sessionId.value) return
  
  const userMessage = {
    id: Date.now(),
    content: inputMessage.value,
    userType: 'USER' as const,
    createdAt: new Date()
  }
  
  messages.value.push(userMessage)
  const messageToSend = inputMessage.value
  inputMessage.value = ''
  
  scrollToBottom()
  
  // 添加加载状态
  const loadingMessage = {
    id: Date.now() + 1,
    content: '正在思考中...',
    userType: 'BOT' as const,
    createdAt: new Date(),
    isLoading: true
  }
  messages.value.push(loadingMessage)
  
  try {
    const response = await sendChatMessage({
      sessionId: sessionId.value,
      message: messageToSend,
      userType: userStore.user?.role || 'USER',
      context: '智能客服页面'
    })
    
    // 移除加载消息
    messages.value = messages.value.filter(msg => !msg.isLoading)
    
    const botResponse = response.data
    botResponse.content = formatMessageContent(botResponse.content)
    messages.value.push(botResponse)
    
    scrollToBottom()
  } catch (error: any) {
    console.error('发送消息失败:', error)
    
    // 移除加载消息
    messages.value = messages.value.filter(msg => !msg.isLoading)
    
    // 根据错误类型提供不同的错误信息
    let errorMessage = '抱歉，发送消息失败，请稍后重试。'
    
    if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
      errorMessage = '抱歉，AI响应超时，可能是模型正在思考中。请稍后重试或尝试重新提问。'
    } else if (error.response?.status === 500) {
      errorMessage = '服务器内部错误，请稍后重试。'
    } else if (error.response?.status === 503) {
      errorMessage = 'AI服务暂时不可用，请稍后重试。'
    }
    
    messages.value.push({
      id: Date.now() + 1,
      content: errorMessage,
      userType: 'BOT' as const,
      createdAt: new Date(),
      isError: true
    })
    
    // 添加重试按钮
    setTimeout(() => {
      const retryButton = {
        id: Date.now() + 2,
        content: '<button onclick="retryLastMessage()" class="retry-btn">🔄 重试</button>',
        userType: 'BOT' as const,
        createdAt: new Date(),
        isRetry: true
      }
      messages.value.push(retryButton)
    }, 1000)
  }
}

// 重试机制
const retryLastMessage = () => {
  // 找到最后一条用户消息
  const lastUserMessage = messages.value
    .filter(msg => msg.userType === 'USER')
    .pop()
  
  if (lastUserMessage) {
    // 移除错误消息和重试按钮
    messages.value = messages.value.filter(msg => !msg.isError && !msg.isRetry)
    
    // 重新发送消息
    inputMessage.value = lastUserMessage.content
    sendMessage()
  }
}

const formatMessageContent = (content: string) => {
  if (!content) return ''
  let formatted = content
    .replace(/\\n/g, '\n')  // 处理转义的换行符
    .replace(/\n/g, '<br>') // 将换行符转换为HTML标签
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  return formatted
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatTime = (time: string | Date) => {
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  createNewSession()
})
</script>

<style scoped>
.chat-widget {
  position: fixed;
  z-index: 1000;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  user-select: none;
  resize: none;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: move;
  user-select: none;
}

.chat-title {
  font-weight: 600;
  font-size: 16px;
}

.chat-controls {
  display: flex;
  gap: 8px;
}

.control-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
  font-size: 16px;
  line-height: 1;
}

.control-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.control-btn.minimize:hover {
  background-color: rgba(255, 193, 7, 0.3);
}

.control-btn.close:hover {
  background-color: rgba(220, 53, 69, 0.3);
}

/* 调整大小手柄 */
.resize-handle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-widget:hover .resize-handle {
  opacity: 1;
}

.resize-handle-nw {
  top: 4px;
  left: 4px;
  cursor: nw-resize;
}

.resize-handle-ne {
  top: 4px;
  right: 4px;
  cursor: ne-resize;
}

.resize-handle-sw {
  bottom: 4px;
  left: 4px;
  cursor: sw-resize;
}

.resize-handle-se {
  bottom: 4px;
  right: 4px;
  cursor: se-resize;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.welcome-message {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
}

.bot-avatar {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-text p {
  margin: 8px 0;
  color: #666;
}

.quick-questions {
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.quick-questions h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 14px;
}

.question-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.question-btn {
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  text-align: left;
  line-height: 1.3;
}

.question-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.user-message {
  align-self: flex-end;
  background: #667eea;
  color: white;
}

.bot-message {
  align-self: flex-start;
  background: #f1f3f4;
  color: #333;
}

/* 加载状态样式 */
.message.loading {
  background: #e3f2fd;
  color: #1976d2;
  font-style: italic;
}

/* 错误状态样式 */
.message.error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

/* 重试按钮样式 */
.retry-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 8px;
  transition: background-color 0.2s;
}

.retry-btn:hover {
  background: #5a6fd8;
}

.message-content {
  margin-bottom: 4px;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
}

.input-area {
  display: flex;
  padding: 16px;
  gap: 8px;
  border-top: 1px solid #e9ecef;
  background: white;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #dee2e6;
  border-radius: 24px;
  outline: none;
  font-size: 14px;
}

.message-input:focus {
  border-color: #667eea;
}

.send-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.send-btn:hover {
  background: #5a6fd8;
}

.minimized-chat {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #667eea;
  color: white;
  text-align: center;
  padding: 12px;
  cursor: pointer;
  font-size: 20px;
}

.chat-toggle {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: #667eea;
  color: white;
  padding: 12px 20px;
  border-radius: 24px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 999;
}

.chat-toggle:hover {
  background: #5a6fd8;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.toggle-text {
  font-size: 14px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-widget {
    width: calc(100vw - 40px) !important;
    height: calc(100vh - 40px) !important;
    left: 20px !important;
    top: 20px !important;
  }
  
  .question-buttons {
    grid-template-columns: 1fr;
  }
}
</style>

