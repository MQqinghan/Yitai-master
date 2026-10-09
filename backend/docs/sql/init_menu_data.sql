-- ============================================================
-- init_menu_data.sql —— 管理端菜单初始化数据
-- 说明：admin-web 的动态菜单（asyncRoutes：system/product/order）
--       依赖 sys_menu + sys_role_menu 数据。component 列存的是
--       前端路由的 name（见 SysMenuServiceImpl#buildMenus：
--       SysMenuVo.name = SysMenu.component）。
-- AI 中心（ai.js）与首页（home.js）走前端固定路由 fixedRoutes，
-- 不依赖本表数据。
-- 可重复执行（先清后插）。角色id=1 为 ADMIN。
-- ============================================================

-- 清理旧数据（含历史遗留的悬空"数据分析"行）
DELETE FROM `sys_role_menu`;
DELETE FROM `sys_menu`;

-- ---------- 菜单树（component = 前端路由 name） ----------
INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `component`, `sort_value`, `status`) VALUES
(1,  0,  '系统管理', 'system',         1, 1),
(2,  1,  '角色管理', 'sysRole',        1, 1),
(3,  1,  '用户管理', 'sysUser',        2, 1),
(4,  1,  '菜单管理', 'sysMenu',        3, 1),
(5,  0,  '商品管理', 'product',        2, 1),
(6,  5,  '分类管理', 'category',       1, 1),
(7,  5,  '品牌管理', 'brand',          2, 1),
(8,  5,  '分类品牌', 'categoryBrand',  3, 1),
(9,  5,  '商品规格', 'productSpec',    4, 1),
(10, 5,  '商品管理', 'productInfo',  5, 1),
(11, 0,  '订单管理', 'order',          3, 1),
(12, 11, '订单统计', 'orderStatistics',1, 1),
(13, 11, '数据分析', 'orderAnalysis',  2, 1);

-- ---------- 角色授权（role_id=1 管理员；父菜单 is_half=1 半选） ----------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `is_half`) VALUES
(1, 1, 1), (1, 5, 1), (1, 11, 1),
(1, 2, 0), (1, 3, 0), (1, 4, 0),
(1, 6, 0), (1, 7, 0), (1, 8, 0), (1, 9, 0), (1, 10, 0),
(1, 12, 0), (1, 13, 0);

-- ---------- 清理重复角色（历史遗留：id=2 与 id=1 同为 ADMIN） ----------
DELETE FROM `sys_user_role` WHERE `role_id` = 2;
DELETE FROM `sys_role` WHERE `id` = 2;
