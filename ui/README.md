# UI

## Getting Started
- Install Nodejs from [Nodejs Official Page](https://nodejs.org/en/)
- Open the terminal and navigate to the project
- Run `npm install` or `yarn install` if you use [Yarn](https://yarnpkg.com/en/)
- Run `npm run dev` or `yarn serve` to start a local development server

## File Structure

Within the download you'll find the following directories and files:

```
ui
├── README.md
├── CHANGELOG.md
├── jest.config.js
├── now.json
├── package.json
├── public
│   ├── favicon.ico
│   └── index.html
├── src
│   ├── App.vue
│   ├── assets
│   │   └── vuetify.svg
│   ├── components
│   │   └── base
│   │       ├── Card.vue
│   │       ├── Item.vue
│   │       ├── ItemGroup.vue
│   │       ├── ItemSubGroup.vue
│   │       ├── MaterialAlert.vue
│   │       ├── MaterialCard.vue
│   │       ├── MaterialChartCard.vue
│   │       ├── MaterialSnackbar.vue
│   │       ├── MaterialStatsCard.vue
│   │       ├── MaterialTabs.vue
│   │       ├── MaterialTestimony.vue
│   │       ├── Subheading.vue
│   │       └── VComponent.vue
│   ├── i18n.js
│   ├── locales
│   │   └── en.json
│   ├── main.ts
│   ├── plugins
│   │   ├── base.js
│   │   ├── chartist.js
│   │   ├── vee-validate.js
│   │   └── vuetify.js
│   ├── router.js
│   ├── sass
│   │   ├── main.scss
│   │   ├── overrides.sass
│   │   └── vuetify-material
│   │       └── _sidebar.sass
│   ├── stores
│       └── catalogStore.ts
│       └── jobsStore.ts
│       └── notificationStore.ts
│       └── providerStore.ts
│       └── resourceDevicesStore.ts
│       └── resourceClustersStore.ts
│       └── serviceOfferingsStore.ts
│       └── serviceInstancesStore.ts
│       └── store.ts
│       └── userStore.ts
│   └── views
│       └── dashboard
│           ├── Charts.vue
│           ├── Dashboard.vue
│           ├── Index.vue
│           ├── Widgets.vue
│           ├── component
│           │   ├── Buttons.vue
│           │   ├── Grid.vue
│           │   ├── Icons.vue
│           │   ├── Notifications.vue
│           │   ├── Tabs.vue
│           │   └── Typography.vue
│           ├── components
│           │   └── core
│           │       ├── AppBar.vue
│           │       ├── Drawer.vue
│           │       ├── Footer.vue
│           │       ├── Settings.vue
│           │       └── View.vue
│           ├── maps
│           │   └── GoogleMaps.vue
│           ├── pages
│           │   └── UserProfile.vue
│           └── tables
│               └── RegularTables.vue
├── tests
│   ├── e2e
│   │   ├── plugins
│   │   │   └── index.js
│   │   ├── specs
│   │   │   └── test.js
│   │   └── support
│   │       ├── commands.js
│   │       └── index.js
│   └── unit
│       └── example.spec.js
├── vue.config.js
```

## Building a custom SLM UI

`@eclipse-slm/ui` is published to npmjs.com as **unbuilt source** plus a Vite plugin (`@eclipse-slm/ui/vite`). A
consumer project builds the SLM UI itself and can shadow individual files, so a customized UI does not need a fork.

### 1. Consumer setup

`package.json`:

```json
{
  "name": "my-slm-ui",
  "private": true,
  "type": "module",
  "scripts": { "dev": "vite", "build": "vite build" },
  "dependencies": {
    "@eclipse-slm/ui": "^1.5.0",
    "pinia": "^2.1.4",
    "vite": "^7.3.1",
    "vue": "^3.4.27",
    "vue-i18n": "^11.2.8",
    "vue-router": "^4.3.2",
    "vuetify": "^3.6.9"
  }
}
```

`vite.config.mts`:

```ts
import { defineConfig } from 'vite'
import { slmUi } from '@eclipse-slm/ui/vite'

export default defineConfig({
  plugins: [slmUi({ overridesDir: 'src/overrides' })],
})
```

`npm run dev` and `npm run build` work with this configuration alone; the plugin sets up dependency pre-bundling for
the package source in `node_modules`, so no extra `optimizeDeps` or CommonJS settings are needed.

Plugin options:

| Option          | Default   | Meaning                                                                                  |
|-----------------|-----------|------------------------------------------------------------------------------------------|
| `overridesDir`  | -         | Directory (relative to the consumer root) whose files shadow files of the package `src`. |
| `apiClientsDir` | `src/api` | Directory containing the generated API clients (see "API clients").                      |
| `allowNewFiles` | `false`   | Allow files in `overridesDir` that have no upstream counterpart (disables the drift check). |

### 2. Overrides and `@slm-original`

A file in `overridesDir` replaces the package file with the same path relative to the package `src`. For example
`src/overrides/components/core/Footer.vue` replaces `src/components/core/Footer.vue`. Inside an override, import the
upstream version with the `@slm-original/` prefix:

```vue
<script setup lang="ts">
import OriginalCard from '@slm-original/components/base/MaterialCard.vue'
</script>
```

**Drift check:** if an override has no upstream file (for example because the file was renamed or removed in a newer
`@eclipse-slm/ui`), the build fails with
`Override "<path>" has no upstream file (renamed or removed in @eclipse-slm/ui@<version>?)`, naming an upstream file
with a different extension if there is one. Set `allowNewFiles: true` to add genuinely new files.

The dev server restarts automatically when override files are added or removed.

### 3. Customization hooks

The hooks are ordinary overridable files in `src/customization/` (and `src/app/` for the data they work on). Shadow
the file in your `overridesDir`, for example `src/overrides/customization/customizeRoutes.ts`.

**`customizeRoutes(routes)`** adds, removes or changes routes:

```ts
import type { RouteRecordRaw } from 'vue-router'
export default function customizeRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
  return [...routes, { path: '/mine', component: () => import('@/pages/MyPage.vue') }]
}
```

**`customizeNavigation(items)`** changes the drawer entries (`NavItem`). `titleKey` is an i18n key, `visible` is a
boolean or `(ctx: { roles: string[]; isDeveloper: boolean }) => boolean`; other fields are `id`, `icon`, `to`,
`group`, `children` and the rendering hints `divider`, `text`, `subGroup`, `goToChildOnClick`:

```ts
import type { NavItem } from '@/app/navigation'
export default function customizeNavigation(items: NavItem[]): NavItem[] {
  return [...items, { id: 'nav-mine', icon: 'mdi-star', titleKey: 'mine.title', to: '/mine',
                      visible: ({ roles }) => roles.includes('slm-admin') }]
}
```

**`customizeMessages(messages)`** adds or overrides i18n messages (keyed by locale). Use `deepMerge` to keep the rest:

```ts
import { deepMerge } from '@/customization/deepMerge'
import type { Messages } from '@/customization/customizeMessages'
export default function customizeMessages(messages: Messages): Messages {
  return deepMerge(messages, { en: { mine: { title: 'My page' } } })
}
```

**`customizeApp(app)`** runs before the Vue app is mounted, to register plugins or components:

```ts
import type { App } from 'vue'
export default function customizeApp(app: App): void {
  app.component('MyWidget', MyWidget)
}
```

The app instance itself lives in `@/app/appInstance` (`app`, and `globals` = `app.config.globalProperties`, e.g.
`globals.$keycloak`). `@/main` still re-exports `globals` for backward compatibility, but new code and overrides should
import from `@/app/appInstance`: any module importing `@/main` makes the dev server evaluate the entry a second time.

**`theme.ts`** (`src/customization/theme.ts`) is a default export `{ colors, variables }` used for the Vuetify theme:

```ts
export default { colors: { primary: '#123456', secondary: '#00A0E3' }, variables: {} }
```

### 4. API clients

The package does **not** ship the generated API clients. The consumer must generate them with OpenAPI Generator
(generator `typescript-axios`) from the services' OpenAPI specs into:

```
<apiClientsDir>/resource-management/client/index.ts
<apiClientsDir>/platform-management/client/index.ts
<apiClientsDir>/service-management/client/index.ts
<apiClientsDir>/notification-service/client/index.ts
```

`ui/pom.xml` in this repository is the reference configuration (generator, input specs and output directories). If a
client is missing, the build fails with `Missing generated API client for "<service>": expected <path>`.

The generated clients import `axios` from the consumer's directory, so add `axios` as a **direct dependency** of your
project (required with strict layouts such as pnpm, where the package's own `axios` is not visible to your files).

### 5. Docker

Copy `node_modules/@eclipse-slm/ui/src/docker/*` (`entrypoint.sh`, `nginx.conf`, `slm.conf`) into your image build
context and use the upstream [`ui/Dockerfile`](Dockerfile) as a template, replacing its build stage with your
consumer's `npm ci && npm run build`.

### 6. Upgrading

- npm dist-tags: `latest` is the last release (`X.Y.Z`), `next` is the latest `develop` snapshot
  (`X.Y.Z-snapshot.<run_number>`). Use a range such as `^1.5.0` for releases. Caret ranges do not match prereleases, so to try a snapshot install `@eclipse-slm/ui@next` or an exact `1.5.0-snapshot.N`.
- After an upgrade, run a build. The drift check reports overrides whose upstream file was renamed or removed.
- Compatibility promise: any shadowable file may change in a minor release, and the drift check surfaces that.
  Changes to **plugin options, hook signatures or `NavItem`** are breaking and only happen in a major release.

### 7. For maintainers: publishing

The `ui-package` job in `.github/workflows/build-core.yml` tests, builds and verifies the package on every run and
publishes with npm trusted publishing (OIDC, with provenance, no `NPM_TOKEN`). The version comes from `<revision>` in
the root `pom.xml` (never edit `ui/package.json`'s version by hand): `develop` publishes `X.Y.Z-snapshot.<run>` as
`next`, a release tag on `main` publishes `X.Y.Z` as `latest`.

Bootstrap (once): trusted publishing can only be configured for an existing package, so

1. publish the first version manually with a personal npm account that can publish to the `@eclipse-slm` scope.
   Do not publish the checked-in `ui/package.json` version. Set the version first, exactly as CI does, and use the
   printed dist-tag:
   `node ui/scripts/npm-version.mjs <revision> <n>` (prints `dist_tag=...`), then
   `npm --prefix ui publish --access public --tag <dist_tag>` (a snapshot revision gives `--tag next`);
2. on npmjs.com, open the package settings, add a **trusted publisher** (GitHub Actions) with repository
   `eclipse-slm/slm` and workflow `build-core.yml`.
