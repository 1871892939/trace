// 风险评分 API 测试脚本
// 使用方法：在浏览器控制台运行 testRiskAPI()

async function testRiskAPI() {
  console.log('🚀 开始测试风险评分功能...\n')
  
  // 测试数据 1：正常数据
  const normalData = {
    batchId: 1,
    pesticide: 0.3,
    heavyMetal: 0.05,
    microbe: 100,
    temperature: 5,
    humidity: 55,
    gpsLng: 113.2644,
    gpsLat: 23.1291
  }
  
  console.log('📊 测试 1 - 正常数据:')
  console.log(JSON.stringify(normalData, null, 2))
  
  try {
    const response = await fetch('http://localhost:8081/api/risk/evaluate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(normalData)
    })
    
    const result = await response.json()
    console.log('\n✅ 响应结果:')
    console.log(JSON.stringify(result, null, 2))
    
    if (result.code === 200) {
      console.log(`\n✓ 风险评分：${result.data.riskScore}`)
      console.log(`✓ 风险等级：${result.data.riskLevel}`)
    } else {
      console.log(`\n✗ 错误：${result.message}`)
    }
  } catch (error) {
    console.error('\n❌ 请求失败:', error.message)
    console.log('\n提示：请确保后端服务已启动（端口 8081）')
  }
  
  console.log('\n' + '='.repeat(50) + '\n')
  
  // 测试数据 2：危险数据
  const dangerData = {
    batchId: 3,
    pesticide: 2.5,
    heavyMetal: 0.35,
    microbe: 520,
    temperature: 18,
    humidity: 85,
    gpsLng: 116.4074,
    gpsLat: 39.9042
  }
  
  console.log('📊 测试 2 - 危险数据:')
  console.log(JSON.stringify(dangerData, null, 2))
  
  try {
    const response = await fetch('http://localhost:8081/api/risk/evaluate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(dangerData)
    })
    
    const result = await response.json()
    console.log('\n✅ 响应结果:')
    console.log(JSON.stringify(result, null, 2))
    
    if (result.code === 200) {
      console.log(`\n✓ 风险评分：${result.data.riskScore}`)
      console.log(`✓ 风险等级：${result.data.riskLevel}`)
    } else {
      console.log(`\n✗ 错误：${result.message}`)
    }
  } catch (error) {
    console.error('\n❌ 请求失败:', error.message)
  }
  
  console.log('\n' + '='.repeat(50))
  console.log('🎉 测试完成！\n')
}

// 运行测试
testRiskAPI()
