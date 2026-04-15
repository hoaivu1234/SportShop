# ===== BUILD STAGE =====
FROM node:18-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ===== RUNTIME STAGE =====
FROM nginx:alpine

# remove default nginx config
RUN rm -rf /usr/share/nginx/html/*

# copy build
COPY --from=build /app/dist/sport-ecommerce-fe/browser /usr/share/nginx/html

# custom nginx config (SPA)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]