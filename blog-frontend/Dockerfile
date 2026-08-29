# ---- build ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --registry=https://registry.npmmirror.com
COPY . .
ENV NODE_ENV=production
RUN npm run build

# ---- run ----
FROM node:22-alpine
WORKDIR /app
ENV TZ=Asia/Shanghai NODE_ENV=production
COPY --from=build /app/.output ./.output
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
