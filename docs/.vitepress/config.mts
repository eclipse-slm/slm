import { defineConfig } from 'vitepress'
import { tabsMarkdownPlugin } from 'vitepress-plugin-tabs'
import lightbox from "vitepress-plugin-lightbox"

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Eclipse SLM",
  description: "The Eclipse Service Lifecycle Management (SLM) provides a set of applications to manage the lifecycle of AI (artificial intelligence) services in production environments. The service lifecycle consists of the release, deploy, and operate phase and exits with the decommissioning of the service. It has a connection to the software development lifecycle (idea, design, code, build, test) and the AI model development lifecycle (idea, data acquisition, data analysis, data preparation, model training, model evaluation).",
  base: "/slm/",
  markdown: {
    config(md) {
      md.use(tabsMarkdownPlugin);
      md.use(lightbox, {});
    }
  },
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    siteTitle: "",
    logo: {
     light: '/img/logo.svg',
     dark: '/img/logo_white.svg'
    },

    nav: [
      {
        text: 'Home',
        link: '/',
      },
      {
        component: "versions"
      }
    ],

    search: {
      provider: "local"
    },

    sidebar:  {
      '/docs/': [
        {
          text: 'Getting Started',
          collapsed: true,
          items: [
            { text: "Overview", link: "/docs/getting-started/overview" },
            { text: "Architecture", link: "/docs/getting-started/architecture" },
            { text: "Installation", link: "/docs/getting-started/installation" },
            {
              text: "First Steps",
              path: "/docs/getting-started/first-steps/",
              collapsed: true,
              items: [
                { text: "Step 1: Add Resource", link: "/docs/getting-started/first-steps/step1-add-resource" },
                { text: "Step 2: Install Deployment Capability", link: "/docs/getting-started/first-steps/step2-install-deployment-capability" },
                { text: "Step 3: Create Service Vendor", link: "/docs/getting-started/first-steps/step3-create-service-vendor" },
                { text: "Step 4: Create Service Offering", link: "/docs/getting-started/first-steps/step4-create-service-offering" },
                { text: "Step 5: Deploy Service", link: "/docs/getting-started/first-steps/step5-deploy-service" }
              ]
            },
            { text: "Import", link: "/docs/getting-started/import" },
          ],
        },
        {
          text: 'Usage',
          collapsed: true,
          items: [
            { text: "Overview", link: "/docs/usage/" },
            {
              text: "Common",
              path: "/docs/usage/common/",
              collapsed: true,
              items: [

                { text: "User Management", link: "/docs/usage/common/user-management" },
                { text: "Credential Management", link: "/docs/usage/common/credential-management" },
                { text: "API", link: "/docs/usage/common/api" },
                { text: "Reporting Issues", link: "/docs/usage/common/reporting-issues" },
              ]
            },
            {
              text: "Resource Management",
              path: "/docs/usage/resource-management/",
              collapsed: true,
              items: [
                { text: "Capabilities", link: "/docs/usage/resource-management/capabilities" },
                { text: "Clusters", link: "/docs/usage/resource-management/clusters" },
                { text: "Discovery", link: "/docs/usage/resource-management/discovery" },
                { text: "Profiler", link: "/docs/usage/resource-management/profiler" },
                { text: "Firmware Update", link: "/docs/usage/resource-management/firmware-update" },
              ]
            },
            {
              text: "Service Management",
              path: "/docs/usage/common/",
              collapsed: true,
              items: [
                { text: "Service Offerings", link: "/docs/usage/service-management/service-offerings" }
              ]
            },
          ],
        },
        {
          text: 'Development',
          collapsed: true,
          items: [
            { text: "Development Environment", link: "/docs/development/development-environment" },
            { text: "Debugging", link: "/docs/development/debugging" },
           ],
        },
      ]
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/eclipse-slm/slm' }
    ]
  }
})
