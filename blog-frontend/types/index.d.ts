// Element Plus 全局挂载（见 plugins/element-plus.client.ts）
declare global {
  interface Window {
    ElMessage: any
    ElMessageBox: any
    ElNotification: any
    ElLoading: any
  }
}
export {}
