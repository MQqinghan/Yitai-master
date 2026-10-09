-- ============================================
-- 添加"数据分析"菜单到订单管理下
-- ============================================

-- 第一步：查询订单管理菜单的ID（请根据实际情况调整）
-- 执行以下查询，找到"订单管理"菜单的ID
SELECT id, title, parent_id FROM sys_menu WHERE title LIKE '%订单%' AND parent_id = 0 LIMIT 5;

-- 第二步：根据查询结果，将下面的 @order_parent_id 替换为实际的订单管理菜单ID
-- 假设订单管理的ID是 20（请根据实际情况修改）
SET @order_parent_id = 20;

-- 第三步：插入"数据分析"菜单
INSERT INTO sys_menu (
    id,
    parent_id,
    title,
    component,
    sort_value,
    status,
    create_time,
    update_time,
    is_deleted
) VALUES (
    NULL,  -- 自增ID，由数据库自动生成
    @order_parent_id,  -- 父菜单ID（订单管理的ID）
    '数据分析',  -- 菜单名称
    '/orderAnalysis',  -- 路由路径/组件路径
    4,  -- 排序号（根据需要调整，数字越小越靠前）
    1,  -- 状态：1正常，0禁用
    NOW(),  -- 创建时间
    NOW(),  -- 更新时间
    0  -- 是否删除：0未删除
);

-- 第四步：验证插入结果
SELECT id, parent_id, title, component, sort_value, status 
FROM sys_menu 
WHERE title = '数据分析';

-- ============================================
-- 如果需要为特定角色分配该菜单权限，执行以下操作：
-- ============================================

-- 查询所有角色ID
-- SELECT id, role_name FROM sys_role WHERE is_deleted = 0;

-- 为管理员角色（假设role_id=1）分配该菜单权限
-- 注意：需要先获取刚插入的"数据分析"菜单的ID
-- SET @analysis_menu_id = LAST_INSERT_ID();
-- INSERT INTO sys_role_menu (role_id, menu_id, create_time, update_time, is_deleted, is_half)
-- VALUES (1, @analysis_menu_id, NOW(), NOW(), 0, 0);

-- ============================================
-- 说明：
-- 1. parent_id: 需要替换为"订单管理"菜单的实际ID
-- 2. component: 前端路由路径，对应前端的 /orderAnalysis 页面
-- 3. sort_value: 排序号，决定菜单显示顺序（建议设置为4，排在订单列表、订单统计之后）
-- 4. status: 1表示启用，0表示禁用
-- 5. 插入后需要重新登录或刷新权限才能看到新菜单
-- ============================================
