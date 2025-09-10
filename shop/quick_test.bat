@echo off
echo ========================================
echo 零食商城系统快速测试脚本
echo ========================================

echo.
echo 1. 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: Java未安装或未配置环境变量
    pause
    exit /b 1
)

echo.
echo 2. 检查Node.js环境...
node --version
if %errorlevel% neq 0 (
    echo 错误: Node.js未安装或未配置环境变量
    pause
    exit /b 1
)

echo.
echo 3. 检查MySQL服务...
mysql --version
if %errorlevel% neq 0 (
    echo 警告: MySQL未安装或未配置环境变量
    echo 请确保MySQL服务正在运行
)

echo.
echo 4. 准备测试数据...
echo 请确保已执行 test_data_preparation.sql 脚本

echo.
echo 5. 启动后端服务...
echo 正在启动Spring Boot应用...
start "后端服务" cmd /k "mvn spring-boot:run"

echo.
echo 6. 等待后端服务启动...
timeout /t 30 /nobreak

echo.
echo 7. 启动前端服务...
echo 正在启动Vue.js应用...
cd frontend
start "前端服务" cmd /k "npm run dev"

echo.
echo 8. 等待前端服务启动...
timeout /t 20 /nobreak

echo.
echo ========================================
echo 服务启动完成！
echo ========================================
echo.
echo 访问地址:
echo 前端应用: http://localhost:5173
echo 后端API: http://localhost:8080/api
echo.
echo 测试账号:
echo 管理员: admin / 123456
echo 商家: merchant / 123456
echo 用户: testuser / 123456
echo.
echo 测试步骤:
echo 1. 打开浏览器访问 http://localhost:5173
echo 2. 使用测试账号登录
echo 3. 测试各项功能
echo 4. 使用Postman测试API接口
echo.
echo 按任意键退出...
pause > nul
