import {createRouter, createWebHistory} from 'vue-router';
import {useUserStore} from "@/stores/userStore";


const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/',
    component: () => import('@/pages/IndexPage.vue'),
    children: [
      // Dashboard
      {
        name: 'Dashboard',
        path: '/dashboard',
        component: () => import('@/pages/DashboardPage.vue'),
      },
      // Error Pages
      {
        name: 'Access Denied',
        path: '/access_denied',
        component: () => import('@/pages/AccessDeniedPage.vue'),
      },
      // Default Pages
      {
        name: 'User Profile',
        path: '/user',
        component: () => import('@/pages/UserProfilePage.vue'),
      },
      // Notifications
      {
        name: 'Notifications',
        path: '/notifications',
        component: () => import('@/pages/NotificationsPage.vue'),
      },
      // Jobs
      {
        name: 'Jobs',
        path: '/jobs',
        component: () => import('@/pages/JobsPage.vue'),
      },
      // Discovery
      {
        name: 'Discovery Inbox',
        path: '/discovery/inbox',
        component: () => import('@/pages/DiscoveryInboxPage.vue'),
      },
      {
        name: 'Discovery Jobs',
        path: '/discovery/jobs',
        component: () => import('@/pages/DiscoveryJobsPage.vue'),
      },
      {
        name: 'Discovery Drivers',
        path: '/discovery/drivers',
        component: () => import('@/pages/DiscoveryDriversPage.vue'),
      },
      // Resources
      {
        name: 'Resource Instances',
        path: '/resources/instances',
        component: () => import('@/pages/ResourceInstancesPage.vue'),
      },
      {
        name: 'Resource Types',
        path: '/resources/types',
        component: () => import('@/pages/ResourceTypesPage.vue'),
      },
      // Clusters
      {
        name: 'Clusters',
        path: '/clusters',
        component: () => import('@/pages/ClustersPage.vue'),
      },
      // Provider
      {
        name: 'Provider',
        path: '/provider',
        component: () => import('@/pages/ProviderPage.vue'),
      },
      // Service Offerings
      {
        name: 'Service Offerings',
        path: '/services/offerings',
        component: () => import('@/pages/ServiceOfferingsPage.vue'),
      },
      {
        name: 'Service Offering Details',
        path: '/services/offerings/:serviceOfferingId',
        component: () => import('@/pages/ServiceOfferingDetailsPage.vue'),
        props: true,
      },
      {
        name: 'Order Service Offering',
        path: '/services/offerings/:serviceOfferingId/versions/:serviceOfferingVersionId/order',
        component: () => import('@/pages/ServiceOfferingOrderPage.vue'),
        props: true,
      },
      // Service Vendors
      {
        name: 'Service Vendor',
        path: '/services/vendors/:serviceVendorId?',
        component: () => import('@/pages/ServiceVendorPage.vue'),
        meta: { developerPermissionRequired: true },
        props: route => ({
          serviceVendorId: route.params.serviceVendorId,
        }),
      },
      {
        name: 'Create Service Offering',
        path: '/services/vendors/:serviceVendorId/offerings',
        component: () => import('@/pages/ServiceOfferingCreateOrEditPage.vue'),
        meta: { developerPermissionRequired: true },
        props: route => ({
          editMode: false,
          serviceVendorId: route.params.serviceVendorId,
          creationType: route.query.creationType
        }),
      },
      {
        name: 'Edit Service Offering',
        path: '/services/vendors/:serviceVendorId/offerings/:serviceOfferingId',
        component: () => import('@/pages/ServiceOfferingCreateOrEditPage.vue'),
        props: route => ({
          editMode: true,
          serviceVendorId: route.params.serviceVendorId,
          serviceOfferingId: route.params.serviceOfferingId,
          creationType: 'manual'
        }),
      },
      {
        name: 'Create Service Offering Version',
        path: '/services/vendors/:serviceVendorId/offerings/:serviceOfferingId/versions',
        component: () => import('@/pages/ServiceOfferingVersionCreateOrEditPage.vue'),
        meta: { developerPermissionRequired: true },
        props: route => ({
          editMode: false,
          serviceVendorId: route.params.serviceVendorId,
          serviceOfferingId: route.params.serviceOfferingId,
        }),
      },
      {
        name: 'Edit Service Offering Version',
        path: '/services/vendors/:serviceVendorId/offerings/:serviceOfferingId/versions/:serviceOfferingVersionId',
        component: () => import('@/pages/ServiceOfferingVersionCreateOrEditPage.vue'),
        props: route => ({
          editMode: true,
          serviceVendorId: route.params.serviceVendorId,
          serviceOfferingId: route.params.serviceOfferingId,
          serviceOfferingVersionId: route.params.serviceOfferingVersionId,
        }),
      },
      // Service Instances
      {
        name: 'Services',
        path: '/services/instances',
        component: () => import('@/pages/ServiceInstancesPage.vue'),
      },
      // Admin
      {
        name: 'Admin',
        path: '/admin',
        component: () => import('@/pages/AdminPage.vue'),
        meta: { adminPermissionRequired: true },
      },
      {
        name: 'AdminComponents',
        path: '/admin/components',
        component: () => import('@/pages/AdminComponentsPage.vue'),
        meta: { adminPermissionRequired: true },
      },
      {
        name: 'AdminServiceCategories',
        path: '/admin/service-categories',
        component: () => import('@/pages/AdminServiceCategoriesPage.vue'),
        meta: { adminPermissionRequired: true },
      },
      {
        name: 'AdminServiceVendors',
        path: '/admin/service-vendors',
        component: () => import('@/pages/AdminServiceVendorsPage.vue'),
        meta: { adminPermissionRequired: true },
      },
    ],
  }
];

export const router = createRouter({
  history: createWebHistory(),
  routes: routes,
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
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