# OData 复杂查询功能示例

本文档展示了如何使用 OData 标准查询参数来执行复杂查询。

## 支持的查询参数

- `$filter` - 过滤数据（支持 OData 函数）
- `$orderby` - 排序
- `$top` - 限制返回记录数
- `$skip` - 跳过指定数量的记录
- `$select` - 选择特定字段
- `$count` - 返回总记录数

## 支持的 OData 函数

### 字符串函数
- `contains(field, 'value')` - 包含检查
- `startswith(field, 'value')` - 开头检查
- `endswith(field, 'value')` - 结尾检查
- `length(field)` - 字符串长度
- `tolower(field)` - 转小写
- `toupper(field)` - 转大写
- `trim(field)` - 去除空格

### 数学函数
- `round(field)` - 四舍五入
- `floor(field)` - 向下取整
- `ceiling(field)` - 向上取整

### 日期函数
- `year(field)` - 提取年份
- `month(field)` - 提取月份
- `day(field)` - 提取日期
- `hour(field)` - 提取小时

## 查询示例

### 基础查询

```bash
# 获取所有项目
GET /simple-odata/Projects

# 获取所有产品
GET /simple-odata/Products

# 获取所有订单
GET /simple-odata/Orders
```

### $filter 过滤查询

```bash
# 查找名称等于特定值的项目
GET /simple-odata/Projects?$filter=name eq 'E-Commerce Platform'

# 查找价格大于1000的产品
GET /simple-odata/Products?$filter=price gt 1000

# 查找库存小于等于50的产品
GET /simple-odata/Products?$filter=stock le 50

# 查找金额在100到500之间的订单
GET /simple-odata/Orders?$filter=amount gt 100 and amount lt 500

# 查找名称包含特定关键词的项目（使用通配符）
GET /simple-odata/Projects?$filter=name eq 'Mobile App Development'

# 复合条件查询
GET /simple-odata/Products?$filter=price gt 5000 and stock gt 10
```

### $orderby 排序查询

```bash
# 按名称升序排列项目
GET /simple-odata/Projects?$orderby=name

# 按名称升序排列项目（显式指定）
GET /simple-odata/Projects?$orderby=name asc

# 按开始时间降序排列项目
GET /simple-odata/Projects?$orderby=startTime desc

# 按价格降序，然后按名称升序排列产品
GET /simple-odata/Products?$orderby=price desc,name asc

# 按创建时间降序排列订单
GET /simple-odata/Orders?$orderby=createdAt desc
```

### $top 和 $skip 分页查询

```bash
# 获取前5个项目
GET /simple-odata/Projects?$top=5

# 跳过前3个，获取接下来的5个项目
GET /simple-odata/Projects?$skip=3&$top=5

# 分页示例：第2页，每页10条记录
GET /simple-odata/Products?$skip=10&$top=10

# 分页示例：第3页，每页5条记录
GET /simple-odata/Orders?$skip=10&$top=5
```

### $select 字段选择

```bash
# 只返回项目的id和名称
GET /simple-odata/Projects?$select=id,name

# 只返回产品的名称和价格
GET /simple-odata/Products?$select=name,price

# 只返回订单的订单号和金额
GET /simple-odata/Orders?$select=orderNo,amount
```

### $count 计数查询

```bash
# 获取项目总数
GET /simple-odata/Projects?$count=true

# 获取价格大于1000的产品总数
GET /simple-odata/Products?$filter=price gt 1000&$count=true

# 获取最近创建的订单总数
GET /simple-odata/Orders?$count=true
```

### 组合查询示例

```bash
# 复杂查询：查找价格大于5000的产品，按价格降序排列，返回前3个，只显示名称和价格
GET /simple-odata/Products?$filter=price gt 5000&$orderby=price desc&$top=3&$select=name,price

# 分页查询：查找所有项目，按开始时间排序，第2页，每页2条记录，包含总数
GET /simple-odata/Projects?$orderby=startTime&$skip=2&$top=2&$count=true

# 过滤和排序：查找库存大于20的产品，按名称排序，只返回名称、价格和库存
GET /simple-odata/Products?$filter=stock gt 20&$orderby=name&$select=name,price,stock

# 时间范围查询示例（需要根据实际数据调整）
GET /simple-odata/Orders?$filter=createdAt gt '2025-01-01T00:00:00'&$orderby=createdAt desc
```

### 高级函数查询示例

```bash
# 字符串函数查询
# 查找名称包含 "Pro" 的产品
GET /simple-odata/Products?$filter=contains(name, 'Pro')

# 查找名称以 "iPhone" 开头的产品
GET /simple-odata/Products?$filter=startswith(name, 'iPhone')

# 查找描述以 "computer" 结尾的产品
GET /simple-odata/Products?$filter=endswith(description, 'computer')

# 查找名称长度大于10的产品
GET /simple-odata/Products?$filter=length(name) gt 10

# 不区分大小写的名称查询
GET /simple-odata/Products?$filter=tolower(name) eq 'iphone 15'

# 数学函数查询
# 查找价格四舍五入后等于8000的产品
GET /simple-odata/Products?$filter=round(price) eq 8000

# 查找价格向下取整后大于7000的产品
GET /simple-odata/Products?$filter=floor(price) gt 7000

# 日期函数查询
# 查找2024年创建的项目
GET /simple-odata/Projects?$filter=year(startTime) eq 2024

# 查找3月份开始的项目
GET /simple-odata/Projects?$filter=month(startTime) eq 3

# 查找上午开始的项目
GET /simple-odata/Projects?$filter=hour(startTime) lt 12

# 复合函数查询
# 查找名称包含"App"且2024年开始的项目
GET /simple-odata/Projects?$filter=contains(name, 'App') and year(startTime) eq 2024
```

### 聚合统计查询

```bash
# 获取产品价格的统计信息（最小值、最大值、平均值、总和、数量）
GET /simple-odata/Products/$stats/price

# 获取产品库存的统计信息
GET /simple-odata/Products/$stats/stock

# 获取订单金额的统计信息
GET /simple-odata/Orders/$stats/amount
```

## 支持的操作符

### 比较操作符
- `eq` - 等于 (=)
- `ne` - 不等于 (!=)
- `gt` - 大于 (>)
- `ge` - 大于等于 (>=)
- `lt` - 小于 (<)
- `le` - 小于等于 (<=)

### 逻辑操作符
- `and` - 逻辑与 (AND)
- `or` - 逻辑或 (OR)
- `not` - 逻辑非 (NOT)

## 数据类型支持

- **字符串**: 使用单引号包围，如 `'Hello World'`
- **数字**: 直接使用，如 `123` 或 `123.45`
- **日期时间**: 使用 ISO 8601 格式，如 `'2024-01-01T10:30:00'`
- **布尔值**: `true` 或 `false`

## 响应格式

查询结果将返回 JSON 格式，包含以下字段：

```json
{
  "@odata.context": "/simple-odata/$metadata#EntitySet",
  "@odata.count": 100,  // 仅当 $count=true 时包含
  "value": [
    // 实体数据数组
  ]
}
```

## 错误处理

如果查询参数有误或查询失败，系统将返回相应的 HTTP 错误状态码和错误信息。

## 性能建议

1. 使用 `$select` 只返回需要的字段
2. 合理使用 `$top` 限制返回记录数
3. 在大数据集上使用 `$filter` 减少数据传输
4. 考虑在数据库层面为常用查询字段添加索引