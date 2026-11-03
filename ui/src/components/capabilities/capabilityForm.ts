import type { GetCapabilities200ResponseInner } from '@/api/resource-management/client'

export type CapabilityClass = 'DeploymentCapability' | 'BaseConfigurationCapability'
export type FormCapabilityType = 'SETUP' | 'DEPLOY'
export type FormActionType = 'INSTALL' | 'UNINSTALL' | 'DEPLOY' | 'UNDEPLOY'

export interface ActionFormData {
  awxRepo: string
  awxBranch: string
  playbook: string
}

export interface CapabilityFormData {
  name: string
  logo: string // '' = use fallback
  capabilityClass: CapabilityClass
  supportedDeploymentTypes: string[] // DeploymentType values
  types: FormCapabilityType[]
  actions: Partial<Record<FormActionType, ActionFormData>>
}

export interface CapabilityForm {
  isFormValid: boolean
  formData: CapabilityFormData
}

export const DEFAULT_BRANCH = 'main'

const TYPE_ORDER: FormCapabilityType[] = ['SETUP', 'DEPLOY']

export const ACTIONS_BY_TYPE: Record<FormCapabilityType, FormActionType[]> = {
  SETUP: ['INSTALL', 'UNINSTALL'],
  DEPLOY: ['DEPLOY', 'UNDEPLOY'],
}

export function newActionFormData(): ActionFormData {
  return { awxRepo: '', awxBranch: DEFAULT_BRANCH, playbook: '' }
}

export function newCapabilityFormData(): CapabilityFormData {
  return {
    name: '',
    logo: '',
    capabilityClass: 'DeploymentCapability',
    supportedDeploymentTypes: [],
    types: [],
    actions: {},
  }
}

export function allowedTypes(cls: CapabilityClass): FormCapabilityType[] {
  return cls === 'BaseConfigurationCapability' ? ['SETUP'] : ['SETUP', 'DEPLOY']
}

export function effectiveTypes(form: CapabilityFormData): FormCapabilityType[] {
  if (form.capabilityClass === 'BaseConfigurationCapability') {
    return ['SETUP']
  }
  const allowed = allowedTypes(form.capabilityClass)
  return TYPE_ORDER.filter(t => allowed.includes(t) && form.types.includes(t))
}

export function requiredActions(form: CapabilityFormData): FormActionType[] {
  return effectiveTypes(form).flatMap(t => ACTIONS_BY_TYPE[t])
}

export function fallbackLogo(name: string): string {
  const first = name.trim().charAt(0).toLowerCase()
  if (/^[a-z]$/.test(first)) {
    return `mdi-alpha-${first}-box`
  }
  if (/^[0-9]$/.test(first)) {
    return `mdi-numeric-${first}-box`
  }
  return 'mdi-puzzle'
}

export function isNameTaken(name: string, existing: { name: string }[]): boolean {
  const wanted = name.trim().toLowerCase()
  return existing.some(c => c.name.trim().toLowerCase() === wanted)
}

function isUntouched(action: ActionFormData): boolean {
  return action.awxRepo === '' && action.playbook === '' && action.awxBranch === DEFAULT_BRANCH
}

// Plain-object copy; structuredClone throws on Vue reactive proxies
export function cloneActions(
  actions: Partial<Record<FormActionType, ActionFormData>>,
): Partial<Record<FormActionType, ActionFormData>> {
  const result: Partial<Record<FormActionType, ActionFormData>> = {}
  for (const [key, value] of Object.entries(actions) as [FormActionType, ActionFormData][]) {
    result[key] = { ...value }
  }
  return result
}

export function prefillUntouchedActions(
  actions: Partial<Record<FormActionType, ActionFormData>>,
  source: FormActionType,
): Partial<Record<FormActionType, ActionFormData>> {
  const src = actions[source]
  const result: Partial<Record<FormActionType, ActionFormData>> = {}
  for (const [key, value] of Object.entries(actions) as [FormActionType, ActionFormData][]) {
    result[key] = src && key !== source && isUntouched(value)
      ? { ...value, awxRepo: src.awxRepo, awxBranch: src.awxBranch }
      : { ...value }
  }
  return result
}

export function buildCapabilityRequest(form: CapabilityFormData, id?: string): GetCapabilities200ResponseInner {
  const name = form.name.trim()
  const actions: Record<string, unknown> = {}
  for (const actionType of requiredActions(form)) {
    const a = form.actions[actionType] ?? newActionFormData()
    actions[actionType] = {
      actionClass: 'AwxAction',
      actionType,
      awxRepo: a.awxRepo.trim(),
      awxBranch: a.awxBranch.trim(),
      playbook: a.playbook.trim(),
      connectionTypes: ['ssh'],
      skipable: false,
      configParameters: [],
    }
  }
  const body: Record<string, unknown> = {
    ...(id !== undefined ? { id } : {}),
    capabilityClass: form.capabilityClass,
    name,
    logo: form.logo.trim() || fallbackLogo(name),
    type: effectiveTypes(form),
    actions,
  }
  if (form.capabilityClass === 'DeploymentCapability') {
    body.supportedDeploymentTypes = form.supportedDeploymentTypes
  }
  return body as unknown as GetCapabilities200ResponseInner
}
