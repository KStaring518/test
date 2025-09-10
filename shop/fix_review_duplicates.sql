-- 检查Review表中的重复数据
-- 1. 查看Review表结构
DESCRIBE reviews;

-- 2. 检查是否有重复的ID
SELECT id, COUNT(*) as count
FROM reviews 
GROUP BY id 
HAVING COUNT(*) > 1;

-- 3. 查看重复ID的详细数据
SELECT * FROM reviews WHERE id = 6;

-- 4. 查看Review表的所有数据
SELECT * FROM reviews ORDER BY id;

-- 5. 检查Review表与OrderItem表的关系
SELECT r.id, r.order_item_id, oi.id as order_item_id_from_oi
FROM reviews r
LEFT JOIN order_items oi ON r.order_item_id = oi.id
ORDER BY r.id;

-- 6. 清理重复数据的方案
-- 方案1：保留最新的评价，删除重复的
-- 首先备份表
CREATE TABLE reviews_backup AS SELECT * FROM reviews;

-- 删除重复数据，保留ID最大的记录
DELETE r1 FROM reviews r1
INNER JOIN reviews r2 
WHERE r1.id < r2.id 
AND r1.order_item_id = r2.order_item_id;

-- 方案2：如果ID是自增的，重置自增值
-- ALTER TABLE reviews AUTO_INCREMENT = (SELECT MAX(id) + 1 FROM reviews);

-- 7. 验证清理结果
SELECT * FROM reviews ORDER BY id;

-- 8. 检查是否还有重复
SELECT id, COUNT(*) as count
FROM reviews 
GROUP BY id 
HAVING COUNT(*) > 1;
