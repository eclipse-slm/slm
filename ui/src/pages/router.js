import {createRouter, createWebHistory} from 'vue-router';
import {useUserStore} from "@/stores/userStore";
import {defaultRoutes} from "@/app/routes";
import customizeRoutes from "@/customization/customizeRoutes";



export const router = createRouter({
  history: createWebHistory(),
  routes: customizeRoutes(defaultRoutes),
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  await userStore.updateStore();

  if (to.meta.developerPermissionRequired) {
    if (userStore.isUserDeveloper) {
      next();
    } else {
      next('/access_denied');
    }
  } else if (to.meta.adminPermissionRequired) {
    if (userStore.userRoles.includes('slm-admin')) {
      next();
    } else {
      next('/access_denied');
    }
  } else {
    next();
  }
});

export default router;