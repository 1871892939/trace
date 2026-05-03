# Sentinel 限流与熔断技术 - 论文内容

以下内容需要添加到论文的对应章节中：

---

## 第一处：第二章「相关理论及技术综述」新增一个小节

在「2.5 MyBatis-Plus 与 Redis」之后，「2.6 本章小结」之前，添加：

### 2.6 Sentinel 限流与熔断技术

随着微服务架构的普及，分布式系统面临的核心挑战之一是如何在高并发场景下保障服务的稳定性与可用性。限流（Rate Limiting）与熔断（Circuit Breaker）作为流量控制领域的两项核心技术，分别从"拒绝过量请求"和"快速失败隔离故障"两个维度为系统提供保护。

**Sentinel** 是阿里巴巴开源的流量控制与容错组件，与 Hystrix 同为业界主流的熔断解决方案。相比 Hystrix（已停止维护），Sentinel 在以下方面具有显著优势：轻量级核心库无外部依赖、丰富的流量控制策略、支持运行时动态规则推送、提供可视化 Dashboard 进行实时监控。本系统选用 Sentinel 1.8.x 版本，结合 Spring Cloud Alibaba 适配层实现限流与熔断功能。

Sentinel 的核心概念包括：

- **资源（Resource）**：Sentinel 保护的对象，可以是一个接口、一个方法或一个服务。本系统以接口 URL 作为资源名（如 `/api/batch/create`），通过 Spring MVC 适配器自动统计；
- **规则（Rule）**：流控规则（FlowRule）定义资源的限流阈值与效果，熔断规则（DegradeRule）定义资源的熔断策略。本系统通过 SentinelConfig 配置类在应用启动时初始化规则，同时支持连接 Sentinel Dashboard 动态下发规则覆盖本地配置；
- **插槽链（Slot Chain）**：Sentinel 的核心处理链，每个插槽负责特定职责：NodeSelectorSlot 负责构建资源调用树，ClusterBuilderSlot 负责聚合统计信息，FlowSlot 负责流控判断，DegradeSlot 负责熔断判断。

本系统的限流规则按接口类型分级配置：核心写接口（批次录入、预警处理）QPS 限制为 10；认证接口（登录、注册）QPS 限制为 20；查询接口（大盘、预警列表）QPS 限制为 30-50；重量级接口（数据模拟）QPS 限制为 5。熔断策略针对慢调用比例（RT）和异常比例两种场景设置，确保在依赖服务响应异常时快速熔断，防止故障蔓延。

---

## 第二处：第四章「4.3 功能结构设计」新增一个模块

在「4.3.7 系统参数配置模块」之后，「4.4 数据库设计」之前，添加：

### 4.3.8 Sentinel 限流熔断模块

**职责**：保护系统核心接口免受过量并发请求冲击，在依赖服务响应异常时自动熔断，防止故障级联蔓延。

**规则配置**：本模块在应用启动时通过 SentinelConfig 配置类初始化两类规则：

（1）**限流规则（FlowRule）**：按接口类型分级配置 QPS 阈值。核心写接口（批次录入、预警处理）严格限制 QPS=10，防止批量提交压垮数据库；认证接口（登录、注册）设置 QPS=20，兼顾可用性与安全性；查询接口（大盘、预警列表）适度放宽至 QPS=30-50，保障监管大屏的流畅访问；重量级接口（数据模拟）设置 QPS=5，避免复杂计算占用过多 CPU。

（2）**熔断规则（DegradeRule）**：针对不同业务场景设置差异化熔断策略。批次录入接口采用慢调用比例熔断（RT>2秒触发，比例阈值50%）；预警处理接口采用异常比例熔断（异常比例>50%触发）；数据模拟接口设置更宽松的慢调用阈值（RT>3秒，比例60%）。

**Dashboard 接入**：通过 `spring.cloud.sentinel.dashboard` 配置项指定 Sentinel Dashboard 地址（`localhost:8080`），客户端启动后主动向 Dashboard 注册心跳，Dashboard 可实时展示各接口的 QPS、响应时间、拦截次数等监控指标，并支持在线推送新规则覆盖本地配置，实现规则的动态管理。

---

## 第三处：第五章「基于SpringCloud的食品安全溯源系统功能模块实现」新增一个小节

在「5.8 操作日志审计模块实现」之后，「5.9 本章小结」之前，添加：

### 5.10 Sentinel 限流熔断模块实现

#### 5.10.1 Sentinel 依赖引入与配置

本系统通过 Spring Cloud Alibaba 适配层引入 Sentinel 依赖。核心依赖包括：`spring-cloud-starter-alibaba-sentinel`（Sentinel 与 Spring Cloud 的集成适配器）、`sentinel-core`（核心库）、`sentinel-transport-simple-http`（客户端与 Dashboard 的 HTTP 通信模块）、`sentinel-annotation-aspectj`（@SentinelResource 注解的 AOP 切面支持）。

在 `application.yml` 中配置 Sentinel 连接参数：

```yaml
spring.cloud.sentinel:
  enabled: true
  dashboard: localhost:8080
```

此外，在应用启动类 Main.java 的 static 块中通过 System.setProperty 硬编码 Dashboard 地址，确保配置优先级：

```java
static {
    System.setProperty("csp.sentinel.dashboard.server", "localhost:8080");
    System.setProperty("csp.sentinel.heartbeat.interval.ms", "1000");
}
```

#### 5.10.2 限流与熔断规则初始化

SentinelConfig 配置类在应用启动时通过 @PostConstruct 初始化限流规则与熔断规则。以限流规则为例，系统为每个需要保护的接口定义一条 FlowRule：

```java
private void initFlowRules() {
    List<FlowRule> rules = new ArrayList<>();
    // 核心写接口：QPS=10
    rules.add(buildFlowRule("batch:create", 10, RuleConstant.FLOW_GRADE_QPS, 0));
    rules.add(buildFlowRule("alert:handle", 10, RuleConstant.FLOW_GRADE_QPS, 0));
    // 认证接口：QPS=20
    rules.add(buildFlowRule("auth:login", 20, RuleConstant.FLOW_GRADE_QPS, 0));
    // 查询接口：QPS=30-50
    rules.add(buildFlowRule("overview:dashboard", 50, RuleConstant.FLOW_GRADE_QPS, 0));
    // ...
    FlowRuleManager.loadRules(rules);
}
```

熔断规则采用慢调用比例和异常比例两种策略：

```java
private void initDegradeRules() {
    List<DegradeRule> rules = new ArrayList<>();
    // 慢调用比例熔断：批次录入 RT>2秒，比例>50%
    rules.add(buildDegradeRule("batch:create", RuleConstant.DEGRADE_GRADE_RT,
            2000, 0.5, 5, 10));
    // 异常比例熔断：预警处理 异常比例>50%
    rules.add(buildDegradeRule("alert:handle", RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO,
            2000, 0.5, 5, 30));
    // ...
    DegradeRuleManager.loadRules(rules);
}
```

#### 5.10.3 Spring MVC 自动适配

Spring Cloud Alibaba Sentinel Starter 自动配置了 SentinelWebMvcConfigurer，向 Spring MVC 拦截器链注册 SentinelWebInterceptor。应用启动日志可见：

```
[HeartBeatService] send heartbeat to dashboard ...
[ReceiveHandler] receive from: 127.0.0.1:8080
```

这表明 Sentinel 客户端已成功与 Dashboard 建立连接，开始上报心跳与监控数据。所有进入 Spring MVC 的 HTTP 请求均自动统计为 Sentinel 资源，资源名默认为请求路径（如 `/api/overview/dashboard`）。

#### 5.10.4 Sentinel Dashboard 监控

Sentinel Dashboard 提供可视化的监控与管理能力：

- **机器列表**：展示已注册的应用实例（IP:PORT），显示健康状态与最后心跳时间；
- **簇点链路**：展示各接口的实时 QPS、平均响应时间、拦截次数、通过率等指标；
- **规则配置**：支持在线新增、修改、删除限流规则与熔断规则，规则实时下发至客户端生效。

通过 Dashboard，监管人员可直观掌握系统各接口的流量情况，在发现异常流量时快速调整限流阈值，实现"监控—告警—处置"的运维闭环。

---

## 第四处：摘要更新

在摘要的"系统实践意义"段落中，可以在"WebSocket 实时推送机制..."之后添加一句：

> 系统集成 Alibaba Sentinel 限流熔断组件，为核心接口提供流量控制保护，支持连接 Sentinel Dashboard 实现规则动态下发与实时监控，确保高并发场景下的服务可用性。

---

## 第五处：第6章「系统测试」可添加 Sentinel 相关测试用例

在「1.2 功能测试」的小节中，可以添加：

### 1.2.5 限流熔断测试

| 用例编号 | 测试内容 | 操作步骤 | 预期结果 |
|---------|---------|---------|---------|
| LT-01 | 限流触发 | 使用 JMeter 模拟 50 QPS 持续请求登录接口 | 超过 20 QPS 的请求返回限流提示（Blocked by Sentinel） |
| LT-02 | 熔断恢复 | 手动触发接口异常率达到 50% | 接口自动熔断，5 次探测请求均失败后熔断 30 秒 |
| LT-03 | Dashboard 规则下发 | 在 Dashboard 修改限流阈值为 5 QPS | 客户端规则实时更新，新阈值立即生效 |

---
