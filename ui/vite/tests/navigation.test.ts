import { describe, expect, it } from 'vitest'
import { defaultNavigation, resolveNavItems } from '@/app/navigation'
import customizeNavigation from '@/customization/customizeNavigation'

describe('navigation', () => {
  it('translates titleKey and evaluates visible', () => {
    const r = resolveNavItems(defaultNavigation, { roles: [], isDeveloper: false }, k => `T:${k}`)
    expect(r[0]).toMatchObject({ id: 'main-menu-button-dashboard', title: 'T:drawer.section.dashboard.title', visible: true })
    expect(r.find(i => i.group === '/admin')!.visible).toBe(false)
  })

  it('shows admin entries for slm-admin', () => {
    const r = resolveNavItems(defaultNavigation, { roles: ['slm-admin'], isDeveloper: false }, k => k)
    expect(r.find(i => i.group === '/admin')!.children!.every(c => c.visible)).toBe(true)
  })

  it('defaults visible to true', () => {
    expect(resolveNavItems([{ titleKey: 'x' }], { roles: [], isDeveloper: false }, k => k)[0].visible).toBe(true)
  })

  it('keeps the original tree for an admin developer', () => {
    const r = resolveNavItems(defaultNavigation, { roles: ['slm-admin'], isDeveloper: true }, k => k)
    const shape = (items: typeof r): unknown[] =>
      items.map(i => [i.id, i.to, i.children ? shape(i.children) : undefined])
    expect(shape(r)).toEqual([
      ['main-menu-button-dashboard', '/dashboard', undefined],
      ['main-menu-button-jobs', '/jobs', undefined],
      ['main-menu-divider-resources', undefined, undefined],
      ['main-menu-button-resources', undefined, [
        ['main-menu-button-admin-components', 'instances', undefined],
        ['main-menu-button-admin-service-categories', 'types', undefined],
      ]],
      ['main-menu-button-clusters', '/clusters', undefined],
      ['main-menu-button-discovery', '/discovery/inbox', [
        ['main-menu-button-discovery-inbox', 'inbox', undefined],
        ['main-menu-button-discovery-jobs', 'jobs', undefined],
        ['main-menu-button-discovery-drivers', 'drivers', undefined],
      ]],
      ['main-menu-divider-services', undefined, undefined],
      ['main-menu-button-service-offering', '/services/offerings', undefined],
      ['main-menu-button-service-instances', '/services/instances', undefined],
      ['main-menu-button-service-vendors', '/services/vendors', undefined],
      ['main-menu-divider-admin', undefined, undefined],
      ['main-menu-button-admin', undefined, [
        ['main-menu-button-admin-components', 'components', undefined],
        ['main-menu-button-admin-service-categories', 'service-categories', undefined],
        ['main-menu-button-admin-service-vendors', 'service-vendors', undefined],
        ['main-menu-button-admin-users', 'users', undefined],
      ]],
    ])
    expect(r.find(i => i.id === 'main-menu-button-clusters')!.visible).toBe(false)
  })

  it('customizeNavigation is the identity by default', () => {
    expect(customizeNavigation(defaultNavigation)).toBe(defaultNavigation)
  })
})
