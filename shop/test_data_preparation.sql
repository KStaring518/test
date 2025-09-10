-- 零食商城系统测试数据准备脚本
-- 执行此脚本前请确保数据库已创建并运行

-- 清理现有测试数据（可选）
-- DELETE FROM reviews WHERE id > 0;
-- DELETE FROM order_items WHERE id > 0;
-- DELETE FROM orders WHERE id > 0;
-- DELETE FROM cart_items WHERE id > 0;
-- DELETE FROM addresses WHERE id > 0;
-- DELETE FROM products WHERE id > 0;
-- DELETE FROM categories WHERE id > 0;
-- DELETE FROM banners WHERE id > 0;
-- DELETE FROM merchants WHERE id > 0;
-- DELETE FROM users WHERE id > 0;

-- 1. 用户数据
INSERT INTO users (id, username, password_hash, nickname, email, role, status, created_at) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 'admin@shop.com', 'ADMIN', 'ENABLED', CURRENT_TIMESTAMP()),
(2, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试用户', 'testuser@shop.com', 'USER', 'ENABLED', CURRENT_TIMESTAMP()),
(3, 'merchant', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试商家', 'merchant@shop.com', 'MERCHANT', 'ENABLED', CURRENT_TIMESTAMP()),
(4, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户2', 'user2@shop.com', 'USER', 'ENABLED', CURRENT_TIMESTAMP()),
(5, 'merchant2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '商家2', 'merchant2@shop.com', 'MERCHANT', 'ENABLED', CURRENT_TIMESTAMP());

-- 2. 商家数据
INSERT INTO merchants (id, user_id, shop_name, shop_description, contact_phone, business_license, status, created_at) VALUES
(1, 3, '零食小铺', '专业零食销售，品质保证', '13800138000', 'BL123456789', 'APPROVED', CURRENT_TIMESTAMP()),
(2, 5, '美味工坊', '精选美味零食，健康生活', '13800138001', 'BL123456790', 'APPROVED', CURRENT_TIMESTAMP());

-- 3. 商品分类数据
INSERT INTO categories (id, name, parent_id, sort_order, status, created_at) VALUES
(1, '零食', NULL, 1, 'ENABLED', CURRENT_TIMESTAMP()),
(2, '饮料', NULL, 2, 'ENABLED', CURRENT_TIMESTAMP()),
(3, '坚果', 1, 1, 'ENABLED', CURRENT_TIMESTAMP()),
(4, '巧克力', 1, 2, 'ENABLED', CURRENT_TIMESTAMP()),
(5, '糖果', 1, 3, 'ENABLED', CURRENT_TIMESTAMP()),
(6, '饼干', 1, 4, 'ENABLED', CURRENT_TIMESTAMP()),
(7, '果汁', 2, 1, 'ENABLED', CURRENT_TIMESTAMP()),
(8, '茶饮', 2, 2, 'ENABLED', CURRENT_TIMESTAMP());

-- 4. 商品数据
INSERT INTO products (id, name, subtitle, category_id, merchant_id, cover_image, price, unit, stock, status, description, created_at) VALUES
(1, '开心果', '香脆可口，营养丰富', 3, 1, '/images/products/happy-nut.jpg', 25.80, '袋', 100, 'ON_SALE', '精选开心果，颗粒饱满，口感香脆', CURRENT_TIMESTAMP()),
(2, '杏仁', '原味杏仁，健康之选', 3, 1, '/images/products/almond.jpg', 32.50, '袋', 80, 'ON_SALE', '优质杏仁，富含维生素E', CURRENT_TIMESTAMP()),
(3, '德芙巧克力', '丝滑口感，经典之选', 4, 1, '/images/products/dove-chocolate.jpg', 15.90, '盒', 150, 'ON_SALE', '德芙经典巧克力，丝滑口感', CURRENT_TIMESTAMP()),
(4, '费列罗巧克力', '意大利进口，奢华享受', 4, 1, '/images/products/ferrero.jpg', 45.00, '盒', 60, 'ON_SALE', '意大利费列罗巧克力，奢华享受', CURRENT_TIMESTAMP()),
(5, '大白兔奶糖', '经典奶糖，童年回忆', 5, 1, '/images/products/white-rabbit.jpg', 12.80, '袋', 200, 'ON_SALE', '经典大白兔奶糖，香甜可口', CURRENT_TIMESTAMP()),
(6, '旺仔小馒头', '可爱造型，营养美味', 6, 1, '/images/products/wangzai.jpg', 8.50, '袋', 300, 'ON_SALE', '旺仔小馒头，可爱造型，营养美味', CURRENT_TIMESTAMP()),
(7, '奥利奥饼干', '扭一扭，舔一舔，泡一泡', 6, 1, '/images/products/oreo.jpg', 18.90, '盒', 120, 'ON_SALE', '经典奥利奥饼干，多种吃法', CURRENT_TIMESTAMP()),
(8, '可口可乐', '经典可乐，畅爽体验', 7, 2, '/images/products/coca-cola.jpg', 3.50, '瓶', 500, 'ON_SALE', '经典可口可乐，畅爽体验', CURRENT_TIMESTAMP()),
(9, '农夫山泉', '天然矿泉水，健康生活', 7, 2, '/images/products/nongfu.jpg', 2.00, '瓶', 800, 'ON_SALE', '农夫山泉天然矿泉水', CURRENT_TIMESTAMP()),
(10, '立顿红茶', '英式红茶，醇香浓郁', 8, 2, '/images/products/lipton.jpg', 25.00, '盒', 90, 'ON_SALE', '立顿英式红茶，醇香浓郁', CURRENT_TIMESTAMP());

-- 5. 轮播图数据
INSERT INTO banners (id, title, image_url, link_url, sort_order, status, created_at) VALUES
(1, '春季新品上市', '/images/banners/spring-new.jpg', '/products?categoryId=4', 1, 'ENABLED', CURRENT_TIMESTAMP()),
(2, '坚果大促销', '/images/banners/nuts-sale.jpg', '/products?categoryId=3', 2, 'ENABLED', CURRENT_TIMESTAMP()),
(3, '饮料专区', '/images/banners/drinks.jpg', '/products?categoryId=2', 3, 'ENABLED', CURRENT_TIMESTAMP()),
(4, '巧克力礼盒', '/images/banners/chocolate.jpg', '/products?categoryId=4', 4, 'ENABLED', CURRENT_TIMESTAMP());

-- 6. 收货地址数据
INSERT INTO addresses (id, user_id, receiver_name, phone, province, city, district, detail, is_default, created_at) VALUES
(1, 2, '张三', '13800138000', '广东省', '深圳市', '南山区', '科技园南区深南大道10000号', TRUE, CURRENT_TIMESTAMP()),
(2, 2, '李四', '13800138001', '北京市', '北京市', '朝阳区', '三里屯街道工体北路8号', FALSE, CURRENT_TIMESTAMP()),
(3, 4, '王五', '13800138002', '上海市', '上海市', '浦东新区', '陆家嘴金融贸易区世纪大道88号', TRUE, CURRENT_TIMESTAMP());

-- 7. 购物车数据
INSERT INTO cart_items (id, user_id, product_id, quantity, checked, created_at) VALUES
(1, 2, 1, 2, TRUE, CURRENT_TIMESTAMP()),
(2, 2, 3, 1, TRUE, CURRENT_TIMESTAMP()),
(3, 2, 5, 3, FALSE, CURRENT_TIMESTAMP()),
(4, 4, 2, 1, TRUE, CURRENT_TIMESTAMP()),
(5, 4, 4, 2, TRUE, CURRENT_TIMESTAMP());

-- 8. 订单数据
INSERT INTO orders (id, user_id, order_number, status, total_amount, address_id, remark, created_at) VALUES
(1, 2, 'O202401010000000001', 'PAID', 67.50, 1, '请尽快发货', CURRENT_TIMESTAMP()),
(2, 2, 'O202401010000000002', 'SHIPPED', 45.00, 1, '包装精美', CURRENT_TIMESTAMP()),
(3, 4, 'O202401010000000003', 'FINISHED', 90.00, 3, '质量很好', CURRENT_TIMESTAMP());

-- 9. 订单项数据
INSERT INTO order_items (id, order_id, product_id, product_name, product_price, quantity, total_price, created_at) VALUES
(1, 1, 1, '开心果', 25.80, 2, 51.60, CURRENT_TIMESTAMP()),
(2, 1, 3, '德芙巧克力', 15.90, 1, 15.90, CURRENT_TIMESTAMP()),
(3, 2, 4, '费列罗巧克力', 45.00, 1, 45.00, CURRENT_TIMESTAMP()),
(4, 3, 2, '杏仁', 32.50, 2, 65.00, CURRENT_TIMESTAMP()),
(5, 3, 4, '费列罗巧克力', 45.00, 1, 45.00, CURRENT_TIMESTAMP());

-- 10. 评价数据
INSERT INTO reviews (id, user_id, order_item_id, product_id, rating, content, images, created_at) VALUES
(1, 2, 1, 1, 5, '开心果很新鲜，颗粒饱满，口感很好！', '["/images/reviews/happy-nut-1.jpg"]', CURRENT_TIMESTAMP()),
(2, 2, 2, 3, 4, '德芙巧克力一如既往的好吃，丝滑口感', '[]', CURRENT_TIMESTAMP()),
(3, 4, 4, 2, 5, '杏仁质量很好，包装精美，会回购', '["/images/reviews/almond-1.jpg", "/images/reviews/almond-2.jpg"]', CURRENT_TIMESTAMP()),
(4, 4, 5, 4, 5, '费列罗巧克力太棒了，意大利进口品质', '[]', CURRENT_TIMESTAMP());

-- 11. 物流数据
INSERT INTO shipments (id, order_id, tracking_number, carrier, status, created_at) VALUES
(1, 2, 'SF1234567890', '顺丰速运', 'SHIPPED', CURRENT_TIMESTAMP()),
(2, 3, 'YT1234567890', '圆通速递', 'DELIVERED', CURRENT_TIMESTAMP());

-- 12. 物流跟踪数据
INSERT INTO logistics_tracks (id, shipment_id, status, description, location, created_at) VALUES
(1, 1, 'PICKED_UP', '快件已揽收', '深圳市南山区', CURRENT_TIMESTAMP()),
(2, 1, 'IN_TRANSIT', '快件运输中', '深圳市转运中心', CURRENT_TIMESTAMP()),
(3, 2, 'PICKED_UP', '快件已揽收', '上海市浦东新区', CURRENT_TIMESTAMP()),
(4, 2, 'IN_TRANSIT', '快件运输中', '上海市转运中心', CURRENT_TIMESTAMP()),
(5, 2, 'DELIVERED', '快件已签收', '北京市朝阳区', CURRENT_TIMESTAMP());

-- 13. 聊天记录数据
INSERT INTO chat_messages (id, user_id, message, is_user, created_at) VALUES
(1, 2, '你好，我想了解一下你们的商品', TRUE, CURRENT_TIMESTAMP()),
(2, 2, '您好！欢迎来到零食商城，我是智能客服小助手。我们有很多优质的商品，包括坚果、巧克力、糖果等。请问您对哪类商品感兴趣呢？', FALSE, CURRENT_TIMESTAMP()),
(3, 2, '我想买一些坚果', TRUE, CURRENT_TIMESTAMP()),
(4, 2, '好的！我们有很多优质的坚果产品，包括开心果、杏仁、腰果等。开心果现在有特价活动，25.8元一袋，颗粒饱满，口感香脆。您想了解哪款坚果呢？', FALSE, CURRENT_TIMESTAMP());

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_merchant_id ON products(merchant_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_user_id ON chat_messages(user_id);

-- 显示数据统计
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Merchants', COUNT(*) FROM merchants
UNION ALL
SELECT 'Categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Banners', COUNT(*) FROM banners
UNION ALL
SELECT 'Addresses', COUNT(*) FROM addresses
UNION ALL
SELECT 'Cart Items', COUNT(*) FROM cart_items
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items
UNION ALL
SELECT 'Reviews', COUNT(*) FROM reviews
UNION ALL
SELECT 'Shipments', COUNT(*) FROM shipments
UNION ALL
SELECT 'Logistics Tracks', COUNT(*) FROM logistics_tracks
UNION ALL
SELECT 'Chat Messages', COUNT(*) FROM chat_messages;
