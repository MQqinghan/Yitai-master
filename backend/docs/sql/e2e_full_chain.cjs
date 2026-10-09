/**
 * Yitai 全链路 API 冒烟测试
 * 管理端(8501 直连) + C 端(网关 8500) + AI 服务
 */
const net = require('net')

const ADMIN = 'http://localhost:8501'
const GW = 'http://localhost:8500'
const results = []

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

async function redisGet(key) {
  const sock = net.connect(6379, '127.0.0.1')
  await new Promise(r => sock.once('connect', r))
  const raw = await redisCmd(sock, 'GET', key)
  sock.destroy()
  const lines = raw.split('\r\n')
  return lines[0].startsWith('$') ? (lines[1] || '') : ''
}

async function api(name, url, opts = {}, expect = 200) {
  const t0 = Date.now()
  try {
    const res = await fetch(url, opts)
    const body = await res.json().catch(() => ({}))
    const ok = res.status === expect && (body.code === undefined || +body.code === 200)
    results.push({ name, ok, ms: Date.now() - t0, info: ok ? '' : `http=${res.status} code=${body.code} msg=${(body.message || '').slice(0, 60)}` })
    return body
  } catch (e) {
    results.push({ name, ok: false, ms: Date.now() - t0, info: String(e).slice(0, 80) })
    return {}
  }
}

;(async () => {
  // ========== 管理端 ==========
  const vc = await api('管理端/生成验证码', `${ADMIN}/admin/system/index/generateValidateCode`)
  const codeKey = vc.data && vc.data.codeKey
  const captcha = await redisGet('user:validate' + codeKey)
  const login = await api('管理端/登录', `${ADMIN}/admin/system/index/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userName: 'admin', password: 'admin123', captcha, codeKey }),
  })
  const at = login.data && login.data.token
  const AH = { token: at, 'Content-Type': 'application/json' }

  await api('管理端/getUserInfo', `${ADMIN}/admin/system/index/getUserInfo`, { headers: AH })
  await api('管理端/动态菜单', `${ADMIN}/admin/system/index/menus`, { headers: AH })
  await api('管理端/用户分页', `${ADMIN}/admin/system/sysUser/findByPage/1/5`, { headers: AH })
  await api('管理端/角色列表', `${ADMIN}/admin/system/sysRole/findAllRoles/1`, { headers: AH })
  await api('管理端/菜单树', `${ADMIN}/admin/system/sysMenu/findNodes`, { headers: AH })
  await api('管理端/商品分页', `${ADMIN}/admin/product/product/1/5`, { headers: AH })
  await api('管理端/订单统计', `${ADMIN}/admin/order/orderInfo/getOrderStatisticsData`, { headers: AH })

  // ========== C 端（走网关）==========
  const clogin = await api('C端/user1登录', `${GW}/api/user/userInfo/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'user1', password: '123456' }),
  })
  const ut = typeof clogin.data === 'string' ? clogin.data : (clogin.data && clogin.data.token)
  const UH = { token: ut, 'Content-Type': 'application/json' }

  const idx = await api('C端/首页数据', `${GW}/api/product/index`)
  const firstSku = idx.data && idx.data.list && idx.data.list[0]
  const skuId = firstSku ? firstSku.id : 372

  await api('C端/商品搜索(ES)', `${GW}/api/product/search/1/5?keyword=裙`)
  await api('C端/商品详情', `${GW}/api/product/item/${skuId}`)
  await api('C端/猜你喜欢(推荐)', `${GW}/api/product/recommend/auth/guessYouLike/1/5`, { headers: UH })
  await api('C端/记录浏览', `${GW}/api/product/recommend/auth/recordBrowse/${skuId}`, { method: 'POST', headers: UH })
  await api('C端/加购物车', `${GW}/api/order/cart/auth/addToCart/${skuId}/2`, { headers: UH })
  await api('C端/购物车列表', `${GW}/api/order/cart/auth/cartList`, { headers: UH })
  await api('C端/结算页trade', `${GW}/api/order/orderInfo/auth/trade`, { headers: UH })
  await api('C端/订单列表', `${GW}/api/order/orderInfo/auth/1/5`, { headers: UH })
  await api('C端/未读消息数', `${GW}/api/user/message/auth/getUnreadCount`, { headers: UH })

  // ========== AI 服务 ==========
  const sse = async (name, payload) => {
    const t0 = Date.now()
    try {
      const res = await fetch(`${GW}/api/ai/chat`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      const text = await res.text()
      const hasErr = text.includes('event:error')
      const answer = text.split('\n').filter(l => l.startsWith('data:')).map(l => l.slice(5)).join('')
      results.push({ name, ok: !hasErr && answer.length > 5, ms: Date.now() - t0, info: hasErr ? text.slice(0, 100) : `回答${answer.length}字` })
    } catch (e) {
      results.push({ name, ok: false, ms: Date.now() - t0, info: String(e).slice(0, 60) })
    }
  }
  await sse('AI/导购(工具循环)', { mode: 'shopping', message: '推荐几件热销商品' })
  await sse('AI/客服(帮助文档)', { mode: 'service', message: '发票怎么开' })
  await api('AI/会话记录', `${GW}/api/ai/admin/conversations?limit=3`)

  // ========== 报告 ==========
  console.log('\n============ 测试报告 ============')
  let pass = 0, fail = 0
  results.forEach(r => {
    console.log(`${r.ok ? '✅ PASS' : '❌ FAIL'}  ${r.name.padEnd(18)} ${String(r.ms).padStart(6)}ms  ${r.info}`)
    r.ok ? pass++ : fail++
  })
  console.log(`\n合计: ${pass} PASS / ${fail} FAIL / 共 ${results.length}`)
  process.exit(fail > 0 ? 1 : 0)
})().catch(e => { console.error('FATAL:', e); process.exit(2) })
