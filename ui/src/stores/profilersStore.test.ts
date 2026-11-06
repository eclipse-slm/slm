import {beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'
import ApiState from '@/api/apiState'

const api = vi.hoisted(() => ({
    getProfiler: vi.fn(),
    createProfiler: vi.fn(),
    updateProfiler: vi.fn(),
    deleteProfiler: vi.fn(),
}))

vi.mock('@/api/resource-management/resource-management-client', () => ({
    default: {profilerApi: api},
}))

import {useProfilersStore} from '@/stores/profilersStore'

describe('profilersStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        vi.resetAllMocks()
    })

    it('createProfiler calls API then reloads list', async () => {
        const body: any = {id: 'p1', name: 'Prof'}
        const list = [{id: 'p1', name: 'Prof'}]
        api.createProfiler.mockResolvedValue({})
        api.getProfiler.mockResolvedValue({data: list})
        const store = useProfilersStore()

        await store.createProfiler(body)

        expect(api.createProfiler).toHaveBeenCalledWith(body)
        expect(api.getProfiler).toHaveBeenCalled()
        expect(api.createProfiler.mock.invocationCallOrder[0])
            .toBeLessThan(api.getProfiler.mock.invocationCallOrder[0])
        expect(store.profilers).toEqual(list)
    })

    it('updateProfiler calls API with id and body then reloads list', async () => {
        const body: any = {id: 'p1', name: 'Renamed'}
        const list = [{id: 'p1', name: 'Renamed'}]
        api.updateProfiler.mockResolvedValue({})
        api.getProfiler.mockResolvedValue({data: list})
        const store = useProfilersStore()

        await store.updateProfiler('p1', body)

        expect(api.updateProfiler).toHaveBeenCalledWith('p1', body)
        expect(api.getProfiler).toHaveBeenCalled()
        expect(api.updateProfiler.mock.invocationCallOrder[0])
            .toBeLessThan(api.getProfiler.mock.invocationCallOrder[0])
        expect(store.profilers).toEqual(list)
    })

    it('getProfilers sorts by name', async () => {
        api.getProfiler.mockResolvedValue({data: [{name: 'b'}, {name: 'a'}]})
        const store = useProfilersStore()

        await store.getProfilers()

        expect(store.profilers.map(p => p.name)).toEqual(['a', 'b'])
    })

    it('keeps a successful delete when the reload fails', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {})
        api.deleteProfiler.mockResolvedValue({})
        api.getProfiler.mockRejectedValue(new Error('reload failed'))
        const store = useProfilersStore()
        store.profilers = [{id: 'p1', name: 'One'}]

        await expect(store.deleteProfiler('p1')).resolves.toBeUndefined()

        expect(api.deleteProfiler).toHaveBeenCalledWith('p1')
        expect(store.profilers).toEqual([])
    })

    it('updateStore sets ERROR when loading fails', async () => {
        vi.spyOn(console, 'log').mockImplementation(() => {})
        api.getProfiler.mockRejectedValue(new Error('network'))
        const store = useProfilersStore()

        await store.updateStore()

        expect(store.apiState).toBe(ApiState.ERROR)
    })
})
