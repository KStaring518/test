-- 修复商家和商品数据的SQL脚本

-- 1. 清理现有的无效数据
DELETE FROM products WHERE merchant_id = 0 OR merchant_id IS NULL;
DELETE FROM merchants WHERE id = 0;

-- 2. 重新创建商家数据
INSERT INTO merchants (id, user_id, shop_name, shop_description, contact_phone, business_license, status, created_at) VALUES
(1, 15, '零食小铺', '专业零食销售', '13800138000', 'LICENSE001', 'APPROVED', CURRENT_TIMESTAMP);

-- 3. 重新创建商品数据，确保merchant_id正确
INSERT INTO products (name, subtitle, category_id, merchant_id, cover_image, price, unit, stock, status, description, created_at) VALUES
('开心果', '香脆可口', 3, 1, '/images/happy-nut.jpg', 25.80, '袋', 100, 'ON_SALE', '精选开心果，营养丰富', CURRENT_TIMESTAMP),
('巧克力', '丝滑浓郁', 4, 1, '/images/chocolate.jpg', 15.50, '盒', 200, 'ON_SALE', '比利时进口巧克力', CURRENT_TIMESTAMP),
('可乐', '经典口味', 5, 1, '/images/cola.jpg', 3.50, '瓶', 500, 'ON_SALE', '百事可乐，清爽解渴', CURRENT_TIMESTAMP);

-- 4. 验证数据
SELECT 'Products count:' as info, COUNT(*) as count FROM products
UNION ALL
SELECT 'Merchants count:', COUNT(*) FROM merchants
UNION ALL
SELECT 'Products with valid merchant:', COUNT(*) FROM products WHERE merchant_id IS NOT NULL AND merchant_id > 0;
