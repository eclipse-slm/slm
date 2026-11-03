export const MDI_META_URL = 'https://cdn.jsdelivr.net/npm/@mdi/svg@latest/meta.json'

export interface MdiIconMeta {
  name: string
  aliases: string[]
  tags: string[]
  deprecated: boolean
}

let cache: Promise<MdiIconMeta[]> | null = null

export function resetMdiIconCache(): void {
  cache = null
}

export function loadMdiIcons(fetchFn: typeof fetch = fetch): Promise<MdiIconMeta[]> {
  if (cache) return cache
  const pending = (async () => {
    const response = await fetchFn(MDI_META_URL)
    if (!response.ok) {
      throw new Error(`Failed to load MDI icon list (HTTP ${response.status})`)
    }
    const data = (await response.json()) as MdiIconMeta[]
    return data
      .filter((icon) => !icon.deprecated)
      .map((icon) => ({
        name: icon.name,
        aliases: icon.aliases ?? [],
        tags: icon.tags ?? [],
        deprecated: false,
      }))
  })()
  cache = pending
  pending.catch(() => {
    if (cache === pending) cache = null
  })
  return pending
}

export function filterIcons(icons: MdiIconMeta[], query: string | null | undefined): MdiIconMeta[] {
  const q = (query ?? '').trim().toLowerCase()
  if (!q) return icons
  return icons.filter(
    (icon) =>
      icon.name.toLowerCase().includes(q) ||
      icon.aliases.some((a) => a.toLowerCase().includes(q)) ||
      icon.tags.some((t) => t.toLowerCase().includes(q)),
  )
}
