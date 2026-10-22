import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import vue from '@vitejs/plugin-vue'
import commonjs from 'vite-plugin-commonjs'
import vuetify, { transformAssetUrls } from 'vite-plugin-vuetify'
import type { Plugin, PluginOption } from 'vite'
import { apiClientsPlugin } from './apiClients'
import { driftCheckPlugin } from './drift'
import { overridesPlugin } from './overrides'
import { resolveSlmPaths, type SlmPaths, type SlmUiOptions } from './paths'

export type { SlmUiOptions }

/** Internal, test-only: not part of the public `SlmUiOptions`. */
type InternalOptions = SlmUiOptions & { __packageRoot?: string }

/**
 * The consumer root is only known in the `config` hook, but the sub-plugins need resolved paths when they are
 * created. So `config` fills `state`, and each sub-plugin is created lazily from it on first hook use.
 */
function lazyPlugin(name: string, hookName: 'resolveId' | 'buildStart', make: () => Plugin): Plugin {
  let inner: Plugin | undefined
  const delegate = function (this: unknown, ...args: unknown[]) {
    const h = (inner ??= make())[hookName] as unknown
    const fn = typeof h === 'function' ? h : (h as { handler: Function }).handler
    return fn.apply(this, args)
  }
  return { name, enforce: 'pre', [hookName]: delegate } as Plugin
}

export function slmUi(options: SlmUiOptions = {}): PluginOption[] {
  const packageRoot =
    (options as InternalOptions).__packageRoot ?? path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..')
  const version: string = JSON.parse(fs.readFileSync(path.join(packageRoot, 'package.json'), 'utf8')).version
  const state: { paths?: SlmPaths } = {}
  const paths = () => state.paths!

  const configPlugin: Plugin = {
    name: 'slm-ui:config',
    config(userConfig) {
      const consumerRoot = path.resolve(userConfig.root ?? process.cwd())
      state.paths = resolveSlmPaths(options, consumerRoot, packageRoot)
      const { packageSrc } = state.paths
      return {
        root: packageRoot,
        envDir: consumerRoot,
        publicDir: path.join(packageRoot, 'public'),
        build: {
          outDir: userConfig.build?.outDir ?? path.join(consumerRoot, 'dist'),
          emptyOutDir: true,
          commonjsOptions: { transformMixedEsModules: true },
        },
        resolve: {
          alias: { '@': packageSrc },
          extensions: ['.js', '.json', '.jsx', '.mjs', '.ts', '.tsx', '.vue'],
        },
        css: { preprocessorOptions: { scss: { api: 'modern-compiler' } } },
        server: { fs: { allow: [packageRoot, consumerRoot] } },
      }
    },
    configureServer(server) {
      const dir = paths().overridesDir
      if (!dir) return
      server.watcher.add(dir)
      const restart = (file: string) => {
        if (file === dir || file.startsWith(dir + path.sep)) void server.restart()
      }
      server.watcher.on('add', restart)
      server.watcher.on('unlink', restart)
    },
  }

  return [
    configPlugin,
    lazyPlugin('slm-ui:api-clients', 'resolveId', () => apiClientsPlugin(paths())),
    lazyPlugin('slm-ui:overrides', 'resolveId', () => overridesPlugin(paths())),
    lazyPlugin('slm-ui:drift-check', 'buildStart', () =>
      driftCheckPlugin(paths(), { allowNewFiles: options.allowNewFiles ?? false, version }),
    ),
    vue({ template: { transformAssetUrls } }),
    commonjs(),
    vuetify({ autoImport: true }),
  ]
}
