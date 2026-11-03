import { beforeEach, describe, expect, it, vi } from 'vitest'
import { filterIcons, loadMdiIcons, MDI_META_URL, resetMdiIconCache } from './mdiIcons'
import type { MdiIconMeta } from './mdiIcons'

const okFetch = (data: unknown) =>
  vi.fn(async () => ({ ok: true, json: async () => data })) as unknown as typeof fetch

describe('loadMdiIcons', () => {
  beforeEach(() => resetMdiIconCache())

  it('loads and drops deprecated', async () => {
    const fetchFn = okFetch([
      { name: 'docker', aliases: [], tags: ['Brand / Logo'], deprecated: false },
      { name: 'old', aliases: [], tags: [], deprecated: true },
    ])
    const icons = await loadMdiIcons(fetchFn)
    expect(icons.map((i) => i.name)).toEqual(['docker'])
    expect(fetchFn).toHaveBeenCalledWith(MDI_META_URL)
  })

  it('caches successful load', async () => {
    const fetchFn = okFetch([{ name: 'docker', aliases: [], tags: [], deprecated: false }])
    await loadMdiIcons(fetchFn)
    await loadMdiIcons(fetchFn)
    expect(fetchFn).toHaveBeenCalledTimes(1)
  })

  it('does not cache failure (rejection)', async () => {
    const failing = vi.fn(async () => {
      throw new Error('network')
    }) as unknown as typeof fetch
    await expect(loadMdiIcons(failing)).rejects.toThrow()
    const icons = await loadMdiIcons(
      okFetch([{ name: 'docker', aliases: [], tags: [], deprecated: false }]),
    )
    expect(icons).toHaveLength(1)
  })

  it('does not cache failure (HTTP 503)', async () => {
    const failing = vi.fn(async () => ({ ok: false, status: 503 })) as unknown as typeof fetch
    await expect(loadMdiIcons(failing)).rejects.toThrow()
    const icons = await loadMdiIcons(
      okFetch([{ name: 'docker', aliases: [], tags: [], deprecated: false }]),
    )
    expect(icons).toHaveLength(1)
  })
})

describe('filterIcons', () => {
  const icons: MdiIconMeta[] = [
    { name: 'docker', aliases: ['whale'], tags: [], deprecated: false },
    { name: 'kubernetes', aliases: [], tags: ['Developer / Languages'], deprecated: false },
  ]

  it('matches name case-insensitively', () => {
    expect(filterIcons(icons, 'DOCK').map((i) => i.name)).toEqual(['docker'])
  })
  it('matches aliases', () => {
    expect(filterIcons(icons, 'whale').map((i) => i.name)).toEqual(['docker'])
  })
  it('matches tags', () => {
    expect(filterIcons(icons, 'developer').map((i) => i.name)).toEqual(['kubernetes'])
  })
  it('treats null query as empty', () => {
    expect(filterIcons(icons, null)).toHaveLength(2)
  })
  it('returns all for blank query', () => {
    expect(filterIcons(icons, '  ')).toHaveLength(2)
  })
})
