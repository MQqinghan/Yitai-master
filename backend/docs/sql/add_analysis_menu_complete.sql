-- ============================================
-- 一键添加"数据分析"菜单并分配权限
-- ============================================

-- 1. 插入"数据分析"菜单
INSERT INTO sys_menu (
    parent_id,
    title,
    component,
    sort_value,
    status,
    create_time,
    update_time,
    is_deleted
) VALUES (
    16,  -- 订单管理的ID
    '数据分析',
    '/orderAnalysis',
    4,
    1,
    NOW(),
    NOW(),
    0
);

-- 2. 获取刚插入的菜单ID
SET @menu_id = LAST_INSERT_ID();

-- 3. 验证菜单是否插入成功
SELECT '菜单插入结果:' AS info;
SELECT id, parent_id, title, component, sort_value, status 
FROM sys_menu 
WHERE id = @menu_id;

-- 4. 为所有现有角色分配该菜单权限
-- 先查看有哪些角色
SELECT '现有角色列表:' AS info;
SELECT id, role_name FROM sys_role WHERE is_deleted = 0;

-- 为每个角色分配菜单权限（这里以role_id=1为例，根据需要调整）
INSERT INTO sys_role_menu (
    role_id, 
    menu_id, 
    create_time, 
    update_time, 
    is_deleted, 
    is_half
) 
SELECT 
    r.id AS role_id,
    @menu_id AS menu_id,
    NOW() AS create_time,
    NOW() AS update_time,
    0 AS is_deleted,
    0 AS is_half
FROM sys_role r
WHERE r.is_deleted = 0;

-- 5. 验证权限分配结果
SELECT '权限分配结果:' AS info;
SELECT srm.role_id, sr.role_name, srm.menu_id, sm.title
FROM sys_role_menu srm
INNER JOIN sys_role sr ON srm.role_id = sr.id
INNER JOIN sys_menu sm ON srm.menu_id = sm.id
WHERE sm.title = '数据分析';

-- ============================================
-- 完成！请重新登录系统查看新菜单
-- ============================================
