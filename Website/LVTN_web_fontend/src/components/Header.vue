<template>
  <div>
    <!-- Ảnh header -->
    <div class="header-img-container">
      <img src="../assets/images/Header.svg" alt="Bảo tàng QK9" class="header-img">
    </div>

    <!-- Nội dung header -->
    <header class="header-content">
      <nav class="desktop-menu">
        <router-link to="/" :class="{active: isActive('/')}">Trang chủ</router-link>
        <router-link to="/about" :class="{active: isActive('/about')}">Giới thiệu</router-link>
        <router-link to="/hienvat" :class="{active: isActive('/hienvat')}">Hiện vật</router-link>
        <router-link to="/baiviet" :class="{active: isActive('/baiviet')}">Bài đăng</router-link>
        <router-link to="/chatbox" :class="{active: isActive('/a')}">Tìm kiếm</router-link>
        <router-link to="/danhgia" :class="{active: isActive('/b')}">Đánh giá</router-link>
      </nav>
    </header>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRoute } from "vue-router";

const open = ref(false);
const toggleMenu = () => (open.value = !open.value);

const route = useRoute();
const isActive = (path) => route.path === path;
</script>

<style src="../assets/css/header.css"></style>