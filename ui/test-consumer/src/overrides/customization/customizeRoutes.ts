import type {RouteRecordRaw} from 'vue-router';

export default function customizeRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
    return [
        ...routes,
        { name: 'Consumer', path: '/consumer', component: () => import('../../pages/ConsumerPage.vue') },
    ];
}
