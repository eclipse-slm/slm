import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { build, type PluginOption } from 'vite'
import type { OutputAsset, OutputChunk, RollupOutput } from 'rollup'

const here = path.dirname(fileURLToPath(import.meta.url))
export const fixturesDir = path.join(here, 'fixtures')

/** Builds `entry` (relative to the fixture dir) and returns all JS and CSS output as one string. */
export async function buildFixture(opts: { fixture: string; plugins: PluginOption[]; entry: string }): Promise<string> {
  const root = path.join(fixturesDir, opts.fixture)
  const result = await build({
    root,
    configFile: false,
    logLevel: 'silent',
    plugins: opts.plugins,
    build: {
      write: false,
      minify: false,
      cssCodeSplit: false,
      rollupOptions: { input: path.resolve(root, opts.entry), external: ['vue'] },
    },
  })
  const outputs = (Array.isArray(result) ? result : [result as RollupOutput]).flatMap((r) => r.output)
  return outputs
    .map((o) => (o.type === 'chunk' ? (o as OutputChunk).code : String((o as OutputAsset).source)))
    .join('\n')
}
