# 新实体系统测试指南

## 测试环境
- Platform Config Service: http://localhost:8081
- OData Gateway Service: http://localhost:8080  
- API Gateway Service: http://localhost:9000

## 数据库配置

### H2数据库（内存模式）
```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
```

### MySQL数据库
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/odata_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    hikari:
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      maximum-pool-size: 20
      minimum-idle: 5
```

**MySQL初始化:**
```sql
CREATE DATABASE IF NOT EXISTS odata_demo 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;
```

## 1. 创建实体定义

### 创建Customer实体
```bash
curl -X POST http://localhost:8081/platform/entity-definitions \
  -H "Content-Type: application/json" \
  -d '{
    "entityName": "Customers",
    "entityCode": "CUSTOMER",
    "displayName": "客户管理",
    "description": "客户信息管理实体",
    "appId": 1,
    "autoCreateTable": true,
    "fields": [
      {
        "fieldName": "name",
        "fieldCode": "NAME",
        "displayName": "客户名称",
        "fieldType": "STRING",
        "fieldLength": 100,
        "isNotNull": true,
        "sortOrder": 1
      },
      {
        "fieldName": "email",
        "fieldCode": "EMAIL", 
        "displayName": "邮箱地址",
        "fieldType": "STRING",
        "fieldLength": 255,
        "isUnique": true,
        "sortOrder": 2
      },
      {
        "fieldName": "phone",
        "fieldCode": "PHONE",
        "displayName": "联系电话", 
        "fieldType": "STRING",
        "fieldLength": 20,
        "sortOrder": 3
      },
      {
        "fieldName": "age",
        "fieldCode": "AGE",
        "displayName": "年龄",
        "fieldType": "INTEGER",
        "sortOrder": 4
      },
      {
        "fieldName": "balance",
        "fieldCode": "BALANCE",
        "displayName": "账户余额",
        "fieldType": "DECIMAL",
        "fieldLength": 10,
        "decimalPlaces": 2,
        "defaultValue": "0.00",
        "sortOrder": 5
      },
      {
        "fieldName": "isVip",
        "fieldCode": "IS_VIP",
        "displayName": "是否VIP",
        "fieldType": "BOOLEAN",
        "defaultValue": "false",
        "sortOrder": 6
      },
      {
        "fieldName": "registerDate",
        "fieldCode": "REGISTER_DATE",
        "displayName": "注册日期",
        "fieldType": "DATETIME",
        "sortOrder": 7
      }
    ]
  }'
```

### 创建Product实体
```bash
curl -X POST http://localhost:8081/platform/entity-definitions \
  -H "Content-Type: application/json" \
  -d '{
    "entityName": "Products",
    "entityCode": "PRODUCT",
    "displayName": "产品管理",
    "description": "产品信息管理实体",
    "appId": 1,
    "autoCreateTable": true,
    "fields": [
      {
        "fieldName": "name",
        "fieldCode": "NAME",
        "displayName": "产品名称",
        "fieldType": "STRING",
        "fieldLength": 200,
        "isNotNull": true,
        "sortOrder": 1
      },
      {
        "fieldName": "description",
        "fieldCode": "DESCRIPTION",
        "displayName": "产品描述",
        "fieldType": "TEXT",
        "sortOrder": 2
      },
      {
        "fieldName": "price",
        "fieldCode": "PRICE",
        "displayName": "价格",
        "fieldType": "DECIMAL",
        "fieldLength": 10,
        "decimalPlaces": 2,
        "isNotNull": true,
        "sortOrder": 3
      },
      {
        "fieldName": "stock",
        "fieldCode": "STOCK",
        "displayName": "库存数量",
        "fieldType": "INTEGER",
        "defaultValue": "0",
        "sortOrder": 4
      },
      {
        "fieldName": "category",
        "fieldCode": "CATEGORY",
        "displayName": "产品分类",
        "fieldType": "STRING",
        "fieldLength": 50,
        "sortOrder": 5
      },
      {
        "fieldName": "isActive",
        "fieldCode": "IS_ACTIVE",
        "displayName": "是否激活",
        "fieldType": "BOOLEAN",
        "defaultValue": "true",
        "sortOrder": 6
      }
    ]
  }'
```

## 2. 查询实体定义

### 获取应用下的所有实体
```bash
curl http://localhost:8081/platform/entity-definitions/app/1
```

### 根据实体名称获取实体定义
```bash
curl http://localhost:8081/platform/entity-definitions/app/1/name/Customers
```

## 3. 创建实体数据

### 创建客户数据
```bash
curl -X POST http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "age": 28,
    "balance": 1500.50,
    "isVip": true,
    "registerDate": "2025-01-15T10:30:00"
  }'
```

```bash
curl -X POST http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER \
  -H "Content-Type: application/json" \
  -d '{
    "name": "李四",
    "email": "lisi@example.com", 
    "phone": "13900139000",
    "age": 32,
    "balance": 2800.00,
    "isVip": false,
    "registerDate": "2025-01-10T14:20:00"
  }'
```

### 创建产品数据
```bash
curl -X POST http://localhost:8081/platform/entity-data/app/1/entity/PRODUCT \
  -H "Content-Type: application/json" \
  -d '{
    "name": "智能手机",
    "description": "最新款智能手机，配备高清摄像头",
    "price": 2999.99,
    "stock": 50,
    "category": "电子产品",
    "isActive": true
  }'
```

```bash
curl -X POST http://localhost:8081/platform/entity-data/app/1/entity/PRODUCT \
  -H "Content-Type: application/json" \
  -d '{
    "name": "笔记本电脑",
    "description": "轻薄便携笔记本电脑，适合办公",
    "price": 5999.00,
    "stock": 30,
    "category": "电子产品", 
    "isActive": true
  }'
```

## 4. 查询实体数据

### 查询客户数据列表
```bash
curl "http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER?page=1&size=10"
```

### 使用OData查询参数
```bash
curl "http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER/query?\$top=5&\$skip=0"
```

```bash
curl "http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER/query?\$filter=age gt 30"
```

```bash
curl "http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER/query?\$orderby=balance desc&\$top=3"
```

### 查询产品数据
```bash
curl "http://localhost:8081/platform/entity-data/app/1/entity/PRODUCT/query?\$filter=price lt 4000&\$orderby=price"
```

## 5. 通过API Gateway测试OData服务

### 获取服务文档
```bash
curl http://localhost:9000/odata/DEMO
```

### 查询Customers实体（通过API Gateway）
```bash
curl http://localhost:9000/odata/DEMO/Customers
```

### 查询Products实体（通过API Gateway）
```bash
curl http://localhost:9000/odata/DEMO/Products
```

### 使用OData查询参数
```bash
curl "http://localhost:9000/odata/DEMO/Customers?\$top=5"
```

```bash
curl "http://localhost:9000/odata/DEMO/Products?\$filter=price gt 3000"
```

## 6. 更新实体数据

### 根据记录ID更新客户数据
```bash
# 首先获取记录ID
RECORD_ID=$(curl -s "http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER/query?\$top=1" | jq -r '.data.value[0].id')

# 更新数据
curl -X PUT "http://localhost:8081/platform/entity-data/entity/1/record/$RECORD_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三（已更新）",
    "email": "zhangsan_updated@example.com",
    "balance": 2000.00,
    "isVip": true
  }'
```

## 7. 删除实体数据

### 根据记录ID删除数据
```bash
# 获取要删除的记录ID
RECORD_ID=$(curl -s "http://localhost:8081/platform/entity-data/app/1/entity/CUSTOMER/query?\$top=1" | jq -r '.data.value[0].id')

# 删除数据
curl -X DELETE "http://localhost:8081/platform/entity-data/entity/1/record/$RECORD_ID"
```

## 8. 验证数据库表创建

### 查看H2数据库控制台
访问: http://localhost:8081/h2-console

连接信息:
- JDBC URL: jdbc:h2:mem:testdb
- User Name: sa
- Password: (空)

### 检查创建的表
```sql
-- 查看实体定义表
SELECT * FROM entity_definitions;

-- 查看字段定义表  
SELECT * FROM entity_field_definitions;

-- 查看数据存储表
SELECT * FROM entity_data_storage;

-- 查看动态创建的业务表
SELECT * FROM dyn_customer;
SELECT * FROM dyn_product;
```

## 预期结果

1. ✅ 实体定义创建成功，自动生成实体编码
2. ✅ 数据库表自动创建，包含定义的字段和系统字段
3. ✅ 实体数据以JSON格式存储在entity_data_storage表中
4. ✅ 支持完整的CRUD操作
5. ✅ OData查询参数正常工作
6. ✅ API Gateway路由正确转发请求
7. ✅ 服务间通信正常，数据一致性保证

## 测试结果 (2025-12-15)

### ✅ 成功完成的功能 - H2数据库版本
1. **实体定义创建**: 成功创建Customers和Products实体，自动生成实体编码和数据库表
2. **数据存储**: 实体数据正确存储在entity_data_storage表中，使用JSON格式
3. **OData查询**: 通过API Gateway成功查询实体数据，支持$top、$filter等参数
4. **OData创建**: 通过POST请求成功创建新的实体数据
5. **服务发现**: 所有微服务正确注册到Nacos，服务间通信正常
6. **数据库兼容**: H2数据库表创建和数据操作正常工作

### ✅ 成功完成的功能 - MySQL数据库版本
1. **MySQL数据库集成**: 成功从H2迁移到MySQL 5.7，支持utf8mb4字符集
2. **表结构优化**: 使用InnoDB引擎，添加适当的索引和注释，BOOLEAN类型映射为TINYINT(1)
3. **中文字符支持**: 正确处理中文实体名称、字段名称和数据内容
4. **动态表创建**: 成功创建TestEntity和Products动态表，包含系统字段和业务字段
5. **数据CRUD操作**: 完整的创建、查询、更新、删除操作在MySQL中正常工作
6. **OData协议支持**: 通过API Gateway的OData查询在MySQL环境下正常工作

### 🔧 已修复的问题
1. **OData Gateway数据源配置**: 排除DataSourceAutoConfiguration，避免不必要的数据库配置
2. **实体编码映射**: 修复OData服务中实体名称到实体编码的映射问题
3. **JSON数据类型**: 将CLOB改为TEXT类型，解决H2数据库兼容性问题
4. **服务集成**: 更新OData Gateway使用新实体系统API
5. **$select参数**: 实现完整的字段选择逻辑，支持单个和多个字段过滤
6. **分页逻辑**: 修复MyBatis Plus分页计算错误，$top和$skip参数正确工作
7. **中文字符编码**: 使用FastJSON UTF-8特性，确保中文字符正确存储和显示
8. **MyBatis Plus分页插件**: 添加PaginationInnerInterceptor，确保分页查询生成正确的LIMIT子句
9. **$filter参数**: 实现完整的过滤功能，支持数字比较、字符串比较和函数过滤

### 📊 测试数据
**H2版本:**
- 创建了2个实体：Customers (4个字段), Products (4个字段)
- 成功创建了3条客户数据和1条产品数据
- OData查询返回正确的数据结构和内容

**MySQL版本:**
- 创建了2个实体：TestEntity (2个字段), Products (2个字段)
- 成功创建了1条测试数据和3条产品数据（包含中文）
- 动态表自动创建，包含系统字段和业务字段
- JSON数据正确存储在LONGTEXT字段中
- 中文字符编码问题已修复，新数据正确显示

### 🌐 API端点测试
**H2版本:**
- ✅ GET http://localhost:9000/odata/DEMO - 服务文档
- ✅ GET http://localhost:9000/odata/DEMO/Customers - 查询客户
- ✅ GET http://localhost:9000/odata/DEMO/Products - 查询产品
- ✅ POST http://localhost:9000/odata/DEMO/Customers - 创建客户
- ✅ POST http://localhost:9000/odata/DEMO/Products - 创建产品
- ✅ GET http://localhost:9000/odata/DEMO/Products?$filter=price gt 2000 - 过滤查询

**MySQL版本:**
- ✅ GET http://localhost:9000/odata/DEMO - 服务文档（显示TestEntity和Products）
- ✅ GET http://localhost:9000/odata/DEMO/TestEntity - 查询测试实体
- ✅ GET http://localhost:9000/odata/DEMO/Products - 查询产品（支持中文数据）
- ✅ GET http://localhost:9000/odata/DEMO/Products?$select=name - 字段选择查询
- ✅ GET http://localhost:9000/odata/DEMO/Products?$top=1 - 分页查询（返回1条记录）
- ✅ GET http://localhost:9000/odata/DEMO/Products?$top=2 - 分页查询（返回2条记录）
- ✅ GET http://localhost:9000/odata/DEMO/Products?$skip=1&$top=1 - 跳过分页查询
- ✅ GET http://localhost:9000/odata/DEMO/Products?$filter=price gt 3000 - 数字过滤查询
- ✅ GET http://localhost:9000/odata/DEMO/Products?$filter=contains(name,'电脑') - 字符串函数过滤
- ✅ GET http://localhost:9000/odata/DEMO/Products?$filter=price gt 3000&$select=name,price&$top=2 - 完整组合查询
- ✅ POST http://localhost:8081/platform/entity-definitions - 创建实体定义
- ✅ POST http://localhost:8081/platform/entity-data/app/1/entity/{CODE} - 创建实体数据

### 🔍 OData查询参数测试验证

**分页参数:**
- `$top=1`: 返回1条记录 ✅
- `$top=2`: 返回2条记录 ✅  
- `$skip=1&$top=1`: 跳过1条，返回1条 ✅
- `$skip=2&$top=1`: 跳过2条，返回1条 ✅
- SQL查询包含正确的LIMIT子句 ✅

**字段选择:**
- `$select=name`: 只返回name和id字段 ✅
- `$select=name,price`: 返回name、price和id字段 ✅

**过滤查询:**
- `$filter=price gt 3000`: 数字大于比较 ✅ (2条记录)
- `$filter=price eq 5999`: 数字等于比较 ✅ (1条记录)
- `$filter=name eq '平板电脑'`: 字符串等于比较 ✅ (1条记录)
- `$filter=contains(name,'电脑')`: 字符串包含函数 ✅ (3条记录)
- `$filter=startswith(name,'笔记本')`: 字符串开始函数 ✅ (2条记录)
- `$filter=endswith(name,'电脑')`: 字符串结束函数 ✅ (3条记录)

**组合查询:**
- `$filter + $select + $top`: 完整组合查询正常工作 ✅

## 故障排除

### 常见问题
1. **服务启动失败**: 检查Nacos是否正常运行
2. **数据库连接失败**: 确认H2数据库配置正确
3. **实体创建失败**: 检查字段定义是否符合规范
4. **OData查询失败**: 确认实体名称和应用ID正确

### 日志查看
- Platform Config Service日志: 查看实体创建和数据操作日志
- OData Gateway日志: 查看OData协议处理日志
- API Gateway日志: 查看路由转发日志

## ✅ $orderby and $stats Functionality Testing (2025-12-15)

### $orderby Parameter Testing
**Single Field Sorting:**
- `$orderby=price desc`: ✅ Correctly sorts by price descending (5999.0, 3999.99, 2999.99)
- `$orderby=price asc`: ✅ Correctly sorts by price ascending (2350.0, 2999.99, 3999.99, 5999.0)
- `$orderby=name asc`: ✅ Correctly sorts by name alphabetically
- `$orderby=name desc`: ✅ Correctly sorts by name reverse alphabetically

**Multi-Field Sorting:**
- `$orderby=price asc,name desc`: ✅ Correctly applies multiple sort criteria
- `$orderby=name asc,price desc`: ✅ Correctly handles secondary sort field

**JSON Field Sorting:**
- Uses MySQL JSON_EXTRACT function for sorting JSON fields ✅
- Handles both numeric and string field types correctly ✅

### $stats Parameter Testing
**Basic Statistics:**
- `$stats=true`: ✅ Returns comprehensive statistics in `@odata.stats` field
- Total records count: ✅ Correctly counts filtered results
- Field-level statistics: ✅ Generates stats for both numeric and string fields

**Numeric Field Statistics (price field):**
- Count: ✅ Number of non-null values
- Sum: ✅ Total sum of all values
- Average: ✅ Mean value calculation
- Min: ✅ Minimum value
- Max: ✅ Maximum value

**String Field Statistics (name field):**
- Count: ✅ Number of non-null values
- Unique Count: ✅ Number of distinct values
- Min Length: ✅ Shortest string length
- Max Length: ✅ Longest string length
- Average Length: ✅ Mean string length

### Combined Parameter Testing
**$orderby + $stats:**
```bash
curl "http://localhost:8081/platform/entity-data/app/1/entity/PRODUCT/query?\$orderby=price desc&\$stats=true&\$top=3"
```
✅ Results correctly sorted AND statistics generated for the sorted data

**$filter + $orderby + $stats:**
```bash
curl "http://localhost:8081/platform/entity-data/app/1/entity/PRODUCT/query?\$filter=price gt 3000&\$orderby=name asc&\$stats=true"
```
✅ Results filtered, sorted, and statistics calculated only for filtered results

### API Gateway Integration Testing
**Through OData Gateway (port 8080) and API Gateway (port 9000):**
- `GET /odata/DEMO/Products?$orderby=price desc&$top=2`: ✅ Sorting works through gateway
- `GET /odata/DEMO/Products?$stats=true&$top=3`: ✅ Statistics work through gateway
- Full OData protocol compliance maintained ✅

### Sample Response Structure
```json
{
  "@odata.context": "$metadata#PRODUCT",
  "@odata.stats": {
    "totalRecords": 3,
    "fieldStats": {
      "price": {
        "average": 4116.33,
        "min": 2350.0,
        "max": 5999.0,
        "count": 3,
        "sum": 12348.99
      },
      "name": {
        "averageLength": 4.67,
        "minLength": 4,
        "count": 3,
        "maxLength": 5,
        "uniqueCount": 2
      }
    }
  },
  "value": [...]
}
```

### Performance Notes
- MySQL JSON functions used for efficient field extraction and sorting
- Statistics calculated in-memory after data retrieval for flexibility
- Pagination applied before statistics calculation for accurate filtered results
- Debug logging available for troubleshooting query parameter parsing

### Implementation Features
1. **Robust Parameter Parsing**: Handles multiple sort fields with asc/desc modifiers
2. **Type-Aware Statistics**: Different statistics for numeric vs string fields
3. **Error Handling**: Graceful fallback if parsing fails
4. **JSON Field Support**: Full support for JSON-stored entity data
5. **OData Compliance**: Follows OData v4 standard for query parameters
6. **Gateway Integration**: Works seamlessly through microservices architecture