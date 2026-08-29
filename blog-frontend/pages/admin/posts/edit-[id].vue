<script setup lang="ts">
// 编辑文章
const route = useRoute()
const postId = Number(route.params.id)
definePageMeta({ layout: 'admin' })

useHead({ title: '编辑文章' })
</script>

<template>
  <div>
    <h2 class="page-h">✏️ 编辑</h2>
    <AdminPostEditor :post-id="postId" />
  </div>
</template>

<style scoped>
.page-h { margin-top: 0; }
</style>
