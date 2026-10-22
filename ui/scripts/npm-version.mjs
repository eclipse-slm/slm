#!/usr/bin/env node
// Maps the Maven <revision> to an npm version and dist-tag, sets it in package.json and prints `dist_tag=<tag>`.
// Usage: node ui/scripts/npm-version.mjs <revision> <runNumber>
import { execFileSync } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

export function toNpmVersion(revision, runNumber) {
  const match = /^(\d+\.\d+\.\d+)(-SNAPSHOT)?$/.exec(revision)
  if (!match) throw new Error(`Unsupported revision "${revision}"`)
  const [, base, snapshot] = match
  return snapshot
    ? { version: `${base}-snapshot.${runNumber}`, distTag: 'next' }
    : { version: base, distTag: 'latest' }
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const [revision, runNumber] = process.argv.slice(2)
  if (!revision || !runNumber) {
    console.error('Usage: npm-version.mjs <revision> <runNumber>')
    process.exit(1)
  }
  const { version, distTag } = toNpmVersion(revision, runNumber)
  const uiDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
  execFileSync('npm', ['version', version, '--no-git-tag-version', '--allow-same-version'], {
    cwd: uiDir,
    stdio: ['ignore', 'ignore', 'inherit'],
  })
  console.log(`dist_tag=${distTag}`)
}
