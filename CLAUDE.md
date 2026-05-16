# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

瑞吉外卖（reggie-takeout）—— 餐厅外卖管理系统，Spring Boot 单体应用。

## 构建与运行

```bash
# 构建（跳过测试）
./mvnw clean package -DskipTests

# 运行
./mvnw spring-boot:run

# 运行所有测试
./mvnw test

# 运行单个测试
./mvnw test -Dtest=ReggieTakeoutApplicationTests

# 打包可执行 JAR
./mvnw package
```

- 启动后访问：`http://localhost:8888/backend/index.html`（管理后台）
- Java 1.8 / Spring Boot 2.4.5 / MyBatis-Plus 3.4.2 / MySQL + Druid
- 上传文件存储目录：`upload/`（相对于项目根目录，已加入 `.gitignore`）
- 需要本地 MySQL 数据库 `reggie`，配置见 `application.yml`

## 项目结构

```
src/main/java/com/wxx/
├── ReggieTakeoutApplication.java   # 启动类（@EnableTransactionManagement）
├── common/                          # 公共组件
│   ├── R.java                       # 统一响应封装（code=1 成功, code=0 失败）
│   ├── Constants.java               # 常量接口（SUCCESS=1, FAIL=0）
│   ├── BaseContext.java             # Session 级用户上下文（RequestContextHolder 实现）
│   ├── BaseContextPlus.java         # ThreadLocal 级用户 ID 持有器（供 MetaObjectHandler 使用）
│   ├── JacksonObjectMapper.java     # Jackson 全局配置（Long→String、日期格式化）
│   └── MyMetaObjectHandler.java     # MyBatis-Plus 自动填充（createTime/updateTime/createUser/updateUser）
├── config/
│   ├── MybatisPlusConfig.java       # MyBatis-Plus 分页插件
│   └── SpringMvcConfig.java         # 静态资源映射 + 消息转换器扩展
├── filter/
│   └── LoginCheckFilter.java        # 登录校验过滤器（放行 /backend、/front、/employee/login）
├── controller/                      # Controller 层（@RestController，接收参数、返回 R<T>）
│   ├── EmployeeController.java      # 员工管理：登录/退出/分页/增改查
│   ├── CategoryController.java      # 分类管理：分页/增改删/列表查询
│   ├── DishController.java          # 菜品管理：分页/增删改查/批量起停售（含口味）
│   └── CommonController.java        # 文件上传（/upload）/ 下载（/download）
├── service/                         # Service 接口 + impl
│   ├── EmployeeService.java         #   → EmployeeServiceImpl
│   ├── CategoryService.java         #   → CategoryServiceImpl（含带业务校验的删除）
│   ├── DishService.java             #   → DishServiceImpl（saveWithFlavors / updateWithFlavors / deleteWithFlavors）
│   ├── DishFlavorService.java       #   → DishFlavorServiceImpl
│   └── SetmealService.java          #   → SetmealServiceImpl
├── mapper/                          # MyBatis-Plus Mapper（继承 BaseMapper）
│   ├── EmployeeMapper.java
│   ├── CategoryMapper.java
│   ├── DishMapper.java
│   ├── DishFlavorMapper.java
│   └── SetmealMapper.java
├── domain/                          # 实体类（Lombok @Data，@TableField 自动填充注解，@TableLogic 逻辑删除）
│   ├── Employee.java                # 员工
│   ├── Category.java                # 分类（type: 1=菜品/2=套餐）
│   ├── Dish.java                    # 菜品（@TableLogic isDeleted）
│   ├── DishFlavor.java              # 菜品口味（@TableLogic isDeleted）
│   ├── Setmeal.java                 # 套餐
│   ├── SetmealDish.java             # 套餐-菜品关联（@TableLogic isDeleted）
│   └── User.java                    # C 端用户
├── dto/
│   └── DishDto.java                 # 菜品 DTO（扩展 Dish，增加 categoryName、flavors）
└── exception/
    ├── BusinessException.java       # 自定义业务异常
    └── GlobalExceptionHandler.java  # 全局异常处理（@ControllerAdvice）
```

```
src/main/resources/
├── application.yml                  # 主配置（端口 8888、数据源、MyBatis-Plus）
├── backend/                         # B 端管理后台（纯静态 HTML/JS/CSS）
│   ├── index.html / pages/          # 登录、员工管理、分类管理、菜品管理等
│   ├── api/                         # axios 请求封装
│   └── js/common.js                 # 公共 JS（含 NOTLOGIN 拦截跳转）
└── front/                           # C 端移动端 H5（Vant UI）
    ├── index.html / page/           # 首页、登录、下单、订单、地址等
    ├── api/                         # axios 请求封装
    └── js/common.js                 # 公共 JS
```

## 核心架构约定

### 分层调用链
```
Controller → Service（接口+实现） → Mapper（MyBatis-Plus BaseMapper）
```
- Controller 只负责参数校验和返回 `R<T>`，业务逻辑在 Service 层
- Service 接口继承 `IService<T>`，实现类继承 `ServiceImpl<Mapper, T>` 自动获得基础 CRUD
- Mapper 继承 `BaseMapper<T>`，无需写 SQL 即可获得单表 CRUD

### 统一响应 `R<T>`
- `R.success(data)` → `{code: 1, data: ...}`
- `R.error(msg)` → `{code: 0, msg: "..."}`
- 前端判断 `code === 1` 为成功，`msg === "NOTLOGIN"` 为未登录

### 用户上下文
- **BaseContext**：基于 `RequestContextHolder` + `HttpSession`，存取当前登录员工 ID
- **BaseContextPlus**：基于 `ThreadLocal`，由 Filter 在 `doFilter` 时设置，供 `MyMetaObjectHandler` 在 insert/update 时自动填充 `createUser`/`updateUser` 字段
- 请求结束后在 Filter 的 `finally` 块中清理 ThreadLocal

### ID 与序列化
- 主键使用雪花算法（`assign_id`），产生的 Long 超出 JS 安全整数范围
- `JacksonObjectMapper` 全局将 `Long` → `String` 序列化，防止前端丢失精度
- 日期格式统一为 `yyyy-MM-dd HH:mm:ss`

### 自动填充
- `createTime`、`createUser`：`INSERT` 时自动填充
- `updateTime`、`updateUser`：`INSERT` + `UPDATE` 时自动填充
- 由 `MyMetaObjectHandler` 实现，无需手动设置

### 静态资源
- `classpath:/backend/` → `http://localhost:8888/backend/`
- `classpath:/front/` → `http://localhost:8888/front/`
- 在 `SpringMvcConfig` 中配置，不走登录过滤器

### 文件上传
- `POST /common/upload` — 接收 `MultipartFile`，UUID 重命名，返回文件名
- `GET /common/download?name=xxx` — 文件流预览
- 上传路径 `upload/`（相对路径，启动时转为绝对路径）
- 配置文件限制：单文件 10MB，单次请求 100MB

### 主表与从表约定
- 外键字段在 N 端（从表），逻辑上维护关联但不建物理外键约束
- 一对多关系中，删除主表时由 Service 层确保级联删除从表数据
- 例：`dish`（主表）→ `dish_flavor`（从表），删除菜品时同步删除口味

### 事务
- 启动类标注 `@EnableTransactionManagement` 启用声明式事务
- 涉及多表写操作的方法标注 `@Transactional`（如菜品保存/修改/删除）

### 注意事项
- 新增 Service/Mapper/Controller 遵循已有模式：接口继承 `IService<T>` / `BaseMapper<T>`，实现类继承 `ServiceImpl<M, T>`
- 删除操作如有业务关联校验（如分类删除前检查菜品引用），在 Service 层抛 `BusinessException`
- 所有实体类统一使用 `Long` 类型雪花 ID、`LocalDateTime` 时间类型、Lombok `@Data`
- 前端页面为纯静态 HTML，通过 axios 调用后端 API