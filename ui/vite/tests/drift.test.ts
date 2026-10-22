import path from 'node:path'
import { describe, expect, it } from 'vitest'
import { driftCheckPlugin, findOrphanedOverrides, formatDriftError } from '../src/drift'
import { resolveSlmPaths } from '../src/paths'
import { buildFixture, fixturesDir } from './helpers'

function paths(fixture: string) {
  const root = path.join(fixturesDir, fixture)
  return resolveSlmPaths({ overridesDir: 'overrides' }, path.join(root, 'consumer'), path.join(root, 'pkg'))
}
const pkgSrc = (fixture: string) => paths(fixture).packageSrc

function buildDrift(opts: { allowNewFiles: boolean }) {
  const p = paths('drift')
  return buildFixture({
    fixture: 'drift',
    entry: path.join(p.packageRoot, 'src/main.ts'),
    plugins: [driftCheckPlugin(p, { ...opts, version: '1.6.0' })],
  })
}

describe('drift check', () => {
  it('lists orphaned overrides', () => {
    expect(findOrphanedOverrides(paths('drift'))).toEqual(['components/Gone.vue', 'pages/router.ts'])
  })
  it('returns nothing without an overridesDir', () => {
    expect(findOrphanedOverrides({ ...paths('drift'), overridesDir: undefined })).toEqual([])
  })
  it('formats the verbatim message and hints at the extension', () => {
    const msg = formatDriftError(['pages/router.ts'], pkgSrc('drift'), '1.6.0')
    expect(msg).toBe(
      'Override "pages/router.ts" has no upstream file (renamed or removed in @eclipse-slm/ui@1.6.0?) Upstream has "pages/router.js".',
    )
  })
  it('fails the build on orphans and passes with allowNewFiles', async () => {
    await expect(buildDrift({ allowNewFiles: false })).rejects.toThrow('components/Gone.vue')
    await expect(buildDrift({ allowNewFiles: true })).resolves.toBeTypeOf('string')
  })
})
