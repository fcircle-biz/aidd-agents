# Todo App 監視ガイド

Spring Boot Actuatorを使用したTodo アプリケーションの監視方法について説明します。

## 概要

このガイドでは、Spring Boot Actuatorエンドポイントから各種監視データを取得する方法を示します。

**ベースURL**: `http://localhost:8080/actuator`

## 1. アプリケーション健康状態の確認

### ヘルスチェック
アプリケーションの全体的な健康状態を確認：
```bash
curl -s http://localhost:8080/actuator/health
```

**期待される出力例**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1081101176832,
        "free": 933196509184,
        "threshold": 10485760,
        "path": "/app/.",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## 2. JVMメモリ使用量の監視

### メモリ使用量の取得
```bash
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used
```

### メモリ使用量をMB単位で計算
```bash
MEMORY_JSON=$(curl -s http://localhost:8080/actuator/metrics/jvm.memory.used)
MEMORY_BYTES=$(echo $MEMORY_JSON | grep -o '"value":[0-9.E]*' | head -1 | cut -d: -f2)
MEMORY_MB=$(echo "scale=2; $MEMORY_BYTES / 1024 / 1024" | bc)
echo "Memory Used: ${MEMORY_MB} MB"
```

## 3. HTTPリクエスト統計の監視

### HTTPリクエスト統計の取得
```bash
curl -s http://localhost:8080/actuator/metrics/http.server.requests
```

この情報から以下を確認できます：
- リクエスト総数
- 応答時間統計
- HTTPステータス別の統計

## 4. データベース接続プールの監視

### アクティブな接続数の確認
```bash
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

### 利用可能な接続数の確認
```bash
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.idle
```

## 5. JVMスレッドの監視

### ライブスレッド数の確認
```bash
curl -s http://localhost:8080/actuator/metrics/jvm.threads.live
```

### デーモンスレッド数の確認
```bash
curl -s http://localhost:8080/actuator/metrics/jvm.threads.daemon
```

## 6. セキュリティメトリクスの監視

### セキュリティリクエスト統計
```bash
curl -s http://localhost:8080/actuator/metrics/spring.security.http.secured.requests
```

## 7. システムリソースの監視

### CPU使用率の確認
```bash
curl -s http://localhost:8080/actuator/metrics/process.cpu.usage
```

### システム全体のCPU使用率
```bash
curl -s http://localhost:8080/actuator/metrics/system.cpu.usage
```

## 8. 包括的な監視レポートの生成

### 自動化されたレポート生成
以下のスクリプトを実行して包括的な監視レポートを生成できます：

```bash
#!/bin/bash
BASE_URL="http://localhost:8080/actuator"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo "=== Todo App Monitoring Report - $TIMESTAMP ==="

echo "1. Application Health:"
curl -s "${BASE_URL}/health" | python3 -m json.tool

echo "2. JVM Memory Usage (MB):"
MEMORY_JSON=$(curl -s "${BASE_URL}/metrics/jvm.memory.used")
MEMORY_BYTES=$(echo $MEMORY_JSON | grep -o '"value":[0-9.E]*' | head -1 | cut -d: -f2)
MEMORY_MB=$(echo "scale=2; $MEMORY_BYTES / 1024 / 1024" | bc)
echo "Memory Used: ${MEMORY_MB} MB"

echo "3. HTTP Request Statistics:"
curl -s "${BASE_URL}/metrics/http.server.requests" | python3 -m json.tool

echo "4. Database Connection Pool:"
curl -s "${BASE_URL}/metrics/hikaricp.connections.active" | python3 -m json.tool

echo "5. JVM Threads:"
curl -s "${BASE_URL}/metrics/jvm.threads.live" | python3 -m json.tool

echo "6. Security Metrics:"
curl -s "${BASE_URL}/metrics/spring.security.http.secured.requests" | python3 -m json.tool

echo "7. CPU Usage:"
curl -s "${BASE_URL}/metrics/process.cpu.usage" | python3 -m json.tool

echo "=== End of Report ==="
```

## 9. 利用可能な全メトリクスの確認

### 全メトリクス一覧の取得
```bash
curl -s http://localhost:8080/actuator/metrics
```

## 10. トラブルシューティング

### 問題: エンドポイントにアクセスできない
**解決策**:
1. アプリケーションが起動していることを確認
2. ポート8080が利用可能であることを確認
3. ヘルスチェックエンドポイントで基本的な接続を確認

### 問題: JSONが読みにくい
**解決策**:
```bash
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
```
または
```bash
curl -s http://localhost:8080/actuator/health | jq .
```

---

このガイドを使用して、Todo アプリケーションの包括的な監視を行うことができます。定期的にこれらのメトリクスを確認することで、アプリケーションの健全性とパフォーマンスを維持できます。