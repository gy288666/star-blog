# 数据库 Schema（MySQL 8，utf8mb4，库名 blog）

后端用 Flyway 管理（V1__schema.sql 引用本文件）。以下 DDL 是唯一权威版本。

```sql
CREATE TABLE blog_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt',
    email VARCHAR(100),
    avatar VARCHAR(500),
    role ENUM('admin','editor') DEFAULT 'admin',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type TINYINT NOT NULL COMMENT '0=article,1=page,2=shuoshuo',
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE,
    content_md LONGTEXT,
    content_html LONGTEXT,
    summary VARCHAR(500),
    cover VARCHAR(500),
    status TINYINT DEFAULT 0 COMMENT '0=draft,1=publish,2=password',
    password VARCHAR(255),
    author_id BIGINT,
    views INT DEFAULT 0,
    upvotes INT DEFAULT 0,
    is_top TINYINT DEFAULT 0,
    allow_comment TINYINT DEFAULT 1,
    comment_count INT DEFAULT 0,
    published_at DATETIME NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FULLTEXT INDEX ft_title_content (title, content_md) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(100),
    description VARCHAR(500),
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_post_category (
    post_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_post_tag (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    root_id BIGINT DEFAULT 0,
    author VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    website VARCHAR(500),
    avatar VARCHAR(500),
    content_md TEXT NOT NULL,
    content_html TEXT,
    ip VARCHAR(45),
    user_agent VARCHAR(500),
    status TINYINT DEFAULT 1 COMMENT '0=pending,1=approved,2=spam',
    is_admin TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_root_id (root_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_shuoshuo_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    union_key VARCHAR(64) NOT NULL COMMENT 'SHA256(ip+ua)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_union (post_id, union_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_friend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    avatar VARCHAR(500),
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200),
    subtitle VARCHAR(500),
    image_url VARCHAR(500),
    bg_color VARCHAR(50),
    typing_effect TINYINT DEFAULT 0,
    is_active TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_setting (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_visit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip VARCHAR(45),
    url VARCHAR(500),
    referer VARCHAR(500),
    user_agent VARCHAR(500),
    visit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_visit_time (visit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

注意：`FULLTEXT ... WITH PARSER ngram` 为 MySQL 专属；除它之外其余 DDL 兼容标准 SQL。
