import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'

const here = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig({
  root: here,
  resolve: { alias: { '@': path.resolve(here, '../src') } },
  test: { include: ['tests/**/*.test.ts'], testTimeout: 30000 },
})
