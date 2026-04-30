# 启动后端步骤

## 方法一：使用 IDEA（推荐）

1. **打开项目**
   - 在 IDEA 中打开 `/Users/al/Desktop/Programme/workspace/trace`

2. **配置 JDK**
   - File → Project Structure → SDKs
   - 确保使用 JDK 17

3. **编译项目**
   - Build → Build Project (或按 Cmd+F9)
   - IDEA 会自动下载 Lombok 插件并处理注解

4. **运行服务**
   - 找到 `core-service/src/main/java/com/ncg/Main.java`
   - 右键 → Run 'Main.main()'

5. **验证启动**
   ```
   ===========================================
   食品安全溯源系统 - Core Service 启动成功！
   访问地址：http://localhost:8081
   登录接口：POST /api/auth/login
   ===========================================
   ```

## 方法二：使用 Maven 命令

```bash
cd /Users/al/Desktop/Programme/workspace/trace/core-service
mvn spring-boot:run
```

## 前提条件

### 1. MySQL 服务
```bash
mysql.server start
# 或
sudo systemctl start mysqld
```

创建数据库：
```sql
CREATE DATABASE trace_food CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行建表脚本：
```bash
mysql -u root -p trace_food < src/main/resources/sql/schema.sql
```

### 2. Redis 服务
```bash
redis-server
```

验证：
```bash
redis-cli ping
# 应返回：PONG
```

## 测试数据录入

启动成功后，运行测试类：
- 定位到：`core-service/src/test/java/com/ncg/service/DataEntryServiceTest.java`
- 右键 → Run 'DataEntryServiceTest'

预期输出：
```
✅ 测试用户已自动创建：admin/123456, supervisor01/123456
✅ 录入成功！批次 ID: 1
生成预警：TEST20260324002, 类型：COMPOSITE, 风险分：58
```

## 常见问题

### 1. Lombok 爆红
- IDEA: Preferences → Plugins → 安装 Lombok 插件
- 重启 IDEA

### 2. Maven 依赖下载失败
```bash
mvn dependency:purge-local-repository
mvn clean install -U
```

### 3. 端口被占用
修改 `application.yml`:
```yaml
server:
  port: 8081  # 改为其他端口
```
