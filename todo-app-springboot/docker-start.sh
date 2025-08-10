#!/bin/bash

# Spring Boot + Prometheus + Grafana 完全Docker環境起動

echo "🚀 Todo App + 監視環境 Docker起動"
echo

# 現在のSpring Boot停止確認
if pgrep -f "spring-boot:run" > /dev/null; then
    echo "⚠️  現在のSpring Bootプロセス（maven）が動作中です"
    echo "   Docker環境と競合するため、Ctrl+C で停止してください"
    echo "   または別ターミナルで実行してください"
    echo
fi

echo "🐳 Docker環境起動中..."
docker-compose up -d

echo
echo "⏳ サービス起動待機中..."
sleep 15

echo
echo "🔍 サービス確認中..."

# Health checks
services_ready=0
if docker exec todo-app-prod curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Todo Application稼働中"
    ((services_ready++))
else
    echo "❌ Todo Application起動失敗"
fi

if curl -s http://localhost:9090/api/v1/status/buildinfo > /dev/null 2>&1; then
    echo "✅ Prometheus稼働中"
    ((services_ready++))
else
    echo "❌ Prometheus起動失敗"
fi

if curl -s http://localhost:3000/api/health > /dev/null 2>&1; then
    echo "✅ Grafana稼働中"
    ((services_ready++))
else
    echo "❌ Grafana起動失敗"
fi

echo
if [ $services_ready -eq 3 ]; then
    echo "🎉 全サービス正常起動完了！"
else
    echo "⚠️  一部サービスで問題が発生している可能性があります"
    echo "   詳細ログ: docker-compose logs -f"
fi

echo
echo "📋 アクセス情報:"
echo "┌─────────────────────────────────────────────┐"
echo "│ 🎯 Todo Application                         │"
echo "│    http://localhost:8080                    │"
echo "│    (Dockerコンテナ版)                       │"
echo "├─────────────────────────────────────────────┤"
echo "│ 📈 Prometheus監視                          │"
echo "│    http://localhost:9090                    │"
echo "│    Targets: http://localhost:9090/targets   │"
echo "├─────────────────────────────────────────────┤"
echo "│ 📊 Grafana ダッシュボード                   │"
echo "│    http://localhost:3000                    │"
echo "│    ユーザー: admin / パスワード: admin123    │"
echo "└─────────────────────────────────────────────┘"

echo
echo "🎯 Grafana初期設定:"
echo "1. http://localhost:3000 でGrafana開く"
echo "2. admin/admin123でログイン"
echo "3. データソースPrometheus自動設定済み"
echo "4. ダッシュボード作成またはインポート (ID: 6756, 4701推奨)"

echo
echo "🔧 管理コマンド:"
echo "  全停止: docker-compose down"
echo "  ログ確認: docker-compose logs -f"
echo "  再起動: docker-compose restart"
echo "  状態確認: docker-compose ps"

echo
echo "✅ Docker環境起動完了！"