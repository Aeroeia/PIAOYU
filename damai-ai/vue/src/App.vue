<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useDark, useToggle } from '@vueuse/core'
import { SunIcon, MoonIcon } from '@heroicons/vue/24/outline'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { ref } from 'vue'

const isDark = useDark()
const toggleDark = useToggle(isDark)
const router = useRouter()

// 添加全局状态来跟踪当前路由
const currentRoute = ref(router.currentRoute.value.path)

// 添加全局路由守卫
router.beforeEach((to, from, next) => {
  // 如果是从 ChatPDF 页面离开
  if (from.path === '/chat-pdf') {
    // 触发一个自定义事件，让 ChatPDF 组件知道要清理资源
    window.dispatchEvent(new CustomEvent('cleanupChatPDF'))
  }
  currentRoute.value = to.path
  next()
})
</script>

<template>
  <div class="app" :class="{ 'dark': isDark }">
    <nav class="navbar">
      <router-link to="/" class="logo">
        <span class="logo-text">大麦 AI</span>
      </router-link>
      <button @click="toggleDark()" class="theme-toggle" aria-label="切换主题">
        <SunIcon v-if="isDark" class="icon" />
        <MoonIcon v-else class="icon" />
      </button>
    </nav>
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style lang="scss">
:root {
  --primary-color: #ff3b1d;
  --primary-light: rgba(255, 59, 29, 0.1);
  --bg-color: #ffffff;
  --text-color: #2c3e50;
  --border-color: #eaeaea;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
  --radius-lg: 16px;
  --radius-md: 12px;
  --radius-sm: 8px;
}

.dark {
  --bg-color: #1a1a1a;
  --text-color: #ffffff;
  --border-color: #2c2c2c;
  --primary-light: rgba(255, 59, 29, 0.15);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  color: var(--text-color);
  background: var(--bg-color);
  min-height: 100vh;
  line-height: 1.6;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  margin: 1.5rem auto;
  width: 92vw;
  max-width: 1280px;
  border-radius: var(--radius-lg);
  background: var(--bg-color);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  height: 64px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all 0.3s ease;
}

.logo {
  text-decoration: none;
  display: flex;
  align-items: center;
  
  .logo-text {
    font-size: 1.5rem;
    font-weight: 800;
    background: linear-gradient(135deg, var(--primary-color), #ff6b3d);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    letter-spacing: 1px;
    position: relative;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-1px);
      filter: brightness(1.1);
      text-shadow: 0 0 20px rgba(255, 59, 29, 0.2);
    }
  }
}

.theme-toggle {
  background: var(--primary-light);
  border: none;
  cursor: pointer;
  padding: 0.6rem;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    transform: scale(1.05);
    background: var(--primary-color);
    
    .icon {
      color: white;
    }
  }

  .icon {
    width: 22px;
    height: 22px;
    color: var(--primary-color);
    transition: color 0.3s ease;
  }
}

.main-content {
  flex: 1;
  width: 92vw;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-enter-active,
.page-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .navbar {
    margin: 1rem auto;
    padding: 0 1.25rem;
    height: 56px;
  }
  
  .logo .logo-text {
    font-size: 1.25rem;
  }
  
  .main-content {
    width: 100%;
    padding: 0 1rem;
  }
}
</style>

