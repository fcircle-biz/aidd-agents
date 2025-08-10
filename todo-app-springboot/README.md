# Todo管理アプリケーション

Spring Bootフレームワークを使用したシンプルなTodo管理アプリケーションです。H2組み込みデータベースを採用し、RESTful APIとWebインターフェースを通じて、タスクの効率的な管理機能を提供します。

## 技術スタック

- **フレームワーク**: Spring Boot 3.x
- **言語**: Java 17
- **ビルドツール**: Maven
- **データベース**: H2 Database (組み込みモード)
- **ORM**: Spring Data JPA / Hibernate
- **Webフレームワーク**: Spring MVC
- **テンプレートエンジン**: Thymeleaf
- **API仕様**: RESTful API (JSON)
- **ロギング**: SLF4J + Logback
- **テスト**: JUnit 5, Mockito, Spring Boot Test

## 機能要件

1. **Todo作成機能** - 新しいTodoタスクの作成
2. **Todo一覧表示機能** - すべてのTodoタスクの一覧表示
3. **Todo詳細表示機能** - 特定のTodoの詳細情報表示
4. **Todo編集機能** - 既存のTodoタスクの編集
5. **Todo削除機能** - 不要なTodoタスクの削除
6. **Todo検索機能** - 特定の条件でTodoタスクを検索
7. **RESTful API提供** - 外部システム連携のためのAPI
8. **データ永続化** - H2データベースによるデータ保存

## プロジェクト構成

```
todo-app-springboot/
├── src/main/java/com/example/todoapp/
│   ├── controller/           # Presentation Layer
│   ├── service/              # Business Layer
│   │   └── impl/
│   ├── repository/           # Data Access Layer
│   ├── entity/               # Domain Model
│   ├── dto/                  # Data Transfer Objects
│   ├── exception/            # Exception Handling
│   ├── config/               # Configuration
│   ├── util/                 # Utility Classes
│   └── TodoApplication.java  # Main Application
├── src/main/resources/
│   ├── templates/            # Thymeleaf Templates
│   │   ├── layout/
│   │   ├── todo/
│   │   └── error/
│   ├── static/               # Static Resources
│   │   ├── css/
│   │   └── js/
│   └── application*.properties
└── src/test/java/            # Test Classes
```

## 開発環境構築

### 前提条件

- Java 17以上
- Maven 3.6以上

### アプリケーション起動

```bash
# Maven使用
mvn spring-boot:run

# 開発環境で起動
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# ビルドして実行
mvn clean package
java -jar target/todo-app-1.0.0.jar
```

### アクセス先

- Web UI: http://localhost:8080/todos
- H2 Console: http://localhost:8080/h2-console
- REST API: http://localhost:8080/api/todos
- Log Management API: http://localhost:8080/admin/logging/levels
- Development Tools: http://localhost:8080/dev/ (development profile only)
- Actuator Endpoints: http://localhost:8080/dev/actuator (development profile only)

## API エンドポイント

### Todo API
```
GET    /api/todos          - Todo一覧取得
GET    /api/todos/{id}     - Todo詳細取得
POST   /api/todos          - Todo作成
PUT    /api/todos/{id}     - Todo更新
DELETE /api/todos/{id}     - Todo削除
GET    /api/todos/search   - Todo検索
```

### Log Management API
```
GET    /admin/logging/levels              - ログレベル一覧取得
PUT    /admin/logging/levels/{logger}     - 個別ログレベル変更
PUT    /admin/logging/levels              - 一括ログレベル変更
POST   /admin/logging/reset               - ログレベルリセット
GET    /admin/logging/info                - ログ設定情報取得
```

## 画面構成

```
/                     - ホーム画面（一覧画面へリダイレクト）
/todos                - Todo一覧画面
/todos/new            - Todo作成画面
/todos/{id}           - Todo詳細画面
/todos/{id}/edit      - Todo編集画面
/todos/search         - Todo検索画面
```

## 開発状況

- [x] プロジェクト構造とコア設定の作成 (TASK-001)
- [x] データモデルとデータベース基盤の実装 (TASK-002)
- [x] リポジトリ層の実装 (TASK-003)
- [x] データ転送オブジェクトとマッパーの実装 (TASK-004)
- [x] サービス層の実装 (TASK-005)
- [x] 例外処理とエラーハンドリングの実装 (TASK-006)
- [x] REST APIコントローラーの実装 (TASK-007)
- [x] Webコントローラーの実装 (TASK-008)
- [x] テストスイートの実装 (TASK-009)
- [x] バリデーション機能の実装 (TASK-010)
- [x] 包括的ログシステムの実装 (TASK-011)
- [x] 開発環境最適化の実装 (TASK-012)
- [x] パフォーマンスチューニングの実装 (TASK-014)
- [x] 最終統合テストとプロダクション準備 (TASK-015)

## 統合テスト

### テスト実行

```bash
# 全てのテストを実行
mvn test

# 統合テストのみ実行
mvn test -Dtest="*IntegrationTest"

# エンドツーエンドテストのみ実行  
mvn test -Dtest="CompleteEndToEndIntegrationTest"

# システム統合テストのみ実行
mvn test -Dtest="SystemIntegrationTest"

# 特定のプロファイルでテスト実行
mvn test -Dspring.profiles.active=test
```

### テストカバレッジ

アプリケーションには包括的なテストスイートが含まれています：

- **単体テスト**: 各コンポーネント（Service、Repository、Controller）の個別機能テスト
- **統合テスト**: レイヤー間の連携テスト
- **エンドツーエンドテスト**: 完全なユーザーワークフローテスト
- **システム統合テスト**: 実際のHTTPサーバーを使用したフルシステムテスト
- **パフォーマンステスト**: キャッシュ、同時実行、レスポンス時間のテスト
- **セキュリティテスト**: SQL インジェクション、XSS 保護のテスト

## プロダクション デプロイメント

### Docker による本番環境デプロイ

#### 前提条件
- Docker Engine 20.10+
- Docker Compose 2.0+
- 4GB RAM 以上
- 10GB ディスク容量

#### クイックスタート

```bash
# リポジトリをクローン
git clone <repository-url>
cd todo-app-springboot

# プロダクション環境で起動
docker-compose up -d

# ログ確認
docker-compose logs -f todo-app

# 健全性確認
curl http://localhost:8080/actuator/health/production
```

#### アクセス先（本番環境）

- **アプリケーション**: http://localhost:8080
- **監視ダッシュボード（Grafana）**: http://localhost:3000 (admin/admin123)
- **メトリクス（Prometheus）**: http://localhost:9090
- **ヘルスチェック**: http://localhost:8080/actuator/health/production

### 本番環境設定

#### 環境変数設定

```bash
# 本番環境用 .env ファイル作成
cat > .env << EOF
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
VCS_REF=$(git rev-parse --short HEAD)
EOF
```

#### JVM チューニング

```bash
# 本番環境推奨JVM設定
JAVA_OPTS="-Xms1g -Xmx2g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+HeapDumpOnOutOfMemoryError \
           -XX:HeapDumpPath=/app/logs/heapdump.hprof"
```

### 監視とメトリクス

#### Prometheus メトリクス

- アプリケーションメトリクス: `/actuator/prometheus`
- JVM メトリクス: メモリ、GC、スレッド
- HTTP リクエストメトリクス: レスポンス時間、ステータスコード
- データベースメトリクス: コネクションプール、クエリパフォーマンス

#### Grafana ダッシュボード

自動的に設定される監視ダッシュボード：
- アプリケーション概要
- JVM パフォーマンス
- HTTP リクエストメトリクス
- データベースパフォーマンス
- システムリソース

#### ヘルスチェック

- **メインヘルス**: `/actuator/health`
- **プロダクションヘルス**: `/actuator/health/production`
- **データベースヘルス**: `/actuator/health/db`
- **ディスク容量**: `/actuator/health/diskSpace`

### バックアップとリカバリ

#### データベースバックアップ

```bash
# バックアップディレクトリ作成
mkdir -p backups

# H2データベースバックアップ
docker exec todo-app-prod cp /app/data/tododb.mv.db /tmp/
docker cp todo-app-prod:/tmp/tododb.mv.db backups/tododb-$(date +%Y%m%d-%H%M%S).mv.db
```

#### ログバックアップ

```bash
# ログバックアップ
docker cp todo-app-prod:/app/logs backups/logs-$(date +%Y%m%d-%H%M%S)
```

### トラブルシューティング

#### 一般的な問題の解決

```bash
# コンテナログ確認
docker-compose logs todo-app

# ヘルスステータス確認
curl http://localhost:8080/actuator/health

# パフォーマンスメトリクス確認
curl http://localhost:8080/dev/performance/report

# メモリ使用量確認
docker stats todo-app-prod
```

#### デバッグモード

```bash
# デバッグログ有効化
docker-compose exec todo-app java -jar app.jar --logging.level.com.example.todoapp=DEBUG
```

### セキュリティ設定

本番環境では以下のセキュリティ機能が有効です：

- CSRF保護
- SQLインジェクション対策
- XSS保護
- セキュリティヘッダー設定
- Actuatorエンドポイントの保護

### パフォーマンス最適化

本番環境では以下の最適化が適用されています：

- **データベース**: HikariCP コネクションプール、インデックス最適化
- **キャッシュ**: Caffeine による多層キャッシュ
- **HTTP**: gzip圧縮、HTTP/2サポート
- **非同期処理**: バックグラウンドタスクの非同期実行
- **監視**: リアルタイムパフォーマンス監視

### 詳細なデプロイメント情報

詳細なプロダクション デプロイメント手順については、[deployment-guide.md](deployment-guide.md) を参照してください。

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。