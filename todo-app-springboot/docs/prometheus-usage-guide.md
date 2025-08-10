# Prometheus監視機能 使い方ガイド

Spring Boot Todo アプリケーションのPrometheus監視機能の使用方法を説明します。

**ベースURL**: `http://localhost:8080/actuator`

## 1. 基本的な監視機能

### 利用可能なエンドポイント一覧
```bash
curl -s http://localhost:8080/actuator
```

### JVMメモリ使用量の監視
```bash
curl -s http://localhost:8080/actuator/prometheus | grep "jvm_memory_used_bytes{"
```

### HTTPリクエスト統計の監視
```bash
curl -s http://localhost:8080/actuator/prometheus | grep "http_server_requests_seconds_count"
```

### データベース接続状況の監視
```bash
curl -s http://localhost:8080/actuator/prometheus | grep "hikaricp_connections"
```

### Spring Security統計の監視
```bash
curl -s http://localhost:8080/actuator/prometheus | grep "spring_security_http_secured_requests"
```

### システムリソースの監視
```bash
curl -s http://localhost:8080/actuator/prometheus | grep -E "(disk_|process_cpu_usage)"
```

### アプリケーション健康状態の確認
```bash
curl -s http://localhost:8080/actuator/health
```

## 2. 具体的な使用例

### A. リアルタイム監視
```bash
watch -n 5 'curl -s http://localhost:8080/actuator/prometheus | grep jvm_memory_used_bytes'
```

### B. メトリクス保存
```bash
curl -s http://localhost:8080/actuator/prometheus > metrics-$(date +%Y%m%d-%H%M%S).txt
```

### C. 特定メトリクス抽出
```bash
curl -s http://localhost:8080/actuator/prometheus | grep 'spring_security'
```

### D. 外部Prometheusサーバー設定
- **メトリクスエンドポイント**: `http://localhost:8080/actuator/prometheus`
- **スクレイピング間隔**: 30秒推奨
- **データ保持期間**: 用途に応じて設定

## 3. PrometheusとGrafanaの連携

### prometheus.yml設定例
```yaml
scrape_configs:
  - job_name: 'spring-boot-todo'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    scrape_interval: 30s
```

### Grafanaダッシュボード作成におすすめのメトリクス
- **JVMメトリクス**: `jvm_memory_used_bytes`, `jvm_threads_live`
- **HTTPメトリクス**: `http_server_requests_seconds_count`
- **セキュリティメトリクス**: `spring_security_*`
- **データベースメトリクス**: `hikaricp_connections_*`

## 4. エラー対処法

### 問題: メトリクスが表示されない
**解決策**: 
```bash
curl http://localhost:8080/actuator/health
```
で健康状態を確認

### 問題: 特定メトリクスが見つからない
**解決策**: 
```bash
curl http://localhost:8080/actuator/metrics
```
で利用可能メトリクスを確認

### 問題: Prometheus形式が読めない
**解決策**: 
```bash
curl http://localhost:8080/actuator/metrics/[メトリクス名]
```
でJSON形式で取得

## 5. 現在の環境情報

- **アプリケーションURL**: http://localhost:8080
- **Prometheus監視URL**: http://localhost:8080/actuator/prometheus
- **ヘルスチェックURL**: http://localhost:8080/actuator/health

### メトリクス情報の確認
現在取得可能なメトリクス数を確認：
```bash
curl -s http://localhost:8080/actuator/prometheus | grep "^# HELP" | wc -l
curl -s http://localhost:8080/actuator/prometheus | wc -l
```

## 6. Docker環境での使用

Docker環境で実行している場合は、以下のコマンドでスクリプトを実行できます：
```bash
./docs/prometheus-usage-guide.sh
```

---

このガイドを使用して、Spring Boot Todo アプリケーションの包括的な監視を行うことができます。