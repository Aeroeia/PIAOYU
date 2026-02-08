<template>
  <svg 
    class="icon" 
    viewBox="0 0 1024 1024" 
    version="1.1" 
    xmlns="http://www.w3.org/2000/svg"
    :width="size" 
    :height="size"
  >
    <path d="M928 224V128h-32V96h-96v32h-32v9.28a225.6 225.6 0 0 0-178.88 22.4L512 82.88 242.88 352H192a96 96 0 0 0-96 96v384a96 96 0 0 0 96 96h640a96 96 0 0 0 96-96V448a96 96 0 0 0-7.68-37.76A225.6 225.6 0 0 0 928 352a224 224 0 0 0-32-115.2V224z" fill="#FFFFFF" />
    <path d="M704 352m-192 0a192 192 0 1 0 384 0 192 192 0 1 0-384 0Z" fill="#E9EAEB" />
    <path d="M832 384h-224v-64h-192v64H192a64 64 0 0 0-64 64v384a64 64 0 0 0 64 64h640a64 64 0 0 0 64-64V448a64 64 0 0 0-64-64z" fill="#FFFFFF" />
    <path d="M704 640v128H320v-128H128v192a64 64 0 0 0 64 64h640a64 64 0 0 0 64-64v-192z" fill="#A3D4FF" />
    <path d="M832 384h-160v32h160a32 32 0 0 1 32 32v192h-160v128H320v-128H160v-192a32 32 0 0 1 32-32h160v-32H192a64 64 0 0 0-64 64v384a64 64 0 0 0 64 64h640a64 64 0 0 0 64-64V448a64 64 0 0 0-64-64z m-96 480H192a32 32 0 0 1-32-32v-160h128v128h448v-128h128v160a32 32 0 0 1-32 32z" fill="#2A5082" />
    <path d="M512 128l-192 192h384l-192-192z" fill="#A3D4FF" />
    <path d="M416 512h32v32h-32zM512 128l-192 192h96v160h32v-160h128v224h32v-224h96z m-114.88 160L512 173.12 626.88 288zM576 576h32v32h-32zM512 640h32v32h-32zM416 576h32v32h-32z" fill="#2A5082" />
    <path d="M512 416h32v160h-32z" fill="#2A5082" />
    <path d="M800 160h96v32h-96z" fill="#BCC0C4" />
    <path d="M864 128v96h-32V128zM832 288h-32V256h-32v32h-32v32h32v32h32v-32h32V288z" fill="#BCC0C4" />
  </svg>
</template>

<script setup>
defineProps({
  size: {
    type: [Number, String],
    default: 24
  }
})
</script>

<style scoped>
.icon {
  display: inline-block;
  vertical-align: middle;
}
</style> 