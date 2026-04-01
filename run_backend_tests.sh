#!/bin/bash

echo "=========================================="
echo "  食品安全溯源系统 - 后端测试"
echo "=========================================="
echo ""

# 检查 MySQL
echo "📋 检查 MySQL 状态..."
if command -v mysql &> /dev/null; then
    echo "✅ MySQL 客户端已安装"
else
    echo "⚠️  MySQL 客户端未安装，请确保 MySQL 服务已启动"
fi

# 检查 Redis
echo "📋 检查 Redis 状态..."
if command -v redis-cli &> /dev/null; then
    if redis-cli ping &> /dev/null; then
        echo "✅ Redis 服务正在运行"
    else
        echo "❌ Redis 服务未启动"
        echo "   启动命令：redis-server"
    fi
else
    echo "⚠️  Redis 客户端未安装，请确保 Redis 服务已启动"
fi

echo ""
echo "=========================================="
echo "  测试步骤："
echo "=========================================="
echo ""
echo "1. 在 IDEA 中打开项目"
echo "2. 定位到测试类："
echo "   core-service/src/test/java/com/ncg/service/DataEntryServiceTest.java"
echo ""
echo "3. 右键运行测试类"
echo "   或运行单个测试方法："
echo "   - testEntryNormalData()"
echo "   - testEntryWarningData()"
echo "   - testEntryDangerData()"
echo "   - testDuplicateBatchNo()"
echo ""
echo "4. 查看控制台输出"
echo "   ✅ 表示测试通过"
echo "   ❌ 表示测试失败"
echo ""
echo "=========================================="
echo ""
echo "预期结果："
echo "- 测试 1-3：成功录入数据并自动执行风险评估"
echo "- 测试 4：正确捕获重复批次编号异常"
echo ""
echo "=========================================="
