/**
 * admin-web 登录流程 E2E 诊断脚本
 * 打开登录页 → Redis 读验证码 → 登录 → 观察跳转与控制台错误
 */
const { chromium } = require('playwright-core')
const net = require('net')

const BASE = 'http://localhost:3003'

// 迷你 Redis 客户端（绕开 node spawnSync EBUSY 问题）
function redisCmd(sock, ...args) {
  return new Promise(resolve => {
    let toSend = `*${args.length}\r\n`
    for (const a of args) toSend += `$${Buffer.byteLength(a)}\r\n${a}\r\n`
    sock.write(toSend)
    let buf = ''
    const onData = d => { buf += d.toString() }
    sock.on('data', onData)
    setTimeout(() => { sock.removeListener('data', onData); resolve(buf) }, 250)
  })
}

async function latestCaptcha() {
  const sock = net.connect(6379, '127.0.0.1')
  await new Promise(r => sock.once('connect', r))
  const keysRaw = await redisCmd(sock, 'KEYS', 'user:validate*')
  const lines = keysRaw.split('\r\n').filter(l => l && !l.startsWith('*') && !l.startsWith('$'))
  let best = { ttl: -1, val: '' }
  for (const k of lines) {
    const ttlRaw = (await redisCmd(sock, 'TTL', k)).split('\r\n')[0] || ''
    const ttl = parseInt(String(ttlRaw).replace(/[^0-9-]/g, ''))
    if (!isNaN(ttl) && ttl > best.ttl) {
      const g = (await redisCmd(sock, 'GET', k)).split('\r\n')
      best = { ttl, val: g[1] || '' }
    }
  }
  sock.destroy()
  return best.val
}

;(async () => {
  const browser = await chromium.launch({ channel: 'msedge', headless: true })
  const page = await browser.newPage()
  const errors = []
  page.on('console', m => {
    if (m.type() === 'error' || m.type() === 'warning') {
      errors.push(`[console.${m.type()}] ${m.text().slice(0, 300)}`)
    }
  })
  page.on('pageerror', e => errors.push(`[pageerror] ${String(e).slice(0, 500)}`))
  page.on('requestfailed', r => errors.push(`[requestfailed] ${r.method()} ${r.url()} :: ${r.failure() && r.failure().errorText}`))

  console.log('== 1. 打开登录页 ==')
  await page.goto(BASE, { waitUntil: 'networkidle' })
  console.log('URL:', page.url())

  console.log('== 2. 填表单 ==')
  await page.getByPlaceholder('用户名').fill('admin')
  await page.getByPlaceholder('密码').fill('admin123')
  await page.waitForTimeout(1500) // 等页面 generateValidateCode 写入 Redis
  const code = await latestCaptcha()
  console.log('验证码(来自Redis):', code)
  await page.getByPlaceholder('请输入验证码').fill(code)

  console.log('== 3. 提交登录 ==')
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForTimeout(6000)

  console.log('== 4. 结果 ==')
  console.log('最终 URL:', page.url())
  const tokenRaw = await page.evaluate(() => window.localStorage.getItem('VEA-TOKEN'))
  console.log('localStorage VEA-TOKEN:', tokenRaw ? tokenRaw.slice(0, 60) + '...' : '(空)')

  // 菜单是否渲染
  const menuText = await page.evaluate(() => {
    const aside = document.querySelector('.el-aside, .sidebar, aside')
    return aside ? aside.innerText.slice(0, 200) : '(未找到侧栏容器)'
  }).catch(() => '(取菜单失败)')
  console.log('侧栏内容:', menuText.replace(/\n/g, ' | '))

  console.log('== 5. 控制台/网络错误汇总 ==')
  if (errors.length === 0) console.log('(无错误)')
  errors.slice(0, 25).forEach(e => console.log(e))

  await page.screenshot({ path: 'D:/xiangmu/Yitai/Yitai-master/backend/docs/sql/after_login.png' })
  console.log('截图: backend/docs/sql/after_login.png')
  await browser.close()
})().catch(e => { console.error('SCRIPT_FAIL:', e); process.exit(1) })
