# 基于应用的OData架构

## 概述

实现了基于应用的OData架构，每个应用拥有独立的metadata、服务和接口。应用信息可以动态新增和保存，支持多租户或多应用的业务场景。

## 架构特点

### 🏗️ 多应用架构
- **应用隔离**: 每个应用有独立的实体集合和OData服务
- **独立元数据**: 每个应用生成自己的$metadata
- **动态管理**: 应用信息可以动态创建、更新和删除
- **实体关联**: 实体与应用的多对多关联关系

### 📊 数据模型

#### 应用表 (applications)
```sql
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_code VARCHAR(50) NOT NULL UNIQUE,     -- 应用代码
    app_name VARCHAR(100) NOT NULL,           -- 应用名称
    description VARCHAR(500),                 -- 应用描述
    version VARCHAR(50) NOT NULL,             -- 版本号
    active BOOLEAN NOT NULL DEFAULT TRUE,     -- 是否激活
    base_url VARCHAR(200),                    -- 基础URL
    owner VARCHAR(100),                       -- 负责人
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

#### 应用实体关联表 (application_entities)
```sql
CREATE TABLE application_entities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,           -- 应用ID
    entity_name VARCHAR(100) NOT NULL,        -- 实体名称
    table_name VARCHAR(100) NOT NULL,         -- 数据库表名
    description VARCHAR(500),                 -- 实体描述
    is_dynamic BOOLEAN NOT NULL DEFAULT FALSE, -- 是否动态实体
    active BOOLEAN NOT NULL DEFAULT TRUE,     -- 是否激活
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (application_id) REFERENCES applications(id)
);
```

## API接口

### 🔧 应用管理接口

#### 创建应用
```http
POST /api/applications
Content-Type: application/json

{
  "appCode": "DEMO",
  "appName": "演示应用",
  "description": "OData协议演示应用",
  "version": "1.0.0",
  "active": true,
  "baseUrl": "/odata/DEMO",
  "owner": "系统管理员"
}
```

#### 获取所有应用
```http
GET /api/applications
```

#### 获取单个应用
```http
GET /api/applications/{id}
GET /api/applications/code/{appCode}
```

#### 更新应用
```http
PUT /api/applications/{id}
Content-Type: application/json

{
  "appName": "更新后的应用名称",
  "description": "更新后的描述",
  "version": "1.1.0"
}
```

#### 删除应用
```http
DELETE /api/applications/{id}
```

### 📋 应用实体管理接口

#### 为应用添加实体
```http
POST /api/applications/{id}/entities
Content-Type: application/json

{
  "entityName": "Customers",
  "tableName": "customers",
  "description": "客户信息",
  "isDynamic": false
}
```

#### 获取应用下的所有实体
```http
GET /api/applications/{id}/entities
GET /api/applications/code/{appCode}/entities
```

#### 从应用中移除实体
```http
DELETE /api/applications/{id}/entities/{entityName}
```

### 🌐 应用级OData接口

#### 应用服务文档
```http
GET /odata/{appCode}
```

#### 应用元数据
```http
GET /odata/{appCode}/$metadata
```

#### 查询应用下的实体集合
```http
GET /odata/{appCode}/{entitySet}
GET /odata/{appCode}/{entitySet}?$filter=...&$orderby=...
```

#### 获取应用下的单个实体
```http
GET /odata/{appCode}/{entitySet}({key})
```

## 使用示例

### 1. 创建新应用

```bash
curl -X POST "http://localhost:8080/api/applications" \
  -H "Content-Type: application/json" \
  -d '{
    "appCode": "INVENTORY",
    "appName": "库存管理系统",
    "description": "企业库存管理应用",
    "version": "1.0.0",
    "active": true,
    "baseUrl": "/odata/INVENTORY",
    "owner": "库存团队"
  }'
```

### 2. 为应用添加实体

```bash
curl -X POST "http://localhost:8080/api/applications/1/entities" \
  -H "Content-Type: application/json" \
  -d '{
    "entityName": "Warehouses",
    "tableName": "warehouses",
    "description": "仓库信息",
    "isDynamic": false
  }'
```

### 3. 访问应用的OData服务

```bash
# 获取应用服务文档
curl "http://localhost:8080/odata/INVENTORY"

# 获取应用元数据
curl "http://localhost:8080/odata/INVENTORY/\$metadata"

# 查询应用下的实体
curl "http://localhost:8080/odata/INVENTORY/Warehouses"
```

## 预置示例数据

系统启动时会自动创建以下示例应用：

### DEMO应用
- **应用代码**: DEMO
- **应用名称**: 演示应用
- **包含实体**: Orders, Products
- **访问地址**: `/odata/DEMO`

### ERP应用
- **应用代码**: ERP
- **应用名称**: 企业资源规划
- **包含实体**: Orders, Products, Projects
- **访问地址**: `/odata/ERP`

### CRM应用
- **应用代码**: CRM
- **应用名称**: 客户关系管理
- **包含实体**: Orders
- **访问地址**: `/odata/CRM`

## 架构优势

### 🎯 多租户支持
- **应用隔离**: 不同应用的数据和服务完全隔离
- **独立配置**: 每个应用可以有自己的配置和权限
- **灵活扩展**: 可以为不同客户或部门创建独立应用

### 🔄 动态管理
- **运行时创建**: 无需重启即可创建新应用
- **实体关联**: 灵活的实体与应用关联关系
- **版本管理**: 支持应用版本控制

### 📈 可扩展性
- **水平扩展**: 支持大量应用和实体
- **模块化**: 应用间相互独立，便于维护
- **标准化**: 遵循OData标准，易于集成

## 测试验证

### 验证应用创建
```bash
# 获取所有应用
curl "http://localhost:8080/api/applications"

# 获取DEMO应用信息
curl "http://localhost:8080/api/applications/code/DEMO"
```

### 验证应用实体
```bash
# 获取DEMO应用下的实体
curl "http://localhost:8080/api/applications/code/DEMO/entities"
```

### 验证OData服务
```bash
# 访问DEMO应用的服务文档
curl "http://localhost:8080/odata/DEMO"

# 访问DEMO应用的元数据
curl "http://localhost:8080/odata/DEMO/\$metadata"

# 查询DEMO应用下的订单
curl "http://localhost:8080/odata/DEMO/Orders"
```

## 后续扩展

### 🔐 权限控制
- 应用级别的访问控制
- 实体级别的权限管理
- 用户与应用的关联关系

### 📊 监控统计
- 应用访问统计
- 实体查询性能监控
- 应用健康状态检查

### 🔄 数据同步
- 应用间数据同步
- 实体数据迁移
- 版本升级支持