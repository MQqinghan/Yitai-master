import { ref, onMounted, onUnmounted } from 'vue';

// 响应式监听窗口宽度，判断是否为PC端
export function useDevice() {
    const isPc = ref(window.innerWidth >= 768);

    const handleResize = () => {
        isPc.value = window.innerWidth >= 768;
    };

    onMounted(() => {
        window.addEventListener('resize', handleResize);
    });

    onUnmounted(() => {
        window.removeEventListener('resize', handleResize);
    });

    return { isPc };
}