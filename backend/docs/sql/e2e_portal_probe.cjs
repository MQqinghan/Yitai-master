/** portal-web(8088) 浏览器 E2E：登录 user1 → 推荐渲染 → AI 悬浮窗导购对话 */
const { chromium } = require('playwright-core')

;(async () => {
  const browser = await chromium.launch({ channel: 'msedge', headless: true })
  const page = await browser.newPage()
  const errors = []
  page.on('pageerror', e => errors.push('[pageerror] ' + String(e).slice(0, 200)))

  console.log('== 1. 打开 8088 ==')
  await page.goto('http://localhost:8088', { waitUntil: 'networkidle', timeout: 30000 })
  console.log('URL:', page.url())
  console.log('标题:', await page.title())

  // 找登录入口
  const loginLink = page.locator('text=登录').first()
  if (await loginLink.count()) {
    console.log('== 2. 点击登录 ==')
    await loginLink.click()
    await page.waitForTimeout(1200)
    console.log('URL after click:', page.url())
  }
  console.log('== 3. 页面快照（找表单）==')
  const inputs = await page.locator('input').all()
  for (const i of inputs) {
    console.log('input:', JSON.stringify({ placeholder: await i.getAttribute('placeholder'), type: await i.getAttribute('type') }))
  }
  const buttons = await page.locator('button').all()
  for (const b of buttons) console.log('button:', (await b.innerText()).trim().slice(0, 20))

  await page.screenshot({ path: 'D:/xiangmu/Yitai/Yitai-master/backend/docs/sql/portal_step1.png' })
  console.log('JS errors:', errors.length ? errors.join('\n') : '(无)')
  await browser.close()
})().catch(e => { console.error('FATAL:', String(e).slice(0, 300)); process.exit(1) })
