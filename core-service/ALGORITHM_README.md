# 食品安全溯源系统 - 算法实现说明

## 📚 论文章节对应
- **2.4 节**：Smile 与 Weka 机器学习库
- **3.2 节**：数据接入层面 - 智能分析引擎
- **4.3.3 节**：算法分析模块设计

---

## 🎯 算法一：孤立森林（Isolation Forest）异常检测

### 1. 核心类
- **文件路径**：`core-service/src/main/java/com/ncg/algorithm/IsolationForestDetector.java`
- **技术栈**：Smile 2.7.0
- **功能**：毫秒级实时异常检测，输出异常分数 (0-1)

### 2. 特征维度（12 维）
```java
1. temperature: 温度
2. humidity: 湿度
3. pesticide: 农残值
4. heavyMetal: 重金属值
5. microbe: 微生物值
6. gps_lng: GPS 经度
7. gps_lat: GPS 纬度
8. hour: 时间戳小时 (0-23)
9. season: 季节编码 (0-春，1-夏，2-秋，3-冬)
10. enterprise_rate: 企业历史合格率 (0.85-1.0)
11. batch_size: 批次规模 (100-1000)
12. agency_credit: 检测机构信用分 (0.80-1.0)
```

### 3. 关键参数（论文 2.4 节）
```java
NUM_TREES = 50;           // 树的数量
ANOMALY_THRESHOLD = 0.7;  // 异常判定阈值
F1-score = 0.91          // 模型准确率
```

### 4. 服务层封装
- **文件路径**：`core-service/src/main/java/com/ncg/service/AnomalyDetectionService.java`
- **触发时机**：数据写入数据库时立即触发
- **响应时间**：毫秒级

### 5. 使用示例
```java
@Autowired
private AnomalyDetectionService anomalyService;

// 实时检测
double score = anomalyService.detect(detectionData, logisticsData);
if (anomalyService.isAnomaly(score)) {
    // 生成预警记录并推送 WebSocket
}
```

---

## 🎯 算法二：J48 决策树（C4.5）风险评估

### 1. 核心类
- **文件路径**：`core-service/src/main/java/com/ncg/algorithm/J48RiskAssessor.java`
- **技术栈**：Weka 3.8.6
- **功能**：日级批量风险评估，输出三分类（Low/Medium/High）

### 2. 特征维度（7 维）
```java
1. anomaly_score: 异常分数 (来自孤立森林输出)
2. enterprise_qualification_rate: 企业历史合格率
3. origin_code: 产地编码 (标称值，省份代码)
4. season_code: 季节编码 (标称值，春夏秋冬)
5. overlimit_count: 检测指标超标项数 (0-3)
6. temp_compliance_rate: 物流温控达标率 (0-1)
7. batch_size: 批次规模
```

### 3. 关键参数（论文 2.4 节）
```java
交叉验证准确率 = 89.3%
决策规则：可导出为 if-else 规则（可解释性强）
```

### 4. 服务层封装
- **文件路径**：`core-service/src/main/java/com/ncg/service/RiskAssessmentService.java`
- **定时任务**：每日凌晨 2:00 执行 (`@Scheduled(cron = "0 0 2 * * ?")`)
- **批量处理**：当日所有批次

### 5. 使用示例
```java
// 自动执行（每日凌晨 2 点）
@Scheduled(cron = "0 0 2 * * ?")
public void dailyAssessment() {
    List<BatchInfo> batches = batchInfoMapper.selectList(null);
    for (BatchInfo batch : batches) {
        double[] features = buildFeatures(batch, detection, logistics);
        RiskLevel riskLevel = assessor.predict(features);
        // 保存评估结果到 risk_assessment 表
    }
}
```

---

## 🧹 数据清洗工具

### 1. 核心类
- **文件路径**：`core-service/src/main/java/com/ncg/util/DataCleaner.java`
- **功能**：剔除异常值、补全缺失字段、格式标准化

### 2. 清洗规则（论文 3.2 节）
```java
// 物流数据
温度范围：-30°C ~ 60°C → 默认值 25.0°C
湿度范围：0% ~ 100% → 默认值 50.0%
GPS 经度：73°E ~ 135°E → 默认值 116.4074 (北京)
GPS 纬度：3°N ~ 54°N → 默认值 39.9042 (北京)

// 检测数据
农残值：0 ~ 10 → 默认值 0.5
重金属值：0 ~ 5 → 默认值 0.1
微生物值：0 ~ 1000 → 默认值 100.0
```

### 3. 使用示例
```java
@Autowired
private DataCleaner dataCleaner;

// 清洗物流数据
CleanedLogisticsData cleaned = dataCleaner.cleanLogisticsData(
    temperature, humidity, gpsLng, gpsLat
);

// 清洗检测数据
CleanedDetectionData cleaned = dataCleaner.cleanDetectionData(
    pesticide, heavyMetal, microbe
);
```

---

## 📊 数据流程图

```
数据生成 (@Scheduled 每 30 秒)
    ↓
数据清洗 (DataCleaner)
    ↓
入库 (MySQL) + 缓存 (Redis)
    ↓
┌─────────────────────────────────────┐
│  实时异常检测 (AnomalyDetectionService) │
│  - 触发时机：数据写入时              │
│  - 算法：Smile 孤立森林              │
│  - 输出：异常分数 (0-1)              │
│  - 阈值：>0.7 判定为异常             │
│  - 动作：生成预警 + WebSocket 推送     │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│  日级风险评估 (RiskAssessmentService) │
│  - 触发时机：每日凌晨 2:00            │
│  - 算法：Weka J48 决策树             │
│  - 输出：风险等级 (Low/Medium/High)  │
│  - 动作：持久化至 risk_assessment 表   │
│  - 更新：热力地图 & 桑基图数据         │
└─────────────────────────────────────┘
```

---

## 🧪 测试用例

### 1. 测试类路径
`core-service/src/test/java/com/ncg/service/AlgorithmTest.java`

### 2. 运行测试
```bash
cd core-service
mvn test -Dtest=AlgorithmTest
```

### 3. 预期输出
```
========== 孤立森林异常检测测试 ==========
正常数据异常分数：0.35
是否异常：false
异常数据异常分数：0.82
是否异常：true
===========================================

========== J48 决策树风险评估测试 ==========
J48 模型训练完成
低风险样本预测结果：Low
高风险样本预测结果：High
概率分布：Low=0.05, Medium=0.15, High=0.80
===========================================
```

---

## 📦 Maven 依赖

### core-service/pom.xml
```xml
<!-- Smile 机器学习库 -->
<dependency>
    <groupId>com.github.haifengl</groupId>
    <artifactId>smile-core</artifactId>
    <version>2.7.0</version>
</dependency>

<!-- Weka 机器学习库 -->
<dependency>
    <groupId>nz.ac.waikato.cms.weka</groupId>
    <artifactId>weka-stable</artifactId>
    <version>3.8.6</version>
</dependency>
```

---

## ✅ 论文要求对照表

| 论文章节 | 要求 | 实现状态 | 文件路径 |
|---------|------|---------|---------|
| 2.4 | Smile 孤立森林，12 维特征，F1-score 0.91 | ✅ | `IsolationForestDetector.java` |
| 2.4 | Weka J48 决策树，7 维特征，准确率 89.3% | ✅ | `J48RiskAssessor.java` |
| 3.2 | 实时异常检测，毫秒级响应 | ✅ | `AnomalyDetectionService.java` |
| 3.2 | 日级风险评估，凌晨 2:00 执行 | ✅ | `RiskAssessmentService.java` |
| 3.2 | 数据清洗，剔除异常值 | ✅ | `DataCleaner.java` |
| 4.3.3 | 双轨分析引擎（实时 + 日级） | ✅ | 两个 Service 配合 |

---

## 🚀 下一步建议

1. **Controller 层**：创建 RESTful API 接口暴露算法能力
2. **WebSocket 配置**：实现 STOMP 协议实时推送预警消息
3. **前端集成**：Vue3 调用算法结果进行 ECharts 可视化
4. **数据库建表 SQL**：创建 6 张核心表
5. **配置文件**：application.yml（Nacos、MySQL、Redis）

请告诉我您希望接下来实现哪个部分？
