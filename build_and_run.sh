#!/bin/bash

echo "=========================================="
echo "  编译并启动 core-service"
echo "=========================================="
echo ""

cd /Users/al/Desktop/Programme/workspace/trace/core-service

# 检查 Java 版本
echo "📋 Java 版本："
java -version
echo ""

# 尝试清理和编译
echo "🔨 清理旧的构建..."
rm -rf target/

echo "🔨 开始编译项目..."
# 如果没有 mvn，提示用户使用 IDEA
if ! command -v mvn &> /dev/null; then
    echo ""
    echo "⚠️  Maven 未安装，请使用以下方式编译："
    echo ""
    echo "1. 打开 IDEA"
    echo "2. File → Project Structure → SDKs"
    echo "   确保使用 JDK 17"
    echo "3. Build → Build Project (Cmd+F9)"
    echo "4. 运行 Main.java"
    echo ""
    exit 1
fi

# 如果有 Maven，直接编译
mvn clean compile -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 编译成功！"
    echo ""
    echo "启动命令：mvn spring-boot:run"
else
    echo ""
    echo "❌ 编译失败，请查看上方错误信息"
fi
