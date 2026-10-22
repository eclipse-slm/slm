import fs from 'node:fs'
import path from 'node:path'
import type { Plugin } from 'vite'
import type { SlmPaths } from './paths.js'

function listFiles(dir: string, base = dir): string[] {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const full = path.join(dir, entry.name)
    return entry.isDirectory() ? listFiles(full, base) : [path.relative(base, full).split(path.sep).join('/')]
  })
}

/** Override files (posix, relative to `overridesDir`) that have no same-path file in the package `src/`. */
export function findOrphanedOverrides(paths: SlmPaths): string[] {
  const { overridesDir, packageSrc } = paths
  if (!overridesDir || !fs.existsSync(overridesDir)) return []
  return listFiles(overridesDir)
    .filter((rel) => !fs.existsSync(path.join(packageSrc, rel)))
    .sort()
}

function siblingWithOtherExtension(rel: string, packageSrc: string): string | undefined {
  const dir = path.posix.dirname(rel)
  const base = path.posix.basename(rel, path.posix.extname(rel))
  const absDir = path.join(packageSrc, dir)
  if (!fs.existsSync(absDir)) return undefined
  const match = fs
    .readdirSync(absDir, { withFileTypes: true })
    .filter((e) => e.isFile())
    .map((e) => e.name)
    .sort()
    .find((name) => path.posix.basename(name, path.posix.extname(name)) === base)
  return match ? (dir === '.' ? match : `${dir}/${match}`) : undefined
}

export function formatDriftError(orphans: string[], packageSrc: string, version: string): string {
  return orphans
    .map((rel) => {
      const line = `Override "${rel}" has no upstream file (renamed or removed in @eclipse-slm/ui@${version}?)`
      const sibling = siblingWithOtherExtension(rel, packageSrc)
      return sibling ? `${line} Upstream has "${sibling}".` : line
    })
    .join('\n')
}

export function driftCheckPlugin(paths: SlmPaths, opts: { allowNewFiles: boolean; version: string }): Plugin {
  return {
    name: 'slm-ui:drift-check',
    buildStart() {
      if (opts.allowNewFiles) return
      const orphans = findOrphanedOverrides(paths)
      if (orphans.length > 0) this.error(formatDriftError(orphans, paths.packageSrc, opts.version))
    },
  }
}
