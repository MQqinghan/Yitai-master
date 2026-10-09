import { createApp } from 'vue'
// 导入Element Plus核心和样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 导入Element Plus图标（可选）
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 导入路由配置（后续创建）
import router from './router/index.ts'
// 导入根组件
import App from './App.vue'

// 创建Vue实例（TS会自动推导app的类型为App<Element>，无需手动声明）
const app = createApp(App)

// 注册Element Plus
app.use(ElementPlus)

// 注册所有Element Plus图标（可选，用的时候直接用）
// 为Object.entries添加类型注解，匹配图标组件的类型
for (const [key, component] of Object.entries(ElementPlusIconsVue) as [string, any][]) {
    app.component(key, component)
}

// 注册路由
app.use(router)

// 挂载到#app节点
app.mount('#app')