@echo off
echo 🚀 Starting OData Cloud Platform with MySQL...

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed or not in PATH. Please install Java 8+ first.
    pause
    exit /b 1
)

REM 检查Maven环境
call mvnw.cmd -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven wrapper is not available. Please check mvnw.cmd file.
    pause
    exit /b 1
)

REM 检查MySQL连接
echo 🔍 Checking MySQL connection...
mysql -u root -p123456 -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Cannot connect to MySQL. Please ensure MySQL is running and credentials are correct.
    echo    Default connection: mysql://localhost:3306 (root/123456)
    pause
    exit /b 1
)

REM 创建数据库
echo 📊 Creating databases...
mysql -u root -p123456 < auth-service/create-mysql-database.sql
mysql -u root -p123456 < platform-config-service/create-mysql-database.sql

REM 构建项目
echo 📦 Building projects...
call mvnw.cmd clean package -DskipTests

if %errorlevel% neq 0 (
    echo ❌ Build failed. Please check the errors above.
    pause
    exit /b 1
)

echo ✅ Build completed successfully!

REM 启动Nacos
echo 🌐 Starting Nacos Server...
cd nacos/nacos-service/bin
start "Nacos Server" cmd /c "startup.cmd -m standalone"
cd ../../../

REM 等待Nacos启动
echo ⏳ Waiting for Nacos to start (30 seconds)...
timeout /t 30 /nobreak >nul

REM 启动平台配置服务
echo 🔧 Starting Platform Config Service...
cd platform-config-service
start "Platform Config Service" cmd /c "mvnw.cmd spring-boot:run"
cd ..

REM 等待平台配置服务启动
echo ⏳ Waiting for Platform Config Service to start (20 seconds)...
timeout /t 20 /nobreak >nul

REM 启动认证服务
echo 🔐 Starting Authentication Service (MySQL)...
cd auth-service
start "Authentication Service" cmd /c "mvnw.cmd spring-boot:run"
cd ..

REM 等待认证服务启动
echo ⏳ Waiting for Authentication Service to start (20 seconds)...
timeout /t 20 /nobreak >nul

REM 启动API网关服务
echo 🚪 Starting API Gateway Service...
cd api-gateway
start "API Gateway Service" cmd /c "mvnw.cmd spring-boot:run"
cd ..

REM 等待API网关启动
echo ⏳ Waiting for API Gateway to start (20 seconds)...
timeout /t 20 /nobreak >nul

REM 启动OData网关服务
echo 🌐 Starting OData Gateway Service...
cd odata-gateway
start "OData Gateway Service" cmd /c "mvnw.cmd spring-boot:run"
cd ..

echo ⏳ Waiting for all services to start (30 seconds)...
timeout /t 30 /nobreak >nul

echo.
echo 🎉 OData Cloud Platform started successfully with MySQL!
echo.
echo 📋 Service URLs:
echo    Nacos Console: http://localhost:8848/nacos (nacos/nacos)
echo    API Gateway: http://localhost:9000 (Main Entry Point)
echo    Authentication Service: http://localhost:8082/auth (Direct)
echo    Platform Config Service: http://localhost:8081/platform (Direct)
echo    OData Gateway Service: http://localhost:8080/odata (Direct)
echo.
echo 🧪 Test Commands (via API Gateway):
echo    # Gateway health check
echo    curl http://localhost:9000/gateway/health
echo.
echo    # Login (admin/admin123)
echo    curl -X POST http://localhost:9000/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
echo.
echo    # Get applications (requires authentication)
echo    curl -H "Authorization: Bearer {token}" http://localhost:9000/platform/applications
echo.
echo    # Get DEMO service document (requires authentication)
echo    curl -H "Authorization: Bearer {token}" http://localhost:9000/odata/DEMO
echo.
echo    # Query products in DEMO app (requires authentication)
echo    curl -H "Authorization: Bearer {token}" http://localhost:9000/odata/DEMO/Products
echo.
echo 📊 Database Information:
echo    MySQL Host: localhost:3306
echo    Auth Database: odata_auth
echo    Platform Database: odata_platform
echo    Username: root
echo    Password: 123456
echo.
echo 📖 Check service logs in the opened terminal windows
echo.
pause