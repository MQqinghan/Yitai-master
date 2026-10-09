-- ============================================
-- 快速添加"数据分析"菜单（简化版）
-- 使用前请先查询订单管理的ID，然后修改第8行的parent_id值
-- ============================================

-- 【重要】先执行这个查询，找到订单管理菜单的ID
SELECT id, title FROM sys_menu WHERE title LIKE '%订单%' AND parent_id = 0;

-- 【重要】将下面的 20 替换为你查询到的订单管理菜单ID
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
    16,  -- ← 这里改成订单管理的实际ID
    '数据分析',
    '/orderAnalysis',
    4,
    1,
    NOW(),
    NOW(),
    0
);

-- 验证是否插入成功
SELECT * FROM sys_menu WHERE title = '数据分析';
