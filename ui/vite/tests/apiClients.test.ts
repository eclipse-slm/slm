import path from 'node:path'
import { describe, expect, it } from 'vitest'
import { apiClientsPlugin } from '../src/apiClients'
import { resolveSlmPaths } from '../src/paths'
import { buildFixture, fixturesDir } from './helpers'

function buildClients(variant: string) {
  const root = path.join(fixturesDir, 'clients', variant)
  const paths = resolveSlmPaths({}, path.join(root, 'consumer'), path.join(root, 'pkg'))
  return buildFixture({
    fixture: `clients/${variant}`,
    entry: path.join(paths.packageRoot, 'src/main.ts'),
    plugins: [
      { name: 'test-alias', config: () => ({ resolve: { alias: { '@': paths.packageSrc } } }) },
      apiClientsPlugin(paths),
    ],
  })
}

describe('apiClientsPlugin', () => {
  it('redirects @/api/<service>/client to the consumer clients dir', async () => {
    expect(await buildClients('complete')).toContain('CONSUMER_RM_CLIENT')
  })
  it('redirects relative ./client imports from package api wrappers', async () => {
    expect(await buildClients('complete')).toContain('CONSUMER_NS_CLIENT')
  })
  it('fails with the verbatim message when a client is missing', async () => {
    await expect(buildClients('missing-service-management')).rejects.toThrow(
      'Missing generated API client for "service-management": expected')
  })
  it('is the identity when apiClientsDir is the package api dir', async () => {
    const root = path.join(fixturesDir, 'clients', 'identity')
    const paths = resolveSlmPaths({ apiClientsDir: 'pkg/src/api' }, root, path.join(root, 'pkg'))
    const code = await buildFixture({
      fixture: 'clients/identity',
      entry: path.join(paths.packageRoot, 'src/main.ts'),
      plugins: [
        { name: 'test-alias', config: () => ({ resolve: { alias: { '@': paths.packageSrc } } }) },
        apiClientsPlugin(paths),
      ],
    })
    expect(code).toContain('PACKAGE_RM_CLIENT')
  })
})
