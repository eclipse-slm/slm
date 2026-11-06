import type { AwxAction, Profiler, ProfilerDTOApi } from '@/api/resource-management/client'

export interface ProfilerFormData {
  name: string
  awxRepo: string
  awxBranch: string
  playbook: string
  username: string
  password: string
}

export interface ProfilerForm {
  isFormValid: boolean
  formData: ProfilerFormData
}

export const DEFAULT_BRANCH = 'main'
export const DEFAULT_PLAYBOOK = 'run.yml'

export function newProfilerFormData(): ProfilerFormData {
  return { name: '', awxRepo: '', awxBranch: DEFAULT_BRANCH, playbook: DEFAULT_PLAYBOOK, username: '', password: '' }
}

// Credentials are never prefilled; leaving them empty on update keeps the stored ones
export function profilerToFormData(profiler: Profiler): ProfilerFormData {
  return {
    name: profiler.name,
    awxRepo: profiler.action?.awxRepo ?? '',
    awxBranch: profiler.action?.awxBranch ?? DEFAULT_BRANCH,
    playbook: profiler.action?.playbook ?? DEFAULT_PLAYBOOK,
    username: '',
    password: '',
  }
}

export function buildProfilerRequest(form: ProfilerFormData, id: string, original?: Profiler): ProfilerDTOApi {
  const action: Record<string, unknown> = {
    ...original?.action,
    actionClass: 'AwxAction',
    actionType: 'RUN',
    awxRepo: form.awxRepo.trim(),
    awxBranch: form.awxBranch.trim(),
    playbook: form.playbook.trim(),
  }
  const username = form.username.trim()
  if (username !== '' && form.password !== '') {
    action.username = username
    action.password = form.password
  }
  return { id, name: form.name.trim(), action: action as unknown as AwxAction }
}

export function credentialsError(form: Pick<ProfilerFormData, 'username' | 'password'>): string | true {
  const hasUsername = form.username.trim() !== ''
  const hasPassword = form.password !== ''
  return hasUsername === hasPassword ? true : 'Enter both username and password, or neither'
}

export function isNameTaken(name: string, profilers: Profiler[], ownId?: string): boolean {
  const wanted = name.trim().toLowerCase()
  return profilers.some(p => p.id !== ownId && p.name.trim().toLowerCase() === wanted)
}
