# 项目结构重构说明

## 重构目标

将原有的单一模块项目重构为框架与业务分离的模块化结构，提高代码的可维护性和可扩展性。

## 新的项目结构

```
src/main/java/com/jinyi/
├── OdataDemoApplication.java           # 主应用类
├── odata/                              # OData框架模块
│   ├── annotation/                     # OData注解
│   │   ├── ODataEntity.java           # 实体注解
│   │   └── ODataField.java            # 字段注解
│   ├── core/                          # 核心功能
│   │   └── EntityRegistryService.java # 实体注册服务
│   ├── dynamic/                       # 动态实体支持
│   │   ├── EntityDefinition.java      # 实体定义DTO
│   │   ├── DynamicEntityRegistrationService.java # 动态实体注册服务
│   │   └── EntityFileGeneratorService.java # 实体文件生成服务
│   ├── service/                       # OData服务
│   │   └── ODataQueryService.java     # OData查询服务
│   └── controller/                    # OData控制器
│       └── ODataController.java       # OData协议控制器
└── business/                          # 业务模块
    ├── entity/                        # 业务实体
    │   ├── Order.java                 # 订单实体
    │   ├── Product.java               # 产品实体
    │   └── Project.java               # 项目实体
    ├── service/                       # 业务服务
    │   └── DataInitService.java       # 数据初始化服务
    └── controller/                    # 业务控制器
        └── DynamicEntityController.java # 动态实体管理控制器
```

## 模块职责划分

### OData框架模块 (`com.jinyi.odata`)

**职责**: 提供通用的OData协议实现，包括实体注册、查询处理、动态实体支持等核心功能。

#### 子模块说明:

1. **annotation**: OData相关注解
   - `@ODataEntity`: 标记实体类
   - `@ODataField`: 标记实体字段

2. **core**: 核心功能实现
   - `EntityRegistryService`: 实体注册和管理

3. **dynamic**: 动态实体支持
   - `EntityDefinition`: 动态实体定义
   - `DynamicEntityRegistrationService`: 动态实体注册
   - `EntityFileGeneratorService`: Java文件生成

4. **service**: OData服务层
   - `ODataQueryService`: OData查询处理

5. **controller**: OData协议接口
   - `ODataController`: 标准OData端点

### 业务模块 (`com.jinyi.business`)

**职责**: 具体的业务逻辑实现，包括业务实体定义、业务服务和管理接口。

#### 子模块说明:

1. **entity**: 业务实体定义
   - `Order`: 订单实体
   - `Product`: 产品实体  
   - `Project`: 项目实体

2. **service**: 业务服务
   - `DataInitService`: 示例数据初始化

3. **controller**: 业务管理接口
   - `DynamicEntityController`: 动态实体管理API

## 重构优势

### 1. 模块化设计
- **框架与业务分离**: OData框架可以独立维护和升级
- **职责清晰**: 每个模块有明确的职责边界
- **可复用性**: OData框架可以在其他项目中复用

### 2. 可维护性提升
- **代码组织**: 相关功能集中在对应模块中
- **依赖管理**: 模块间依赖关系清晰
- **测试友好**: 可以针对不同模块进行独立测试

### 3. 可扩展性增强
- **新业务实体**: 在business.entity包中添加
- **新业务逻辑**: 在business.service包中实现
- **框架扩展**: 在odata包中添加新功能

### 4. 团队协作
- **并行开发**: 不同团队可以专注不同模块
- **代码冲突减少**: 模块化降低代码冲突概率
- **技能专业化**: 团队成员可以专精特定模块

## API端点映射

### OData协议端点 (框架提供)
- `GET /odata/{entitySet}` - 查询实体集合
- `GET /odata/{entitySet}({key})` - 获取单个实体
- `GET /odata/$metadata` - 获取元数据

### 动态实体管理端点 (业务提供)
- `POST /api/entities/register` - 注册动态实体
- `GET /api/entities` - 获取所有动态实体
- `GET /api/entities/{entityName}` - 获取实体定义
- `DELETE /api/entities/{entityName}` - 删除动态实体
- `POST /api/entities/preview` - 预览实体文件

## 配置更新

### 主应用类更新
```java
@SpringBootApplication(scanBasePackages = "com.jinyi")
public class OdataDemoApplication {
    // 扫描整个com.jinyi包，包含框架和业务模块
}
```

### 实体扫描路径更新
```java
// EntityRegistryService中的扫描路径
Reflections reflections = new Reflections("com.jinyi.business.entity");
```

### 文件生成路径更新
```java
// EntityFileGeneratorService中的生成路径
private static final String ENTITY_PACKAGE = "com.jinyi.business.entity";
private static final String ENTITY_PATH = "src/main/java/com/jinyi/business/entity/";
```

## 迁移步骤

1. ✅ 创建新的包结构
2. ✅ 移动OData框架相关类到odata包
3. ✅ 移动业务实体到business.entity包
4. ✅ 移动业务服务到business.service包
5. ✅ 移动业务控制器到business.controller包
6. ✅ 更新包引用和依赖关系
7. ✅ 更新主应用类扫描路径
8. ⏳ 删除旧的包结构文件
9. ⏳ 测试新结构的功能完整性

## 后续优化建议

1. **进一步模块化**: 考虑将OData框架提取为独立的Maven模块
2. **配置外部化**: 将框架配置参数外部化
3. **文档完善**: 为每个模块添加详细的API文档
4. **单元测试**: 为每个模块添加完整的单元测试
5. **集成测试**: 添加模块间的集成测试