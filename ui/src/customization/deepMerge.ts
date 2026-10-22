export type DeepPartial<T> = {
    [K in keyof T]?: T[K] extends (infer U)[]
        ? U[]
        : T[K] extends object
            ? DeepPartial<T[K]>
            : T[K];
};

function isPlainObject(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null && !Array.isArray(value)
        && Object.getPrototypeOf(value) === Object.prototype;
}

/**
 * Recursively merges `patch` into `base` and returns a new object.
 * Neither argument is mutated. Arrays and non-plain values are replaced, not merged.
 */
export function deepMerge<T extends object>(base: T, patch: DeepPartial<T>): T {
    const result: Record<string, unknown> = {...base};
    for (const [key, value] of Object.entries(patch as Record<string, unknown>)) {
        if (value === undefined) continue;
        const current = result[key];
        result[key] = isPlainObject(current) && isPlainObject(value)
            ? deepMerge(current, value)
            : value;
    }
    return result as T;
}
