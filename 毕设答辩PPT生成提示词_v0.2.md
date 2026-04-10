# 毕业设计答辩 PPT 生成提示词 v0.2

> 本文件是给 PPT 生成 AI 的完整输入提示词。将其完整复制给生成 AI（如 Gamma、Beautiful.ai、通义听悟、MindShow、腾讯文档PPT等），AI即可按照要求制作答辩PPT。
>
> **使用方式：** 复制以下全部内容 → 粘贴到 AI 输入框 → 等待生成

---

## 一、PPT基本信息

- **主题：** 基于SpringBoot的食品安全溯源系统的设计与实现
- **学生姓名：** 刘勇
- **学号：** 221040100211
- **专业班级：** 计算机科学与技术辅修2022级
- **指导老师：** 唐建国（副教授）
- **学院：** 信息科学与工程学院
- **答辩日期：** 2026年1月（根据论文封面填写）
- **PPT总页数：** 约20页
- **配色建议：** 深蓝/科技蓝（#0a2e5c / #1a5a96）为主色，白色背景为底，辅以绿色（#6bcb77）表示安全、红色（#ff6b6b）表示预警、橙色（#ffd93d）表示中风险
- **风格：** 学术答辩风格，简洁专业，图表丰富，不要过度花哨

---

## 二、PPT完整结构与每页内容要求

### 第1页：封面页

**标题：** 基于SpringBoot的食品安全溯源系统的设计与实现

**副标题/正文内容（居中排列）：**
- 信息科学与工程学院
- 计算机科学与技术辅修2022级
- 学生姓名：刘勇
- 学号：221040100211
- 指导教师：唐建国（副教授）
- 答辩日期：[填入实际答辩日期]

**设计要求：** 深蓝色渐变背景，标题白色大字居中，副标题信息整齐排列，可加装饰线条

---

### 第2页：目录页

**标题：** 目录 / Contents

**章节列表：**
1. 研究背景与意义
2. 关键技术综述
3. 需求分析与系统设计
4. 系统实现
5. 总结与展望

**设计建议：** 每个章节配序号和简短图标，排列整齐

---

### 第3页：章节页——研究背景与意义

**标题：** 01 研究背景与意义

**设计要求：** 独立章节页，大字标题居中，可用半透明深蓝色块或左侧色条装饰

---

### 第4页：研究背景

**标题：** 研究背景

**页面内容（建议用左右分栏，左文右图）：**

**核心问题：**
- 食品安全是关系国计民生的重大公共安全问题
- 全国超68%的食品安全事件源于：源头信息缺失、物流温控失准、检测数据滞后、风险识别能力薄弱
- 问题批次定位平均耗时超过48小时，严重制约应急响应效率

**传统方案痛点：**
- 纸质台账：信息割裂，难以追溯
- 区块链方案：部署成本高（单节点¥2000+/月），运维复杂，与Java企业栈存在技术鸿沟

**政策驱动：**
- 国务院《"十四五"数字经济发展规划》明确要求推进食品安全智慧监管

**本页图片预留位置：** [图1] 食品安全监管/溯源相关场景图片（来自网络，标注来源即可）

---

### 第5页：项目目的与意义

**标题：** 项目目的与意义

**四项目标（建议用图标+卡片布局，2×2网格）：**

| 目标 | 核心内容 |
|---|---|
| 架构轻量化 | SpringBoot单体架构，单台云服务器即可部署，零授权费用 |
| 数据自动化 | 批次录入+数据模拟，数据清洗流水线自动处理 |
| 分析智能化 | 3σ统计异常检测 + 加权规则风险评分，所有阈值参数可动态配置 |
| 交互实时化 | WebSocket服务端主动推送，监管大屏数据无刷新自动更新 |

**本页图片预留位置：** [图2] 项目目录结构截图（core-service + frontend 两层目录）

---

### 第6页：章节页——关键技术综述

**标题：** 02 关键技术综述

**设计要求：** 独立章节页，样式与第3页保持一致

---

### 第7页：系统技术栈

**标题：** 系统技术栈

**建议用分层表格或四层架构图展示：**

| 层级 | 技术选型 | 说明 |
|---|---|---|
| 后端框架 | SpringBoot 2.7 + MyBatis-Plus 3.5 | 单体架构，简化部署 |
| 数据存储 | MySQL 8.0 + Redis 7.0 | 主数据库 + 配置缓存层 |
| 实时通信 | Spring WebSocket（原生） | 服务端主动推送，非STOMP |
| 前端框架 | Vue3 + Vite 5 + Element Plus 2 | Composition API |
| 可视化 | ECharts 5.4 | 仪表盘、饼图、柱状图、折线图 |
| 认证鉴权 | JWT + BCrypt | 无状态Token认证 |
| 任务调度 | Spring TaskScheduler | 动态调度，无需重启 |

**本页图片预留位置：** [图3] 项目技术栈相关截图（如 pom.xml 依赖截图或 package.json 截图）

---

### 第8页：WebSocket实时通信技术

**标题：** 核心技术：WebSocket实时通信

**页面内容（建议用时序图+文字说明的左右分栏）：**

**左栏：技术原理**
- WebSocket通过单次握手建立全双工长连接，服务端可主动向浏览器推送数据
- 本系统采用Spring原生WebSocket，端点：`/ws/data-change`
- 后端以`CopyOnWriteArraySet<WebSocketSession>`管理所有在线会话
- `broadcast()`遍历所有会话发送JSON消息，自动移除发送失败的异常会话
- 推送消息格式：`{ type: "OVERVIEW_UPDATE", data: {...}, timestamp: "..." }`

**右栏：前端订阅-发布模式**
- `ws.js`封装WebSocket客户端，核心方法：
  - `on(type, handler)`：订阅指定类型消息
  - `emit(type, data)`：根据type分发给对应处理器
- 内置指数退避重连：1s → 2s → 5s → 10s → 30s
- `onUnmounted`时主动关闭，`manualClose`标志防止误触发重连

**推送触发场景：** 批次录入完成、定时清洗完成、新预警生成

**本页图片预留位置：** [图4] Chrome DevTools Network面板 ws连接截图，或运行时WebSocket推送效果截图

---

### 第9页：章节页——需求分析与系统设计

**标题：** 03 需求分析与系统设计

**设计要求：** 独立章节页，样式与第3、6页保持一致

---

### 第10页：系统需求分析

**标题：** 系统需求分析

**左侧：系统用例图（建议用简化的用例图或功能列表）：**

| 用户角色 | 可用功能 |
|---|---|
| 监管员 | 登录、大盘概览、批次录入、批次查询、预警查看、预警处理、溯源链查询、数据模拟 |
| 管理员 | 以上全部 + 系统参数配置、操作日志查看、用户管理、定时清洗开关 |

**右侧：核心业务流程（3条关键链路）：**

**链路1：批次录入**
监管人员录入批次基础信息、检测数据、物流轨迹 → 数据写入MySQL → 触发清洗流水线 → 3σ异常检测+风险评分 → 结果落库 → WebSocket推送 → 前端大盘自动刷新

**链路2：预警处置**
监管人员查看预警列表 → 按类型/状态过滤 → 点击处理 → 系统标记`handled=1` → 预警列表刷新

**链路3：参数动态配置**
管理员修改参数配置 → MySQL+Redis同步更新 → ConfigService发布-订阅通知 → TaskScheduler立即响应 → 清洗任务以新间隔执行

**非功能需求：** 实时性≤1s，并发≥100，WebSocket消息延迟<500ms

---

### 第11页：系统总体架构

**标题：** 系统总体架构

**页面内容：用分层架构图展示，每层注明该层用到的技术及其交互方式**

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          前端层（Vue3 SPA + Element Plus）                     │
│                                                                              │
│  技术栈：Vue3 Composition API + Vite 5 + Element Plus 2 + ECharts 5.4        │
│  组件：Overview / AlertDashboard / BatchEntry / BatchQuery / TraceChain       │
│       / Config / Simulation / OperationLog / UserManagement / Login          │
│  状态管理：Pinia（Token、用户信息）                                          │
│  HTTP请求：Axios拦截器（携带Token / 401拦截跳转登录）                          │
│  实时通信：ws.js（订阅-发布 + 指数退避重连）                                  │
│                                                                              │
│  ┌─────────────────── HTTP REST API（/api/...） ──────────────────────────┐ │
│  │         WebSocket连接（ws://host/ws/data-change）                      │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     接口层（@RestController + TextWebSocketHandler）            │
│                                                                              │
│  技术栈：SpringBoot Web（@RestController）+ spring-boot-starter-websocket      │
│  控制器：AuthController / BatchController / AlertController / ConfigController │
│         / OverviewController / TraceController / SimulationController        │
│         / CleanController / OperationLogController / UserController          │
│  WebSocket：DataChangeWebSocketHandler（管理CopyOnWriteArraySet<Session>）    │
│  交互方式：@RestController接收HTTP请求 → 调用Service层处理 → 返回JSON响应     │
│            TextWebSocketHandler接收WebSocket升级 → 管理会话 → 广播消息        │
│                                                                              │
│  ┌─────────────────────── 调用Service层 ──────────────────────────────────┐ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           业务层（Service类）                                │
│                                                                              │
│  技术栈：Spring @Service + Spring TaskScheduler + Spring AOP                   │
│                                                                              │
│  【数据管理】                                                                │
│    BatchService：批次CRUD（录入/编辑/删除/查询）                             │
│    TraceService：溯源链聚合（批次+检测+物流+风险+预警）                       │
│    SimulationService：模拟批次生成                                           │
│                                                                              │
│  【分析引擎】                                                                │
│    DataCleanService：清洗流水线入口（串联检测+异常+评分+落库）                 │
│    StatisticalAnomalyDetector：3σ统计异常检测（纯Java实现）                  │
│    RiskScoring：加权规则风险评分（0-100分，Low/Medium/High）                  │
│                                                                              │
│  【预警与大盘】                                                              │
│    AlertService：预警查询/统计/处理状态管理                                   │
│    OverviewService：大盘聚合（批次总数/预警数/平均温湿度/趋势）                │
│                                                                              │
│  【配置与调度】                                                              │
│    ConfigService：配置CRUD + Redis缓存 + 发布-订阅监听（addListener）         │
│    DataCleanScheduler：TaskScheduler动态调度（监听配置变更）                  │
│                                                                              │
│  【实时通信】                                                                │
│    DataChangeNotifier：WebSocket广播门面类（pushOverviewUpdate/pushAlertNew）│
│                                                                              │
│  【审计】                                                                    │
│    OperationLogAspect：AOP切面自动采集操作日志                               │
│                                                                              │
│  交互方式：Controller调用Service → Service间互相调用 → 结果返回Controller      │
│            ConfigService.notifyListeners() → DataCleanScheduler响应           │
│                                                                              │
│  ┌─────────── 调用Mapper层（MyBatis-Plus） ─── 调用RedisTemplate ──────────┐ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                    持久层（MyBatis-Plus + RedisTemplate）                     │
│                                                                              │
│  MyBatis-Plus 3.5                                                            │
│    IServcie<T> + BaseMapper<T> 双层封装                                      │
│    LambdaQueryWrapper（Lambda条件构造器）                                     │
│    @TableName / @TableField 注解映射                                          │
│                                                                              │
│  RedisTemplate（StringRedisTemplate）                                        │
│    配置缓存：config:{param_key} → TTL 30分钟                                  │
│    Token黑名单：logout时写入，过期时间=Token有效期                            │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          数据存储层                                          │
│                                                                              │
│  MySQL 8.0（主数据库）：user_info / batch_info / detection_data /           │
│                          logistics_data / alert_record /                     │
│                          risk_assessment / config_param / operation_log      │
│                                                                              │
│  Redis 7.0（缓存）：配置参数 + Token黑名单                                   │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

**本页图片预留位置：** [图5] 可插入实际项目代码目录截图（展示core-service和frontend的包结构/目录结构）

---

### 第12页：功能模块总览

**标题：** 功能模块总览

**页面内容（建议用功能架构树或2×4卡片网格）：**

系统划分为7大功能模块：

```
食品安全溯源系统
│
├── 模块一：用户认证
│   └── JWT登录/注册，BCrypt密码比对，admin/supervisor角色权限控制
│
├── 模块二：批次管理
│   ├── 批次录入（基础信息+检测数据+物流轨迹）
│   ├── 批次查询（多条件筛选+编辑+删除）
│   └── 溯源链查询（时间线聚合展示）
│
├── 模块三：数据清洗与智能分析
│   ├── StatisticalAnomalyDetector：3σ统计异常检测
│   └── RiskScoring：加权规则风险评分（0-100分）
│
├── 模块四：预警管理
│   ├── 预警大盘（处理率/等级分布/7天趋势）
│   └── 预警列表（类型筛选/一键处理）
│
├── 模块五：监管大屏
│   └── 大盘概览（5个统计卡片+5个ECharts图表）
│
├── 模块六：系统参数配置
│   └── ConfigService三层架构（Redis缓存+DB持久+发布订阅监听）
│
└── 模块七：操作日志审计
    └── AOP切面自动采集，异步写入数据库
```

**本页图片预留位置：** [图6] 系统侧边导航菜单运行截图
> 文件路径：`frontend/src/components/Sidebar.vue`（运行效果）
> 截图范围：左侧菜单树（大盘概览/批次管理/预警管理/参数配置等）

---

### 第13页：数据库设计（第一页）

**标题：** 数据库设计（一）

**页面内容：8张核心数据表详细说明（表格布局）：**

| 表名 | 说明 | 核心字段 |
|---|---|---|
| user_info | 用户认证表 | id(PK), username(UK), password(BCrypt), role(admin/supervisor) |
| batch_info | 食品批次主表 | id(PK), batch_no(UK), origin, production_date, enterprise, cleaned(0未清洗/1已清洗) |
| detection_data | 检测数据表 | id(PK), batch_id(FK), pesticide, heavy_metal, microbe, test_time |
| logistics_data | 物流轨迹表 | id(PK), batch_id(FK), gps_lng, gps_lat, temperature, humidity, record_time |
| alert_record | 预警记录表 | id(PK), batch_id(FK), alert_type(TEMP/HUMIDITY/PESTICIDE/HEAVY_METAL/MICROBE/COMPOSITE), risk_score, handled, create_time |
| risk_assessment | 风险评估表 | id(PK), batch_id(FK), risk_level(Low/Medium/High), risk_score(0-100), factors(JSON), assessment_date |
| config_param | 系统参数表 | id(PK), param_key(UK), param_value, param_group(风险评分/异常检测/预警阈值/定时清洗), editable |
| operation_log | 操作日志表 | id(PK), username, operation_type(新增/修改/删除), module, description, status, ip_address, create_time |

**关键设计说明：**
- password字段长度设为100，兼容BCrypt哈希值（60字符）
- batch_no和username设置唯一索引（UK），保障业务主键唯一性
- alert_record.handled用TINYINT存储（0=未处理，1=已处理）
- risk_assessment.factors存JSON格式，归因分析明细便于溯源

**本页图片预留位置：** [图7] 数据库工具截图（Navicat / DBeaver / HeidiSQL 截图，展示8张表的列表或关系视图）

---

### 第14页：数据库设计（第二页）——E-R图

**标题：** 数据库设计（二）——E-R关系图

**页面内容：** 直接使用以下Mermaid代码生成E-R图

> 如果你的PPT工具支持Mermaid，直接复制以下代码即可渲染：
> 如果不支持，请根据下方文字描述手绘或用PPT形状工具绘制：

```
erDiagram
    USER ||--o{ OPERATION_LOG : "操作"
    USER {
        bigint id PK "用户ID"
        varchar username UK "用户名（唯一）"
        varchar password "BCrypt加密密码"
        varchar role "角色：admin/supervisor"
        tinyint status "状态：0禁用/1启用"
        datetime create_time "创建时间"
    }
    OPERATION_LOG {
        bigint id PK "日志ID"
        varchar username "操作用户"
        varchar operation_type "操作类型：新增/修改/删除"
        varchar module "所属模块"
        text description "操作描述"
        varchar status "操作状态"
        varchar ip_address "IP地址"
        datetime create_time "操作时间"
    }
    
    BATCH ||--o{ DETECTION_DATA : "包含"
    BATCH ||--o{ LOGISTICS_DATA : "包含"
    BATCH ||--o{ ALERT_RECORD : "触发"
    BATCH ||--o{ RISK_ASSESSMENT : "生成"
    BATCH {
        bigint id PK "批次ID"
        varchar batch_no UK "批次编号（唯一）"
        varchar origin "产地"
        varchar enterprise "所属企业"
        date production_date "生产日期"
        tinyint cleaned "清洗标记：0未清洗/1已清洗"
        datetime create_time "录入时间"
    }
    DETECTION_DATA {
        bigint id PK "检测ID"
        bigint batch_id FK "关联批次"
        decimal pesticide "农残值（mg/kg）"
        decimal heavy_metal "重金属值（mg/kg）"
        decimal microbe "微生物指标（CFU/g）"
        datetime test_time "检测时间"
    }
    LOGISTICS_DATA {
        bigint id PK "物流ID"
        bigint batch_id FK "关联批次"
        decimal gps_lng "GPS经度"
        decimal gps_lat "GPS纬度"
        decimal temperature "温度（℃）"
        decimal humidity "湿度（%）"
        datetime record_time "记录时间"
    }
    ALERT_RECORD {
        bigint id PK "预警ID"
        bigint batch_id FK "关联批次"
        varchar alert_type "预警类型"
        decimal risk_score "风险评分"
        tinyint handled "处理状态：0未处理/1已处理"
        datetime create_time "预警时间"
    }
    RISK_ASSESSMENT {
        bigint id PK "评估ID"
        bigint batch_id FK "关联批次"
        varchar risk_level "风险等级：Low/Medium/High"
        decimal risk_score "综合评分（0-100）"
        json factors "归因明细（JSON）"
        datetime assessment_date "评估时间"
    }
    
    CONFIG_PARAM ||--o{ PARAM_CHANGE : "变更记录"
    CONFIG_PARAM {
        bigint id PK "参数ID"
        varchar param_key UK "参数标识（唯一）"
        varchar param_value "参数值"
        varchar param_type "参数类型"
        varchar param_group "所属分组"
        tinyint editable "是否可编辑：0只读/1可编辑"
        datetime update_time "更新时间"
    }
```

**E-R图关系说明（供手绘参考）：**
- USER（用户） 1:N OPERATION_LOG（操作日志）：一个用户可产生多条操作日志
- BATCH（批次） 1:N DETECTION_DATA（检测数据）：一个批次对应多条检测记录
- BATCH（批次） 1:N LOGISTICS_DATA（物流轨迹）：一个批次对应多条物流记录
- BATCH（批次） 1:N ALERT_RECORD（预警记录）：一个批次可触发多条预警
- BATCH（批次） 1:1 RISK_ASSESSMENT（风险评估）：一个批次对应一条风险评估结果

---

### 第15页：章节页——系统实现

**标题：** 04 系统实现

**设计要求：** 独立章节页，样式与前面章节页保持一致

---

### 第16页：模块一——数据清洗与智能分析（第一页：流水线与核心代码）

**标题：** 模块一：数据清洗与智能分析（流水线架构）

**页面内容（建议用流程图+代码框左右分栏）：**

**左侧/上部：数据清洗流水线完整流程图（文字描述）：**

批次录入触发 → 读取检测数据+物流数据 → **StatisticalAnomalyDetector**（3σ异常检测）→ **RiskScoring**（加权风险评分）→ 落库 `risk_assessment` → 生成预警记录 `alert_record`（条件触发）→ 标记 `batch_info.cleaned=1` → **DataChangeNotifier**（WebSocket推送大盘数据）→ 前端图表自动刷新

**右侧/下部：DataCleanService.cleanBatch() 核心代码框架：**

```java
public void cleanBatch(Long batchId) {
    // 1. 读取检测数据和物流轨迹
    DetectionData detection = detectionDataMapper.selectLatestByBatchId(batchId);
    List<LogisticsData> logistics = logisticsDataMapper.selectByBatchId(batchId);

    // 2. 3σ 统计异常检测
    double tempAnomaly = anomalyDetector.detectTemperatureAnomaly(logistics);
    double humAnomaly  = anomalyDetector.detectHumidityAnomaly(logistics);

    // 3. 加权规则风险评分
    RiskScoringResult result = riskScoring.calculateScore(detection, logistics);

    // 4. 落库 risk_assessment
    RiskAssessment assessment = new RiskAssessment();
    assessment.setBatchId(batchId);
    assessment.setRiskLevel(result.getLevel());   // Low/Medium/High
    assessment.setRiskScore(result.getScore());   // 0-100
    assessment.setFactors(toJson(result.getFactors()));
    riskAssessmentMapper.insert(assessment);

    // 5. 条件触发预警
    if (tempAnomaly > threshold || result.getScore() > alertThreshold) {
        AlertRecord alert = new AlertRecord();
        alert.setBatchId(batchId);
        alert.setAlertType(determineAlertType(tempAnomaly, result));
        alert.setRiskScore(BigDecimal.valueOf(result.getScore()));
        alertRecordMapper.insert(alert);
    }

    // 6. 标记批次已清洗
    batchInfoMapper.updateCleaned(batchId, 1);
}
```

**清洗触发方式：** 批次录入时同步触发 + DataCleanScheduler定时扫描未清洗批次（`cleaned=0`）

---

### 第17页：模块一——数据清洗与智能分析（第二页：算法详解）

**标题：** 模块一：数据清洗与智能分析（算法详解）

**页面内容（建议左右分栏：左侧算法公式+右侧代码）：**

**左侧一：3σ统计异常检测算法（StatisticalAnomalyDetector）**

- 原理：基于**拉依达准则（3σ原则）**，计算样本均值与样本标准差
- 公式：
  - 样本均值：x̄ = Σxᵢ / n
  - 样本标准差：s = √[ Σ(xᵢ - x̄)² / (n-1) ]
  - 偏离度：d = |xᵢ - x̄| / s
  - 异常分数 = min(1, max(0, (d - 2) / 3 × 0.4 + 0.3))
- 分数映射：0-2σ → 0~0.3（正常）；2-3σ → 0.3~0.7（预警）；>3σ → 0.7~1.0（异常）
- 所有σ系数从`config_param`表动态读取（`anomaly.temp.sigma`、`anomaly.humidity.sigma`）

**核心代码：**
```java
public double calculateAnomalyScore(List<LogisticsData> records, double value) {
    if (records.size() < 2) return 0.0;
    double mean = calculateMean(records);        // 计算样本均值
    double std  = calculateStd(records, mean);   // 计算样本标准差
    if (std < 0.001) return 0.0;                // 方差过小视为无波动
    double deviation = Math.abs(value - mean) / std;
    // 0-2σ: 0~0.3 | 2-3σ: 0.3~0.7 | >3σ: 0.7~1.0
    double score = Math.min(1.0, Math.max(0.0, (deviation - 2.0) / 3.0 * 0.4 + 0.3));
    return score;
}
```

**右侧二：加权规则风险评分算法（RiskScoring）**

- 检测维度（占总分的70%）：农残×35% + 重金属×35% + 微生物×30%
- 物流维度（占总分的30%）：温度×60% + 湿度×40%
- 超标型指标分段线性评分：超标<1倍→0-60分；1-3倍→60-90分；>3倍→91-100分
- 最终评分 = Σ(检测分×权重)×0.7 + Σ(物流分×权重)×0.3
- 风险等级判定：0-30分→Low；31-70分→Medium；71-100分→High
- 所有权重和国标阈值均从`config_param`表动态读取

**核心代码：**
```java
// 权重从配置表动态读取
BigDecimal pesticideW = configService.getNumericValue("risk.weight.pesticide", new BigDecimal("0.35"));
BigDecimal heavyMetalW = configService.getNumericValue("risk.weight.heavy_metal", new BigDecimal("0.35"));
BigDecimal microbeW = configService.getNumericValue("risk.weight.microbe", new BigDecimal("0.30"));
BigDecimal tempW = configService.getNumericValue("risk.weight.temp", new BigDecimal("0.60"));
BigDecimal humW = configService.getNumericValue("risk.weight.humidity", new BigDecimal("0.40"));

// 检测指标分段线性评分
BigDecimal score = (pesticideScore.multiply(pesticideW))
    .add(heavyMetalScore.multiply(heavyMetalW))
    .add(microbeScore.multiply(microbeW))
    .multiply(new BigDecimal("0.7"))   // 检测维度占70%
    .add(logisticsScore.multiply(new BigDecimal("0.3"))); // 物流维度占30%
```

---

### 第18页：模块二——批次管理与溯源链

**标题：** 模块二：批次管理

**页面内容（建议上下分栏：流程说明+溯源链描述）：**

**批次录入流程（左侧或上部）：**

`BatchController.createBatch()` 在事务内依次执行：
1. `batchInfoMapper.insert(batch)` — 批次主表
2. `detectionDataMapper.insert(detection)` — 检测数据
3. `logisticsDataMapper.insertBatch(logisticsList)` — 物流轨迹（支持多条）
4. 批次号服务端重复性校验（返回409 Conflict）
5. 录入成功后自动触发 `DataCleanService.cleanBatch(batchId)`

**溯源链查询（右侧或下部）：**

`TraceService.getTraceChain(batchId)` 以批次为中心聚合全量数据：

| 环节 | 数据内容 |
|---|---|
| 批次基础 | 批次号、产地、企业、生产日期 |
| 检测环节 | 农残/重金属/微生物值（超标项红色高亮） |
| 物流轨迹 | GPS/温度/湿度记录（温湿度异常行红色高亮） |
| 风险评估 | 圆环分数（0-100）+ JSON归因明细 |
| 预警记录 | 预警类型+时间+处理状态 |

**系统提供两种溯源入口：**
- 批次查询页 → 点击批次号跳转溯源链
- 溯源链页面 → 下拉框远程搜索批次

**本页图片预留位置：**
- [图8] 批次录入页面截图
  > 文件路径：`frontend/src/views/BatchEntry.vue`
  > 截图范围：三个表单区块（基础信息/检测数据/物流轨迹）
  > 关键文字：`批次录入`、`完整录入批次信息，包括基础信息、检测数据与物流轨迹`
- [图9] 溯源链页面截图
  > 文件路径：`frontend/src/views/TraceChain.vue`
  > 截图范围：批次选择器 + 溯源时间线（检测→物流→风险→预警）

---

### 第19页：模块三——预警管理与WebSocket推送

**标题：** 模块三：预警管理

**页面内容（建议上下分栏）：**

**预警大盘（AlertDashboard.vue）：**
| 组件 | 说明 |
|---|---|
| 环形进度卡 | 预警处理率 = 已处理数 / 总数 |
| 处理时效卡 | 今日处理 / 本周处理 / 本月处理 / 平均处理时长 |
| 等级分布进度条 | 紧急（≥0.8）/ 重要（0.5-0.8）/ 一般（<0.5） |
| 近7天趋势柱状图 | 每日预警数量统计 |
| TOP 5预警批次 | 风险评分最高的5个批次 |

**预警列表（AlertList.vue）：**
- 筛选条件：预警类型（温度/湿度/农残/重金属/微生物/综合）+ 处理状态（已处理/未处理）
- 统计概览条：`当前筛选结果：X条，其中X条未处理`
- 表格列：批次号（可点击跳转溯源链）/ 产地 / 企业 / 预警类型(el-tag) / 风险分 / 预警时间 / 处理状态(el-tag) / 操作
- 处理按钮仅对未处理预警可见

**WebSocket推送效果：**
- 批次录入 → 清洗完成 → `DataChangeNotifier.pushOverviewUpdate()` → 所有在线客户端收到 `OVERVIEW_UPDATE` → 图表数据自动刷新
- 预警生成 → `pushAlertNew()` → 预警列表新增行

**本页图片预留位置：**
- [图10] 预警大盘截图
  > 文件路径：`frontend/src/views/AlertDashboard.vue`
  > 截图范围：环形处理率 + 等级分布进度条 + 7天趋势柱状图 + TOP5预警批次
  > 关键文字：`预警大盘`、`预警处理追踪与批次关联分析`
- [图11] 预警列表截图
  > 文件路径：`frontend/src/views/AlertList.vue`
  > 截图范围：筛选栏 + 表格列表 + 处理按钮
  > 关键文字：`预警列表`、`查看所有预警记录`

---

### 第20页：模块四——监管大屏

**标题：** 模块四：监管大屏

**页面内容（建议用模拟界面示意图或分区描述）：**

**Overview.vue 页面布局（从上到下）：**

**顶部标题区：**
- 页面标题：`大盘概览`
- 副标题：`多维度聚合数据可视化，实时监控溯源系统运行状态`

**顶部5个统计卡片（横向排列）：**
| 卡片 | 显示内容 |
|---|---|
| 批次总数 | totalBatches |
| 预警总数 | totalAlerts |
| 待处理预警 | unhandledAlerts |
| 平均温度 | avgTemperature + °C |
| 平均湿度 | avgHumidity + % |

**中部图表区（3列布局）：**
| 图表类型 | 说明 |
|---|---|
| 风险等级分布（饼图） | Low绿色 / Medium橙色 / High红色 |
| 风险评分分布（柱状图） | 0-30/31-70/71-100分段柱状图 |
| 预警类型分布（饼图） | 6种预警类型占比 |

**底部图表区（2列布局）：**
| 图表类型 | 说明 |
|---|---|
| 近30天批次新增趋势（折线图） | 每日批次入库数量 |
| 近30天预警趋势（折线图） | 每日预警生成数量 |

**WebSocket驱动：** 收到 `OVERVIEW_UPDATE` 消息后，ECharts调用 `chart.setOption(newOption, true)` 无刷新重绘

**本页图片预留位置：** [图12] 大盘概览页面完整截图
> 文件路径：`frontend/src/views/Overview.vue`
> 截图范围：顶部5个统计卡片 + 全部图表区域
> 关键文字：`大盘概览`、`多维度聚合数据可视化`

---

### 第21页：模块五——系统参数配置

**标题：** 模块五：系统参数配置

**页面内容（建议左右分栏：架构说明+参数分类）：**

**ConfigService三层架构（左侧）：**
```
读取时：请求 → Redis缓存（config:{key}，TTL 30分钟）→ 未命中 → MySQL查询 → 回填Redis → 返回
更新时：请求 → MySQL写入 → Redis同步更新 → ConfigService.notifyListeners() → 所有监听器响应
```

**动态调度响应（右侧）：**
- 监听 `schedule.enabled=true` → TaskScheduler立即开启定时清洗任务
- 监听 `schedule.interval=60` → TaskScheduler以60秒间隔重新调度
- 监听算法权重/阈值变更 → 新批次以新参数计算

**参数分组展示（Config.vue）：**
| 分组 | 参数示例 |
|---|---|
| 风险评分 | risk.weight.pesticide=0.35 / risk.threshold.high=70 |
| 异常检测 | anomaly.temp.sigma=3 / anomaly.humidity.sigma=3 |
| 预警阈值 | alert.score.threshold=0.5 |
| 定时清洗 | schedule.enabled=true / schedule.interval=60 |

**本页图片预留位置：** [图13] 参数配置页面截图
> 文件路径：`frontend/src/views/Config.vue`
> 截图范围：el-radio-group分组标签 + 参数表格 + 行内编辑/保存按钮
> 关键文字：`参数配置`、`系统算法与预警阈值的可配置参数`

---

### 第22页：总结与展望

**标题：** 总结与展望

**左侧：已完成的工作（建议用勾选卡片或完成清单）：**
- SpringBoot单体架构，单台服务器即可部署，零授权费用
- 3σ统计异常检测 + 加权规则风险评分，纯Java原生实现，无Python依赖
- WebSocket服务端主动推送，多浏览器标签页实时同步，指数退避重连保障健壮性
- 监管大屏 ECharts 多维可视化（仪表盘/饼图/柱状图/折线图），数据无刷新自动更新
- 参数动态配置（Redis缓存 + 发布-订阅），改配置即生效，无需重启服务
- AOP切面操作日志审计，零侵入，异步写入不阻塞主业务
- TaskScheduler动态调度定时清洗，监听配置变更即时响应

**右侧：未来改进方向（建议用箭头或扩展图标）：**
- 接入真实IoT设备（MQTT协议，温湿度传感器、GPS追踪器）
- 引入Redis Pub-Sub实现跨节点WebSocket广播，支持多实例水平扩展
- 引入ECharts Geo热力地图，展示各省风险分布（geographic扩展）
- 算法深化：多批次联合分析，综合季节性因素、产地聚集特征

---

### 第23页：致谢页

**标题：** 致谢

**正文内容（居中，简洁大方）：**

感谢指导老师 **唐建国副教授** 在课题选题、技术路线规划和论文撰写过程中的悉心指导与耐心帮助。

感谢学院提供的学习环境，感谢身边同学和朋友的支持与陪伴。

感谢所有在毕业设计过程中给予帮助的老师和同学。

---

### 第24页：答辩结束页

**标题：** 感谢聆听，欢迎提问！

**副标题/正文（居中）：**
- 刘勇 | 221040100211
- 计算机科学与技术辅修2022级
- 信息科学与工程学院

---

## 三、图片插入说明（供后续填充）

> 当你准备好系统运行截图后，按以下路径找到对应页面，截取指定范围即可：

| 图片编号 | 对应页面 | 文件路径 | 截图重点 |
|---|---|---|---|
| [图1] | 研究背景 | 网络图片 | 食品安全/溯源相关场景 |
| [图2] | 项目目的 | 项目根目录 | core-service + frontend 两层目录结构 |
| [图3] | 技术栈 | pom.xml / package.json | 关键依赖坐标 |
| [图4] | WebSocket | Chrome DevTools | ws连接截图或推送消息截图 |
| [图5] | 系统架构 | 项目代码结构 | core-service和frontend的包/目录结构 |
| [图6] | 功能模块总览 | Sidebar运行效果 | 左侧菜单树截图 |
| [图7] | 数据库设计（一） | Navicat/DBeaver | 8张表列表或关系视图 |
| [图8] | 批次录入 | `BatchEntry.vue` | 三区块表单截图 |
| [图9] | 溯源链 | `TraceChain.vue` | 批次选择器+时间线 |
| [图10] | 预警大盘 | `AlertDashboard.vue` | 处理率+等级分布+7天趋势 |
| [图11] | 预警列表 | `AlertList.vue` | 筛选栏+表格+处理按钮 |
| [图12] | 大盘概览 | `Overview.vue` | 5个统计卡片+全部图表 |
| [图13] | 参数配置 | `Config.vue` | 分组标签+参数表格 |

**截图技巧：**
- 建议使用浏览器全屏截图（Chrome按F11进入全屏）
- 截图前先登录系统（账号：admin 或 supervisor01，密码：123456），进入对应页面
- 图片命名按 `[图X]` 编号命名，便于插入PPT对应位置

---

## 四、PPT设计参考提示词

**如果AI支持Markdown/PPT模板格式，可附加以下设计要求：**

```
设计风格要求：
- 主色调：深蓝 #0a2e5c → #1a5a96 渐变（封面/章节页使用）
- 内容页背景：白色或浅灰色（#f5f7fa）
- 强调色：绿色#6bcb77（安全/低风险）/ 红色#ff6b6b（预警/高风险）/ 橙色#ffd93d（中风险）
- 字体：中文使用思源黑体/微软雅黑，英文使用Arial
- 章节页：深蓝渐变背景，白色大字标题，左侧色条装饰
- 内容页：白底，深蓝标题栏，图表/卡片布局
- 代码块：浅灰色背景（#f5f5f5），等宽字体（Consolas / Source Code Pro）
- 每页右下角：页码（如 "12 / 24"）
- 封面和致谢页：深蓝渐变背景，白色文字
```

---
