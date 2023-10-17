module.exports = {
    title: 'EclipseSLM',
    description: 'Eclipse Service Lifecycle Management',
    base: `/slm/${process.env.VERSION_PATH || ''}`,
    themeConfig: {
          repo: 'eclipse-slm/slm',
          logo: '/img/logo.svg',
          editLinks: false,
          docsDir: '',
          editLinkText: '',
          lastUpdated: false,
          nav: [
              { text: 'Home', link: '/' },
          ],
          sidebar: {
              '/docs/': [
                {
                  title: 'Getting Started',
                  collapsable: true,
                  children: [
                    'getting-started/',
                    'getting-started/overview',
                    'getting-started/architecture',
                    'getting-started/installation',
                    {
                      title: "First Steps",
                      path: "/docs/getting-started/first-steps/",
                      collapsable: true,
                      children: [
                        '/docs/getting-started/first-steps/step1-add-resource',
                        '/docs/getting-started/first-steps/step2-install-deployment-capability',
                        '/docs/getting-started/first-steps/step3-create-service-vendor',
                        '/docs/getting-started/first-steps/step4-create-service-offering',
                        '/docs/getting-started/first-steps/step5-deploy-service',
                      ]
                    },
                    'getting-started/import',
                  ],
                },
                {
                  title: 'Usage',
                  collapsable: true,
                  children: [
                    'usage/',
                    'usage/capabilities',
                    'usage/clusters',
                    'usage/profiler',
                    'usage/service-offerings',
                    'usage/api',
                    'usage/reporting-issues',
                  ],
                },
                {
                  title: 'Development',
                  collapsable: true,
                  children: [
                    'development/development-environment',
                    'development/debugging',
                  ],
                },
              ]
          },
      },
      plugins: [
        '@vuepress/plugin-back-to-top',
        '@vuepress/plugin-medium-zoom',
        '@dovyp/vuepress-plugin-clipboard-copy',
        '@vuepress/medium-zoom'
      ],
}
