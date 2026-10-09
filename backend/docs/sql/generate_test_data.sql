-- ========================================
-- 压力测试数据生成脚本
-- 用途：为 JMeter 压力测试生成充足的测试数据
-- 目标：500+ 商品SKU，100+ 用户
-- ========================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 1. 生成分类数据（三级分类）
-- ========================================

-- 一级分类（10个）
INSERT IGNORE INTO category (id, name, image_url, parent_id, status, order_num, create_time, update_time, is_deleted) VALUES
(1, '数码电子', '/images/category/digital.jpg', 0, 1, 1, NOW(), NOW(), 0),
(2, '服装服饰', '/images/category/clothing.jpg', 0, 1, 2, NOW(), NOW(), 0),
(3, '食品饮料', '/images/category/food.jpg', 0, 1, 3, NOW(), NOW(), 0),
(4, '家居家装', '/images/category/home.jpg', 0, 1, 4, NOW(), NOW(), 0),
(5, '美妆护肤', '/images/category/beauty.jpg', 0, 1, 5, NOW(), NOW(), 0),
(6, '运动户外', '/images/category/sports.jpg', 0, 1, 6, NOW(), NOW(), 0),
(7, '图书音像', '/images/category/book.jpg', 0, 1, 7, NOW(), NOW(), 0),
(8, '母婴用品', '/images/category/baby.jpg', 0, 1, 8, NOW(), NOW(), 0),
(9, '医药保健', '/images/category/health.jpg', 0, 1, 9, NOW(), NOW(), 0),
(10, '汽车用品', '/images/category/car.jpg', 0, 1, 10, NOW(), NOW(), 0);

-- 二级分类（每个一级分类下3个，共30个）
INSERT IGNORE INTO category (id, name, image_url, parent_id, status, order_num, create_time, update_time, is_deleted) VALUES
-- 数码电子下级
(11, '手机通讯', '/images/category/phone.jpg', 1, 1, 1, NOW(), NOW(), 0),
(12, '电脑办公', '/images/category/computer.jpg', 1, 1, 2, NOW(), NOW(), 0),
(13, '智能穿戴', '/images/category/wearable.jpg', 1, 1, 3, NOW(), NOW(), 0),
-- 服装服饰下级
(14, '男装', '/images/category/men.jpg', 2, 1, 1, NOW(), NOW(), 0),
(15, '女装', '/images/category/women.jpg', 2, 1, 2, NOW(), NOW(), 0),
(16, '童装', '/images/category/kids.jpg', 2, 1, 3, NOW(), NOW(), 0),
-- 食品饮料下级
(17, '休闲零食', '/images/category/snack.jpg', 3, 1, 1, NOW(), NOW(), 0),
(18, '生鲜水果', '/images/category/fruit.jpg', 3, 1, 2, NOW(), NOW(), 0),
(19, '酒水饮料', '/images/category/drink.jpg', 3, 1, 3, NOW(), NOW(), 0),
-- 家居家装下级
(20, '家具', '/images/category/furniture.jpg', 4, 1, 1, NOW(), NOW(), 0),
(21, '家纺', '/images/category/textile.jpg', 4, 1, 2, NOW(), NOW(), 0),
(22, '灯具', '/images/category/light.jpg', 4, 1, 3, NOW(), NOW(), 0),
-- 美妆护肤下级
(23, '面部护肤', '/images/category/skincare.jpg', 5, 1, 1, NOW(), NOW(), 0),
(24, '彩妆', '/images/category/makeup.jpg', 5, 1, 2, NOW(), NOW(), 0),
(25, '香水', '/images/category/perfume.jpg', 5, 1, 3, NOW(), NOW(), 0),
-- 运动户外下级
(26, '运动鞋包', '/images/category/sportshoe.jpg', 6, 1, 1, NOW(), NOW(), 0),
(27, '健身器材', '/images/category/fitness.jpg', 6, 1, 2, NOW(), NOW(), 0),
(28, '户外装备', '/images/category/outdoor.jpg', 6, 1, 3, NOW(), NOW(), 0),
-- 图书音像下级
(29, '小说文学', '/images/category/novel.jpg', 7, 1, 1, NOW(), NOW(), 0),
(30, '科技计算机', '/images/category/tech.jpg', 7, 1, 2, NOW(), NOW(), 0),
(31, '教育考试', '/images/category/education.jpg', 7, 1, 3, NOW(), NOW(), 0),
-- 母婴用品下级
(32, '奶粉', '/images/category/milk.jpg', 8, 1, 1, NOW(), NOW(), 0),
(33, '纸尿裤', '/images/category/diaper.jpg', 8, 1, 2, NOW(), NOW(), 0),
(34, '玩具', '/images/category/toy.jpg', 8, 1, 3, NOW(), NOW(), 0),
-- 医药保健下级
(35, '保健食品', '/images/category/healthfood.jpg', 9, 1, 1, NOW(), NOW(), 0),
(36, '医疗器械', '/images/category/medical.jpg', 9, 1, 2, NOW(), NOW(), 0),
(37, '中药滋补', '/images/category/herb.jpg', 9, 1, 3, NOW(), NOW(), 0),
-- 汽车用品下级
(38, '车载电器', '/images/category/carElec.jpg', 10, 1, 1, NOW(), NOW(), 0),
(39, '美容清洗', '/images/category/carWash.jpg', 10, 1, 2, NOW(), NOW(), 0),
(40, '汽车装饰', '/images/category/carDecor.jpg', 10, 1, 3, NOW(), NOW(), 0);

-- 三级分类（每个二级分类下2-3个，共80个）
INSERT IGNORE INTO category (id, name, image_url, parent_id, status, order_num, create_time, update_time, is_deleted) VALUES
-- 手机通讯下级
(41, '智能手机', '/images/category/smartphone.jpg', 11, 1, 1, NOW(), NOW(), 0),
(42, '老人机', '/images/category/oldphone.jpg', 11, 1, 2, NOW(), NOW(), 0),
(43, '手机配件', '/images/category/phoneacc.jpg', 11, 1, 3, NOW(), NOW(), 0),
-- 电脑办公下级
(44, '笔记本', '/images/category/laptop.jpg', 12, 1, 1, NOW(), NOW(), 0),
(45, '台式机', '/images/category/desktop.jpg', 12, 1, 2, NOW(), NOW(), 0),
(46, '平板电脑', '/images/category/tablet.jpg', 12, 1, 3, NOW(), NOW(), 0),
-- 智能穿戴下级
(47, '智能手表', '/images/category/smartwatch.jpg', 13, 1, 1, NOW(), NOW(), 0),
(48, '智能手环', '/images/category/band.jpg', 13, 1, 2, NOW(), NOW(), 0),
-- 男装下级
(49, 'T恤', '/images/category/tshirt.jpg', 14, 1, 1, NOW(), NOW(), 0),
(50, '衬衫', '/images/category/shirt.jpg', 14, 1, 2, NOW(), NOW(), 0),
(51, '外套', '/images/category/jacket.jpg', 14, 1, 3, NOW(), NOW(), 0),
-- 女装下级
(52, '连衣裙', '/images/category/dress.jpg', 15, 1, 1, NOW(), NOW(), 0),
(53, '上衣', '/images/category/top.jpg', 15, 1, 2, NOW(), NOW(), 0),
(54, '裤装', '/images/category/pants.jpg', 15, 1, 3, NOW(), NOW(), 0),
-- 童装下级
(55, '婴幼服装', '/images/category/babycloth.jpg', 16, 1, 1, NOW(), NOW(), 0),
(56, '儿童外套', '/images/category/kidjacket.jpg', 16, 1, 2, NOW(), NOW(), 0),
-- 休闲零食下级
(57, '坚果炒货', '/images/category/nut.jpg', 17, 1, 1, NOW(), NOW(), 0),
(58, '糖果巧克力', '/images/category/candy.jpg', 17, 1, 2, NOW(), NOW(), 0),
(59, '饼干蛋糕', '/images/category/cake.jpg', 17, 1, 3, NOW(), NOW(), 0),
-- 生鲜水果下级
(60, '新鲜水果', '/images/category/freshfruit.jpg', 18, 1, 1, NOW(), NOW(), 0),
(61, '海鲜水产', '/images/category/seafood.jpg', 18, 1, 2, NOW(), NOW(), 0),
-- 酒水饮料下级
(62, '白酒', '/images/category/liquor.jpg', 19, 1, 1, NOW(), NOW(), 0),
(63, '红酒', '/images/category/wine.jpg', 19, 1, 2, NOW(), NOW(), 0),
(64, '啤酒', '/images/category/beer.jpg', 19, 1, 3, NOW(), NOW(), 0),
-- 家具下级
(65, '沙发', '/images/category/sofa.jpg', 20, 1, 1, NOW(), NOW(), 0),
(66, '床', '/images/category/bed.jpg', 20, 1, 2, NOW(), NOW(), 0),
-- 家纺下级
(67, '四件套', '/images/category/bedding.jpg', 21, 1, 1, NOW(), NOW(), 0),
(68, '被子', '/images/category/quilt.jpg', 21, 1, 2, NOW(), NOW(), 0),
-- 灯具下级
(69, '吸顶灯', '/images/category/ceilinglight.jpg', 22, 1, 1, NOW(), NOW(), 0),
(70, '台灯', '/images/category/desklamp.jpg', 22, 1, 2, NOW(), NOW(), 0),
-- 面部护肤下级
(71, '洁面', '/images/category/cleanser.jpg', 23, 1, 1, NOW(), NOW(), 0),
(72, '面膜', '/images/category/mask.jpg', 23, 1, 2, NOW(), NOW(), 0),
(73, '乳液面霜', '/images/category/cream.jpg', 23, 1, 3, NOW(), NOW(), 0),
-- 彩妆下级
(74, '口红', '/images/category/lipstick.jpg', 24, 1, 1, NOW(), NOW(), 0),
(75, '粉底', '/images/category/foundation.jpg', 24, 1, 2, NOW(), NOW(), 0),
-- 香水下级
(76, '女士香水', '/images/category/womenperfume.jpg', 25, 1, 1, NOW(), NOW(), 0),
(77, '男士香水', '/images/category/menperfume.jpg', 25, 1, 2, NOW(), NOW(), 0),
-- 运动鞋包下级
(78, '跑步鞋', '/images/category/runningshoe.jpg', 26, 1, 1, NOW(), NOW(), 0),
(79, '篮球鞋', '/images/category/basketballshoe.jpg', 26, 1, 2, NOW(), NOW(), 0),
-- 健身器材下级
(80, '跑步机', '/images/category/treadmill.jpg', 27, 1, 1, NOW(), NOW(), 0),
(81, '哑铃', '/images/category/dumbbell.jpg', 27, 1, 2, NOW(), NOW(), 0),
-- 户外装备下级
(82, '帐篷', '/images/category/tent.jpg', 28, 1, 1, NOW(), NOW(), 0),
(83, '登山杖', '/images/category/hiking.jpg', 28, 1, 2, NOW(), NOW(), 0),
-- 小说文学下级
(84, '言情小说', '/images/category/romance.jpg', 29, 1, 1, NOW(), NOW(), 0),
(85, '科幻小说', '/images/category/scifi.jpg', 29, 1, 2, NOW(), NOW(), 0),
-- 科技计算机下级
(86, '编程语言', '/images/category/programming.jpg', 30, 1, 1, NOW(), NOW(), 0),
(87, '人工智能', '/images/category/ai.jpg', 30, 1, 2, NOW(), NOW(), 0),
-- 教育考试下级
(88, '考研英语', '/images/category/kaoyan.jpg', 31, 1, 1, NOW(), NOW(), 0),
(89, '公务员考试', '/images/category/gongkao.jpg', 31, 1, 2, NOW(), NOW(), 0),
-- 奶粉下级
(90, '婴儿奶粉', '/images/category/babymilk.jpg', 32, 1, 1, NOW(), NOW(), 0),
(91, '孕妇奶粉', '/images/category/pregnantmilk.jpg', 32, 1, 2, NOW(), NOW(), 0),
-- 纸尿裤下级
(92, '拉拉裤', '/images/category/pullup.jpg', 33, 1, 1, NOW(), NOW(), 0),
(93, '纸尿片', '/images/category/diaperpad.jpg', 33, 1, 2, NOW(), NOW(), 0),
-- 玩具下级
(94, '益智玩具', '/images/category/puzzle.jpg', 34, 1, 1, NOW(), NOW(), 0),
(95, '遥控玩具', '/images/category/rc.jpg', 34, 1, 2, NOW(), NOW(), 0),
-- 保健食品下级
(96, '维生素', '/images/category/vitamin.jpg', 35, 1, 1, NOW(), NOW(), 0),
(97, '蛋白粉', '/images/category/protein.jpg', 35, 1, 2, NOW(), NOW(), 0),
-- 医疗器械下级
(98, '血压计', '/images/category/bloodpressure.jpg', 36, 1, 1, NOW(), NOW(), 0),
(99, '血糖仪', '/images/category/glucose.jpg', 36, 1, 2, NOW(), NOW(), 0),
-- 中药滋补下级
(100, '人参', '/images/category/ginseng.jpg', 37, 1, 1, NOW(), NOW(), 0),
(101, '枸杞', '/images/category/goji.jpg', 37, 1, 2, NOW(), NOW(), 0),
-- 车载电器下级
(102, '行车记录仪', '/images/category/dvr.jpg', 38, 1, 1, NOW(), NOW(), 0),
(103, '车载充电器', '/images/category/carcharger.jpg', 38, 1, 2, NOW(), NOW(), 0),
-- 美容清洗下级
(104, '洗车液', '/images/category/carwash.jpg', 39, 1, 1, NOW(), NOW(), 0),
(105, '打蜡机', '/images/category/wax.jpg', 39, 1, 2, NOW(), NOW(), 0),
-- 汽车装饰下级
(106, '座垫', '/images/category/carseat.jpg', 40, 1, 1, NOW(), NOW(), 0),
(107, '脚垫', '/images/category/carmat.jpg', 40, 1, 2, NOW(), NOW(), 0);

-- ========================================
-- 2. 生成品牌数据（20个品牌）
-- ========================================

INSERT IGNORE INTO brand (id, name, logo, create_time, update_time, is_deleted) VALUES
(1, '华为', '/images/brand/huawei.png', NOW(), NOW(), 0),
(2, '小米', '/images/brand/xiaomi.png', NOW(), NOW(), 0),
(3, '苹果', '/images/brand/apple.png', NOW(), NOW(), 0),
(4, '三星', '/images/brand/samsung.png', NOW(), NOW(), 0),
(5, '耐克', '/images/brand/nike.png', NOW(), NOW(), 0),
(6, '阿迪达斯', '/images/brand/adidas.png', NOW(), NOW(), 0),
(7, '优衣库', '/images/brand/uniqlo.png', NOW(), NOW(), 0),
(8, '三只松鼠', '/images/brand/squirrel.png', NOW(), NOW(), 0),
(9, '良品铺子', '/images/brand/liangpin.png', NOW(), NOW(), 0),
(10, '茅台', '/images/brand/maotai.png', NOW(), NOW(), 0),
(11, '五粮液', '/images/brand/wuliangye.png', NOW(), NOW(), 0),
(12, '欧莱雅', '/images/brand/loreal.png', NOW(), NOW(), 0),
(13, '兰蔻', '/images/brand/lancome.png', NOW(), NOW(), 0),
(14, 'SK-II', '/images/brand/skii.png', NOW(), NOW(), 0),
(15, '全友家居', '/images/brand/quanyou.png', NOW(), NOW(), 0),
(16, '顾家家居', '/images/brand/kuka.png', NOW(), NOW(), 0),
(17, '安踏', '/images/brand/anta.png', NOW(), NOW(), 0),
(18, '李宁', '/images/brand/lining.png', NOW(), NOW(), 0),
(19, '联想', '/images/brand/lenovo.png', NOW(), NOW(), 0),
(20, '戴尔', '/images/brand/dell.png', NOW(), NOW(), 0);

-- ========================================
-- 3. 生成计量单位
-- ========================================

INSERT IGNORE INTO product_unit (id, name, create_time, update_time, is_deleted) VALUES
(1, '件', NOW(), NOW(), 0),
(2, '盒', NOW(), NOW(), 0),
(3, '瓶', NOW(), NOW(), 0),
(4, '袋', NOW(), NOW(), 0),
(5, '双', NOW(), NOW(), 0),
(6, '台', NOW(), NOW(), 0),
(7, '本', NOW(), NOW(), 0),
(8, '套', NOW(), NOW(), 0);

-- ========================================
-- 4. 生成商品数据（200个商品）
-- ========================================

-- 这里使用存储过程批量生成商品
DROP PROCEDURE IF EXISTS generate_products;
DELIMITER $$
CREATE PROCEDURE generate_products()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE product_name VARCHAR(100);
    DECLARE brand_id_val INT;
    DECLARE category1_id INT;
    DECLARE category2_id INT;
    DECLARE category3_id INT;
    DECLARE unit_name VARCHAR(10);
    
    WHILE i <= 200 DO
        -- 根据序号分配分类和品牌
        SET category1_id = ((i - 1) % 10) + 1;
        SET category2_id = 11 + ((i - 1) % 30);
        SET category3_id = 41 + ((i - 1) % 67);
        SET brand_id_val = ((i - 1) % 20) + 1;
        
        -- 根据分类选择单位
        IF category1_id IN (1, 12) THEN
            SET unit_name = '台';
        ELSEIF category1_id IN (2, 6) THEN
            SET unit_name = '件';
        ELSEIF category1_id IN (3, 19) THEN
            SET unit_name = '瓶';
        ELSEIF category1_id IN (7) THEN
            SET unit_name = '本';
        ELSE
            SET unit_name = '件';
        END IF;
        
        -- 生成商品名称
        SET product_name = CONCAT('测试商品_', i, '_', 
            CASE category1_id
                WHEN 1 THEN '数码'
                WHEN 2 THEN '服装'
                WHEN 3 THEN '食品'
                WHEN 4 THEN '家居'
                WHEN 5 THEN '美妆'
                WHEN 6 THEN '运动'
                WHEN 7 THEN '图书'
                WHEN 8 THEN '母婴'
                WHEN 9 THEN '保健'
                WHEN 10 THEN '汽车'
            END
        );
        
        -- 插入商品（使用 INSERT IGNORE 避免重复）
        INSERT IGNORE INTO product (
            id, name, brand_id, category1_id, category2_id, category3_id, 
            unit_name, slider_urls, spec_value, status, audit_status, 
            audit_message, create_time, update_time, is_deleted
        ) VALUES (
            i, 
            product_name, 
            brand_id_val, 
            category1_id, 
            category2_id, 
            category3_id,
            unit_name,
            '/images/product/slider1.jpg,/images/product/slider2.jpg,/images/product/slider3.jpg',
            '[{"key":"颜色","valueList":["红色","蓝色","黑色"]},{"key":"尺寸","valueList":["S","M","L"]}]',
            1, 1, '审核通过',
            NOW(), NOW(), 0
        );
        
        -- 插入商品详情
        INSERT IGNORE INTO product_details (
            id, product_id, image_urls, create_time, update_time, is_deleted
        ) VALUES (
            i,
            i,
            '/images/product/detail1.jpg,/images/product/detail2.jpg,/images/product/detail3.jpg,/images/product/detail4.jpg',
            NOW(), NOW(), 0
        );
        
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 执行存储过程生成商品
CALL generate_products();

-- 删除存储过程
DROP PROCEDURE IF EXISTS generate_products;

-- ========================================
-- 5. 生成商品SKU数据（每个商品2-3个SKU，共500+ SKU）
-- ========================================

DROP PROCEDURE IF EXISTS generate_skus;
DELIMITER $$
CREATE PROCEDURE generate_skus()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE sku_index INT DEFAULT 1;
    DECLARE product_id_val INT;
    DECLARE sku_name VARCHAR(100);
    DECLARE sale_price DECIMAL(10,2);
    DECLARE market_price DECIMAL(10,2);
    DECLARE cost_price DECIMAL(10,2);
    DECLARE stock_num INT;
    DECLARE sale_num INT;
    DECLARE sku_spec VARCHAR(200);
    DECLARE j INT DEFAULT 1;
    DECLARE sku_count INT;
    
    SET sku_index = 1;
    
    WHILE i <= 200 DO
        -- 每个商品生成2-3个SKU
        SET j = 1;
        SET sku_count = 2 + (i % 2); -- 交替生成2或3个SKU
        
        WHILE j <= sku_count DO
            SET product_id_val = i;
            
            -- 生成SKU名称
            SET sku_name = CONCAT('SKU-', sku_index, '-', 
                CASE j
                    WHEN 1 THEN '红色-S'
                    WHEN 2 THEN '蓝色-M'
                    WHEN 3 THEN '黑色-L'
                END
            );
            
            -- 生成价格（50-999元之间）
            SET sale_price = ROUND(50 + RAND() * 949, 2);
            SET market_price = ROUND(sale_price * (1.2 + RAND() * 0.3), 2);
            SET cost_price = ROUND(sale_price * 0.6, 2);
            
            -- 生成库存（10-500之间）
            SET stock_num = 10 + FLOOR(RAND() * 490);
            
            -- 生成销量（0-1000之间）
            SET sale_num = FLOOR(RAND() * 1000);
            
            -- 生成规格JSON
            SET sku_spec = CASE j
                WHEN 1 THEN '{"颜色":"红色","尺寸":"S"}'
                WHEN 2 THEN '{"颜色":"蓝色","尺寸":"M"}'
                WHEN 3 THEN '{"颜色":"黑色","尺寸":"L"}'
            END;
            
            -- 插入SKU（使用 INSERT IGNORE 避免重复）
            INSERT IGNORE INTO product_sku (
                id, sku_code, sku_name, product_id, thumb_img, 
                sale_price, market_price, cost_price, stock_num, sale_num,
                sku_spec, weight, volume, status, create_time, update_time, is_deleted
            ) VALUES (
                sku_index,
                CONCAT('SKU', LPAD(sku_index, 6, '0')),
                sku_name,
                product_id_val,
                CONCAT('/images/product/sku_', sku_index, '.jpg'),
                sale_price,
                market_price,
                cost_price,
                stock_num,
                sale_num,
                sku_spec,
                CONCAT(ROUND(0.1 + RAND() * 5, 2), 'kg'),
                CONCAT(ROUND(0.01 + RAND() * 0.1, 3), 'm³'),
                1,
                NOW(),
                NOW(),
                0
            );
            
            SET sku_index = sku_index + 1;
            SET j = j + 1;
        END WHILE;
        
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 执行存储过程生成SKU
CALL generate_skus();

-- 删除存储过程
DROP PROCEDURE IF EXISTS generate_skus;

-- ========================================
-- 6. 生成测试用户数据（100个用户）
-- ========================================

DROP PROCEDURE IF EXISTS generate_users;
DELIMITER $$
CREATE PROCEDURE generate_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE phone VARCHAR(11);
    DECLARE pwd VARCHAR(100);
    DECLARE nick_name VARCHAR(50);
    
    WHILE i <= 100 DO
        -- 生成手机号
        SET phone = CONCAT('138', LPAD(i, 8, '0'));
        
        -- 密码统一为 123456 的 MD5 值（实际项目中应该加密）
        SET pwd = 'e10adc3949ba59abbe56e057f20f883e';
        
        -- 生成昵称
        SET nick_name = CONCAT('测试用户_', i);
        
        -- 插入用户（假设 user_info 表存在）
        -- 注意：如果你的用户表名不同，请修改下面的表名
        INSERT IGNORE INTO user_info (
            id, username, password, nick_name, phone, avatar, status, 
            create_time, update_time, is_deleted
        ) VALUES (
            i,
            phone,
            pwd,
            nick_name,
            phone,
            '/images/avatar/default.png',
            1,
            NOW(),
            NOW(),
            0
        ) ON DUPLICATE KEY UPDATE
            nick_name = VALUES(nick_name);
        
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 执行存储过程生成用户（如果 user_info 表存在）
-- CALL generate_users();

-- 删除存储过程
DROP PROCEDURE IF EXISTS generate_users;

-- ========================================
-- 7. 数据统计验证
-- ========================================

SELECT '===== 数据统计 =====' AS info;

SELECT COUNT(*) AS '一级分类数量' FROM category WHERE parent_id = 0 AND is_deleted = 0;
SELECT COUNT(*) AS '二级分类数量' FROM category WHERE parent_id > 0 AND parent_id <= 10 AND is_deleted = 0;
SELECT COUNT(*) AS '三级分类数量' FROM category WHERE parent_id > 10 AND is_deleted = 0;
SELECT COUNT(*) AS '品牌数量' FROM brand WHERE is_deleted = 0;
SELECT COUNT(*) AS '商品数量' FROM product WHERE is_deleted = 0;
SELECT COUNT(*) AS 'SKU数量' FROM product_sku WHERE is_deleted = 0;
SELECT COUNT(*) AS '商品详情数量' FROM product_details WHERE is_deleted = 0;

SELECT '===== 数据生成完成 =====' AS info;
SELECT '提示：如果 user_info 表结构不同，请手动调整用户数据生成部分' AS note;

SET FOREIGN_KEY_CHECKS = 1;
