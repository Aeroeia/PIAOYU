<template>
  <div class="customer-service" :class="{ 'dark': isDark }">
    <div class="chat-container">
      <div class="sidebar">
        <div class="history-header">
          <h2>聊天记录</h2>
          <button class="new-chat" @click="startNewChat">
            <PlusIcon class="icon" :size="24" />
            新的聊天
          </button>
        </div>
        <div class="history-list">
          <div 
            v-for="chat in chatHistory" 
            :key="`${chat.id}-${chat.title}`"
            class="history-item"
            :class="{ 'active': currentChatId === chat.id }"
            @click="loadChat(chat.id)"
          >
            <ChatBubbleIcon class="icon" :size="24" />
            <span class="title">{{ chat.title || '新的对话' }}</span>
            <button 
              class="delete-btn" 
              @click.stop="deleteChat(chat.id)"
              title="删除对话"
            >
              <TrashIcon class="icon" :size="20" />
            </button>
          </div>
        </div>
      </div>
      
      <div class="chat-main">
        <div class="service-header">
          <div class="service-info">
            <LaptopIcon class="avatar" :size="48" />
            <div class="info">
              <h3>麦小蜜</h3>
              <p>大麦系统智能客服</p>
            </div>
          </div>
        </div>

        <div class="messages" ref="messagesRef">
          <Chat
            v-for="(message, index) in currentMessages"
            :key="index"
            :message="message"
            :is-stream="isStreaming && index === currentMessages.length - 1"
          />
        </div>
        
        <div class="input-area">
          <textarea
            v-model="userInput"
            @keydown.enter.prevent="sendMessage()"
            placeholder="请描述您的问题，麦小蜜会帮您解答哦..."
            rows="1"
            ref="inputRef"
          ></textarea>
          <button 
            class="send-button" 
            @click="sendMessage()"
            :disabled="isStreaming || !userInput.trim()"
          >
            <SendIcon class="icon" :size="40" />
          </button>
        </div>
      </div>
    </div>

    <!-- 生成订单成功弹窗 -->
    <div v-if="createOrderModal" class="create-order-modal">
      <div class="modal-content">
        <h3>生成订单成功！</h3>
        <div class="create-order-info" v-html="bookingInfo"></div>
        <button @click="createOrderModal = false">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {nextTick, onMounted, ref, triggerRef} from 'vue'
import {useDark} from '@vueuse/core'
import {marked} from 'marked'
import DOMPurify from 'dompurify'
import Chat from '../components/Chat.vue'
import {chatAPI} from '../api/api'
import LaptopIcon from '../components/icons/LaptopIcon.vue'
import ChatBubbleIcon from '../components/icons/ChatBubbleIcon.vue'
import PlusIcon from '../components/icons/PlusIcon.vue'
import SendIcon from '../components/icons/SendIcon.vue'
import TrashIcon from '../components/icons/TrashIcon.vue'

const isDark = useDark()
const messagesRef = ref(null)
const inputRef = ref(null)
const userInput = ref('')
const isStreaming = ref(false)
const currentChatId = ref(null)
const currentMessages = ref([])
const chatHistory = ref([])
const createOrderModal = ref(false)
const bookingInfo = ref('')
const titleUpdateTimer = ref(null)

// 配置 marked
marked.setOptions({
  breaks: true,  // 支持换行
  gfm: true,     // 支持 GitHub Flavored Markdown
  sanitize: false // 允许 HTML
})

// 自动调整输入框高度
const adjustTextareaHeight = () => {
  const textarea = inputRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = textarea.scrollHeight + 'px'
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 发送消息
const sendMessage = async (content) => {
  if (isStreaming.value || (!content && !userInput.value.trim())) return
  
  // 使用传入的 content 或用户输入框的内容
  const messageContent = content || userInput.value.trim()
  
  // 添加用户消息
  const userMessage = {
    role: 'user',
    content: messageContent,
    timestamp: new Date()
  }
  currentMessages.value.push(userMessage)
  
  // 清空输入
  if (!content) {  // 只有在非传入内容时才清空输入框
    userInput.value = ''
    adjustTextareaHeight()
  }
  await scrollToBottom()
  
  // 添加助手消息占位
  const assistantMessage = {
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    isMarkdown: true  // 添加标记表示这是 Markdown 内容
  }
  currentMessages.value.push(assistantMessage)
  isStreaming.value = true
  
  let totalContent = ''
  
  try {
    const reader = await chatAPI.sendAssistantMessage(messageContent, currentChatId.value)
    const decoder = new TextDecoder('utf-8')
    
    while (true) {
      try {
        const { value, done } = await reader.read()
        if (done) break
        
        // 累积新内容
        totalContent += decoder.decode(value)
        
        await nextTick(() => {
          // 更新消息
          const updatedMessage = {
            ...assistantMessage,
            content: totalContent,
            isMarkdown: true  // 保持 Markdown 标记
          }
          const lastIndex = currentMessages.value.length - 1
          currentMessages.value.splice(lastIndex, 1, updatedMessage)
        })
        await scrollToBottom()
      } catch (readError) {
        console.error('读取流错误:', readError)
        break
      }
    }

    checkAndUpdateChatTitles()

    // 检查是否包含订单编号信息
    if (totalContent.includes('订单编号')) {
      const createOrderMatch = totalContent.match(/【(.*?)】/s)
      if (createOrderMatch) {
        // 使用 marked 处理预约信息中的 Markdown
        bookingInfo.value = DOMPurify.sanitize(
          marked.parse(createOrderMatch[1]),
          {
            ADD_TAGS: ['code', 'pre', 'span'],
            ADD_ATTR: ['class', 'language']
          }
        )
        createOrderModal.value = true
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    assistantMessage.content = '抱歉，发生了错误，请稍后重试。'
  } finally {
    isStreaming.value = false
    await scrollToBottom()
  }
}

// 加载特定对话
const loadChat = async (chatId) => {
  currentChatId.value = chatId
  try {
    const messages = await chatAPI.chatHistoryMessageList(chatId, 2)
    currentMessages.value = messages.map(msg => ({
      ...msg,
      isMarkdown: msg.role === 'assistant'  // 为助手消息添加 Markdown 标记
    }))
  } catch (error) {
    console.error('加载对话消息失败:', error)
    currentMessages.value = []
  }
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const history = await chatAPI.chatTypeHistoryList(2)
    chatHistory.value = history || []
    if (history && history.length > 0) {
      await loadChat(history[0].id)
    } else {
      await startNewChat()  // 等待 startNewChat 完成
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    chatHistory.value = []
    await startNewChat()  // 等待 startNewChat 完成
  }
}

// 开始新对话
const startNewChat = async () => {  // 添加 async
  const newChatId = Date.now().toString()
  currentChatId.value = newChatId
  currentMessages.value = []
  
  // 添加新对话到历史列表
  const newChat = {
    id: newChatId,
    title: '新的对话'
  }
  chatHistory.value = [newChat, ...chatHistory.value]
  // 发送初始问候语
  //await sendMessage('你好啊，麦小蜜')
}

// 删除对话
const deleteChat = async (chatId) => {
  if (!confirm('确定要删除这个对话吗？')) {
    return
  }
  
  try {
    await chatAPI.deleteChat(chatId,2)
    // 从历史记录中移除
    chatHistory.value = chatHistory.value.filter(chat => chat.id !== chatId)
    
    // 如果删除的是当前对话，则创建新对话
    if (currentChatId.value === chatId) {
      await startNewChat()
    }
  } catch (error) {
    console.error('删除对话失败:', error)
    alert('删除对话失败，请稍后重试')
  }
}

// 检查并更新聊天标题
const checkAndUpdateChatTitles = async () => {
  try {
    // 检查是否有标题为"新的对话"的聊天记录
    const hasNewChatTitle = chatHistory.value.some(chat => 
      chat.title === '新的对话' || chat.title === '新的聊天'
    )
    
    if (!hasNewChatTitle) {
      return
    }
    
    // 调用接口获取聊天记录列表
    const chatListData = await chatAPI.chatTypeHistoryList(2)
    
    // 检查响应是否有内容
    if (!chatListData || (Array.isArray(chatListData) && chatListData.length === 0)) {
      return
    }
    
    // 更新聊天记录标题
    if (Array.isArray(chatListData)) {
      // 使用 forEach 修改数组元素，然后强制触发更新
      let hasUpdated = false
      
      for (let i = 0; i < chatHistory.value.length; i++) {
        const chat = chatHistory.value[i]
        const matchedChat = chatListData.find(apiChat => apiChat.id === chat.id)
        
        if (matchedChat && matchedChat.title && matchedChat.title.trim()) {
          // 直接修改对象属性并创建新引用
          chatHistory.value[i] = { 
            ...chat, 
            title: matchedChat.title 
          }
          hasUpdated = true
        }
      }
      
      // 如果有更新，强制触发响应式更新
      if (hasUpdated) {
        // 触发数组的响应式更新
        triggerRef(chatHistory)
        await nextTick()
      }
    }
  } catch (error) {
    console.error('检查并更新聊天标题失败:', error)
  }
}



onMounted(() => {
  loadChatHistory()
  adjustTextareaHeight()
  checkAndUpdateChatTitles()
})

</script>

<style scoped lang="scss">
.customer-service {
  position: fixed;
  inset: 64px 0 0 0;
  display: flex;
  background: var(--bg-color);
  overflow: hidden;

  .chat-container {
    flex: 1;
    display: flex;
    max-width: 1800px;
    width: 100%;
    margin: 0 auto;
    padding: 1.5rem 2rem;
    gap: 1.5rem;
    height: 100%;
    overflow: hidden;
  }

  .sidebar {
    width: 300px;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 1rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s ease;
    
    .history-header {
      flex-shrink: 0;
      padding: 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      
      h2 {
        font-size: 1.25rem;
        font-weight: 600;
      }
      
      .new-chat {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.5rem 1rem;
        border-radius: 0.5rem;
        background: #333;
        color: white;
        border: none;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          background: #000;
          transform: translateY(-1px);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
    
    .history-list {
      flex: 1;
      overflow-y: auto;
      padding: 0.5rem;
      scrollbar-width: thin;
      scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
      }
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(0, 0, 0, 0.2);
        border-radius: 3px;
      }
      
      .history-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.75rem;
        border-radius: 0.5rem;
        cursor: pointer;
        transition: all 0.3s ease;
        margin-bottom: 0.25rem;
        
        &:hover {
          background: rgba(0, 0, 0, 0.05);
          transform: translateX(2px);
          
          .delete-btn {
            opacity: 1;
          }
        }
        
        &.active {
          background: rgba(0, 0, 0, 0.1);
          font-weight: 500;
        }
        
        .title {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          font-size: 0.95rem;
        }

        .delete-btn {
          opacity: 0;
          background: none;
          border: none;
          padding: 0.25rem;
          cursor: pointer;
          color: #666;
          transition: all 0.3s ease;
          border-radius: 0.25rem;
          
          &:hover {
            color: #ff4d4f;
            background: rgba(255, 77, 79, 0.1);
          }
        }
      }
    }
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 1rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    overflow: hidden;
    transition: all 0.3s ease;

    .service-header {
      flex-shrink: 0;
      padding: 1rem 2rem;
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      background: rgba(255, 255, 255, 0.98);
      transition: all 0.3s ease;

      .service-info {
        display: flex;
        align-items: center;
        gap: 1rem;

        .info {
          h3 {
            font-size: 1.25rem;
            margin-bottom: 0.25rem;
            font-weight: 600;
          }

          p {
            font-size: 0.875rem;
            color: #666;
          }
        }
      }
    }
    
    .messages {
      flex: 1;
      overflow-y: auto;
      padding: 2rem;
      scrollbar-width: thin;
      scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
      }
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(0, 0, 0, 0.2);
        border-radius: 3px;
      }
    }
    
    .input-area {
      flex-shrink: 0;
      padding: 1.5rem 2rem;
      background: rgba(255, 255, 255, 0.98);
      border-top: 1px solid rgba(0, 0, 0, 0.05);
      display: flex;
      gap: 1rem;
      align-items: flex-end;
      transition: all 0.3s ease;
      
      textarea {
        flex: 1;
        resize: none;
        border: 1px solid rgba(0, 0, 0, 0.1);
        background: white;
        border-radius: 0.75rem;
        padding: 1rem;
        color: inherit;
        font-family: inherit;
        font-size: 1rem;
        line-height: 1.5;
        max-height: 150px;
        transition: all 0.3s ease;
        
        &:focus {
          outline: none;
          border-color: #333;
          box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
        }
      }
      
      .send-button {
        background: #f5f5f5;
        color: #333;
        border: 1px solid #e0e0e0;
        border-radius: 0.75rem;
        width: 3.5rem;
        height: 3.5rem;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s ease;
        padding: 0;
        
        &:hover:not(:disabled) {
          background: #e8e8e8;
          transform: scale(1.05);
        }
        
        &:active:not(:disabled) {
          transform: scale(0.95);
        }
        
        &:disabled {
          background: #f5f5f5;
          border-color: #e0e0e0;
          cursor: not-allowed;
          opacity: 0.6;
        }
      }
    }
  }

  .create-order-modal {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    backdrop-filter: blur(4px);
    animation: fadeIn 0.3s ease;

    .modal-content {
      background: white;
      padding: 2rem;
      border-radius: 1rem;
      max-width: 500px;
      width: 90%;
      text-align: center;
      animation: slideUp 0.3s ease;

      h3 {
        font-size: 1.5rem;
        margin-bottom: 1rem;
        color: #333;
        font-weight: 600;
      }

      .create-order-info {
        margin: 1.5rem 0;
        text-align: left;
        line-height: 1.6;
        color: #666;
        max-height: 60vh;
        overflow-y: auto;
        padding-right: 0.5rem;
        scrollbar-width: thin;
        scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
        
        &::-webkit-scrollbar {
          width: 6px;
        }
        
        &::-webkit-scrollbar-track {
          background: transparent;
        }
        
        &::-webkit-scrollbar-thumb {
          background-color: rgba(0, 0, 0, 0.2);
          border-radius: 3px;
        }
      }

      button {
        padding: 0.75rem 2rem;
        background: #333;
        color: white;
        border: none;
        border-radius: 0.5rem;
        cursor: pointer;
        transition: all 0.3s ease;
        font-weight: 500;

        &:hover {
          background: #000;
          transform: translateY(-1px);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
  }
}

.dark {
  .sidebar {
    background: rgba(40, 40, 40, 0.95);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
    
    .history-header {
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }
    
    .history-list {
      scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(255, 255, 255, 0.2);
      }
      
      .history-item {
        &:hover {
          background: rgba(255, 255, 255, 0.05);
        }
        
        &.active {
          background: rgba(255, 255, 255, 0.1);
        }
      }
    }
  }
  
  .chat-main {
    background: rgba(40, 40, 40, 0.95);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
    
    .service-header {
      background: rgba(30, 30, 30, 0.98);
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);

      .info p {
        color: #999;
      }
    }

    .messages {
      scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(255, 255, 255, 0.2);
      }
    }

    .input-area {
      background: rgba(30, 30, 30, 0.98);
      border-top: 1px solid rgba(255, 255, 255, 0.05);
      
      textarea {
        background: rgba(50, 50, 50, 0.95);
        border-color: rgba(255, 255, 255, 0.1);
        color: white;
        
        &:focus {
          border-color: #666;
          box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
        }
      }
    }
  }

  .create-order-modal .modal-content {
    background: #333;

    h3 {
      color: #fff;
    }

    .create-order-info {
      color: #ccc;
      scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(255, 255, 255, 0.2);
      }
    }

    button {
      background: #666;

      &:hover {
        background: #888;
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .customer-service {
    .chat-container {
      padding: 0;
    }
    
    .sidebar {
      position: fixed;
      left: -300px;
      top: 64px;
      bottom: 0;
      z-index: 100;
      
      &.show {
        transform: translateX(300px);
      }
    }
    
    .chat-main {
      border-radius: 0;
      
      .service-header {
        padding: 1rem;
      }
      
      .messages {
        padding: 1rem;
      }
      
      .input-area {
        padding: 1rem;
      }
    }
  }
}

@media (max-width: 480px) {
  .customer-service {
    .chat-main {
      .service-header {
        .service-info {
          .info {
            h3 {
              font-size: 1.1rem;
            }
            
            p {
              font-size: 0.8rem;
            }
          }
        }
      }
      
      .input-area {
        textarea {
          font-size: 0.95rem;
        }
      }
    }
  }
}
</style> 