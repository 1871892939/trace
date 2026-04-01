package com.ncg.service;

import com.ncg.dto.DataEntryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据录入服务测试
 */
@SpringBootTest
public class DataEntryServiceTest {
    
    @Autowired
    private DataEntryService dataEntryService;
    
    /**
     * 测试正常数据录入
     */
    @Test
    public void testEntryNormalData() {
        System.out.println("========== 测试 1：录入正常数据 ==========");
        
        DataEntryRequest request = new DataEntryRequest();
        request.setBatchNo("TEST20260324001");
        request.setOrigin("440000");
        request.setProductionDate(LocalDate.now());
        request.setEnterprise("广州食品加工厂");
        
        // 检测数据（正常）
        request.setPesticide(new BigDecimal("0.3"));
        request.setHeavyMetal(new BigDecimal("0.05"));
        request.setMicrobe(new BigDecimal("100"));
        request.setTestTime(LocalDateTime.now());
        
        // 物流数据（正常）
        request.setTemperature(new BigDecimal("5.0"));
        request.setHumidity(new BigDecimal("55"));
        request.setGpsLng(new BigDecimal("113.2644"));
        request.setGpsLat(new BigDecimal("23.1291"));
        request.setRecordTime(LocalDateTime.now());
        
        try {
            Long batchId = dataEntryService.entryData(request);
            System.out.println("✅ 录入成功！批次 ID: " + batchId);
            System.out.println("批次编号：" + request.getBatchNo());
        } catch (Exception e) {
            System.err.println("❌ 录入失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===========================================\n");
    }
    
    /**
     * 测试预警数据录入
     */
    @Test
    public void testEntryWarningData() {
        System.out.println("========== 测试 2：录入预警数据 ==========");
        
        DataEntryRequest request = new DataEntryRequest();
        request.setBatchNo("TEST20260324002");
        request.setOrigin("330000");
        request.setProductionDate(LocalDate.now());
        request.setEnterprise("杭州农业合作社");
        
        // 检测数据（偏高）
        request.setPesticide(new BigDecimal("1.2"));
        request.setHeavyMetal(new BigDecimal("0.08"));
        request.setMicrobe(new BigDecimal("180"));
        request.setTestTime(LocalDateTime.now());
        
        // 物流数据（偏高）
        request.setTemperature(new BigDecimal("12.0"));
        request.setHumidity(new BigDecimal("75"));
        request.setGpsLng(new BigDecimal("120.1551"));
        request.setGpsLat(new BigDecimal("30.2741"));
        request.setRecordTime(LocalDateTime.now());
        
        try {
            Long batchId = dataEntryService.entryData(request);
            System.out.println("✅ 录入成功！批次 ID: " + batchId);
            System.out.println("批次编号：" + request.getBatchNo());
        } catch (Exception e) {
            System.err.println("❌ 录入失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===========================================\n");
    }
    
    /**
     * 测试危险数据录入
     */
    @Test
    public void testEntryDangerData() {
        System.out.println("========== 测试 3：录入危险数据 ==========");
        
        DataEntryRequest request = new DataEntryRequest();
        request.setBatchNo("TEST20260324003");
        request.setOrigin("110000");
        request.setProductionDate(LocalDate.now());
        request.setEnterprise("北京乳业集团");
        
        // 检测数据（严重超标）
        request.setPesticide(new BigDecimal("2.5"));
        request.setHeavyMetal(new BigDecimal("0.35"));
        request.setMicrobe(new BigDecimal("520"));
        request.setTestTime(LocalDateTime.now());
        
        // 物流数据（严重超标）
        request.setTemperature(new BigDecimal("18.0"));
        request.setHumidity(new BigDecimal("85"));
        request.setGpsLng(new BigDecimal("116.4074"));
        request.setGpsLat(new BigDecimal("39.9042"));
        request.setRecordTime(LocalDateTime.now());
        
        try {
            Long batchId = dataEntryService.entryData(request);
            System.out.println("✅ 录入成功！批次 ID: " + batchId);
            System.out.println("批次编号：" + request.getBatchNo());
        } catch (Exception e) {
            System.err.println("❌ 录入失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===========================================\n");
    }
    
    /**
     * 测试重复批次编号
     */
    @Test
    public void testDuplicateBatchNo() {
        System.out.println("========== 测试 4：重复批次编号 ==========");
        
        DataEntryRequest request = new DataEntryRequest();
        request.setBatchNo("TEST20260324001"); // 与测试 1 相同
        request.setOrigin("440000");
        request.setProductionDate(LocalDate.now());
        request.setEnterprise("广州食品加工厂");
        request.setPesticide(new BigDecimal("0.3"));
        request.setHeavyMetal(new BigDecimal("0.05"));
        request.setMicrobe(new BigDecimal("100"));
        request.setTemperature(new BigDecimal("5.0"));
        request.setHumidity(new BigDecimal("55"));
        request.setGpsLng(new BigDecimal("113.2644"));
        request.setGpsLat(new BigDecimal("23.1291"));
        
        try {
            Long batchId = dataEntryService.entryData(request);
            System.err.println("❌ 应该抛出异常但没有！");
        } catch (Exception e) {
            System.out.println("✅ 正确捕获异常：" + e.getMessage());
        }
        
        System.out.println("===========================================\n");
    }
}
