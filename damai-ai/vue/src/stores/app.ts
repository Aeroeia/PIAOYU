import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 定义类型
interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

interface Session {
  id: string
  title: string
  messages: Message[]
  createdAt: number
  updatedAt: number
}

interface UserSettings {
  fontSize: number
  theme: 'light' | 'dark'
  language: string
  notifications: boolean
}

export const useAppStore = defineStore('app', () => {
  // 状态
  const isDark = ref(false)
  const isLoading = ref(false)
  const currentSession = ref<Session | null>(null)
  const sessionList = ref<Session[]>([])
  const userSettings = ref<UserSettings>({
    fontSize: 16,
    theme: 'light',
    language: 'zh-CN',
    notifications: true
  })

  // 计算属性
  const currentSessionMessages = computed(() => {
    if (!currentSession.value) return []
    return currentSession.value.messages || []
  })

  const hasActiveSession = computed(() => {
    return currentSession.value !== null
  })

  // 方法
  function toggleTheme() {
    isDark.value = !isDark.value
    userSettings.value.theme = isDark.value ? 'dark' : 'light'
    // 保存设置到本地存储
    localStorage.setItem('userSettings', JSON.stringify(userSettings.value))
  }

  function setLoading(status: boolean) {
    isLoading.value = status
  }

  function setCurrentSession(session: Session | null) {
    currentSession.value = session
  }

  function updateSessionList(sessions: Session[]) {
    sessionList.value = sessions
  }

  function updateUserSettings(settings: Partial<UserSettings>) {
    userSettings.value = { ...userSettings.value, ...settings }
    // 保存设置到本地存储
    localStorage.setItem('userSettings', JSON.stringify(userSettings.value))
  }

  function loadUserSettings() {
    const savedSettings = localStorage.getItem('userSettings')
    if (savedSettings) {
      userSettings.value = JSON.parse(savedSettings)
      isDark.value = userSettings.value.theme === 'dark'
    }
  }

  // 初始化
  loadUserSettings()

  return {
    // 状态
    isDark,
    isLoading,
    currentSession,
    sessionList,
    userSettings,
    // 计算属性
    currentSessionMessages,
    hasActiveSession,
    // 方法
    toggleTheme,
    setLoading,
    setCurrentSession,
    updateSessionList,
    updateUserSettings
  }
}) 