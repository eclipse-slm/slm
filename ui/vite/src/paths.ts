import fs from 'node:fs'
import path from 'node:path'

export interface SlmUiOptions {
  /** Directory whose files shadow same-path files in the package `src/`. */
  overridesDir?: string
  /** Directory with the consumer's generated API clients. */
  apiClientsDir?: string
  allowNewFiles?: boolean
}

/** All paths are absolute and realpath'd. */
export interface SlmPaths {
  packageRoot: string
  packageSrc: string
  overridesDir?: string
  apiClientsDir: string
}

function real(p: string): string {
  try {
    return fs.realpathSync(p)
  } catch {
    return path.resolve(p)
  }
}

export function resolveSlmPaths(options: SlmUiOptions, consumerRoot: string, packageRoot: string): SlmPaths {
  const root = real(packageRoot)
  return {
    packageRoot: root,
    packageSrc: path.join(root, 'src'),
    overridesDir: options.overridesDir ? real(path.resolve(consumerRoot, options.overridesDir)) : undefined,
    apiClientsDir: real(path.resolve(consumerRoot, options.apiClientsDir ?? 'src/api')),
  }
}
