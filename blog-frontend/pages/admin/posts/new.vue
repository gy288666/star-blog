<script setup lang="ts">
// 新建文章
definePageMeta({ layout: 'admin' })

useHead({ title: '写文章' })
</script>

<template>
  <div>
    <h2 class="page-h">✍️ 写文章</h2>
    <AdminPostEditor />
  </div>
</template>

<style scoped>
.page-h { margin-top: 0; }
</style>
