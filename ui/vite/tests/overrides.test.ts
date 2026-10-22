import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import vue from '@vitejs/plugin-vue'
import { afterAll, describe, expect, it } from 'vitest'
import { overridesPlugin } from '../src/overrides'
import { resolveSlmPaths, type SlmUiOptions } from '../src/paths'
import { buildFixture, fixturesDir } from './helpers'

const tmpDirs: string[] = []
afterAll(() => tmpDirs.forEach((d) => fs.rmSync(d, { recursive: true, force: true })))

async function run(fixture: string, packageRoot: string, options: SlmUiOptions): Promise<string> {
  const consumerRoot = path.join(fixturesDir, fixture, 'consumer')
  const paths = resolveSlmPaths(options, consumerRoot, packageRoot)
  return buildFixture({
    fixture,
    entry: path.join(packageRoot, 'src/main.ts'),
    plugins: [
      { name: 'test-alias', config: () => ({ resolve: { alias: { '@': paths.packageSrc } } }) },
      overridesPlugin(paths),
      vue(),
    ],
  })
}

function build(fixture: string, options: SlmUiOptions = { overridesDir: 'overrides' }) {
  return run(fixture, path.join(fixturesDir, fixture, 'pkg'), options)
}

function buildViaSymlink(fixture: string) {
  const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'slm-ui-'))
  tmpDirs.push(tmp)
  const real = path.join(tmp, 'real-pkg')
  fs.cpSync(path.join(fixturesDir, fixture, 'pkg'), real, { recursive: true })
  fs.mkdirSync(path.join(tmp, 'node_modules'))
  const link = path.join(tmp, 'node_modules', 'pkg')
  fs.symlinkSync(real, link)
  return run(fixture, link, { overridesDir: 'overrides' })
}

describe('overridesPlugin', () => {
  it('uses the override for a shadowed file', async () => {
    expect(await build('basic')).toContain('OVERRIDE_A')
  })
  it('resolves @slm-original to the package file', async () => {
    expect(await build('basic')).toContain('UPSTREAM_A')
  })
  it('resolves relative imports from an override to the package when not overridden', async () => {
    expect(await build('basic')).toContain('UPSTREAM_ROW')
  })
  it('serves all SFC sub-requests from the override', async () => {
    expect(await build('basic')).toContain('.override-sfc')
  })
  it('keeps the original SFC blocks when an override wraps it via @slm-original', async () => {
    const code = await build('basic')
    expect(code).toContain('.override-sfc')
    expect(code).toContain('.upstream-sfc')
  })
  it('applies overrides for imports through the @ alias', async () => {
    const code = await build('basic')
    expect(code).toContain('OVERRIDE_B')
    expect(code).not.toContain('UPSTREAM_B')
  })
  it('applies overrides when the package dir is a symlink', async () => {
    expect(await buildViaSymlink('basic')).toContain('OVERRIDE_A')
  })
  it('changes nothing without overridesDir', async () => {
    const code = await build('basic', { overridesDir: undefined })
    expect(code).toContain('UPSTREAM_A')
    expect(code).not.toContain('OVERRIDE_A')
  })
})
