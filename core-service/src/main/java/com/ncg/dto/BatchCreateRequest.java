package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批次录入请求 DTO（包含批次信息、检测数据、物流数据）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchCreateRequest {

    // 批次信息
    private String batchNo;
    private String origin;
    private String enterprise;
    private String productionDate;

    // 检测数据
    private DetectionDataItem detection;

    // 物流数据（可多条）
    private List<LogisticsDataItem> logistics;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetectionDataItem {
        private String pesticide;
        private String heavyMetal;
        private String microbe;
        private String testTime;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogisticsDataItem {
        private String gpsLng;
        private String gpsLat;
        private String temperature;
        private String humidity;
        private String recordTime;
    }
}
