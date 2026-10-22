import fs from 'node:fs'
import path from 'node:path'
import type { Plugin } from 'vite'
import type { SlmPaths } from './paths.js'

export const API_SERVICES = [
  'resource-management',
  'platform-management',
  'service-management',
  'notification-service',
] as const

function realIfExists(file: string): string {
  try {
    return fs.realpathSync(file)
  } catch {
    return file
  }
}

/** Redirects the (gitignored, generated) API clients of the package to the consumer's own generated clients. */
export function apiClientsPlugin(paths: SlmPaths): Plugin {
  const { packageSrc, apiClientsDir } = paths
  const packageApi = path.join(packageSrc, 'api')
  const pattern = new RegExp(`^(${API_SERVICES.join('|')})/client(/.*)?$`)

  return {
    name: 'slm-ui:api-clients',
    enforce: 'pre',
    async resolveId(source, importer, opts) {
      if (source.startsWith('\0')) return null
      let abs: string
      if (path.isAbsolute(source)) {
        abs = source
      } else if (source.startsWith('.') && importer) {
        abs = path.resolve(path.dirname(realIfExists(importer.split('?')[0])), source)
      } else {
        return null
      }
      const rel = path.relative(packageApi, abs).split(path.sep).join('/')
      if (rel.startsWith('..')) return null
      const match = pattern.exec(rel)
      if (!match) return null

      const [, service, rest = ''] = match
      const clientDir = path.join(apiClientsDir, service, 'client')
      if (!fs.existsSync(path.join(clientDir, 'index.ts'))) {
        this.error(
          `Missing generated API client for "${service}": expected ${path.join(clientDir, 'index.ts')}. ` +
            `Generate the API clients into "${apiClientsDir}" (apiClientsDir option).`,
        )
      }
      const target = path.join(clientDir, rest)
      // Identity mapping (the package itself): let the normal resolution continue.
      if (path.resolve(target) === path.resolve(abs)) return null
      return this.resolve(target, importer, { ...opts, skipSelf: true })
    },
  }
}
