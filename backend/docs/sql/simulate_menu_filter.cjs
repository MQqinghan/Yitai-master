// 模拟 admin-web/src/pinia/modules/menu.js 的 generateMenus 逻辑
// 用真实接口返回的菜单数据 + 前端路由模块源码提取的 name 结构
const fs = require('fs')

// ---- 1. 从路由源文件提取 name（模拟 asyncRoutes 结构） ----
function extractRoutes(file, topName) {
  const src = fs.readFileSync(file, 'utf8')
  // 顶级路由 name
  const names = [...src.matchAll(/name:\s*'([^']+)'/g)].map(m => m[1])
  return { topName, childNames: names }
}

const base = 'D:/xiangmu/Yitai/Yitai-master/admin-web/src/router/modules/'
const sys = extractRoutes(base + 'system.js', 'system')
const prod = extractRoutes(base + 'product.js', 'product')
const order = extractRoutes(base + 'order.js', 'order')

// asyncRoutes 简化结构：顶级 name + children name 列表（含顶级自身 name 会被剔除）
const asyncRoutes = [sys, prod, order].map(r => ({
  name: r.topName,
  children: r.childNames.filter(n => n !== r.topName),
}))
console.log('前端 asyncRoutes 结构:')
asyncRoutes.forEach(r => console.log(`  ${r.name} -> [${r.children.join(', ')}]`))

// ---- 2. 真实后端 menus 数据 ----
const menusRaw = JSON.parse(fs.readFileSync(process.env.MENUS_FILE || '/tmp/menus.json', 'utf8'))
const data = menusRaw.data
console.log('\n后端菜单 name 树:')
const dump = (arr, ind) => arr.forEach(m => { console.log(ind + m.name); if (m.children) dump(m.children, ind + '  ') })
dump(data, '  ')

// ---- 3. 复现 getFilterRoutes ----
function getFilterRoutes(targetRoutes, ajaxRoutes) {
  const out = []
  ajaxRoutes.forEach(item => {
    const target = targetRoutes.find(t => t.name === item.name)
    if (target) {
      const route = { name: target.name }
      if (item.children) {
        route.children = getFilterRoutes(target.children || [], item.children)
        if (!route.children.length) delete route.children
      }
      out.push(route)
    }
  })
  return out
}
const filterRoutes = getFilterRoutes(asyncRoutes, data)
console.log('\ngetFilterRoutes 结果:', JSON.stringify(filterRoutes))
if (!filterRoutes.length) console.log('>>> 顶级 name 全部匹配失败！addRoute 不会执行任何动态路由')

// ---- 4. 递归对齐检查（后端每个 name 是否在前端对应层级存在） ----
function checkAlign(feList, beList, path) {
  beList.forEach(be => {
    const hit = feList.find(f => f.name === be.name)
    if (!hit) {
      console.log(`MISMATCH: ${path}${be.name} 后端有、前端无对应路由 name`)
    }
    if (be.children && hit) checkAlign(hit.children || [], be.children, path + be.name + '.')
    if (be.children && !hit) checkAlign([], be.children, path + be.name + '.(孤儿)')
  })
}
console.log('\n递归对齐检查:')
checkAlign(asyncRoutes, data, '')
console.log('（无 MISMATCH 输出即全对齐）')
