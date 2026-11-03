import {beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'

const api = vi.hoisted(() => ({
    createCapability: vi.fn(),
    deleteCapability: vi.fn(),
    getCapabilities: vi.fn(),
    getAllCapabilityServices: vi.fn(),
}))

vi.mock('@/api/resource-management/resource-management-client', () => ({
    default: {capabilityApi: api},
}))

import {CapabilityInUseError, useCapabilitiesStore} from '@/stores/capabilitiesStore'

describe('capabilitiesStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        vi.resetAllMocks()
    })

    it('createCapability calls API then reloads list', async () => {
        const body: any = {id: 'c1', name: 'Cap'}
        const list = [{id: 'c1', name: 'Cap'}]
        api.createCapability.mockResolvedValue({})
        api.getCapabilities.mockResolvedValue({data: list})
        const store = useCapabilitiesStore()

        await store.createCapability(body)

        expect(api.createCapability).toHaveBeenCalledWith(body)
        expect(api.getCapabilities).toHaveBeenCalled()
        expect(api.createCapability.mock.invocationCallOrder[0])
            .toBeLessThan(api.getCapabilities.mock.invocationCallOrder[0])
        expect(store.capabilities).toEqual(list)
    })

    it('deleteCapability deletes when not installed', async () => {
        api.getAllCapabilityServices.mockResolvedValue({data: [{capabilityId: 'other'}]})
        api.deleteCapability.mockResolvedValue({})
        api.getCapabilities.mockResolvedValue({data: [{id: 'c1', name: 'Deleted capability'}]})
        const store = useCapabilitiesStore()
        store.capabilities = [{id: 'c1', name: 'Deleted capability'}]

        await store.deleteCapability('c1')

        expect(api.deleteCapability).toHaveBeenCalledWith('c1')
        expect(api.getCapabilities).toHaveBeenCalled()
        expect(api.deleteCapability.mock.invocationCallOrder[0])
            .toBeLessThan(api.getCapabilities.mock.invocationCallOrder[0])
        expect(store.capabilities).toEqual([])
    })

    it('createCapability resolves when reload fails after successful create', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {})
        api.createCapability.mockResolvedValue({})
        api.getCapabilities.mockRejectedValue(new Error('reload failed'))
        const store = useCapabilitiesStore()

        await expect(store.createCapability({id: 'c1', name: 'Cap'} as any)).resolves.toBeUndefined()
        expect(api.createCapability).toHaveBeenCalled()
    })

    it('deleteCapability resolves when reload fails after successful delete', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {})
        api.getAllCapabilityServices.mockResolvedValue({data: []})
        api.deleteCapability.mockResolvedValue({})
        api.getCapabilities.mockRejectedValue(new Error('reload failed'))
        const store = useCapabilitiesStore()
        store.capabilities = [{id: 'c1', name: 'Deleted capability'}]

        await expect(store.deleteCapability('c1')).resolves.toBeUndefined()
        expect(api.deleteCapability).toHaveBeenCalledWith('c1')
        expect(store.capabilities).toEqual([])
    })

    it('deleteCapability refuses when installed', async () => {
        api.getAllCapabilityServices.mockResolvedValue({
            data: [{capabilityId: 'c1'}, {capabilityId: 'c1'}],
        })
        const store = useCapabilitiesStore()

        const error = await store.deleteCapability('c1').catch(e => e)

        expect(error).toBeInstanceOf(CapabilityInUseError)
        expect(error.installCount).toBe(2)
        expect(api.deleteCapability).not.toHaveBeenCalled()
    })

    it('deleteCapability fails closed when services fetch fails', async () => {
        api.getAllCapabilityServices.mockRejectedValue(new Error('network'))
        const store = useCapabilitiesStore()

        await expect(store.deleteCapability('c1')).rejects.toThrow('network')
        expect(api.deleteCapability).not.toHaveBeenCalled()
    })

    it('deleteCapability fails closed when services response has no data', async () => {
        api.getAllCapabilityServices.mockResolvedValue({})
        const store = useCapabilitiesStore()

        await expect(store.deleteCapability('c1')).rejects.toThrow()
        expect(api.deleteCapability).not.toHaveBeenCalled()
    })

    it('installCountOfCapability counts services per capability', () => {
        const store = useCapabilitiesStore()
        store.capabilityServices = [
            {capabilityId: 'a'}, {capabilityId: 'a'}, {capabilityId: 'b'},
        ] as any

        expect(store.installCountOfCapability('a')).toBe(2)
        expect(store.installCountOfCapability('c')).toBe(0)
    })
})
