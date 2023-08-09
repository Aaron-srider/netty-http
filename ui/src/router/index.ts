import Vue from 'vue';
import VueRouter, { RouteConfig } from 'vue-router';
import HelloWorld from '@/views/HelloWorld.vue';
import About from '@/views/About.vue';
Vue.use(VueRouter);

const routes: Array<RouteConfig> = [
    {
        path: '/',
        redirect: '/hello-world',
    },
    {
        path: '/hello-world',
        name: 'HelloWorld',
        component: HelloWorld,
    },
    {
        path: '/about',
        name: 'About',
        component: About,
    },
];

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes,
});

export default router;
