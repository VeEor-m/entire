# Spring Boot 全栈项目规范文档

## 1. 项目概述

- **项目名称**: springboot-auth-starter
- **项目类型**: Spring Boot REST API 项目
- **核心功能**: 提供完整的用户认证、授权、持久化和缓存解决方案
- **目标用户**: 开发者快速搭建带有完整认证体系的后端服务

## 2. 技术栈

| 层级 | 技术选型 |
|------|----------|
| 框架 | Spring Boot 3.2.x |
| 安全 | Spring Security 6 + JWT |
| 持久化 | Spring Data JPA + MySQL 8.x |
| 缓存 | Spring Cache + Redis |
| 构建工具 | Maven |
| Java版本 | Java 17 |

## 3. 功能模块

### 3.1 认证模块 (Authentication)
- 用户注册接口 (`POST /api/auth/register`)
- 用户登录接口 (`POST /api/auth/login`)
- JWT Token 刷新机制
- 密码加密存储 (BCrypt)

### 3.2 鉴权模块 (Authorization)
- 基于角色的权限控制 (RBAC)
- 角色: ADMIN, USER
- 接口级别的权限控制
- Spring Security Filter Chain 配置

### 3.3 持久化模块 (Persistence)
- User 实体 (id, username, email, password, role, createdAt, updatedAt)
- 使用 Spring Data JPA
- MySQL 数据库
- 自动建表策略

### 3.4 缓存模块 (Caching)
- Redis 集成
- 用户信息缓存
- Token 缓存
- 可配置缓存过期时间

## 4. API 接口设计

### 4.1 认证接口
```
POST /api/auth/register
Request: { "username": "string", "email": "string", "password": "string" }
Response: { "message": "User registered successfully" }

POST /api/auth/login
Request: { "username": "string", "password": "string" }
Response: { "token": "jwt_token", "type": "Bearer" }
```

### 4.2 用户接口
```
GET /api/users/me - 获取当前用户信息 (需要认证)
GET /api/users/{id} - 获取指定用户 (需要 ADMIN 权限)
```

## 5. 项目结构

```
springboot-auth-starter/
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   └── CacheConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── UserController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   └── JwtService.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── entity/
│   │   └── User.java
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   └── UserResponse.java
│   └── security/
│       ├── JwtAuthenticationFilter.java
│       └── CustomUserDetails.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── README.md
```

## 6. 配置项 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_demo?createDatabaseIfNotExist=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your-256-bit-secret-key-here-must-be-long-enough
  expiration: 86400000
```

## 7. 验收标准

1. ✅ 项目可以成功编译打包
2. ✅ 用户可以注册新账号
3. ✅ 用户可以登录获取 JWT Token
4. ✅ 携带有效 Token 可以访问受保护资源
5. ✅ 未授权用户无法访问受保护资源
6. ✅ ADMIN 角色可以访问管理接口
7. ✅ 用户信息会被缓存到 Redis
8. ✅ Token 过期后需要重新登录
