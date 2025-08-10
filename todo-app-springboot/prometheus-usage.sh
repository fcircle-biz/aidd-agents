#!/bin/bash

# Prometheus監視機能使い方ガイド
# Spring Boot Todo アプリケーション

BASE_URL="http://localhost:8080/actuator"
echo "=== Prometheus監視機能 使い方ガイド ==="
echo "ベースURL: $BASE_URL"
echo

# 1. 全エンドポイント確認
echo "1. 利用可能なエンドポイント一覧:"
curl -s "${BASE_URL}" | python3 -m json.tool 2>/dev/null | grep href || curl -s "${BASE_URL}"
echo
echo "-----------------------------"

# 2. JVMメモリ監視
echo "2. JVMメモリ使用量 (Prometheus形式):"
curl -s "${BASE_URL}/prometheus" | grep "jvm_memory_used_bytes{"
echo
echo "-----------------------------"

# 3. HTTPリクエスト監視
echo "3. HTTPリクエスト統計 (Prometheus形式):"
curl -s "${BASE_URL}/prometheus" | grep "http_server_requests_seconds_count"
echo
echo "-----------------------------"

# 4. データベース接続監視
echo "4. データベース接続状況 (Prometheus形式):"
curl -s "${BASE_URL}/prometheus" | grep "hikaricp_connections"
echo
echo "-----------------------------"

# 5. Spring Security監視
echo "5. Spring Security統計 (Prometheus形式):"
curl -s "${BASE_URL}/prometheus" | grep "spring_security_http_secured_requests"
echo
echo "-----------------------------"

# 6. システムリソース監視
echo "6. システムリソース (Prometheus形式):"
curl -s "${BASE_URL}/prometheus" | grep -E "(disk_|process_cpu_usage)"
echo
echo "-----------------------------"

# 7. アプリケーションヘルス
echo "7. アプリケーション健康状態:"
curl -s "${BASE_URL}/health"
echo
echo "-----------------------------"

echo
echo "=== 具体的な使用例 ==="
echo
echo "A. リアルタイム監視:"
echo "watch -n 5 'curl -s ${BASE_URL}/prometheus | grep jvm_memory_used_bytes'"
echo
echo "B. メトリクス保存:"
echo "curl -s ${BASE_URL}/prometheus > metrics-\$(date +%Y%m%d-%H%M%S).txt"
echo
echo "C. 特定メトリクス抽出:"
echo "curl -s ${BASE_URL}/prometheus | grep 'spring_security'"
echo
echo "D. 外部Prometheusサーバー設定用:"
echo "  - メトリクスエンドポイント: ${BASE_URL}/prometheus"
echo "  - スクレイピング間隔: 30秒推奨"
echo "  - データ保持期間: 用途に応じて設定"
echo

echo "=== PrometheusとGrafanaの連携 ==="
echo
echo "1. prometheus.yml設定例:"
cat << 'EOF'
scrape_configs:
  - job_name: 'spring-boot-todo'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    scrape_interval: 30s
EOF
echo
echo "2. Grafanaダッシュボード作成:"
echo "   - JVMメトリクス: jvm_memory_used_bytes, jvm_threads_live"
echo "   - HTTPメトリクス: http_server_requests_seconds_count"
echo "   - セキュリティメトリクス: spring_security_*"
echo "   - データベースメトリクス: hikaricp_connections_*"
echo

echo "=== エラー対処法 ==="
echo
echo "問題: メトリクスが表示されない"
echo "解決: curl ${BASE_URL}/health で健康状態確認"
echo
echo "問題: 特定メトリクスが見つからない"
echo "解決: curl ${BASE_URL}/metrics で利用可能メトリクス確認"
echo
echo "問題: Prometheus形式が読めない"
echo "解決: curl ${BASE_URL}/metrics/[メトリクス名] でJSON形式取得"
echo

echo "=== 実行中のアプリケーション情報 ==="
echo "アプリケーションURL: http://localhost:8080"
echo "Prometheus監視URL: http://localhost:8080/actuator/prometheus"
echo "ヘルスチェックURL: http://localhost:8080/actuator/health"
echo
echo "現在取得可能なメトリクス数:"
curl -s "${BASE_URL}/prometheus" | grep "^# HELP" | wc -l | xargs echo -n && echo " 個のメトリクス"
curl -s "${BASE_URL}/prometheus" | wc -l | xargs echo -n && echo " 行のデータ"
echo
echo "=== ガイド終了 ==="