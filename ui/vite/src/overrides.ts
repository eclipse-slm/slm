import fs from 'node:fs'
import path from 'node:path'
import type { Plugin } from 'vite'
import type { SlmPaths } from './paths.js'

const ORIGINAL_PREFIX = '@slm-original/'
const ALIAS_PREFIX = '@/'

function splitQuery(id: string): [string, string] {
  const i = id.indexOf('?')
  return i === -1 ? [id, ''] : [id.slice(0, i), id.slice(i)]
}

function isInside(file: string, dir: string): boolean {
  const rel = path.relative(dir, file)
  return rel !== '' && !rel.startsWith('..') && !path.isAbsolute(rel)
}

function realIfExists(file: string): string {
  try {
    return fs.realpathSync(file)
  } catch {
    return file
  }
}

export function overridesPlugin(paths: SlmPaths): Plugin {
  const { packageSrc, overridesDir } = paths

  function toOverride(resolvedId: string): string | undefined {
    if (!overridesDir) return undefined
    const [file, query] = splitQuery(resolvedId)
    const real = realIfExists(file)
    if (!isInside(real, packageSrc)) return undefined
    const candidate = path.join(overridesDir, path.relative(packageSrc, real))
    return fs.existsSync(candidate) ? realIfExists(candidate) + query : undefined
  }

  return {
    name: 'slm-ui:overrides',
    enforce: 'pre',
    async resolveId(source, importer, opts) {
      if (source.startsWith('\0')) return null
      const skip = { ...opts, skipSelf: true }

      if (source.startsWith(ORIGINAL_PREFIX)) {
        const target = path.join(packageSrc, source.slice(ORIGINAL_PREFIX.length))
        return this.resolve(target, importer, skip)
      }

      if (!overridesDir) return null

      if (source.startsWith(ALIAS_PREFIX)) {
        source = path.join(packageSrc, source.slice(ALIAS_PREFIX.length))
      }

      if (importer && source.startsWith('.')) {
        const importerFile = realIfExists(splitQuery(importer)[0])
        if (isInside(importerFile, overridesDir)) {
          const dir = path.dirname(path.relative(overridesDir, importerFile))
          const inOverrides = await this.resolve(path.join(overridesDir, dir, source), importer, skip)
          if (inOverrides) return inOverrides
          const inPackage = await this.resolve(path.join(packageSrc, dir, source), importer, skip)
          if (!inPackage) return null
          return toOverride(inPackage.id) ?? inPackage
        }
      }

      const resolved = await this.resolve(source, importer, skip)
      if (!resolved || resolved.external) return resolved
      const override = toOverride(resolved.id)
      return override ? { ...resolved, id: override } : resolved
    },
  }
}
