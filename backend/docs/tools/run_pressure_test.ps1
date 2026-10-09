# JMeter 压力测试执行脚本

$JMETER_PATH = "E:\jmeter\apache-jmeter-5.6.3\bin\jmeter.bat"
$TEST_PLAN = "E:\毕设\代码\代码\后端\代码\glxt-hou\pressure_test_plan.jmx"
$RESULT_DIR = "E:\毕设\压力测试\results"

Write-Host "========================================" -ForegroundColor Green
Write-Host "  购物车智能决策系统 - 压力测试" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 创建结果目录
New-Item -ItemType Directory -Force -Path $RESULT_DIR | Out-Null

# 定义测试场景
$scenarios = @(
    @{threads=10; loops=50; rampup=5; name="10_users"},
    @{threads=50; loops=100; rampup=10; name="50_users"},
    @{threads=100; loops=200; rampup=15; name="100_users"},
    @{threads=200; loops=300; rampup=30; name="200_users"},
    @{threads=500; loops=500; rampup=60; name="500_users"},
    @{threads=1000; loops=500; rampup=120; name="1000_users"}
)

foreach ($scenario in $scenarios) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "开始测试：${$scenario.name}" -ForegroundColor Yellow
    Write-Host "并发用户数：${$scenario.threads}" -ForegroundColor Yellow
    Write-Host "循环次数：${$scenario.loops}" -ForegroundColor Yellow
    Write-Host "Ramp-Up时间：${$scenario.rampup}秒" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    
    $resultFile = "$RESULT_DIR\result_${$scenario.name}.csv"
    $reportDir = "$RESULT_DIR\report_${$scenario.name}"
    
    # 删除旧的报告目录
    if (Test-Path $reportDir) {
        Remove-Item -Recurse -Force $reportDir
    }
    
    Write-Host "执行 JMeter 测试..." -ForegroundColor Gray
    
    # 执行测试
    & $JMETER_PATH -n -t $TEST_PLAN `
        -Jthreads=$scenario.threads `
        -Jloops=$scenario.loops `
        -Jrampup=$scenario.rampup `
        -l $resultFile `
        -e -o $reportDir
    
    Write-Host "✅ 测试完成：${$scenario.name}" -ForegroundColor Green
    Write-Host "📊 报告位置：$reportDir\index.html" -ForegroundColor Cyan
    Write-Host ""
    
    # 等待一段时间让系统恢复（最后一个测试不等待）
    if ($scenario.name -ne "1000_users") {
        $waitMinutes = if ($scenario.threads -ge 500) { 10 } else { 5 }
        Write-Host "⏳ 等待 $waitMinutes 分钟后继续下一个测试..." -ForegroundColor Gray
        Start-Sleep -Seconds ($waitMinutes * 60)
    }
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "  🎉 所有测试完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "测试结果位置：$RESULT_DIR" -ForegroundColor Cyan
Write-Host ""
Write-Host "查看报告命令：" -ForegroundColor Yellow
Write-Host "start `"E:\毕设\压力测试\results\report_500_users\index.html`"" -ForegroundColor White
Write-Host "start `"E:\毕设\压力测试\results\report_1000_users\index.html`"" -ForegroundColor White
