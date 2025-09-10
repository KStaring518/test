-- =============================================
-- 网上零食销售系统数据库创建脚本
-- 创建时间：2025-08-18
-- 数据库名：shop_db
-- =============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS shop_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE shop_db;

-- 2. 创建用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(50) COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar_url VARCHAR(200) COMMENT '头像URL',
    role TINYINT NOT NULL DEFAULT 1 COMMENT '角色(1:用户,2:商家,3:管理员)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 创建商家表
CREATE TABLE merchants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商家ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    shop_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
    shop_description TEXT COMMENT '店铺描述',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    business_license VARCHAR(100) COMMENT '营业执照',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:正常,0:暂停)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_shop_name (shop_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- 4. 创建地址表
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail VARCHAR(200) NOT NULL COMMENT '详细地址',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否默认地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

-- 5. 创建商品分类表
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT COMMENT '父分类ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 6. 创建商品表
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    subtitle VARCHAR(200) COMMENT '商品副标题',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    cover_image VARCHAR(200) COMMENT '主图URL',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    unit VARCHAR(20) COMMENT '单位',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:上架,0:下架)',
    description TEXT COMMENT '商品描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_category_id (category_id),
    INDEX idx_name (name),
    INDEX idx_price (price),
    INDEX idx_stock (stock),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 7. 创建购物车表
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    checked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否选中',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_checked (checked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 8. 创建订单表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '订单状态(UNPAID/PAID/SHIPPED/FINISHED/CLOSED)',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_type VARCHAR(20) DEFAULT 'MOCK' COMMENT '支付方式',
    pay_time TIMESTAMP NULL COMMENT '支付时间',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名(快照)',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话(快照)',
    receiver_address TEXT NOT NULL COMMENT '收货地址(快照)',
    remark VARCHAR(200) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 9. 创建订单明细表
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name_snapshot VARCHAR(100) NOT NULL COMMENT '商品名称(快照)',
    price_snapshot DECIMAL(10,2) NOT NULL COMMENT '价格(快照)',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 10. 创建评价表
CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    order_item_id BIGINT NOT NULL COMMENT '订单项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    rating INT NOT NULL COMMENT '评分(1-5)',
    content TEXT COMMENT '评价内容',
    images JSON COMMENT '图片URL数组',
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否匿名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_order_item_id (order_item_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 11. 创建轮播图表
CREATE TABLE banners (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
    title VARCHAR(100) COMMENT '标题',
    image_url VARCHAR(200) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(200) COMMENT '链接URL',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- 12. 创建发货单表
CREATE TABLE shipments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发货单ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    shipment_no VARCHAR(64) UNIQUE NOT NULL COMMENT '发货单号',
    carrier VARCHAR(50) NOT NULL COMMENT '物流公司',
    tracking_no VARCHAR(100) COMMENT '物流单号',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '发货状态(PENDING/SHIPPED/IN_TRANSIT/DELIVERED/RECEIVED)',
    shipped_at TIMESTAMP NULL COMMENT '发货时间',
    estimated_delivery TIMESTAMP NULL COMMENT '预计送达时间',
    actual_delivery TIMESTAMP NULL COMMENT '实际送达时间',
    shipping_address TEXT NOT NULL COMMENT '收货地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_shipment_no (shipment_no),
    INDEX idx_tracking_no (tracking_no),
    INDEX idx_status (status),
    INDEX idx_shipped_at (shipped_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发货单表';

-- 13. 创建物流轨迹表
CREATE TABLE logistics_tracks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轨迹ID',
    shipment_id BIGINT NOT NULL COMMENT '发货单ID',
    location VARCHAR(100) NOT NULL COMMENT '当前位置',
    description VARCHAR(200) NOT NULL COMMENT '物流描述',
    track_time TIMESTAMP NOT NULL COMMENT '轨迹时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    INDEX idx_shipment_id (shipment_id),
    INDEX idx_track_time (track_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流轨迹表';

-- =============================================
-- 插入初始数据
-- =============================================

-- 插入管理员账号
INSERT INTO users (username, password_hash, nickname, role, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 3, 1);

-- 插入测试用户
INSERT INTO users (username, password_hash, nickname, role, status) VALUES 
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试用户', 1, 1);

-- 插入测试商家
INSERT INTO users (username, password_hash, nickname, role, status) VALUES 
('merchant', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试商家', 2, 1);

INSERT INTO merchants (user_id, shop_name, shop_description, contact_phone, status) VALUES 
(3, '零食小铺', '专业零食销售', '13800138000', 1);

-- 插入商品分类
INSERT INTO categories (name, parent_id, sort_order, status) VALUES 
('零食', NULL, 1, 1),
('饮料', NULL, 2, 1),
('坚果', 1, 1, 1),
('糖果', 1, 2, 1),
('碳酸饮料', 2, 1, 1);

-- 插入测试商品
INSERT INTO products (name, subtitle, category_id, cover_image, price, unit, stock, status, description) VALUES 
('开心果', '香脆可口', 3, '/images/happy-nut.jpg', 25.80, '袋', 100, 1, '精选开心果，营养丰富'),
('巧克力', '丝滑浓郁', 4, '/images/chocolate.jpg', 15.50, '盒', 200, 1, '比利时进口巧克力'),
('可乐', '经典口味', 5, '/images/cola.jpg', 3.50, '瓶', 500, 1, '百事可乐，清爽解渴');

-- 插入轮播图
INSERT INTO banners (title, image_url, link_url, sort_order, status) VALUES 
('新品上市', '/images/banner1.jpg', '/products', 1, 1),
('限时特价', '/images/banner2.jpg', '/promotion', 2, 1),
('品牌推荐', '/images/banner3.jpg', '/brands', 3, 1);

-- =============================================
-- 创建视图
-- =============================================

-- 商品详情视图
CREATE VIEW v_product_detail AS
SELECT 
    p.id,
    p.name,
    p.subtitle,
    p.cover_image,
    p.price,
    p.unit,
    p.stock,
    p.status,
    p.description,
    c.name as category_name,
    c.parent_id,
    pc.name as parent_category_name
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN categories pc ON c.parent_id = pc.id
WHERE p.status = 1;

-- 订单详情视图
CREATE VIEW v_order_detail AS
SELECT 
    o.id,
    o.order_no,
    o.status,
    o.total_amount,
    o.receiver_name,
    o.receiver_phone,
    o.receiver_address,
    o.created_at,
    u.username,
    u.nickname,
    COUNT(oi.id) as item_count
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id;

-- =============================================
-- 脚本执行完成
-- =============================================

SELECT '数据库创建完成！' as message;
SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'shop_db';