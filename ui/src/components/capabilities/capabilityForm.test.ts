import { describe, expect, test } from 'vitest'
import { reactive } from 'vue'
import {
  buildCapabilityRequest,
  cloneActions,
  fallbackLogo,
  isNameTaken,
  newActionFormData,
  newCapabilityFormData,
  prefillUntouchedActions,
  requiredActions,
  type CapabilityFormData,
} from './capabilityForm'

const base = (over: Partial<CapabilityFormData> = {}): CapabilityFormData => ({
  ...newCapabilityFormData(), name: 'Docker', types: ['SETUP', 'DEPLOY'],
  supportedDeploymentTypes: ['DOCKER_CONTAINER'],
  actions: {
    INSTALL:   { awxRepo: 'https://r', awxBranch: 'main', playbook: 'install.yml' },
    UNINSTALL: { awxRepo: 'https://r', awxBranch: 'main', playbook: 'uninstall.yml' },
    DEPLOY:    { awxRepo: 'https://r', awxBranch: 'main', playbook: 'deploy.yml' },
    UNDEPLOY:  { awxRepo: 'https://r', awxBranch: 'main', playbook: 'undeploy.yml' },
  }, ...over })

describe('capabilityForm', () => {
  test('fallbackLogo', () => {
    expect(fallbackLogo('Docker')).toBe('mdi-alpha-d-box')
    expect(fallbackLogo('  kubernetes')).toBe('mdi-alpha-k-box')
    expect(fallbackLogo('3D printer')).toBe('mdi-numeric-3-box')
    expect(fallbackLogo('Ölpumpe')).toBe('mdi-puzzle')
    expect(fallbackLogo('')).toBe('mdi-puzzle')
  })
  test('isNameTaken trims and ignores case', () => {
    const existing = [{ name: 'Docker' }]
    expect(isNameTaken(' docker ', existing)).toBe(true)
    expect(isNameTaken('DOCKER', existing)).toBe(true)
    expect(isNameTaken('Dockers', existing)).toBe(false)
  })
  test('requiredActions follows types and class', () => {
    expect(requiredActions(base())).toEqual(['INSTALL', 'UNINSTALL', 'DEPLOY', 'UNDEPLOY'])
    expect(requiredActions(base({ types: ['DEPLOY'] }))).toEqual(['DEPLOY', 'UNDEPLOY'])
    expect(requiredActions(base({ capabilityClass: 'BaseConfigurationCapability', types: ['DEPLOY'] })))
      .toEqual(['INSTALL', 'UNINSTALL'])
  })
  test('newActionFormData provides a default playbook for each action type', () => {
    expect(newActionFormData('INSTALL').playbook).toBe('install.yml')
    expect(newActionFormData('UNINSTALL').playbook).toBe('uninstall.yml')
    expect(newActionFormData('DEPLOY').playbook).toBe('deploy.yml')
    expect(newActionFormData('UNDEPLOY').playbook).toBe('undeploy.yml')
  })
  test('buildCapabilityRequest: deployment capability', () => {
    const body: any = buildCapabilityRequest(base({ name: ' Docker ', logo: 'mdi-docker' }), 'id-1')
    expect(body).toMatchObject({ id: 'id-1', capabilityClass: 'DeploymentCapability', name: 'Docker',
      logo: 'mdi-docker', type: ['SETUP', 'DEPLOY'], supportedDeploymentTypes: ['DOCKER_CONTAINER'] })
    expect(Object.keys(body.actions)).toEqual(['INSTALL', 'UNINSTALL', 'DEPLOY', 'UNDEPLOY'])
    expect(body.actions.INSTALL).toEqual({ actionClass: 'AwxAction', actionType: 'INSTALL', awxRepo: 'https://r',
      awxBranch: 'main', playbook: 'install.yml', connectionTypes: ['ssh'], skipable: false, configParameters: [] })
  })
  test('buildCapabilityRequest: omits id when undefined', () => {
    expect(buildCapabilityRequest(base(), undefined)).not.toHaveProperty('id')
  })
  test('buildCapabilityRequest: empty logo uses fallback', () => {
    expect((buildCapabilityRequest(base({ logo: '' }), 'x') as any).logo).toBe('mdi-alpha-d-box')
  })
  test('buildCapabilityRequest: unticked type drops stale actions', () => {
    const body: any = buildCapabilityRequest(base({ types: ['SETUP'] }), 'x')
    expect(Object.keys(body.actions)).toEqual(['INSTALL', 'UNINSTALL'])
    expect(body.type).toEqual(['SETUP'])
  })
  test('buildCapabilityRequest: switched to base configuration', () => {
    const body: any = buildCapabilityRequest(base({ capabilityClass: 'BaseConfigurationCapability' }), 'x')
    expect(body.capabilityClass).toBe('BaseConfigurationCapability')
    expect(body.type).toEqual(['SETUP'])
    expect(Object.keys(body.actions)).toEqual(['INSTALL', 'UNINSTALL'])
    expect(body).not.toHaveProperty('supportedDeploymentTypes')
  })
  test('buildCapabilityRequest trims action fields', () => {
    const f = base(); f.actions.INSTALL = { awxRepo: ' https://r ', awxBranch: ' dev ', playbook: ' i.yml ' }
    expect((buildCapabilityRequest(f, 'x') as any).actions.INSTALL)
      .toMatchObject({ awxRepo: 'https://r', awxBranch: 'dev', playbook: 'i.yml' })
  })
  test('prefillUntouchedActions fills only untouched cards', () => {
    const out = prefillUntouchedActions({
      INSTALL:   { awxRepo: 'https://r', awxBranch: 'v1', playbook: 'install.yml' },
      UNINSTALL: newActionFormData('UNINSTALL'),
      DEPLOY:    { awxRepo: '', awxBranch: 'main', playbook: 'deploy.yml' },
    }, 'INSTALL')
    expect(out.UNINSTALL).toEqual({ awxRepo: 'https://r', awxBranch: 'v1', playbook: 'uninstall.yml' })
    expect(out.DEPLOY).toEqual({ awxRepo: 'https://r', awxBranch: 'v1', playbook: 'deploy.yml' })
    expect(out.INSTALL!.awxBranch).toBe('v1')
  })
})

describe('cloneActions', () => {
  test('copies reactive actions into a plain, independent object', () => {
    const source = reactive({ INSTALL: newActionFormData() })
    const result = cloneActions(source)
    expect(result).toEqual({ INSTALL: newActionFormData() })
    expect(() => structuredClone(result)).not.toThrow()
    result.INSTALL!.playbook = 'changed.yml'
    expect(source.INSTALL.playbook).toBe('')
  })
})
