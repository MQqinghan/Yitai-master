# 压力测试执行指南

## ✅ 已完成的优化

### 1. 系统配置优化
- ✅ 网关服务：Redis 连接池 200，Tomcat 线程 500
- ✅ 商品服务：数据库连接池 50，Redis 连接池 100，Tomcat 线程 300
- ✅ 购物车服务：数据库连接池 50，Redis 连接池 150，Tomcat 线程 300
- ✅ 用户服务：数据库连接池 50，Redis 连接池 100，Tomcat 线程 300
- ✅ 订单服务：数据库连接池 50，Redis 连接池 100，Tomcat 线程 300

### 2. 测试数据准备
- ✅ 商品数量：499 个 SKU（足够）
- ✅ 数据库：db_spzx

## 📋 测试前准备清单

### 1. 启动所有服务（按顺序）

```powershell
# 1. 确保 Nacos 已启动
docker ps | Select-String "nacos"
# 如果没启动：docker start nacos

# 2. 确保 Redis 已启动
docker ps | Select-String "redis"
# 如果没启动：docker start redis

# 3. 确保 MySQL 已启动
docker ps | Select-String "mysql"
# 如果没启动：docker start mysql

# 4. 确保 Elasticsearch 已启动（如果需要测试搜索）
docker ps | Select-String "elasticsearch"
# 如果没启动：docker start elasticsearch
```

### 2. 启动微服务

**方式 1：使用 IDEA 启动（推荐）**
- 依次启动以下主类：
  1. `ManagerApplication` (可选，管理后台)
  2. `GatewayApplication` (端口 8500) ⭐ 必须
  3. `ProductApplication` (端口 8511) ⭐ 必须
  4. `UserApplication` (端口 8512) ⭐ 必须
  5. `CartApplication` (端口 8513) ⭐ 必须
  6. `OrderApplication` (端口 8514) ⭐ 必须

**方式 2：使用 Maven 命令启动**
```powershell
# 在每个服务的目录下执行
cd E:\毕设\代码\代码\后端\代码\glxt-hou\server-gateway
mvn spring-boot:run

# 新开窗口
cd E:\毕设\代码\代码\后端\代码\glxt-hou\service\service-product
mvn spring-boot:run

# 以此类推...
```

### 3. 验证服务是否正常

等待所有服务启动完成后（看到 "Started XXXApplication" 日志），测试接口：

```powershell
# 测试商品接口
curl "http://localhost:8500/api/product/productSku/list?page=1&limit=10"

# 应该返回 JSON 数据
```

## 🚀 执行压力测试

### 方式 1：使用自动化脚本（推荐）

```powershell
# 在项目根目录执行
cd "E:\毕设\代码\代码\后端\代码\glxt-hou"

# 执行测试脚本（会自动执行 6 个场景）
.\run_pressure_test.ps1
```

**脚本会自动执行以下场景：**
1. 10 用户（预测试）
2. 50 用户（轻度负载）
3. 100 用户（中度负载）
4. 200 用户（较高负载）
5. **500 用户（目标测试）** ⭐
6. **1000 用户（目标测试）** ⭐

每个场景之间会自动等待 5-10 分钟让系统恢复。

### 方式 2：手动执行单个场景

```powershell
# 500 用户测试
E:\jmeter\apache-jmeter-5.6.3\bin\jmeter.bat -n -t "E:\毕设\代码\代码\后端\代码\glxt-hou\pressure_test_plan.jmx" -Jthreads=500 -Jloops=500 -Jrampup=60 -l "E:\毕设\压力测试\results\result_500.csv" -e -o "E:\毕设\压力测试\results\report_500"

# 1000 用户测试
E:\jmeter\apache-jmeter-5.6.3\bin\jmeter.bat -n -t "E:\毕设\代码\代码\后端\代码\glxt-hou\pressure_test_plan.jmx" -Jthreads=1000 -Jloops=500 -Jrampup=120 -l "E:\毕设\压力测试\results\result_1000.csv" -e -o "E:\毕设\压力测试\results\report_1000"
```

## 📊 查看测试结果

测试完成后，查看 HTML 报告：

```powershell
# 查看 500 用户测试报告
start "E:\毕设\压力测试\results\report_500_users\index.html"

# 查看 1000 用户测试报告
start "E:\毕设\压力测试\results\report_1000_users\index.html"
```

## 🎯 预期结果

### 500 并发用户
- ✅ 平均响应时间：< 2 秒
- ✅ 吞吐量：≥ 3000 次/分钟（50 TPS）
- ✅ 错误率：< 1%

### 1000 并发用户
- ✅ 平均响应时间：≈ 2.5 秒
- ✅ 吞吐量：≥ 2500 次/分钟（41.67 TPS）
- ✅ 错误率：< 2%

## 🔍 监控建议

测试过程中，打开任务管理器监控：
- CPU 使用率
- 内存使用情况
- 网络流量

同时可以监控数据库：
```sql
-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
```

## ⚠️ 注意事项

1. **测试前务必启动所有服务**
2. **确保有足够的测试数据（已有 499 个商品）**
3. **每次测试间隔至少 5-10 分钟**
4. **关闭不必要的程序，释放系统资源**
5. **测试期间不要进行其他 heavy 操作**

## 🛠️ 如果遇到问题

### 问题 1：连接被拒绝
```
解决：检查服务是否启动，端口是否正确
netstat -ano | findstr "8500"
```

### 问题 2：大量超时
```
解决：
1. 增加 Ramp-Up 时间
2. 减少并发用户数
3. 检查服务器性能
```

### 问题 3：内存不足
```
解决：修改各服务的 JVM 参数
-Xms2g -Xmx4g
```

## 📝 记录测试结果

测试完成后，填写以下表格：

| 并发用户 | 平均响应时间 | 95%响应时间 | 错误率 | 吞吐量(TPS) | 吞吐量(次/分) | CPU使用率 | 内存使用 |
|---------|------------|-----------|-------|-----------|-------------|----------|---------|
| 10      |            |           |       |           |             |          |         |
| 50      |            |           |       |           |             |          |         |
| 100     |            |           |       |           |             |          |         |
| 200     |            |           |       |           |             |          |         |
| 500     |            |           |       |           |             |          |         |
| 1000    |            |           |       |           |             |          |         |

---

**祝测试顺利！🎉**
