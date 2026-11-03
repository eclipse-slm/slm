import { describe, expect, test } from 'vitest'
import { reactive } from 'vue'
import {
  buildCapabilityRequest,
  capabilityToFormData,
  cloneActions,
  fallbackLogo,
  getActionTabColorProps,
  hasActionErrors,
  importErrorMessage,
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
  test('getActionTabColorProps keeps invalid tabs colored when unselected', () => {
    expect(getActionTabColorProps('INSTALL', { 'INSTALL.playbook': 'Playbook is required' }))
      .toEqual({ color: 'error', baseColor: 'error' })
    expect(getActionTabColorProps('INSTALL', {})).toEqual({})
  })

  test('hasActionErrors identifies non-empty errors for the exact action type', () => {
    expect(hasActionErrors('INSTALL', { 'INSTALL.playbook': 'Playbook is required' })).toBe(true)
    expect(hasActionErrors('INSTALL', { 'UNINSTALL.playbook': 'Playbook is required' })).toBe(false)
    expect(hasActionErrors('INSTALL', { 'INSTALLATION.playbook': 'Playbook is required' })).toBe(false)
    expect(hasActionErrors('INSTALL', { 'INSTALL.playbook': '' })).toBe(false)
    expect(hasActionErrors('INSTALL', { 'INSTALL.playbook': undefined })).toBe(false)
  })

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

describe('capabilityToFormData', () => {
  const repo = 'https://github.com/eclipse-slm/slm-dc-docker'
  const awx = (playbook: string) => ({ actionClass: 'AwxAction', awxRepo: repo, awxBranch: '1.0.3', playbook })

  test('maps a deployment capability and drops unsupported actions', () => {
    expect(capabilityToFormData({
      capabilityClass: 'DeploymentCapability', name: 'Docker', logo: 'mdi-docker', type: ['SETUP', 'DEPLOY'],
      supportedDeploymentTypes: ['DOCKER_CONTAINER', 'DOCKER_COMPOSE'],
      actions: {
        INSTALL: awx('install.yml'), UNINSTALL: awx('uninstall.yml'),
        DEPLOY: awx('deploy.yml'), UNDEPLOY: awx('undeploy.yml'), UPDATE: awx('deploy.yml'),
      },
    })).toEqual({
      name: 'Docker', logo: 'mdi-docker', capabilityClass: 'DeploymentCapability',
      supportedDeploymentTypes: ['DOCKER_CONTAINER', 'DOCKER_COMPOSE'], types: ['SETUP', 'DEPLOY'],
      actions: {
        INSTALL:   { awxRepo: repo, awxBranch: '1.0.3', playbook: 'install.yml' },
        UNINSTALL: { awxRepo: repo, awxBranch: '1.0.3', playbook: 'uninstall.yml' },
        DEPLOY:    { awxRepo: repo, awxBranch: '1.0.3', playbook: 'deploy.yml' },
        UNDEPLOY:  { awxRepo: repo, awxBranch: '1.0.3', playbook: 'undeploy.yml' },
      },
    })
  })

  test('maps base configuration and ignores deployment types', () => {
    const f = capabilityToFormData({ capabilityClass: 'BaseConfigurationCapability', supportedDeploymentTypes: ['DOCKER_CONTAINER'] })
    expect(f.capabilityClass).toBe('BaseConfigurationCapability')
    expect(f.supportedDeploymentTypes).toEqual([])
  })

  test('filters unknown types and fills missing fields', () => {
    const f = capabilityToFormData({
      capabilityClass: 'DeploymentCapability', type: ['DEPLOY', 'SCALE'], actions: { DEPLOY: { playbook: 'd.yml' } },
    })
    expect(f.types).toEqual(['DEPLOY'])
    expect(f.name).toBe('')
    expect(f.logo).toBe('')
    expect(f.actions.DEPLOY).toEqual({ awxRepo: '', awxBranch: 'main', playbook: 'd.yml' })
  })

  test('defaults unknown class to DeploymentCapability', () => {
    expect(capabilityToFormData({ capabilityClass: 'Other' }).capabilityClass).toBe('DeploymentCapability')
  })
})

describe('importErrorMessage', () => {
  test('uses the server message when present', () => {
    expect(importErrorMessage({ response: { data: { message: 'File not found' } } })).toBe('File not found')
  })
  test('falls back to a generic message', () => {
    expect(importErrorMessage(new Error('x'))).toBe('Failed to import capability')
    expect(importErrorMessage({ response: { data: { message: '' } } })).toBe('Failed to import capability')
  })
})
