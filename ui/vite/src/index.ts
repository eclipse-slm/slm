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
  const state: { paths?: SlmPaths } = {}
  // The consumer root is only known in `config`, so sub-plugins get a getter and read paths when hooks run.
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
    apiClientsPlugin(paths),
    overridesPlugin(paths),
    driftCheckPlugin(paths, { allowNewFiles: options.allowNewFiles ?? false, version }),
    vue({ template: { transformAssetUrls } }),
    commonjs(),
    vuetify({ autoImport: true }),
  ]
}
