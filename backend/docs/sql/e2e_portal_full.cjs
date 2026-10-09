/** portal-web 完整 E2E：登录 → 首页推荐渲染 → AI 悬浮窗导购对话 */
const { chromium } = require('playwright-core')

;(async () => {
  const browser = await chromium.launch({ channel: 'msedge', headless: true })
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
  const errors = []
  page.on('pageerror', e => errors.push('[pageerror] ' + String(e).slice(0, 200)))
  page.on('console', m => { if (m.type() === 'error') errors.push('[console.error] ' + m.text().slice(0, 200)) })

  console.log('== 1. 登录 user1 ==')
  await page.goto('http://localhost:8088/login', { waitUntil: 'networkidle', timeout: 30000 })
  await page.getByPlaceholder('用户名').fill('user1')
  await page.getByPlaceholder('密码').fill('123456')
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForTimeout(2500)
  console.log('登录后 URL:', page.url())

  console.log('== 2. 首页推荐商品渲染检查 ==')
  await page.waitForTimeout(2500) // 等推荐接口返回
  const bodyText = await page.evaluate(() => document.body.innerText.slice(0, 1500))
  const hasRecommend = /猜你喜欢|推荐/.test(bodyText)
  const priceCount = (bodyText.match(/¥\s?\d+/g) || []).length
  console.log(`推荐区块出现: ${hasRecommend}，价格标签数: ${priceCount}`)

  console.log('== 3. 打开 AI 悬浮窗 ==')
  await page.locator('text=🤖 AI').first().click()
  await page.waitForTimeout(1000)
  const panelVisible = await page.locator('text=智能客服').count()
  console.log('悬浮窗打开(找到"智能客服"标签):', panelVisible > 0)

  console.log('== 4. 智能导购对话 ==')
  await page.locator('text=智能导购').first().click()
  await page.waitForTimeout(500)
  await page.locator('input[placeholder*="问"]').last().fill('推荐两件100元以内的商品')
  await page.locator('input[placeholder*="问"]').last().press('Enter')
  // 等待流式回答完成（SSE 打字机）
  await page.waitForTimeout(25000)
  const chatText = await page.evaluate(() => document.body.innerText)
  const m = chatText.match(/推荐[\s\S]{20,400}/)
  console.log('AI 回答片段:', m ? m[0].replace(/\n+/g, ' ').slice(0, 200) : '(未捕获到回答)')

  await page.screenshot({ path: 'D:/xiangmu/Yitai/Yitai-master/backend/docs/sql/portal_final.png', fullPage: false })
  console.log('== 5. JS 错误 ==')
  console.log(errors.length ? errors.slice(0, 8).join('\n') : '(无)')
  console.log('截图: backend/docs/sql/portal_final.png')
  await browser.close()
})().catch(e => { console.error('FATAL:', String(e).slice(0, 300)); process.exit(1) })
