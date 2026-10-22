import { describe, expect, it } from 'vitest'
// @ts-expect-error plain ESM script without type declarations
import { toNpmVersion } from '../../scripts/npm-version.mjs'

describe('toNpmVersion', () => {
  it('maps a SNAPSHOT revision to a snapshot prerelease on the next tag', () => {
    expect(toNpmVersion('1.5.0-SNAPSHOT', 42)).toEqual({ version: '1.5.0-snapshot.42', distTag: 'next' })
  })
  it('maps a release revision to itself on the latest tag', () => {
    expect(toNpmVersion('1.5.0', 42)).toEqual({ version: '1.5.0', distTag: 'latest' })
  })
  it('rejects unsupported revisions', () => {
    expect(() => toNpmVersion('1.5', 1)).toThrow('Unsupported revision "1.5"')
  })
})
