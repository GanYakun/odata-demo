@echo off
echo Starting OData Demo with MySQL Database...

echo.
echo Step 1: Initialize MySQL Database
echo Please make sure MySQL is running and execute the following command:
echo mysql -u root -p123456 < mysql-init.sql
echo.
pause

echo.
echo Step 2: Starting Platform Config Service...
start "Platform Config Service" cmd /k "cd platform-config-service && ..\mvnw.cmd spring-boot:run"

echo Waiting for Platform Config Service to start...
timeout /t 30

echo.
echo Step 3: Starting OData Gateway Service...
start "OData Gateway Service" cmd /k "cd odata-gateway && ..\mvnw.cmd spring-boot:run"

echo Waiting for OData Gateway Service to start...
timeout /t 20

echo.
echo Step 4: Starting API Gateway Service...
start "API Gateway Service" cmd /k "cd api-gateway && ..\mvnw.cmd spring-boot:run"

echo.
echo All services are starting...
echo Platform Config Service: http://localhost:8081
echo OData Gateway Service: http://localhost:8080  
echo API Gateway Service: http://localhost:9000
echo.
echo Press any key to exit...
pause