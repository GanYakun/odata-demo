# 认证服务使用指南

## 🎯 概述

认证服务是基于OAuth2.0和JWT的用户认证和授权微服务，提供完整的用户管理、角色管理、权限管理功能。

## 🏗️ 架构设计

### 技术栈
- **Spring Boot 2.7.18** - 基础框架
- **Spring Security** - 安全框架
- **OAuth2.0** - 认证协议
- **JWT** - 令牌机制
- **BCrypt** - 密码加密
- **MyBatis Plus** - 数据访问层
- **MySQL/H2** - 数据库

### 核心组件
- **认证服务** (`AuthService`) - 处理登录、令牌刷新
- **用户服务** (`UserService`) - 用户CRUD操作
- **用户详情服务** (`UserDetailsServiceImpl`) - Spring Security集成
- **JWT工具** (`JwtUtil`) - JWT令牌生成和验证
- **密码工具** (`PasswordUtil`) - 密码加密和验证

## 📊 数据模型

### 核心实体

#### 用户表 (users)
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login_time DATETIME,
    password_change_time DATETIME,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    deleted BOOLEAN DEFAULT FALSE
);
```

#### 角色表 (roles)
```sql
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_system BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME,
    deleted BOOLEAN DEFAULT FALSE
);
```

#### 权限表 (permissions)
```sql
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) UNIQUE NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    resource VARCHAR(50),
    action VARCHAR(50),
    type VARCHAR(20) DEFAULT 'API',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_system BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME,
    deleted BOOLEAN DEFAULT FALSE
);
```

### 关联表
- **user_roles** - 用户角色关联
- **role_permissions** - 角色权限关联

## 🔐 权限设计

### 权限模型
采用 **RBAC (Role-Based Access Control)** 模型：
- **用户** ← 多对多 → **角色** ← 多对多 → **权限**

### 权限格式
权限代码格式：`{resource}:{action}`
- `user:create` - 创建用户
- `user:read` - 查看用户
- `application:update` - 更新应用
- `odata:query` - OData查询

### 预置角色
1. **SUPER_ADMIN** - 超级管理员（所有权限）
2. **ADMIN** - 管理员（大部分管理权限）
3. **USER_MANAGER** - 用户管理员
4. **APP_MANAGER** - 应用管理员
5. **DEVELOPER** - 开发者
6. **USER** - 普通用户（只读权限）

### 预置用户
- **admin/admin123** - 超级管理员
- **test/test123** - 测试用户

## 🚀 API接口

### 认证接口

#### 用户登录
```http
POST /auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

**响应：**
```json
{
    "success": true,
    "message": "登录成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
        "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 86400,
        "userId": 1,
        "username": "admin",
        "realName": "系统管理员",
        "email": "admin@jinyi.com",
        "roles": ["SUPER_ADMIN"],
        "permissions": ["user:create", "user:read", ...]
    }
}
```

#### 刷新令牌
```http
POST /auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### 用户登出
```http
POST /auth/logout
Authorization: Bearer {accessToken}
```

#### 验证令牌
```http
GET /auth/validate
Authorization: Bearer {accessToken}
```

### 用户管理接口

#### 获取所有用户
```http
GET /users
Authorization: Bearer {accessToken}
```

#### 获取用户详情
```http
GET /users/{id}
Authorization: Bearer {accessToken}
```

#### 创建用户
```http
POST /users
Authorization: Bearer {accessToken}
Content-Type: application/json

{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@example.com",
    "realName": "新用户"
}
```

#### 更新用户
```http
PUT /users/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
    "username": "updateduser",
    "email": "updated@example.com",
    "realName": "更新用户"
}
```

#### 删除用户
```http
DELETE /users/{id}
Authorization: Bearer {accessToken}
```

## 🔧 配置说明

### JWT配置
```yaml
jwt:
  secret: odata-auth-secret-key-2024-very-long-and-secure-key-for-jwt-token-generation
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天
```

### OAuth2配置
```yaml
oauth2:
  client:
    client-id: odata-client
    client-secret: odata-secret
    redirect-uri: http://localhost:8080/login/oauth2/code/odata
    scope: read,write
    authorized-grant-types: authorization_code,refresh_token,password,client_credentials
```

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/odata_auth
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
```

## 🛡️ 安全特性

### 密码安全
- **BCrypt加密** - 使用BCrypt算法加密密码
- **随机盐值** - 每个密码使用不同的盐值
- **密码强度** - 支持密码复杂度验证

### 账户安全
- **登录失败锁定** - 5次失败后锁定1小时
- **账户状态管理** - 支持激活/禁用账户
- **密码过期** - 支持密码过期策略

### 令牌安全
- **JWT签名** - 使用HS512算法签名
- **令牌过期** - 访问令牌24小时，刷新令牌7天
- **令牌类型** - 区分访问令牌和刷新令牌

## 🧪 测试指南

### 启动服务
```bash
# 启动所有服务
.\start-cloud.cmd

# 或单独启动认证服务
cd auth-service
mvnw.cmd spring-boot:run
```

### 测试登录
```bash
# 管理员登录
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 测试用户登录
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

### 测试用户管理
```bash
# 获取用户列表（需要管理员权限）
curl -X GET http://localhost:8082/users \
  -H "Authorization: Bearer {accessToken}"

# 创建新用户
curl -X POST http://localhost:8082/users \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"password123","email":"new@example.com"}'
```

## 🔄 集成指南

### 与其他服务集成

#### 1. API网关集成
在API网关中配置认证过滤器，验证JWT令牌：

```java
@Component
public class AuthFilter implements GlobalFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());
        
        if (token != null && jwtUtil.validateToken(token)) {
            // 令牌有效，继续处理
            return chain.filter(exchange);
        } else {
            // 令牌无效，返回401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
```

#### 2. 业务服务集成
在业务服务中使用`@PreAuthorize`注解进行权限控制：

```java
@RestController
public class BusinessController {
    
    @GetMapping("/data")
    @PreAuthorize("hasAuthority('odata:query')")
    public ResponseEntity<?> getData() {
        // 业务逻辑
    }
    
    @PostMapping("/data")
    @PreAuthorize("hasAuthority('odata:create')")
    public ResponseEntity<?> createData() {
        // 业务逻辑
    }
}
```

### 前端集成

#### 1. 登录流程
```javascript
// 登录
const login = async (username, password) => {
    const response = await fetch('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
    });
    
    const result = await response.json();
    if (result.success) {
        // 保存令牌
        localStorage.setItem('accessToken', result.data.accessToken);
        localStorage.setItem('refreshToken', result.data.refreshToken);
    }
};
```

#### 2. 请求拦截器
```javascript
// 添加认证头
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 处理令牌过期
axios.interceptors.response.use(
    response => response,
    async error => {
        if (error.response?.status === 401) {
            // 尝试刷新令牌
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
                try {
                    const response = await axios.post('/auth/refresh', {
                        refreshToken
                    });
                    
                    const newToken = response.data.data.accessToken;
                    localStorage.setItem('accessToken', newToken);
                    
                    // 重试原请求
                    error.config.headers.Authorization = `Bearer ${newToken}`;
                    return axios.request(error.config);
                } catch (refreshError) {
                    // 刷新失败，跳转到登录页
                    window.location.href = '/login';
                }
            }
        }
        return Promise.reject(error);
    }
);
```

## 📈 扩展性设计

### 1. 权限扩展
- **动态权限** - 支持运行时添加新权限
- **权限继承** - 支持权限层级和继承
- **资源权限** - 支持细粒度的资源级权限

### 2. 认证方式扩展
- **多因子认证** - 支持短信、邮箱验证码
- **第三方登录** - 支持OAuth2第三方登录
- **单点登录** - 支持SSO集成

### 3. 存储扩展
- **Redis缓存** - 支持令牌黑名单和会话管理
- **分布式会话** - 支持集群部署
- **审计日志** - 支持操作审计和安全日志

## 🚨 注意事项

### 安全建议
1. **生产环境** - 修改默认密码和JWT密钥
2. **HTTPS** - 生产环境必须使用HTTPS
3. **密钥管理** - 使用密钥管理服务存储敏感信息
4. **定期更新** - 定期更新依赖和安全补丁

### 性能优化
1. **令牌缓存** - 使用Redis缓存用户权限信息
2. **数据库优化** - 为查询字段添加索引
3. **连接池** - 合理配置数据库连接池
4. **监控告警** - 添加性能监控和告警

## 🎉 总结

认证服务提供了完整的OAuth2.0认证和RBAC权限管理功能，具有以下特点：

- ✅ **安全可靠** - 采用业界标准的安全机制
- ✅ **易于集成** - 提供标准的REST API
- ✅ **高度可扩展** - 支持多种扩展方式
- ✅ **开箱即用** - 预置角色和权限，快速上手

通过这个认证服务，整个OData云平台具备了完整的用户认证和权限管理能力，为后续的业务功能提供了安全保障。