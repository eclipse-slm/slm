import type {RouteRecordRaw} from 'vue-router';

/** Hook for consumers: shadow this file to add, remove or change routes. */
export default function customizeRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
    return routes;
}
