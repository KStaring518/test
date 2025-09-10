#!/bin/bash

echo "========================================"
echo "零食商城系统快速测试脚本"
echo "========================================"

echo ""
echo "1. 检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "错误: Java未安装或未配置环境变量"
    exit 1
fi
java -version

echo ""
echo "2. 检查Node.js环境..."
if ! command -v node &> /dev/null; then
    echo "错误: Node.js未安装或未配置环境变量"
    exit 1
fi
node --version

echo ""
echo "3. 检查MySQL服务..."
if ! command -v mysql &> /dev/null; then
    echo "警告: MySQL未安装或未配置环境变量"
    echo "请确保MySQL服务正在运行"
fi

echo ""
echo "4. 准备测试数据..."
echo "请确保已执行 test_data_preparation.sql 脚本"

echo ""
echo "5. 启动后端服务..."
echo "正在启动Spring Boot应用..."
mvn spring-boot:run &
BACKEND_PID=$!

echo ""
echo "6. 等待后端服务启动..."
sleep 30

echo ""
echo "7. 启动前端服务..."
echo "正在启动Vue.js应用..."
cd frontend
npm run dev &
FRONTEND_PID=$!

echo ""
echo "8. 等待前端服务启动..."
sleep 20

echo ""
echo "========================================"
echo "服务启动完成！"
echo "========================================"
echo ""
echo "访问地址:"
echo "前端应用: http://localhost:5173"
echo "后端API: http://localhost:8080/api"
echo ""
echo "测试账号:"
echo "管理员: admin / 123456"
echo "商家: merchant / 123456"
echo "用户: testuser / 123456"
echo ""
echo "测试步骤:"
echo "1. 打开浏览器访问 http://localhost:5173"
echo "2. 使用测试账号登录"
echo "3. 测试各项功能"
echo "4. 使用Postman测试API接口"
echo ""
echo "按Ctrl+C停止服务..."

# 等待用户中断
trap "echo '正在停止服务...'; kill $BACKEND_PID $FRONTEND_PID; exit" INT
wait
