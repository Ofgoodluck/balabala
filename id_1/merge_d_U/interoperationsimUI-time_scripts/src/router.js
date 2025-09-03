// router.js
import {createRouter, createWebHashHistory, createWebHistory} from 'vue-router';

import SimulationEditorPage from "@/components/editor/SimulationEditorPage.vue";
import WelcomePage from "@/components/WelcomePage.vue";

import SimulationInfoShowPage from "@/components/showPage/SimulationInfoShowPage.vue";




const routes = [
    {path: '/editor', component: SimulationEditorPage},
    {path: '/',component: WelcomePage},
    {path: '/info',component: SimulationInfoShowPage}
    // {path: '/info',component: ShowPageMain}

    // {path: '/', component: MainPage},

];

const router = createRouter({
    history: createWebHashHistory(),
    routes,
});



export default router;



