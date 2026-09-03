-- 说说开放游客发布：记录游客身份与昵称
ALTER TABLE blog_post ADD COLUMN guest_key VARCHAR(64) NULL COMMENT '游客发布标识(localStorage uuid)';
ALTER TABLE blog_post ADD COLUMN guest_name VARCHAR(50) NULL COMMENT '游客昵称';
CREATE INDEX idx_guest_key ON blog_post (guest_key);

-- 封面历史：保证随机封面全局不重复
CREATE TABLE blog_cover_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(32) NOT NULL UNIQUE,
    used_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
