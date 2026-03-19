ARG MIRROR=
FROM ${MIRROR}node:20-alpine AS build
WORKDIR /app
# 生产环境使用相对路径，由 server.js 代理转发到后端
ENV VITE_API_URL=
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

# 自定义 Node 服务：静态资源 + /api、/v1 代理到后端
FROM ${MIRROR}node:20-alpine
WORKDIR /app
COPY --from=build /app/dist ./dist
COPY server.mjs ./
EXPOSE 80
ENV BACKEND_URL=http://backend:20118
CMD ["node", "server.mjs"]
