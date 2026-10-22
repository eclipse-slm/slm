import path from 'node:path'
import type { Plugin, ResolvedConfig, UserConfig } from 'vite'
import { describe, expect, it, vi } from 'vitest'
import { slmUi } from '../src/index.js'
import { fixturesDir } from './helpers'

const pkg = path.join(fixturesDir, 'basic', 'pkg')
const consumer = path.join(fixturesDir, 'basic', 'consumer')

function setup(userConfig: UserConfig, overridesDir: string | undefined = 'overrides') {
  const plugin = slmUi({ overridesDir, __packageRoot: pkg } as never)[0] as Plugin
  const result = (plugin.config as (c: UserConfig) => UserConfig)({ root: consumer, ...userConfig })
  return { plugin, result }
}

describe('config hook', () => {
  it('resolves a relative build.outDir against the consumer root', () => {
    expect(setup({ build: { outDir: 'out' } }).result.build?.outDir).toBe(path.join(consumer, 'out'))
  })
  it('keeps an absolute build.outDir and defaults to <consumer>/dist', () => {
    expect(setup({ build: { outDir: '/abs/out' } }).result.build?.outDir).toBe('/abs/out')
    expect(setup({}).result.build?.outDir).toBe(path.join(consumer, 'dist'))
  })
  it('respects an explicit emptyOutDir false and defaults to true', () => {
    expect(setup({ build: { emptyOutDir: false } }).result.build?.emptyOutDir).toBe(false)
    expect(setup({}).result.build?.emptyOutDir).toBe(true)
  })
  it('excludes the package aliases from dependency optimization', () => {
    expect(setup({}).result.optimizeDeps?.exclude).toEqual(expect.arrayContaining(['@', '@slm-original']))
  })
  it('warns when overridesDir does not exist', () => {
    const { plugin } = setup({}, 'nope')
    const warn = vi.fn()
    ;(plugin.configResolved as (c: ResolvedConfig) => void)({ logger: { warn } } as unknown as ResolvedConfig)
    expect(warn).toHaveBeenCalledWith(expect.stringContaining(path.join(consumer, 'nope')))
  })
  it('does not warn when overridesDir exists', () => {
    const { plugin } = setup({})
    const warn = vi.fn()
    ;(plugin.configResolved as (c: ResolvedConfig) => void)({ logger: { warn } } as unknown as ResolvedConfig)
    expect(warn).not.toHaveBeenCalled()
  })
})
