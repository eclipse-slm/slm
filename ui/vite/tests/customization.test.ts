import { describe, expect, it } from 'vitest'
import { deepMerge } from '@/customization/deepMerge'

describe('deepMerge', () => {
  it('merges nested keys and keeps untouched siblings', () => {
    const base = { a: { x: 1, y: 2 }, b: 'keep' }
    expect(deepMerge(base, { a: { x: 9 } })).toEqual({ a: { x: 9, y: 2 }, b: 'keep' })
  })

  it('replaces arrays instead of merging them', () => {
    const base = { list: [1, 2, 3] }
    expect(deepMerge(base, { list: [4] })).toEqual({ list: [4] })
  })

  it('does not mutate its inputs', () => {
    const base = { a: { x: 1 } }
    const patch = { a: { x: 2 } }
    const result = deepMerge(base, patch)
    expect(base).toEqual({ a: { x: 1 } })
    expect(patch).toEqual({ a: { x: 2 } })
    expect(result.a).not.toBe(base.a)
  })
})
