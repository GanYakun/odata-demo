# 基于应用的动态实体管理

## 概述

重构了动态实体管理控制器，现在所有动态实体操作都必须在应用上下文中完成。不能独立注册实体，只能将实体注册到特定的应用中。

## 架构变更

### 🔄 路径变更
- **旧路径**: `/api/entities/*`
- **新路径**: `/api/applications/{appId}/dynamic-entities/*`

### 🎯 设计原则
- **应用隔离**: 所有动态实体都必须属于特定应用
- **上下文验证**: 每个操作都会验证应用存在性和实体归属
- **统一管理**: 通过应用维度管理动态实体的生命周期

## API接口

### 📝 动态实体注册

#### 在应用中注册动态实体
```http
POST /api/applications/{appId}/dynamic-entities
Content-Type: application/json

{
  "entityName": "Customer",
  "tableName": "customers",
  "description": "Customer management entity",
  "autoCreate": true,
  "fields": [
    {
      "fieldName": "id",
      "dataType": "LONG",
      "key": true,
      "nullable": false,
      "description": "Customer ID"
    },
    {
      "fieldName": "name",
      "dataType": "STRING",
      "length": 100,
      "nullable": false,
      "description": "Customer name"
    }
  ]
}
```

**参数**:
- `appId`: 应用ID（路径参数）
- `generateJavaFile`: 是否生成Java文件（查询参数，默认true）

**响应**:
```json
{
  "success": true,
  "message": "Entity registered successfully...",
  "applicationId": 1,
  "entityName": "Customer",
  "tableName": "customers",
  "javaFileGenerated": true,
  "javaFilePath": "src/main/java/com/jinyi/business/entity/Customer.java",
  "applicationEntity": {
    "id": 5,
    "applicationId": 1,
    "entityName": "Customer",
    "tableName": "customers",
    "description": "Customer management entity",
    "isDynamic": true,
    "active": true
  }
}
```

### 📋 动态实体查询

#### 获取应用中的所有动态实体
```http
GET /api/applications/{appId}/dynamic-entities
```

**响应**:
```json
{
  "success": true,
  "applicationId": 1,
  "count": 2,
  "entities": {
    "Customer": {
      "applicationEntity": {
        "id": 5,
        "applicationId": 1,
        "entityName": "Customer",
        "isDynamic": true
      },
      "entityDefinition": {
        "entityName": "Customer",
        "tableName": "customers",
        "fields": [...]
      }
    }
  }
}
```

#### 获取应用中特定动态实体的定义
```http
GET /api/applications/{appId}/dynamic-entities/{entityName}
```

**响应**:
```json
{
  "success": true,
  "applicationId": 1,
  "entity": {
    "entityName": "Customer",
    "tableName": "customers",
    "description": "Customer management entity",
    "fields": [...]
  },
  "applicationEntity": {
    "id": 5,
    "applicationId": 1,
    "entityName": "Customer",
    "isDynamic": true
  }
}
```

### 🗑️ 动态实体删除

#### 从应用中删除动态实体
```http
DELETE /api/applications/{appId}/dynamic-entities/{entityName}?dropTable=false&deleteJavaFile=true
```

**参数**:
- `dropTable`: 是否删除数据库表（默认false）
- `deleteJavaFile`: 是否删除Java文件（默认true）

**响应**:
```json
{
  "success": true,
  "message": "Entity unregistered successfully...",
  "applicationId": 1,
  "entityName": "Customer",
  "tableDropped": false,
  "javaFileDeleted": true
}
```

### 📄 Java文件管理

#### 预览动态实体Java文件
```http
POST /api/applications/{appId}/dynamic-entities/preview
Content-Type: application/json

{
  "entityName": "Customer",
  "fields": [...]
}
```

#### 为已注册的动态实体生成Java文件
```http
POST /api/applications/{appId}/dynamic-entities/{entityName}/generate-file
```

#### 删除动态实体的Java文件
```http
DELETE /api/applications/{appId}/dynamic-entities/{entityName}/file
```

#### 检查动态实体Java文件状态
```http
GET /api/applications/{appId}/dynamic-entities/{entityName}/file-status
```

## 验证机制

### 🔍 应用验证
每个API调用都会验证：
1. **应用存在性**: 检查指定的应用ID是否存在
2. **实体归属**: 验证实体是否属于指定应用
3. **动态实体**: 确认实体标记为动态实体（isDynamic=true）

### ⚠️ 错误处理
- **404 Not Found**: 应用不存在或实体不属于应用
- **400 Bad Request**: 请求参数错误或业务逻辑错误
- **409 Conflict**: 资源冲突（如文件已存在）

## 使用示例

### 1. 为DEMO应用注册动态实体

```bash
# 获取DEMO应用ID
curl "http://localhost:8080/api/applications/code/DEMO"

# 在DEMO应用中注册Customer实体
curl -X POST "http://localhost:8080/api/applications/1/dynamic-entities" \
  -H "Content-Type: application/json" \
  -d '{
    "entityName": "Customer",
    "tableName": "customers",
    "description": "Customer management",
    "autoCreate": true,
    "fields": [
      {
        "fieldName": "id",
        "dataType": "LONG",
        "key": true,
        "nullable": false,
        "description": "Customer ID"
      },
      {
        "fieldName": "name",
        "dataType": "STRING",
        "length": 100,
        "nullable": false,
        "description": "Customer name"
      }
    ]
  }'
```

### 2. 查询应用中的动态实体

```bash
# 获取DEMO应用中的所有动态实体
curl "http://localhost:8080/api/applications/1/dynamic-entities"

# 获取特定动态实体定义
curl "http://localhost:8080/api/applications/1/dynamic-entities/Customer"
```

### 3. 通过OData访问动态实体

```bash
# 访问DEMO应用的服务文档（包含动态实体）
curl "http://localhost:8080/odata/DEMO"

# 查询DEMO应用中的Customer实体
curl "http://localhost:8080/odata/DEMO/Customer"
```

### 4. 管理Java文件

```bash
# 检查Java文件状态
curl "http://localhost:8080/api/applications/1/dynamic-entities/Customer/file-status"

# 生成Java文件（如果不存在）
curl -X POST "http://localhost:8080/api/applications/1/dynamic-entities/Customer/generate-file"

# 删除Java文件
curl -X DELETE "http://localhost:8080/api/applications/1/dynamic-entities/Customer/file"
```

## 架构优势

### 🎯 强制应用隔离
- **数据隔离**: 每个应用的动态实体完全独立
- **权限控制**: 可以基于应用实现细粒度权限控制
- **多租户支持**: 天然支持多租户架构

### 🔧 统一管理
- **生命周期管理**: 动态实体与应用生命周期绑定
- **批量操作**: 可以批量管理应用下的所有动态实体
- **依赖关系**: 清晰的应用-实体依赖关系

### 📊 可追溯性
- **操作审计**: 所有操作都有明确的应用上下文
- **资源归属**: 每个动态实体都有明确的归属应用
- **影响范围**: 操作影响范围限定在应用内

## 迁移指南

### 从旧API迁移到新API

| 旧API | 新API |
|-------|-------|
| `POST /api/entities/register` | `POST /api/applications/{appId}/dynamic-entities` |
| `GET /api/entities` | `GET /api/applications/{appId}/dynamic-entities` |
| `GET /api/entities/{entityName}` | `GET /api/applications/{appId}/dynamic-entities/{entityName}` |
| `DELETE /api/entities/{entityName}` | `DELETE /api/applications/{appId}/dynamic-entities/{entityName}` |
| `POST /api/entities/preview` | `POST /api/applications/{appId}/dynamic-entities/preview` |

### 注意事项
1. **应用ID必需**: 所有操作都需要指定应用ID
2. **权限验证**: 需要验证操作者对应用的访问权限
3. **实体归属**: 确保实体只能在其归属应用中操作
4. **批量迁移**: 现有独立实体需要分配到相应应用中

## 🧪 测试结果

### ✅ 功能验证完成

经过全面测试，基于应用的动态实体管理系统已成功实现并验证了以下功能：

#### 1. **动态实体注册** ✅
- 成功在DEMO应用（ID: 1）中注册Customer实体
- 自动创建数据库表 `demo_customers`
- 自动生成Java实体文件 `Customer.java`
- 正确关联到应用实体表 `application_entities`

#### 2. **应用上下文验证** ✅
- 验证应用存在性检查正常工作
- 实体归属验证功能正确
- 跨应用访问被正确拒绝（404错误）
- 不存在应用访问被正确拒绝（404错误）

#### 3. **OData集成** ✅
- 动态实体自动出现在应用的OData服务文档中
- 可通过 `/odata/DEMO/Customer` 访问动态实体
- 支持标准OData查询操作
- 应用隔离正确工作

#### 4. **CRUD生命周期** ✅
- **创建**: 动态实体注册成功
- **读取**: 获取实体定义和列表正常
- **删除**: 实体删除、表删除、文件删除正常
- **验证**: 删除后实体从OData服务中消失

#### 5. **Java文件管理** ✅
- 文件生成功能正常
- 文件状态检查正常
- 文件预览功能正常
- 文件删除功能正常

#### 6. **错误处理** ✅
- 应用不存在时返回404
- 实体不属于应用时返回404
- 重复注册时正确处理
- 表已存在时正确处理

### 📊 测试用例摘要

| 测试场景 | 状态 | 结果 |
|---------|------|------|
| 在应用中注册动态实体 | ✅ | 成功创建表、文件和关联 |
| 获取应用中的所有动态实体 | ✅ | 正确返回实体列表和定义 |
| 获取特定动态实体定义 | ✅ | 正确返回详细信息 |
| 跨应用访问实体 | ✅ | 正确拒绝并返回404 |
| 访问不存在的应用 | ✅ | 正确拒绝并返回404 |
| 删除动态实体 | ✅ | 成功删除表、文件和关联 |
| OData服务集成 | ✅ | 动态实体正确出现在服务中 |
| Java文件预览 | ✅ | 正确生成预览代码 |

### 🎯 架构优势验证

1. **强制应用隔离** ✅
   - 实体只能在其归属应用中操作
   - 跨应用访问被正确阻止
   - 数据完全隔离

2. **统一管理** ✅
   - 实体生命周期与应用绑定
   - 删除应用时可批量清理实体
   - 清晰的依赖关系

3. **可追溯性** ✅
   - 所有操作都有明确的应用上下文
   - 实体归属关系清晰
   - 操作日志完整

### 🚀 系统就绪

基于应用的动态实体管理系统已完全就绪，可以投入生产使用。所有核心功能都已验证通过，错误处理机制完善，与现有OData框架完美集成。