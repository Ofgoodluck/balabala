import './assets/main.css'

import { createApp } from 'vue';
import {createPinia} from 'pinia'
import Antd from 'ant-design-vue';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue';
import 'ant-design-vue/dist/reset.css';
import router from './router'
const pinia = createPinia()
const app = createApp(App);


app.use(router).use(Antd).use(ElementPlus).use(pinia).mount('#app');
