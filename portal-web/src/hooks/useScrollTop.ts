import { ref, onMounted, onUnmounted } from 'vue'

export function useScrollTop() {
    const showGoTop = ref<boolean>(false)

    // 滚动超过 【半个页面】 才显示
    const handleScroll = (): void => {
        const halfPage = window.innerHeight / 2
        showGoTop.value = window.scrollY > halfPage
    }

    const goToTop = (): void => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth',
        })
    }

    onMounted((): void => {
        window.addEventListener('scroll', handleScroll)
    })

    onUnmounted((): void => {
        window.removeEventListener('scroll', handleScroll)
    })

    return {
        showGoTop,
        goToTop,
    }
}