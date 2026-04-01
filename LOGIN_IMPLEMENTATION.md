# 登录功能实现说明

## 📋 已完成的工作

### 一、后端实现（core-service）

#### 1. **工具类**
- ✅ [`JwtUtil.java`](file:///Users/al/Desktop/Programme/workspace/trace/core-service/src/main/java/com/ncg/util/JwtUtil.java) - JWT Token 生成与验证工具
  - 生成 Token（有效期 2 小时）
  - 验证 Token 有效性
  - 刷新 Token
  - 从 Token 提取用户名和角色

#### 2. **DTO 类**
- ✅ [`LoginRequest.java`](file:///Users/al/Desktop/Programme/workspace/trace/core-service/src/main/java/com/ncg/dto/LoginRequest.java) - 登录请求参数
- ✅ [`LoginResponse.java`](file:///Users/al/Desktop/Programme/workspace/trace/core-service/src/main/java/com/ncg/dto/LoginResponse.java) - 登录响应（Token + 角色）

#### 3. **服务层**
- ✅ [`AuthService.java`](file:///Users/al/Desktop/Programme/workspace/trace/core-service/src/main/java/com/ncg/service/AuthService.java) - 认证服务
  - 用户登录（查询用户 + 验证密码 + 生成 Token）
  - 用户登出（Redis 中使 Token 失效）
  - Token 刷新
  - BCrypt 密码加密

#### 4. **Controller 层**
- ✅ [`AuthController.java`](file:///Users/al/Desktop/Programme/workspace/trace/core-service/src/main/java/com/ncg/web/controller/AuthController.java) - 认证接口
  - `POST /api/auth/login` - 用户登录
  - `POST /api/auth/logout` - 用户登出
  - `POST /api/auth/refresh` - 刷新 Token

#### 5. **配置文件**
- ✅ [`application.yml`](file:///Users/al/Desktop/Programme/workspace/trace/core-service/src/main/resources/application.yml) - Spring Boot 配置
  - 端口：8081
  - MySQL 连接配置
  - Redis 连接配置
  - JWT 密钥和过期时间配置

---

### 二、前端实现（track-frontend）

#### 1. **项目结构**
```
track-frontend/
├── src/
│   ├── api/
│   │   ├── request.ts      # Axios 封装（拦截器）
│   │   └── auth.ts         # 登录相关 API
│   ├── router/
│   │   └── index.ts        # Vue Router 路由配置
│   ├── stores/
│   │   └── user.ts         # Pinia 用户状态管理
│   ├── views/
│   │   ├── Login.vue       # 登录页面
│   │   └── Dashboard.vue   # 首页（登录后）
│   └── main.js             # 入口文件
├── package.json
└── vite.config.js          # Vite 配置
```

#### 2. **核心功能**
- ✅ **Pinia Store** - 用户状态管理
  - Token、角色、用户名持久化（localStorage）
  - 登录/登出方法
  - 权限检查方法

- ✅ **Axios 封装**
  - 请求拦截器（自动添加 Token）
  - 响应拦截器（统一错误处理）
  - 401 自动跳转登录

- ✅ **路由守卫**
  - 未登录访问需要认证的页面 → 跳转登录
  - 已登录访问登录页 → 跳转首页

- ✅ **登录页面**
  - Element Plus UI 组件
  - 表单验证（用户名 3-20 字符，密码 6-20 字符）
  - 渐变背景 + 卡片式设计
  - 测试账号提示

- ✅ **Dashboard 页面**
  - 显示用户信息
  - 角色标签（管理员/监管员）
  - 退出登录功能

---

## 🚀 使用说明

### 1. 数据库准备

执行 SQL 脚本创建数据库和表：
```bash
mysql -u root -p < core-service/src/main/resources/sql/schema.sql
```

或手动执行 [`schema.sql`](file:///Users/al/Desktop/Programme/workspace/trace/core-service/src/main/resources/sql/schema.sql) 中的 SQL 语句。

**测试账号**（已内置 BCrypt 加密密码 `123456`）：
- 管理员：`admin` / `123456`
- 监管员：`supervisor01` / `123456`

### 2. 启动后端（core-service）

```bash
cd /Users/al/Desktop/Programme/workspace/trace/core-service
mvn clean install
mvn spring-boot:run
```

或直接运行 `Main.java`

**访问地址**：http://localhost:8081

### 3. 启动前端（track-frontend）

```bash
cd /Users/al/Desktop/Programme/workspace/trace/track-frontend
npm install
npm run dev
```

**访问地址**：http://localhost:3000

---

## 🎯 功能演示

### 登录流程
1. 打开 http://localhost:3000
2. 输入用户名和密码
3. 点击"登录"按钮
4. 成功后跳转到 Dashboard 页面
5. Token 保存在 localStorage

### 登出流程
1. 点击右上角"退出登录"按钮
2. 确认后调用后端登出 API
3. 清除本地 Token 和状态
4. 跳转回登录页

### 自动登录
- 刷新页面时，从 localStorage 读取 Token
- Pinia Store 自动恢复用户状态
- 可直接访问 Dashboard 页面

---

## 🔧 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3.1
- JWT (io.jsonwebtoken)
- Spring Security Crypto (BCrypt 密码加密)
- Redis（Token 存储）

### 前端
- Vue 3.5.30
- Vite 8.0.1
- Pinia 3.0.4（状态管理）
- Vue Router 4.5.1（路由）
- Element Plus 2.10.7（UI 组件库）
- Axios 1.10.0（HTTP 请求）

---

## 📝 下一步建议

1. **完善 Dashboard 页面** - 添加 ECharts 大屏可视化
2. **实现数据模拟定时任务** - 自动生成食品批次、检测、物流数据
3. **集成风险评分算法** - 在 Controller 中调用 RiskScoring 服务
4. **WebSocket 实时推送** - 实现 STOMP 协议推送预警消息
5. **完善权限控制** - 根据角色动态加载菜单和按钮权限

---

## ⚠️ 注意事项

1. **Node.js 版本**：建议升级到 20.19+ 或 22.12+（当前 Vite 8 要求）
2. **MySQL 版本**：8.0+
3. **Redis 版本**：7.0+
4. **端口占用**：确保 8081（后端）和 3000（前端）未被占用
5. **跨域问题**：已通过 Vite 代理解决开发环境跨域

---

## 🎉 测试截图

### 登录页面
- 渐变紫色背景
- 白色卡片式表单
- 用户名/密码输入框
- 测试账号提示

### Dashboard 页面
- 顶部导航栏
- 用户信息和角色标签
- 欢迎卡片
- 退出登录按钮
