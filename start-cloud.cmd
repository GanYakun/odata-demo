@echo off
echo 🚀 Starting OData Cloud Platform...

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed or not in PATH. Please install Java 8+ first.
    pause
    exit /b 1
)

REM 检查Maven环境
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed or not in PATH. Please install Maven first.
    pause
    exit /b 1
)

REM 构建项目
echo 📦 Building projects...
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo ❌ Build failed. Please check the errors above.
    pause
    exit /b 1
)

echo ✅ Build completed successfully!

REM 启动Nacos
echo 🌐 Starting Nacos Server...
cd nacos/nacos-server/bin
start "Nacos Server" cmd /c "startup.cmd -m standalone"
cd ../../../

REM 等待Nacos启动
echo ⏳ Waiting for Nacos to start (30 seconds)...
timeout /t 30 /nobreak >nul

REM 启动平台配置服务
echo 🔧 Starting Platform Config Service...
cd platform-config-service
start "Platform Config Service" cmd /c "mvn spring-boot:run"
cd ..

REM 等待平台配置服务启动
echo ⏳ Waiting for Platform Config Service to start (20 seconds)...
timeout /t 20 /nobreak >nul

REM 启动OData网关服务
echo 🌐 Starting OData Gateway Service...
cd odata-gateway
start "OData Gateway Service" cmd /c "mvn spring-boot:run"
cd ..

echo ⏳ Waiting for all services to start (30 seconds)...
timeout /t 30 /nobreak >nul

echo.
echo 🎉 OData Cloud Platform started successfully!
echo.
echo 📋 Service URLs:
echo    Nacos Console: http://localhost:8848/nacos (nacos/nacos)
echo    Platform Config Service: http://localhost:8081/platform
echo    OData Gateway Service: http://localhost:8080/odata
echo.
echo 🧪 Test Commands:
echo    # Get applications
echo    curl http://localhost:8081/platform/applications
echo.
echo    # Get DEMO service document
echo    curl http://localhost:8080/odata/DEMO
echo.
echo    # Query products in DEMO app
echo    curl http://localhost:8080/odata/DEMO/Products
echo.
echo 📖 Check service logs in the opened terminal windows
echo.
pause