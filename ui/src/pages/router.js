import Vue from 'vue'
import Router from 'vue-router'
import userStore from '@/store/userStore'

Vue.use(Router)

const routes = [
  {
    path: '/',
    component: () => import('@/pages/IndexPage'),
    children: [
      // Dashboard
      {
        name: 'Dashboard',
        path: '',
        component: () => import('@/pages/DashboardPage'),
      },
      // Error Pages
      {
        name: 'Access Denied',
        path: '/access_denied',
        component: () => import('@/pages/AccessDeniedPage'),
      },
      // Default Pages
      {
        name: 'User Profile',
        path: '/user',
        component: () => import('@/pages/UserProfilePage'),
      },
      // {
      //   name: 'Notifications',
      //   path: 'components/notifications',
      //   component: () => import('../components/dashboard/Notifications'),
      // },
      // Notifications
      {
        name: 'Notifications',
        path: '/notifications',
        component: () => import('@/pages/NotificationsPage'),
      },
      // Jobs
      {
        name: 'Jobs',
        path: '/jobs',
        component: () => import('@/pages/JobsPage'),
      },
      // Resources
      {
        name: 'Resources',
        path: '/resources',
        component: () => import('@/pages/ResourcesPage'),
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
        component: () => import('@/pages/ProviderPage'),
      },
      // Service Offerings
      {
        name: 'Service Offerings',
        path: '/services/offerings',
        component: () => import('@/pages/ServiceOfferingsPage'),
      },
      {
        name: 'Service Offering Details',
        path: '/services/offerings/:serviceOfferingId',
        component: () => import('@/pages/ServiceOfferingDetailsPage'),
        props: true,
      },
      {
        name: 'Order Service Offering',
        path: '/services/offerings/:serviceOfferingId/versions/:serviceOfferingVersionId/order',
        component: () => import('@/pages/ServiceOfferingOrderPage'),
        props: true,
      },
      // Service Vendors
      {
        name: 'Service Vendor',
        path: '/services/vendors/:serviceVendorId?',
        component: () => import('@/pages/ServiceVendorPage'),
        meta: { developerPermissionRequired: true },
        props: route => ({
          serviceVendorId: route.params.serviceVendorId,
        }),
      },
      {
        name: 'Create Service Offering',
        path: '/services/vendors/:serviceVendorId/offerings',
        component: () => import('@/pages/ServiceOfferingCreateOrEditPage'),
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
        component: () => import('@/pages/ServiceOfferingCreateOrEditPage'),
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
        component: () => import('@/pages/ServiceOfferingVersionCreateOrEditPage'),
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
        component: () => import('@/pages/ServiceOfferingVersionCreateOrEditPage'),
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
        component: () => import('@/pages/ServiceInstancesPage'),
      },
      // Admin
      {
        name: 'Admin',
        path: '/admin',
        component: () => import('@/pages/AdminPage'),
        meta: { adminPermissionRequired: true },
      },
    ],
  },
]

const router = new Router({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes: routes,

})

router.beforeEach((to, from, next) => {
  if (to.meta.developerPermissionRequired) {
    if (userStore.getters.isUserDeveloper) {
      next()
    } else {
      next('/access_denied')
    }
  } else if (to.meta.adminPermissionRequired) {
    if (userStore.getters.userRoles().includes('slm-admin')) {
      next()
    } else {
      next('/access_denied')
    }
  } else {
    next()
  }
})

export default router
