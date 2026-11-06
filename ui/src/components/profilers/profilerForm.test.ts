import { describe, expect, test } from 'vitest'
import type { Profiler } from '@/api/resource-management/client'
import {
  buildProfilerRequest,
  credentialsError,
  isNameTaken,
  newProfilerFormData,
  profilerToFormData,
} from './profilerForm'

const profiler: Profiler = {
  id: 'p1',
  name: 'Prof',
  action: {
    actionClass: 'AwxAction',
    actionType: 'RUN',
    awxRepo: 'https://r',
    awxBranch: 'dev',
    playbook: 'profile.yml',
    connectionTypes: ['ssh'],
    skipable: true,
    configParameters: [{ name: 'x' }],
    parameter: [{ name: 'survey' }],
  } as any,
}

describe('profilerForm', () => {
  test('newProfilerFormData defaults', () => {
    const f = newProfilerFormData()
    expect(f.awxBranch).toBe('main')
    expect(f.playbook).toBe('run.yml')
    expect(f.name).toBe('')
  })

  test('profilerToFormData leaves credentials empty', () => {
    const f = profilerToFormData({
      ...profiler,
      action: { ...profiler.action!, username: 'u', password: 'p' },
    })
    expect(f.username).toBe('')
    expect(f.password).toBe('')
    expect(f).toMatchObject({ name: 'Prof', awxRepo: 'https://r', awxBranch: 'dev', playbook: 'profile.yml' })
  })

  test('buildProfilerRequest sets action class and type', () => {
    const body = buildProfilerRequest({ ...newProfilerFormData(), name: 'Prof' }, 'id-1')
    expect(body.id).toBe('id-1')
    expect(body.action).toMatchObject({ actionClass: 'AwxAction', actionType: 'RUN' })
  })

  test('buildProfilerRequest omits empty credentials', () => {
    const body = buildProfilerRequest({ ...newProfilerFormData(), name: 'Prof' }, 'id-1')
    expect('username' in body.action!).toBe(false)
    expect('password' in body.action!).toBe(false)
  })

  test('buildProfilerRequest includes credentials when set', () => {
    const body = buildProfilerRequest({ ...newProfilerFormData(), username: 'u', password: 'p' }, 'id-1')
    expect(body.action).toMatchObject({ username: 'u', password: 'p' })
  })

  test('buildProfilerRequest keeps fields the form does not show', () => {
    const form = { ...profilerToFormData(profiler), awxRepo: 'https://new' }
    const action: any = buildProfilerRequest(form, 'p1', profiler).action
    expect(action.parameter).toEqual([{ name: 'survey' }])
    expect(action.connectionTypes).toEqual(['ssh'])
    expect(action.configParameters).toEqual([{ name: 'x' }])
    expect(action.skipable).toBe(true)
    expect(action.awxRepo).toBe('https://new')
  })

  test('credentialsError requires both or neither', () => {
    expect(credentialsError({ username: '', password: '' })).toBe(true)
    expect(credentialsError({ username: 'u', password: 'p' })).toBe(true)
    expect(credentialsError({ username: 'u', password: '' })).toBe('Enter both username and password, or neither')
    expect(credentialsError({ username: '', password: 'p' })).toBe('Enter both username and password, or neither')
  })

  test('isNameTaken trims, ignores case and skips own id', () => {
    const existing = [{ id: 'p1', name: 'Prof Docker' }] as Profiler[]
    expect(isNameTaken(' prof docker ', existing)).toBe(true)
    expect(isNameTaken('Other', existing)).toBe(false)
    expect(isNameTaken('Prof Docker', existing, 'p1')).toBe(false)
  })
})
