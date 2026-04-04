-- 食品安全溯源系统 - 核心数据库表结构
-- 数据库版本：MySQL 8.0+
-- 字符集：utf8mb4

-- ==================== 1. 用户信息表 ====================
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码 (BCrypt 加密)',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：supervisor(监管员)/admin(管理员)',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ==================== 2. 食品批次信息表 ====================
DROP TABLE IF EXISTS `batch_info`;
CREATE TABLE `batch_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `batch_no` VARCHAR(50) NOT NULL COMMENT '批次编号 (业务主键)',
  `origin` VARCHAR(20) NOT NULL COMMENT '产地编码 (省份代码)',
  `production_date` DATE NOT NULL COMMENT '生产日期',
  `enterprise` VARCHAR(100) NOT NULL COMMENT '所属企业',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_production_date` (`production_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品批次信息表';

-- ==================== 3. 检测数据表 ====================
DROP TABLE IF EXISTS `detection_data`;
CREATE TABLE `detection_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `batch_id` BIGINT NOT NULL COMMENT '批次 ID',
  `pesticide` DECIMAL(10,4) NOT NULL COMMENT '农残值 (mg/kg)',
  `heavy_metal` DECIMAL(10,4) NOT NULL COMMENT '重金属值 (mg/kg)',
  `microbe` DECIMAL(10,2) NOT NULL COMMENT '微生物值 (CFU/g)',
  `test_time` DATETIME NOT NULL COMMENT '检测时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_test_time` (`test_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测数据表';

-- ==================== 4. 物流数据表 ====================
DROP TABLE IF EXISTS `logistics_data`;
CREATE TABLE `logistics_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `batch_id` BIGINT NOT NULL COMMENT '批次 ID',
  `gps_lng` DECIMAL(10,8) NOT NULL COMMENT 'GPS 经度',
  `gps_lat` DECIMAL(10,8) NOT NULL COMMENT 'GPS 纬度',
  `temperature` DECIMAL(5,2) NOT NULL COMMENT '车厢温度 (℃)',
  `humidity` DECIMAL(5,2) NOT NULL COMMENT '车厢湿度 (%)',
  `record_time` DATETIME NOT NULL COMMENT '采集时间戳',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流数据表';

-- ==================== 5. 预警记录表 ====================
DROP TABLE IF EXISTS `alert_record`;
CREATE TABLE `alert_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `batch_id` BIGINT NOT NULL COMMENT '批次 ID',
  `alert_type` VARCHAR(50) NOT NULL COMMENT '预警类型：TEMP/HUMIDITY/PESTICIDE/HEAVY_METAL/MICROBE/COMPOSITE',
  `risk_score` DECIMAL(5,2) NOT NULL COMMENT '风险分数 (0-100)',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `handled` TINYINT DEFAULT 0 COMMENT '是否已处理：0-未处理 1-已处理',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_handled` (`handled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- ==================== 6. 风险评估表 ====================
DROP TABLE IF EXISTS `risk_assessment`;
CREATE TABLE `risk_assessment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `batch_id` BIGINT NOT NULL COMMENT '批次 ID',
  `risk_level` VARCHAR(20) NOT NULL COMMENT '风险等级：Low(0-40)/Medium(41-70)/High(71-100)',
  `risk_score` INT NOT NULL COMMENT '风险评分 (0-100)',
  `assessment_date` DATE NOT NULL COMMENT '评估日期',
  `factors` TEXT COMMENT '风险因素 (JSON 字符串，包含各项得分明细)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_assessment_date` (`assessment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险评估表';

-- ==================== 7. 系统配置参数表 ====================
DROP TABLE IF EXISTS `config_param`;
CREATE TABLE `config_param` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `param_key` VARCHAR(100) NOT NULL COMMENT '参数唯一标识',
  `param_name` VARCHAR(100) NOT NULL COMMENT '参数中文名称',
  `param_value` VARCHAR(500) NOT NULL COMMENT '参数值',
  `param_type` VARCHAR(20) NOT NULL COMMENT '参数类型：number/string/boolean',
  `param_group` VARCHAR(50) NOT NULL COMMENT '参数分组：risk/anomaly/alert',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '参数说明',
  `editable` TINYINT DEFAULT 1 COMMENT '是否可编辑：0-不可编辑 1-可编辑',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_param_key` (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置参数表';

-- ==================== 初始化系统默认参数 ====================
INSERT INTO `config_param` (`param_key`, `param_name`, `param_value`, `param_type`, `param_group`, `description`, `editable`) VALUES
-- 风险评分阈值
('risk.low.threshold', '低风险阈值', '40', 'number', 'risk', '风险评分低于此值判定为 Low 等级', 1),
('risk.high.threshold', '高风险阈值', '70', 'number', 'risk', '风险评分高于此值判定为 High 等级', 1),
('risk.weight.detection', '检测指标权重（%）', '70', 'number', 'risk', '检测指标（农残+重金属+微生物）在总分中的权重占比', 1),
('risk.weight.pesticide', '农残权重（%）', '35', 'number', 'risk', '农残在检测指标内的权重占比', 1),
('risk.weight.heavy_metal', '重金属权重（%）', '35', 'number', 'risk', '重金属在检测指标内的权重占比', 1),
('risk.weight.microbe', '微生物权重（%）', '30', 'number', 'risk', '微生物在检测指标内的权重占比', 1),
('risk.weight.temp', '温度权重（%）', '60', 'number', 'risk', '温度在物流指标内的权重占比', 1),
('risk.weight.humidity', '湿度权重（%）', '40', 'number', 'risk', '湿度在物流指标内的权重占比', 1),
-- 国标限量阈值
('limit.pesticide', '农残限量阈值（mg/kg）', '0.5', 'number', 'risk', 'GB 2763 农残超标判定阈值', 1),
('limit.heavy_metal', '重金属限量阈值（mg/kg）', '0.1', 'number', 'risk', 'GB 2762 重金属超标判定阈值', 1),
('limit.microbe', '微生物限量阈值（CFU/g）', '200', 'number', 'risk', 'GB 29921 微生物超标判定阈值', 1),
('limit.temp.min', '冷链最低温度（℃）', '0', 'number', 'risk', '冷链适宜温度下限', 1),
('limit.temp.max', '冷链最高温度（℃）', '10', 'number', 'risk', '冷链适宜温度上限', 1),
('limit.humidity.min', '适宜湿度下限（%）', '40', 'number', 'risk', '物流适宜湿度下限', 1),
('limit.humidity.max', '适宜湿度上限（%）', '70', 'number', 'risk', '物流适宜湿度上限', 1),
-- 异常检测参数
('anomaly.sigma.warning', '预警σ系数', '2.0', 'number', 'anomaly', '2σ 预警阈值系数（超过 2σ 触发预警）', 0),
('anomaly.sigma.critical', '异常σ系数', '3.0', 'number', 'anomaly', '3σ 异常阈值系数（超过 3σ 判定为异常）', 0),
-- 预警阈值
('alert.score.urgent', '紧急告警分数', '0.8', 'number', 'alert', '风险分数≥此值判定为紧急告警（0-1）', 1),
('alert.score.serious', '严重告警分数', '0.5', 'number', 'alert', '风险分数≥此值判定为严重告警（0-1）', 1),
('alert.composite.threshold', '综合预警触发分数', '70', 'number', 'alert', '风险评分高于此值且无具体类型预警时，触发综合预警', 1);

-- ==================== 初始化测试数据 ====================

-- 插入测试用户（密码都是 123456，BCrypt 加密）
INSERT INTO `user_info` (`username`, `password`, `role`, `status`) VALUES
('admin', '$2a$10$rOZ7HkzS9FqvxXmWqJCuL.8yNvVqC5bKZZ9mYxF7H8KqN2pL4mR5u', 'admin', 1),
('supervisor01', '$2a$10$rOZ7HkzS9FqvxXmWqJCuL.8yNvVqC5bKZZ9mYxF7H8KqN2pL4mR5u', 'supervisor', 1);

-- 插入测试批次（示例）
INSERT INTO `batch_info` (`batch_no`, `origin`, `production_date`, `enterprise`) VALUES
('BATCH20260324001', '440000', '2026-03-20', '广州食品加工厂'),
('BATCH20260324002', '330000', '2026-03-21', '杭州农业合作社'),
('BATCH20260324003', '110000', '2026-03-22', '北京乳业集团');
