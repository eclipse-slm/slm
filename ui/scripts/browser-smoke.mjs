// Headless browser smoke test for a running SLM UI dev server.
// Usage: node browser-smoke.mjs <appUrl> <keycloakUrlPrefix> [timeoutMs]
// Passes when the app boots far enough to redirect to Keycloak without module/runtime errors.
// `playwright` resolves from this script's location (ui/node_modules), so the consumer under test stays isolated.
import { chromium } from 'playwright'

const [appUrl, keycloakPrefix, timeoutArg] = process.argv.slice(2)
if (!appUrl || !keycloakPrefix) {
  console.error('usage: node browser-smoke.mjs <appUrl> <keycloakUrlPrefix> [timeoutMs]')
  process.exit(2)
}
const timeoutMs = Number(timeoutArg ?? 60000)
const appOrigin = new URL(appUrl).origin

const CONSOLE_PATTERNS = ['SyntaxError', 'does not provide an export', 'require is not defined', 'before initialization']
const MODULE_PATH = /^\/(src|node_modules|@fs|@id)\//

const errors = []
let resolveKeycloak
const keycloakSeen = new Promise((resolve) => (resolveKeycloak = resolve))

const browser = await chromium.launch()
try {
  const page = await browser.newPage()

  // Only the dev server is reachable; external assets (fonts, CDN css) and Keycloak are aborted after being observed.
  await page.route('**/*', (route) => {
    const url = route.request().url()
    if (url.startsWith(keycloakPrefix)) resolveKeycloak(url)
    return url.startsWith(appOrigin) ? route.continue() : route.abort()
  })
  page.on('pageerror', (err) => errors.push(`pageerror: ${err.stack || String(err)}`))
  page.on('console', (msg) => {
    if (msg.type() !== 'error') return
    const text = msg.text()
    if (CONSOLE_PATTERNS.some((p) => text.includes(p))) errors.push(`console.error: ${text}`)
  })
  const isModuleUrl = (url) => url.startsWith(appOrigin) && MODULE_PATH.test(new URL(url).pathname)
  page.on('response', (res) => {
    if (res.status() >= 400 && isModuleUrl(res.url())) errors.push(`HTTP ${res.status()}: ${res.url()}`)
  })
  page.on('requestfailed', (req) => {
    if (isModuleUrl(req.url())) errors.push(`request failed (${req.failure()?.errorText}): ${req.url()}`)
  })

  await page.goto(appUrl, { waitUntil: 'commit', timeout: timeoutMs })
  const timeout = new Promise((resolve) => setTimeout(() => resolve(undefined), timeoutMs).unref())
  const keycloakUrl = await Promise.race([keycloakSeen, timeout])

  if (errors.length > 0 || !keycloakUrl) {
    console.error(`FAIL: browser smoke test against ${appUrl}`)
    if (!keycloakUrl) console.error(`  - no request to Keycloak (${keycloakPrefix}) within ${timeoutMs}ms`)
    for (const e of errors) console.error(`  - ${e}`)
    process.exitCode = 1
  } else {
    console.log(`OK: browser smoke test passed (Keycloak redirect attempted: ${keycloakUrl})`)
  }
} finally {
  await browser.close()
}
