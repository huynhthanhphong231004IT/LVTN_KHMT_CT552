<template>
<div id = "mainform">
<div class="iframe-wrapper">
    <div class="form-page-wrapper">
    <div class="form-header">
      <div class="tag">HỆ THỐNG KHẢO SÁT & ĐÁNH GIÁ</div>
      <p>Bạn có thể đóng góp ý kiến trải nghiệm hệ thống "SMAL-AIMuseum" nhằm hoàn thiện đề tài.</p>
    </div>

    <div class="button-wrapper">
      <button 
        @click="showForm = !showForm" 
        :class="['action-btn', { 'active': showForm }]"
      >
        <span class="btn-icon">
          <svg v-if="!showForm" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/></svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/></svg>
        </span>
        {{ showForm ? 'Ẩn Biểu Mẫu Khảo Sát' : 'Bắt Đầu Điền Biểu Mẫu' }}
      </button>
    </div>

    <Transition name="fade-slide">
      <div v-if="showForm" class="form-container">
        <iframe 
          :src="googleFormUrl" 
          frameborder="0" 
          marginheight="0" 
          marginwidth="0"
          class="custom-form-iframe"
        >
          Đang tải biểu mẫu
        </iframe>
      </div>
    </Transition>
  </div>
</div>
</div>
</template>

<script setup>
import { ref } from 'vue'
const googleFormUrl = ref('https://docs.google.com/forms/d/e/1FAIpQLSeKGvpYJmDGt4Ozqgl16q1BgeWditoXTSF5vbBwwi1Wi7XwXg/viewform?usp=publish-editor')
const showForm = ref(false)
</script>

<style src="../assets/css/danhgia.css" scoped></style>