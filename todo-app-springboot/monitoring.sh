#!/bin/bash

# Todo App Monitoring Script
# SpringBoot Actuatorから監視データを取得

BASE_URL="http://localhost:8080/actuator"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo "=== Todo App Monitoring Report - $TIMESTAMP ==="
echo

# 1. ヘルスチェック
echo "1. Application Health:"
curl -s "${BASE_URL}/health" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/health"
echo
echo

# 2. JVM メトリクス
echo "2. JVM Memory Usage (MB):"
MEMORY_JSON=$(curl -s "${BASE_URL}/metrics/jvm.memory.used")
MEMORY_BYTES=$(echo $MEMORY_JSON | grep -o '"value":[0-9.E]*' | head -1 | cut -d: -f2)
MEMORY_MB=$(echo "scale=2; $MEMORY_BYTES / 1024 / 1024" | bc 2>/dev/null || echo "N/A")
echo "Memory Used: ${MEMORY_MB} MB"
echo

# 3. HTTPリクエスト統計
echo "3. HTTP Request Statistics:"
curl -s "${BASE_URL}/metrics/http.server.requests" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/metrics/http.server.requests"
echo
echo

# 4. データベース接続状況
echo "4. Database Connection Pool:"
curl -s "${BASE_URL}/metrics/hikaricp.connections.active" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/metrics/hikaricp.connections.active"
echo
echo

# 5. スレッド情報
echo "5. JVM Threads:"
curl -s "${BASE_URL}/metrics/jvm.threads.live" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/metrics/jvm.threads.live"
echo
echo

# 6. セキュリティメトリクス
echo "6. Security Metrics:"
curl -s "${BASE_URL}/metrics/spring.security.http.secured.requests" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/metrics/spring.security.http.secured.requests"
echo
echo

# 7. CPU使用率
echo "7. CPU Usage:"
curl -s "${BASE_URL}/metrics/process.cpu.usage" | python3 -m json.tool 2>/dev/null || curl -s "${BASE_URL}/metrics/process.cpu.usage"
echo

echo "=== End of Report ==="