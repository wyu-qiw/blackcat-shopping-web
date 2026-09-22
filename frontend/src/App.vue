<template>
  <div class="app-shell">
    <BackButton />
    <router-view />
    <ChatAssistant />
  </div>
</template>

<script setup>
import ChatAssistant from './components/ChatAssistant.vue'
import BackButton from './components/BackButton.vue'
</script>

<style>
.app-shell {
  position: relative;
  min-height: 100vh;
}
</style>