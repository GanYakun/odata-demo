# 动态实体注册功能

本文档介绍如何使用动态实体注册功能，在不重启应用的情况下创建新的实体和数据库表。

## 功能特性

- 🚀 **动态注册** - 无需重启应用即可注册新实体
- 🗄️ **自动建表** - 根据实体定义自动创建数据库表
- 📊 **OData 支持** - 动态实体自动支持 OData 查询
- 🔍 **实时生效** - 注册后立即可用于查询
- 🛠️ **管理接口** - 提供完整的 CRUD 管理接口

## API 接口

### 1. 注册新实体

**POST** `/api/entities/register`

```json
{
  "entityName": "Customer",
  "tableName": "customers",
  "description": "客户信息表",
  "autoCreate": true,
  "fields": [
    {
      "fieldName": "id",
      "columnName": "id",
      "dataType": "LONG",
      "key": true,
      "nullable": false,
      "description": "主键ID"
    },
    {
      "fieldName": "name",
      "columnName": "name",
      "dataType": "STRING",
      "key": false,
      "nullable": false,
      "length": 100,
      "description": "客户姓名"
    },
    {
      "fieldName": "email",
      "columnName": "email",
      "dataType": "STRING",
      "key": false,
      "nullable": true,
      "length": 200,
      "description": "邮箱地址"
    },
    {
      "fieldName": "phone",
      "columnName": "phone",
      "dataType": "STRING",
      "key": false,
      "nullable": true,
      "length": 20,
      "description": "电话号码"
    },
    {
      "fieldName": "balance",
      "columnName": "balance",
      "dataType": "DECIMAL",
      "key": false,
      "nullable": false,
      "description": "账户余额"
    },
    {
      "fieldName": "isActive",
      "columnName": "is_active",
      "dataType": "BOOLEAN",
      "key": false,
      "nullable": false,
      "description": "是否激活"
    },
    {
      "fieldName": "createdAt",
      "columnName": "created_at",
      "dataType": "DATETIME",
      "key": false,
      "nullable": false,
      "description": "创建时间"
    }
  ]
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "Entity registered successfully: Customer",
  "entityName": "Customer",
  "tableName": "customers"
}
```

### 2. 获取实体定义

**GET** `/api/entities/{entityName}`

**响应示例：**
```json
{
  "success": true,
  "entity": {
    "entityName": "Customer",
    "tableName": "customers",
    "description": "客户信息表",
    "autoCreate": true,
    "fields": [...]
  }
}
```

### 3. 获取所有动态实体

**GET** `/api/entities`

**响应示例：**
```json
{
  "success": true,
  "count": 2,
  "entities": {
    "Customer": {
      "entityName": "Customer",
      "tableName": "customers",
      "description": "客户信息表",
      "fields": [...]
    }
  }
}
```

### 4. 获取所有实体（静态+动态）

**GET** `/api/entities/all`

**响应示例：**
```json
{
  "success": true,
  "totalCount": 5,
  "entities": {
    "static": {
      "Orders": {
        "type": "static",
        "className": "com.jinyi.odatademo.entity.Order",
        "tableName": "orders"
      },
      "Products": {
        "type": "static",
        "className": "com.jinyi.odatademo.entity.Product",
        "tableName": "products"
      }
    },
    "dynamic": {
      "Customer": {
        "type": "dynamic",
        "tableName": "customers",
        "description": "客户信息表",
        "fieldCount": 7
      }
    }
  }
}
```

### 5. 验证实体定义

**POST** `/api/entities/validate`

```json
{
  "entityName": "TestEntity",
  "tableName": "test_entity",
  "fields": [
    {
      "fieldName": "id",
      "dataType": "LONG",
      "key": true,
      "nullable": false
    }
  ]
}
```

### 6. 删除实体

**DELETE** `/api/entities/{entityName}?dropTable=true`

**响应示例：**
```json
{
  "success": true,
  "message": "Entity unregistered successfully: Customer",
  "entityName": "Customer",
  "tableDropped": true
}
```

## 支持的数据类型

| 数据类型 | Java 类型 | SQL 类型 | 说明 |
|---------|-----------|----------|------|
| STRING | String | VARCHAR(length) | 字符串类型 |
| LONG | Long | BIGINT | 长整型 |
| INTEGER | Integer | INT | 整型 |
| DECIMAL | BigDecimal | DECIMAL(19,2) | 小数类型 |
| DATETIME | LocalDateTime | DATETIME | 日期时间类型 |
| BOOLEAN | Boolean | BOOLEAN | 布尔类型 |

## 使用示例

### 1. 注册客户实体

```bash
curl -X POST http://localhost:8080/api/entities/register \
  -H "Content-Type: application/json" \
  -d '{
    "entityName": "Customer",
    "tableName": "customers",
    "description": "客户信息表",
    "autoCreate": true,
    "fields": [
      {
        "fieldName": "id",
        "dataType": "LONG",
        "key": true,
        "nullable": false
      },
      {
        "fieldName": "name",
        "dataType": "STRING",
        "nullable": false,
        "length": 100
      },
      {
        "fieldName": "email",
        "dataType": "STRING",
        "nullable": true,
        "length": 200
      }
    ]
  }'
```

### 2. 查询动态实体数据

注册成功后，可以立即使用 OData 接口查询：

```bash
# 获取所有客户
GET http://localhost:8080/simple-odata/Customer

# 获取特定客户
GET http://localhost:8080/simple-odata/Customer/1

# 使用过滤查询
GET http://localhost:8080/simple-odata/Customer?$filter=contains(name, 'John')

# 使用排序和分页
GET http://localhost:8080/simple-odata/Customer?$orderby=name&$top=10&$skip=0
```

### 3. 查看元数据

动态实体会自动添加到 OData 元数据中：

```bash
GET http://localhost:8080/simple-odata/$metadata
```

### 4. 获取统计信息

```bash
GET http://localhost:8080/simple-odata/Customer/$stats/balance
```

## 注意事项

1. **实体名称唯一性** - 实体名称在系统中必须唯一
2. **主键必需** - 每个实体至少需要一个主键字段
3. **表名规范** - 建议使用下划线命名法
4. **字段名称** - 建议使用驼峰命名法
5. **数据类型** - 必须使用支持的数据类型
6. **长度限制** - STRING 类型需要指定合适的长度

## 错误处理

常见错误及解决方案：

- **实体已存在** - 检查实体名称是否重复
- **缺少主键** - 确保至少有一个字段标记为 key=true
- **数据类型无效** - 使用支持的数据类型
- **字段名重复** - 确保字段名称唯一
- **表创建失败** - 检查数据库权限和表名规范

## 最佳实践

1. **命名规范** - 使用有意义的实体和字段名称
2. **字段设计** - 合理设置字段类型和长度
3. **索引考虑** - 对于大表，考虑在数据库层面添加索引
4. **数据验证** - 在应用层添加数据验证逻辑
5. **备份策略** - 重要数据表要有备份策略