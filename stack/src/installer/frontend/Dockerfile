FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package.json ./package.json
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY frontend/nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

