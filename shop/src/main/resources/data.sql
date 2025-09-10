-- 创建addresses表（如果不存在）
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    detail VARCHAR(200) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);
CREATE INDEX IF NOT EXISTS idx_addresses_is_default ON addresses(is_default);

INSERT INTO users (id, username, password_hash, nickname, role, status, created_at) VALUES
 (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 'ADMIN', 'ENABLED', CURRENT_TIMESTAMP()),
 (2, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试用户', 'USER', 'ENABLED', CURRENT_TIMESTAMP()),
 (3, 'merchant', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试商家', 'MERCHANT', 'ENABLED', CURRENT_TIMESTAMP());

INSERT INTO merchants (id, user_id, shop_name, shop_description, contact_phone, business_license, status, created_at) VALUES
 (1, 3, '零食小铺', '专业零食销售', '13800138000', NULL, 'APPROVED', CURRENT_TIMESTAMP());

INSERT INTO categories (id, name, parent_id, sort_order, status, created_at) VALUES
 (1, '零食', NULL, 1, 'ENABLED', CURRENT_TIMESTAMP()),
 (2, '饮料', NULL, 2, 'ENABLED', CURRENT_TIMESTAMP()),
 (3, '坚果', 1, 1, 'ENABLED', CURRENT_TIMESTAMP());

INSERT INTO products (id, name, subtitle, category_id, cover_image, price, unit, stock, status, description, created_at) VALUES
 (1, '开心果', '香脆可口', 3, '/images/happy-nut.jpg', 25.80, '袋', 100, 'ON_SALE', '精选开心果，营养丰富', CURRENT_TIMESTAMP());

INSERT INTO banners (id, title, image_url, link_url, sort_order, status, created_at) VALUES
 (1, '新品上市', '/images/banner1.jpg', '/products', 1, 'ENABLED', CURRENT_TIMESTAMP());

