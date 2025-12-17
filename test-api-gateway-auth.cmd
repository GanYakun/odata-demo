@echo off
echo 🧪 Testing API Gateway Authentication...

REM 测试网关健康检查
echo.
echo 1. Testing Gateway Health Check...
curl -s http://localhost:9000/gateway/health
echo.

REM 测试未认证访问（应该返回401）
echo.
echo 2. Testing Unauthorized Access (should return 401)...
curl -s http://localhost:9000/platform/applications
echo.

REM 测试登录获取令牌
echo.
echo 3. Testing Login to get JWT token...
for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:9000/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" ^| jq -r ".data.accessToken"') do set TOKEN=%%i
echo Token obtained: %TOKEN:~0,50%...
echo.

REM 测试认证访问
echo.
echo 4. Testing Authenticated Access...
curl -s -H "Authorization: Bearer %TOKEN%" http://localhost:9000/platform/applications
echo.

REM 测试OData访问
echo.
echo 5. Testing OData Access...
curl -s -H "Authorization: Bearer %TOKEN%" http://localhost:9000/odata/
echo.

REM 测试权限验证
echo.
echo 6. Testing Permission Validation...
curl -s -H "Authorization: Bearer %TOKEN%" http://localhost:9000/platform/entity-definitions
echo.

echo.
echo ✅ API Gateway Authentication Test Completed!
pause