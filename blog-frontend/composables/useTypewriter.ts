/** 打字机效果 composable */
export const useTypewriter = (text: () => string, opts: { speed?: number; pause?: number } = {}) => {
  const output = ref('')
  const speed = opts.speed ?? 120
  const pause = opts.pause ?? 2400

  let timer: ReturnType<typeof setTimeout> | null = null

  const stop = () => {
    if (timer) { clearTimeout(timer); timer = null }
  }

  const type = (str: string, i: number, deleting: boolean) => {
    stop()
    if (!import.meta.client) return
    if (!deleting) {
      output.value = str.slice(0, i)
      if (i >= str.length) {
        if (str) timer = setTimeout(() => type(str, str.length, true), pause)
      } else {
        timer = setTimeout(() => type(str, i + 1, false), speed)
      }
    } else {
      output.value = str.slice(0, i)
      if (i <= 0) {
        timer = setTimeout(() => type(str, 1, false), 500)
      } else {
        timer = setTimeout(() => type(str, i - 1, true), speed / 2)
      }
    }
  }

  watch(text, (val) => { output.value = ''; type(val, 1, false) }, { immediate: true })

  onBeforeUnmount(stop)

  return { output }
}
