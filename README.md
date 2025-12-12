# OData 自动化 CRUD 系统

这是一个基于 Spring Boot 和 Apache Olingo 的 OData 自动化 CRUD 系统。只需要通过注解标记实体类，系统就会自动：

1. 扫描并注册实体
2. 根据实体字段自动创建数据库表
3. 提供完整的 OData CRUD 接口

## 功能特性

- 🚀 **零配置 CRUD**：注册实体后无需任何额外代码配置
- 🗄️ **自动建表**：根据实体注解自动创建数据库表结构
- 📊 **OData 协议**：完全符合 OData v4 标准
- 🔍 **动态发现**：运行时自动扫描和注册实体
- 💾 **多数据库支持**：支持 MySQL、H2 等数据库

## 快速开始

### 1. 定义实体

使用 `@ODataEntity` 和 `@ODataField` 注解定义你的实体：

```java
@Data
@ODataEntity(name = "Products", table = "products")
public class Product {
    @ODataField(key = true)
    private Long id;
    
    @ODataField(nullable = false, length = 100)
    private String name;
    
    @ODataField(length = 500)
    private String description;
    
    @ODataField(nullable = false)
    private BigDecimal price;
    
    @ODataField(nullable = false)
    private Integer stock;
    
    @ODataField
    private LocalDateTime createdAt;
}
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 访问 OData 服务

- **服务文档**: `http://localhost:8080/odata`
- **元数据**: `http://localhost:8080/odata/$metadata`
- **实体集合**: `http://localhost:8080/odata/Products`
- **单个实体**: `http://localhost:8080/odata/Products(1)`

## 注解说明

### @ODataEntity

标记一个类为 OData 实体：

- `name`: OData 实体集名称（默认为类名）
- `table`: 数据库表名（默认为类名小写）
- `autoCreate`: 是否自动创建表（默认 true）

### @ODataField

标记一个字段为 OData 属性：

- `name`: 属性名称（默认为字段名）
- `key`: 是否为主键（默认 false）
- `nullable`: 是否可为空（默认 true）
- `length`: 字符串长度（默认 255）
- `type`: 自定义类型（可选）

## 支持的数据类型

- `String` → VARCHAR
- `Long/long` → BIGINT
- `Integer/int` → INT
- `BigDecimal` → DECIMAL(19,2)
- `LocalDateTime` → DATETIME
- `Boolean/boolean` → BOOLEAN

## OData 查询示例

```bash
# 获取所有产品
GET /odata/Products

# 获取特定产品
GET /odata/Products(1)

# 过滤查询
GET /odata/Products?$filter=price gt 100

# 排序
GET /odata/Products?$orderby=name asc

# 分页
GET /odata/Products?$top=10&$skip=20

# 选择字段
GET /odata/Products?$select=name,price
```

## 配置

### 数据库配置

在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
```

### 日志配置

```yaml
logging:
  level:
    com.jinyi.odatademo: DEBUG
```

## 项目结构

```
src/main/java/com/jinyi/odatademo/
├── annotation/          # 注解定义
│   ├── ODataEntity.java
│   └── ODataField.java
├── config/             # 配置类
│   └── ODataConfig.java
├── controller/         # 控制器
│   ├── ODataController.java
│   └── TestController.java
├── entity/            # 实体类
│   ├── Order.java
│   └── Product.java
├── odata/             # OData 处理器
│   ├── DynamicEdmProvider.java
│   └── DynamicEntityProcessor.java
├── service/           # 服务层
│   ├── EntityRegistryService.java
│   └── DynamicEntityService.java
└── OdataDemoApplication.java
```

## 扩展功能

系统设计为可扩展的，你可以：

1. 添加自定义字段类型映射
2. 实现复杂的查询逻辑
3. 添加权限控制
4. 集成缓存机制
5. 支持关联关系

## 注意事项

- 确保实体类在 `com.jinyi.odatademo.entity` 包下
- 主键字段必须标记 `@ODataField(key = true)`
- 自动建表功能需要数据库用户有 DDL 权限
- 当前版本主要支持基础的 CRUD 操作

## 技术栈

- Spring Boot 2.7.18
- Apache Olingo 4.10.0
- H2/MySQL Database
- Lombok
- Reflections

## 许可证

MIT License