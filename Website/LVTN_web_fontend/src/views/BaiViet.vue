<template>
  <div class="baiviet-page">
    <header class="hero_baiviet">
      <div class="hero-inner">
        <div class="tag">BÀI VIẾT MỚI NHẤT</div>
         <div class="meta-line">
          <span>Khơi nguồn ký ức – Gìn giữ di sản – Lan tỏa giá trị lịch sử đến cộng đồng.</span>
        </div>
      </div>
    </header>

    <div v-if="loading" class="grid">
      <div v-for="n in 6" :key="n" class="card skeleton-card">
        <div class="skeleton skeleton-title"></div>
        <div class="skeleton skeleton-text"></div>
        <div class="skeleton skeleton-text short"></div>
        <div class="skeleton skeleton-footer"></div>
      </div>
    </div>

    <div v-else class="grid">
      <div
        v-for="item in danhSach"
        :key="item.MA_BAI_VIET"
        class="card"
      >
        <div class="card-content">
          <span class="date">{{ formatDate(item.NGAY_DANG) }}</span>
          <h2>{{ item.TIEU_DE }}</h2>
          <p>{{ item.MO_TA_NGAN }}</p>
        </div>

        <router-link :to="`/baiviet/${item.MA_BAI_VIET}`" class="read-more">
          <span>Xem chi tiết</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      danhSach: [],
      loading: true
    };
  },

  async mounted() {
    await this.fetchList();
  },

  methods: {
    async fetchList() {
      try {
        const res = await axios.get("http://localhost:3000/api/baiviet");
        this.danhSach = res.data;
      } catch (err) {
        console.error(err);
      } finally {
        this.loading = false;
      }
    },

    formatDate(date) {
      if (!date) return "";
      return new Date(date).toLocaleDateString("vi-VN", {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    }
  }
};
</script>
<style src="../assets/css/baiviet.css"></style>