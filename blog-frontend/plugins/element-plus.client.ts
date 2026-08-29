// Element Plus 仅在客户端注册（后台为 ssr:false，公共页面不使用其组件）
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(ElementPlus, { locale: zhCn })
})
