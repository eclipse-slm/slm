import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import vue from '@vitejs/plugin-vue'
import commonjsModule from 'vite-plugin-commonjs'
import vuetify, { transformAssetUrls } from 'vite-plugin-vuetify'
import type { Plugin, PluginOption } from 'vite'
import { apiClientsPlugin } from './apiClients.js'
import { driftCheckPlugin } from './drift.js'
import { overridesPlugin } from './overrides.js'
import { resolveSlmPaths, type SlmPaths, type SlmUiOptions } from './paths.js'

export type { SlmUiOptions }

// Under NodeNext the CJS typings expose the module namespace as default; at runtime default is the function.
const commonjs = commonjsModule as unknown as typeof commonjsModule.default

/** Internal, test-only: not part of the public `SlmUiOptions`. */
type InternalOptions = SlmUiOptions & { __packageRoot?: string }

export function slmUi(options: SlmUiOptions = {}): PluginOption[] {
  const packageRoot =
    (options as InternalOptions).__packageRoot ?? path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..')
  const version: string = JSON.parse(fs.readFileSync(path.join(packageRoot, 'package.json'), 'utf8')).version
  const state: { paths?: SlmPaths; consumerRoot?: string } = {}
  // The consumer root is only known in `config`, so sub-plugins get a getter and read paths when hooks run.
  const paths = () => state.paths!

  const configPlugin: Plugin = {
    name: 'slm-ui:config',
    config(userConfig) {
      const consumerRoot = path.resolve(userConfig.root ?? process.cwd())
      state.consumerRoot = consumerRoot
      state.paths = resolveSlmPaths(options, consumerRoot, packageRoot)
      const { packageSrc } = state.paths
      return {
        root: packageRoot,
        envDir: consumerRoot,
        publicDir: path.join(packageRoot, 'public'),
        build: {
          outDir: path.resolve(consumerRoot, userConfig.build?.outDir ?? 'dist'),
          emptyOutDir: userConfig.build?.emptyOutDir ?? true,
          commonjsOptions: { transformMixedEsModules: true },
        },
        resolve: {
          alias: { '@': packageSrc },
          extensions: ['.js', '.json', '.jsx', '.mjs', '.ts', '.tsx', '.vue'],
        },
        // The package src lives under node_modules, so without this Vite's dep scanner would prebundle raw `@/...`
        // imports with esbuild, bypassing the slm plugins (API-client redirect, overrides).
        optimizeDeps: { exclude: ['@', '@slm-original'] },
        server: { fs: { allow: [packageRoot, consumerRoot] } },
      }
    },
    configResolved(config) {
      const dir = options.overridesDir && path.resolve(state.consumerRoot!, options.overridesDir)
      if (dir && !fs.existsSync(dir)) {
        config.logger.warn(`[slm-ui] overridesDir "${options.overridesDir}" does not exist (resolved to ${dir}); no overrides will be applied.`)
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
    apiClientsPlugin(paths),
    overridesPlugin(paths),
    driftCheckPlugin(paths, { allowNewFiles: options.allowNewFiles ?? false, version }),
    vue({ template: { transformAssetUrls } }),
    commonjs(),
    vuetify({ autoImport: true }),
  ]
}
