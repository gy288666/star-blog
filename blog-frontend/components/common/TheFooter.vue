<script setup lang="ts">
// 页脚：自定义文案 + ICP 备案
const { footerText, icpText, siteTitle } = useSettings()
const year = new Date().getFullYear()
</script>

<template>
  <footer class="site-footer">
    <div class="container footer-inner">
      <p class="footer-text">{{ footerText || `© ${year} ${siteTitle}` }}</p>
      <p v-if="icpText" class="footer-icp">
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="nofollow noopener">{{ icpText }}</a>
      </p>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  padding: 26px 0 30px;
  text-align: center;
  color: var(--text-3);
  font-size: 13px;
  border-top: 1px solid var(--divider);
  background: transparent;
  backdrop-filter: none;
}
.footer-inner p { margin: 4px 0; }
.footer-icp a { color: var(--text-3); }
.footer-icp a:hover { color: var(--primary); }
</style>
