# 公共反代逻辑（nginx.conf 中 include 引用）

gzip on;
gzip_types text/plain text/css application/json application/javascript application/xml image/svg+xml;

client_max_body_size 60m;

# API → Spring Boot
location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout 30s;
}

# 上传文件 → Spring Boot 静态映射（uploads_data 卷）
location /uploads/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    expires 30d;
    add_header Cache-Control "public, immutable";
}

# 站点地图与robots 直接透传后端
location = /sitemap.xml {
    proxy_pass http://backend:8080;
}

# 其余 → Nuxt SSR
location / {
    proxy_pass http://web:3000;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}

# 静态资源缓存
location /_nuxt/ {
    proxy_pass http://web:3000;
    expires 30d;
    add_header Cache-Control "public, immutable";
}
