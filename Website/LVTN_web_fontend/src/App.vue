<template>
  <div class="min-h-screen flex flex-col bg-gray-100">
    <Header v-if="!$route.meta.hideLayout" />

    <main class="flex-grow">
      <router-view />
    </main>

    <Footer v-if="!$route.meta.hideLayout" />
  </div>
</template>

<script>
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'

export default {
  name: 'App',
  components: {
    Header,
    Footer
  }
}
</script>