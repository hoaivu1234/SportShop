# 🚀 SportyShop - Nền Tảng Thương Mại Điện Tử Thể Thao Thời Gian Thực

[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%204.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Frontend-Angular%2017%2B-red)](https://angular.io/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Cache-Redis-critical)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/Messaging-RabbitMQ-orange)](https://www.rabbitmq.com/)
[![Elasticsearch](https://img.shields.io/badge/Search-Elasticsearch-yellow)](https://www.elastic.co/)
[![Docker](https://img.shields.io/badge/DevOps-Docker-blue)](https://www.docker.com/)

**SportShop** là nền tảng thương mại điện tử chuyên về đồ thể thao, được xây dựng với kiến trúc hiện đại, tập trung vào **hiệu năng cao**, **khả năng mở rộng** và **trải nghiệm người dùng thời gian thực**.

Dự án áp dụng các công nghệ tiên tiến: **Caching**, **Message Queue**, **Full-text Search**, và **Event-Driven Architecture**.

---

## 🌐 Thông Tin Dự Án

- **Website**: [https://hvsport.xyz](https://hvsport.xyz)
- **Backend API**: [https://api.hvsport.xyz](https://api.hvsport.xyz)
- **Tài khoản Admin**:
- Email: admin@sport.com
- Mật khẩu: daica123

## ✨ Tính Năng Nổi Bật

- **🔐 Xác thực & Bảo mật mạnh mẽ**
  - JWT với Access Token + Refresh Token
  - Đăng nhập Google (OAuth2)
  - Phân quyền RBAC (Customer / Admin) bằng Spring Security


- **🔍 Tìm kiếm thông minh**
  - Elasticsearch hỗ trợ fuzzy search, autocomplete, tìm kiếm không phân biệt hoa thường


- **🛒 Giỏ hàng nhanh & mượt**
  - Sử dụng Redis làm cache giỏ hàng


- **📦 Đặt hàng bất đồng bộ**
  - Event-Driven với RabbitMQ
  - Gửi email xác nhận đơn hàng mà không làm chậm quá trình thanh toán


- **📊 Database tối ưu**
  - Thiết kế database chuẩn hóa với Indexing
  - Xử lý triệt để vấn đề N+1 query
  - Tối ưu truy vấn bằng Explain Analyze

---

## 🏗 Kiến Trúc Hệ Thống



```
                    ┌──────────────────────┐
                    │      Frontend        │
                    │       Angular        │
                    └──────────┬───────────┘
                               │
                             HTTPS
                               │
                    ┌──────────▼───────────┐
                    │      API Gateway     │
                    │     Spring Boot      │
                    └──────────┬───────────┘
                               │
     ┌─────────────────────────┼──────────────────────────┐
     │                         │                          │
┌────▼─────┐           ┌──────▼──────┐           ┌───────▼───────┐
│ Auth     │           │ Product     │           │ Order Service │
│ Service  │           │ Service     │           │               │
└────┬─────┘           └──────┬──────┘           └───────┬───────┘
     │                         │                          │
     │                         │                          │
     │                  ┌──────▼──────┐                   │
     │                  │ Redis (Cart)│                   │
     │                  └─────────────┘                   │
     │                                                    │
     │                                             ┌──────▼──────┐
     │                                             │ RabbitMQ    │
     │                                             └──────┬──────┘
     │                                                    │
     │                                      ┌─────────────▼────────────┐
     │                                      │ Notification / Email Svc │
     │                                      └───────────────────────────┘
     │
┌────▼───────────────┐
│   Mysql Database   │
└────────────────────┘

```


## 🌐 Triển Khai (Deployment)

- Dockerize: Toàn bộ ứng dụng được đóng gói bằng Docker
- Coolify: Tự động deploy lên VPS/Server
- Cloudflare Tunnel: Công khai ứng dụng từ local ra Internet một cách an toàn mà không cần mở port
