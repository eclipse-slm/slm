import fs from 'node:fs'
import path from 'node:path'
import { build } from 'vite'
import { afterAll, describe, expect, it } from 'vitest'
import { slmUi } from '../src/index'
import { fixturesDir } from './helpers'

const consumer = path.join(fixturesDir, 'basic', 'consumer')
const dist = path.join(consumer, 'dist')
afterAll(() => fs.rmSync(dist, { recursive: true, force: true }))

describe('slmUi', () => {
  it('builds the consumer with overrides into <consumer>/dist', async () => {
    const pkg = path.join(fixturesDir, 'basic', 'pkg')
    const options = { overridesDir: 'overrides', __packageRoot: pkg }
    await build({
      root: consumer,
      configFile: false,
      logLevel: 'silent',
      plugins: slmUi(options),
      build: { minify: false, rollupOptions: { input: path.join(pkg, 'src/main.ts'), external: ['vue'] } },
    })
    const files = fs.readdirSync(dist, { recursive: true, encoding: 'utf8' }).filter((f) => f.endsWith('.js'))
    const code = files.map((f) => fs.readFileSync(path.join(dist, f), 'utf8')).join('\n')
    expect(code).toContain('OVERRIDE_A')
  })
})
