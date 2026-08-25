<template>
  <div class="bg-white rounded-lg shadow hover:shadow-lg transition overflow-hidden">
    <img :src="img" alt="" class="w-full h-48 object-cover">
    <div class="p-4">
      <h3 class="font-bold text-xl mb-2">{{ title }}</h3>
      <p class="text-gray-700 text-sm">{{ desc }}</p>
    </div>
  </div>
</template>

<script>
export default {
  props: ['title','img','desc']
}
</script>