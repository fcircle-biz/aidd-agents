# Todo アプリケーション - 本番デプロイメントガイド

## 概要

このガイドでは、DockerとPrometheus/Grafanaを使用した本番環境でのTodoアプリケーションのデプロイメント方法について包括的な手順を提供します。

## 前提条件

- Docker Engine 20.10以上
- Docker Compose 2.0以上
- 最低4GB RAM（8GB推奨）
- 10GBのディスク容量
- ポート80、8080、3000、9090へのネットワークアクセス

## クイックスタート

### 1. クローンとビルド

```bash
git clone <リポジトリURL>
cd todo-app-springboot
```

### 2. 本番デプロイメント

```bash
# 全サービスをビルドして開始
docker-compose up -d

# ログを表示
docker-compose logs -f todo-app

# ヘルスステータスを確認
docker-compose ps
```

### 3. アプリケーションアクセス

- **Todoアプリケーション**: http://localhost:8080
- **Grafanaダッシュボード**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **APIドキュメント**: http://localhost:8080/actuator

## 詳細デプロイメント手順

### ステップ 1: 環境設定

本番環境変数を作成：

```bash
# 本番用の.envファイルを作成
cat > .env << EOF
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
VCS_REF=$(git rev-parse --short HEAD)
EOF
```

### ステップ 2: データベース設定

アプリケーションは永続化ストレージ付きH2データベースを使用：

```yaml
volumes:
  - todo-data:/app/data  # 永続化データベースストレージ
  - todo-logs:/app/logs  # アプリケーションログ
```

### ステップ 3: アプリケーションデプロイ

```bash
# アプリケーションイメージをビルド
docker build -t todo-app:latest .

# 監視スタックと共に開始
docker-compose up -d

# デプロイメントを検証
curl -f http://localhost:8080/actuator/health/production
```

### ステップ 4: 監視設定

#### Prometheus設定

以下から自動的にメトリクスが収集されます：
- アプリケーションメトリクス: `/actuator/prometheus`
- JVMメトリクス: メモリ、GC、スレッド
- HTTPリクエストメトリクス: レスポンス時間、ステータスコード
- データベースメトリクス: コネクションプール、クエリ性能

#### Grafanaダッシュボード

デフォルトダッシュボードには以下が含まれます：
- アプリケーション概要
- JVM性能
- HTTPリクエストメトリクス
- データベース性能
- システムリソース

### ステップ 5: セキュリティ設定

#### アプリケーションセキュリティ

- CSRF保護有効
- SQLインジェクション保護
- XSS保護
- セキュリティヘッダー設定
- Actuatorエンドポイントセキュア化

#### コンテナセキュリティ

- 非rootユーザー実行
- 最小ベースイメージ
- セキュリティスキャン推奨

## 本番設定

### JVMチューニング

```bash
# 本番環境推奨JVM設定
JAVA_OPTS="-Xms1g -Xmx2g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+HeapDumpOnOutOfMemoryError \
           -XX:HeapDumpPath=/app/logs/heapdump.hprof \
           -Dspring.profiles.active=prod"
```

### データベース最適化

```properties
# 本番データベース設定 (application-prod.properties)
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

### ログ設定

```properties
# 本番ログレベル
logging.level.com.example.todoapp=INFO
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN
logging.file.name=/app/logs/todo-app.log
logging.file.max-size=100MB
logging.file.max-history=30
```

## ヘルスチェックと監視

### アプリケーションヘルスエンドポイント

- **メインヘルス**: `/actuator/health`
- **本番ヘルス**: `/actuator/health/production`
- **データベースヘルス**: `/actuator/health/db`
- **ディスク容量**: `/actuator/health/diskSpace`

### 監視エンドポイント

- **メトリクス**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`
- **情報**: `/actuator/info`
- **環境**: `/actuator/env`

### 監視すべき主要メトリクス

1. **アプリケーションメトリクス**
   - リクエスト数とレスポンス時間
   - エンドポイント別エラー率
   - データベースコネクションプール使用量
   - キャッシュヒット率

2. **JVMメトリクス**
   - ヒープ・非ヒープメモリ使用量
   - ガベージコレクション統計
   - スレッド数と状態

3. **システムメトリクス**
   - CPU使用率
   - ディスク容量使用量
   - ネットワークI/O

## バックアップとリカバリ

### データベースバックアップ

```bash
# バックアップディレクトリを作成
mkdir -p backups

# データベースバックアップ（H2）
docker exec todo-app-prod cp /app/data/tododb.mv.db /tmp/
docker cp todo-app-prod:/tmp/tododb.mv.db backups/tododb-$(date +%Y%m%d-%H%M%S).mv.db
```

### アプリケーションログバックアップ

```bash
# ログバックアップ
docker cp todo-app-prod:/app/logs backups/logs-$(date +%Y%m%d-%H%M%S)
```

## スケーリング考慮事項

### 水平スケーリング

複数インスタンスの場合：

```yaml
todo-app:
  deploy:
    replicas: 3
  environment:
    - SPRING_PROFILES_ACTIVE=prod,cluster
```

### ロードバランシング

nginxまたは同等のものを使用：

```bash
# nginx プロキシを有効化
docker-compose --profile with-proxy up -d
```

## トラブルシューティング

### 一般的な問題

1. **コンテナが起動しない**
   ```bash
   docker-compose logs todo-app
   docker exec todo-app-prod java -version
   ```

2. **データベース接続問題**
   ```bash
   docker exec todo-app-prod ls -la /app/data/
   curl http://localhost:8080/actuator/health/db
   ```

3. **メモリ使用量が多い**
   ```bash
   docker stats todo-app-prod
   curl http://localhost:8080/actuator/metrics/jvm.memory.used
   ```

4. **性能問題**
   ```bash
   curl http://localhost:8080/dev/performance/report
   curl http://localhost:8080/actuator/metrics/http.server.requests
   ```

### デバッグモード

デバッグログを有効化：

```bash
docker-compose exec todo-app java -jar app.jar --logging.level.com.example.todoapp=DEBUG
```

## セキュリティベストプラクティス

1. **環境変数**: 秘密情報は環境変数に保存
2. **ネットワークセキュリティ**: 内部Dockerネットワークを使用
3. **定期更新**: ベースイメージを最新に保つ
4. **セキュリティスキャン**: イメージの脆弱性をスキャン
5. **アクセス制御**: actuatorエンドポイントアクセスを制限
6. **SSL/TLS**: 本番環境でHTTPSを使用（nginxプロキシ）

## 性能最適化

### データベース最適化

- コネクションプール設定済み
- データベースインデックス実装済み
- クエリ最適化有効

### キャッシュ戦略

- Caffeineを使用したアプリケーションレベルキャッシング
- HTTPレスポンスキャッシング
- 静的リソースキャッシング

### JVM最適化

- G1ガベージコレクター
- 負荷に基づくヒープサイジング
- ガベージコレクションチューニング

## メンテナンス

### 定期タスク

1. **ログローテーション**: 自動設定済み
2. **ヘルスチェック**: Grafana経由で監視
3. **バックアップスケジュール**: 自動バックアップを実装
4. **性能レビュー**: 週次性能レポート
5. **セキュリティ更新**: 月次依存関係更新

### 監視アラート

以下に対してアラートを設定：
- アプリケーションダウンタイム
- 高エラー率
- メモリ/CPUしきい値
- データベース接続問題
- ディスク容量警告

## サポートとドキュメント

- **アプリケーションログ**: `/app/logs/todo-app.log`
- **エラーログ**: `/app/logs/todo-app-error.log`
- **性能ログ**: `/app/logs/performance.log`
- **監査ログ**: `/app/logs/audit.log`

追加サポートについては、デプロイメントに付属する包括的なログ機能と監視ダッシュボードを確認してください。