-- 智能客服相关表结构

-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT,
    user_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    message_count INT NOT NULL DEFAULT 0,
    last_message_at TIMESTAMP NULL,
    assigned_agent_id BIGINT NULL,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    user_id BIGINT,
    user_type VARCHAR(20) NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confidence DOUBLE NULL,
    is_handled_by_human BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE
);

-- 知识库表（用于存储FAQ和商品信息）
CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    keywords TEXT,
    priority INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_keywords (keywords(100)),
    FULLTEXT idx_content (question, answer)
);

-- 插入一些测试数据
INSERT INTO chat_sessions (session_id, user_id, user_type, status) VALUES
('test-session-1', 1, 'USER', 'ACTIVE'),
('test-session-2', 2, 'MERCHANT', 'ACTIVE');

INSERT INTO chat_messages (session_id, user_id, user_type, message_type, content) VALUES
('test-session-1', 1, 'USER', 'USER_QUESTION', '如何查询订单状态？'),
('test-session-1', NULL, 'BOT', 'BOT_ANSWER', '您可以通过"我的订单"页面查看订单状态。如果有具体订单号，我可以帮您查询详细信息。'),
('test-session-2', 2, 'MERCHANT', 'USER_QUESTION', '退换货政策是什么？'),
('test-session-2', NULL, 'BOT', 'BOT_ANSWER', '我们支持7天无理由退换货。您可以在订单详情页申请退款，客服会在24小时内处理。');

-- 插入知识库数据
INSERT INTO knowledge_base (category, question, answer, keywords, priority) VALUES
('订单', '如何查询订单状态？', '您可以通过"我的订单"页面查看所有订单状态。在页面右上角点击"我的订单"，可以看到待付款、待发货、已发货、已完成等状态。', '订单,查询,状态,我的订单', 10),
('订单', '订单多久能发货？', '我们承诺24小时内发货。如果您在上午下单，通常当天下午就能发货；下午或晚上下单，第二天上午发货。', '发货,时间,订单,24小时', 9),
('订单', '如何取消订单？', '在"我的订单"页面，找到要取消的订单，点击"取消订单"按钮。注意：已发货的订单无法取消。', '取消,订单,我的订单', 8),
('退换货', '退换货政策是什么？', '我们支持7天无理由退换货。商品必须保持原包装、标签完整，不影响二次销售。食品类商品开封后不支持退换。', '退换货,政策,退款,7天', 10),
('退换货', '如何申请退款？', '在"我的订单"页面，找到要退款的订单，点击"申请退款"，选择退款原因并上传照片，客服会在24小时内处理。', '退款,申请,流程,我的订单', 9),
('退换货', '退款多久到账？', '退款会在1-3个工作日内原路返回。支付宝支付会退到支付宝账户，银行卡支付会退到银行卡。', '退款,到账,时间,支付宝', 8),
('配送', '配送时间需要多久？', '我们使用顺丰、圆通等知名快递公司。同城1天内送达，省内1-2天，省外2-3天，偏远地区3-5天。', '配送,快递,时间,顺丰,圆通', 8),
('配送', '运费怎么计算？', '满99元包邮，不满99元收取8元运费。新疆、西藏、青海等偏远地区运费15元。', '运费,包邮,99元,偏远地区', 9),
('配送', '可以指定快递公司吗？', '可以。在结算页面选择配送方式时，您可以选择顺丰、圆通、中通等快递公司。', '快递,选择,配送方式', 7),
('支付', '支持哪些支付方式？', '我们主要支持支付宝支付。在支付页面选择支付宝，扫码或输入密码即可完成支付。', '支付,支付宝,扫码', 10),
('支付', '支付宝支付失败怎么办？', '请检查支付宝余额是否充足，网络是否正常。如果问题持续，可以联系客服或重新尝试支付。', '支付宝,支付失败,余额,网络', 8),
('支付', '支付成功后订单没确认？', '支付成功后订单会自动确认，如果显示异常，请刷新页面或联系客服处理。', '支付成功,订单确认,刷新', 7),
('商品', '商品保质期是多久？', '零食类商品保质期一般在6-12个月，饮料类3-6个月。具体保质期请查看商品详情页或包装上的标识。', '保质期,商品,时间,零食,饮料', 7),
('商品', '商品是正品吗？', '我们所有商品都是正品，有正规的进货渠道和授权证明。如有质量问题，我们承诺假一赔十。', '正品,质量,假一赔十,授权', 9),
('商品', '可以查看商品成分吗？', '可以。在商品详情页有详细的成分表、营养信息和过敏原提示，请仔细查看后再购买。', '成分,营养,过敏原,详情页', 8),
('商品', '有进口零食吗？', '有。我们提供来自日本、韩国、泰国等国家的进口零食，包括饼干、糖果、薯片等。', '进口,日本,韩国,泰国,零食', 7),
('客服', '如何联系人工客服？', '您可以通过智能客服转人工，或拨打客服热线400-123-4567。客服工作时间为9:00-18:00，节假日不休。', '客服,人工,联系,400,热线', 8),
('客服', '客服工作时间？', '我们的客服工作时间为每天9:00-18:00，节假日不休。非工作时间可以留言，我们会尽快回复。', '客服,工作时间,9点,18点', 7),
('活动', '有优惠活动吗？', '我们经常有优惠活动，包括满减、折扣、赠品等。关注首页轮播图和"优惠活动"页面，及时了解最新优惠。', '优惠,活动,满减,折扣,赠品', 8),
('活动', '新用户有优惠吗？', '新用户注册后可以获得10元优惠券，满50元可用。还有新人专享商品和特价活动。', '新用户,优惠券,10元,新人专享', 9);
