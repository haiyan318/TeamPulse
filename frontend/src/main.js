import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import pinia from './stores'

// 样式顺序：Element Plus 在前，本项目样式在后，保证 index.css 可覆盖 EP 默认值
import 'element-plus/dist/index.css'
import './styles/index.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 全量注册图标（293 个），模板中可直接 <el-icon><User /></el-icon>
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app.mount('#app')
