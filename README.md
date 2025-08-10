# AIDD Agents - AI駆動開発エージェントシステム

AI駆動開発（AI-Driven Development）を支援する専門エージェントシステムと、実装例としてのTodo管理アプリケーションを含むプロジェクトです。

## プロジェクト概要

このリポジトリは、AI駆動開発のワークフローを支援する以下のコンポーネントを提供します：

### 🤖 AIDAエージェントシステム

ソフトウェア開発の各フェーズに特化した6つの専門エージェント：

- **要件分析エージェント** (`aidd-step01-requirements`) - 初期仕様を正式な要件定義書に変換
- **システム設計エージェント** (`aidd-step02-design`) - 要件から包括的な設計文書を作成
- **タスク計画エージェント** (`aidd-step03-task-plan`) - 設計から詳細な実装タスクを生成
- **実装エージェント** (`aidd-step04-implementation`) - 仕様書から実際のコード実装を実行
- **Issue管理エージェント** (`aidd-step05-issue-management`) - GitHub Issue作成・管理（日本語対応）
- **PR管理エージェント** (`aidd-step06-pr-workflow`) - コミット・プッシュ・プルリクエスト作成（日本語対応）

### 📋 開発仕様書

実際の開発プロセスで作成された仕様書（`/docs/specs/`）：

- `requirements.md` - 要件定義書
- `design.md` - システム設計文書
- `tasks.md` - 実装タスク計画書

### 📝 Todo管理アプリケーション

**AIDAエージェントで作成されたプロジェクト例** - Spring Boot 3.x + Java 17で実装された本格的なTodo管理システム（`/todo-app-springboot/`）：

このアプリケーションは、AIDAエージェントシステムの全工程（要件定義 → 設計 → タスク計画 → 実装）を通じて作成された実例です。

- **機能**: CRUD操作、バリデーション、エラーハンドリング、REST API、Webインターフェース
- **アーキテクチャ**: レイヤードアーキテクチャ（Controller → Service → Repository → Entity）
- **技術スタック**: Spring Boot, Spring Data JPA, Thymeleaf, H2 Database, Maven
- **監視**: Actuator、Prometheus、Grafanaダッシュボード
- **開発プロセス**: AIDAエージェント6段階による完全自動化開発

## クイックスタート

### 前提条件

- Java 17以上
- Maven 3.6以上
- Docker & Docker Compose（本番デプロイ用）

### Todo管理アプリケーションの起動

```bash
# リポジトリをクローン
git clone <repository-url>
cd aidd-agents/todo-app-springboot

# 開発モードで起動
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# または通常モードで起動
mvn spring-boot:run
```

### アクセス先

起動後、以下のURLでアプリケーションにアクセスできます：

- **Webアプリケーション**: http://localhost:8080/todos
- **REST API**: http://localhost:8080/api/todos
- **H2データベースコンソール**: http://localhost:8080/h2-console
- **開発ツール** (devプロファイル): http://localhost:8080/dev/
- **Actuator監視**: http://localhost:8080/dev/actuator

## 本番環境デプロイメント

### Dockerを使用したデプロイ

```bash
cd todo-app-springboot

# 本番環境で起動
docker-compose up -d

# ログ確認
docker-compose logs -f todo-app

# ヘルスチェック
curl http://localhost:8080/actuator/health/production
```

### 監視ダッシュボード

- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090

## 開発ガイド

### テスト実行

```bash
cd todo-app-springboot

# 全テスト実行
mvn test

# 統合テストのみ
mvn test -Dtest="*IntegrationTest"

# エンドツーエンドテストのみ
mvn test -Dtest="CompleteEndToEndIntegrationTest"
```

### AIDAエージェントの使用方法

AIDAエージェントは、Claude Code環境でTaskツールを使用して呼び出します：

```markdown
# 要件定義書作成
- エージェント: aidd-step01-requirements
- 入力: 初期仕様、システム概要
- 出力: docs/specs/{system-name}/requirements.md
- 実例: docs/specs/todo-app-springboot/requirements.md

# システム設計文書作成  
- エージェント: aidd-step02-design
- 入力: requirements.md
- 出力: docs/specs/{system-name}/design.md
- 実例: docs/specs/todo-app-springboot/design.md

# 実装タスク計画作成
- エージェント: aidd-step03-task-plan  
- 入力: design.md
- 出力: docs/specs/{system-name}/tasks.md
- 実例: docs/specs/todo-app-springboot/tasks.md

# コード実装
- エージェント: aidd-step04-implementation
- 入力: 仕様書、特定タスク
- 出力: 実装コード

# GitHub Issue作成
- エージェント: aidd-step05-issue-management
- 機能: バグ報告、機能要求の日本語Issue作成

# PR管理
- エージェント: aidd-step06-pr-workflow  
- 機能: コミット、プッシュ、PR作成（日本語）
```

### プロファイル別設定

- **default**: 基本設定
- **dev**: 開発モード（デバッグ機能、H2コンソール、開発用エンドポイント）
- **prod**: 本番モード（セキュリティ、監視、パフォーマンス最適化）
- **test**: テスト設定（インメモリデータベース）

## プロジェクト構造

```
aidd-agents/
├── docs/specs/                    # システム仕様書
│   └── todo-app-springboot/       # Todo管理アプリの開発仕様書
│       ├── requirements.md        # 要件定義書
│       ├── design.md              # システム設計文書
│       └── tasks.md               # 実装タスク計画書
├── todo-app-springboot/           # Todo管理アプリ実装
│   ├── src/main/java/com/example/todoapp/
│   │   ├── controller/           # コントローラー層
│   │   ├── service/             # サービス層
│   │   ├── repository/          # リポジトリ層
│   │   ├── entity/              # エンティティ
│   │   ├── dto/                 # データ転送オブジェクト
│   │   ├── config/              # 設定クラス
│   │   └── util/                # ユーティリティ
│   ├── src/main/resources/
│   │   ├── templates/           # Thymeleafテンプレート
│   │   └── static/              # 静的リソース
│   ├── monitoring/              # Grafana/Prometheus設定
│   ├── logs/                    # アプリケーションログ
│   └── docker-compose.yml       # 本番デプロイ設定
└── CLAUDE.md                     # Claude Code用ガイド
```

## API仕様

### Todo管理API

| Method | Endpoint | 説明 |
|--------|----------|------|
| GET | `/api/todos` | Todo一覧取得 |
| GET | `/api/todos/{id}` | Todo詳細取得 |
| POST | `/api/todos` | Todo作成 |
| PUT | `/api/todos/{id}` | Todo更新 |
| DELETE | `/api/todos/{id}` | Todo削除 |
| GET | `/api/todos/search` | Todo検索 |

### ログ管理API

| Method | Endpoint | 説明 |
|--------|----------|------|
| GET | `/admin/logging/levels` | ログレベル一覧 |
| PUT | `/admin/logging/levels/{logger}` | ログレベル変更 |
| POST | `/admin/logging/reset` | ログレベルリセット |

## 特徴

### Todo管理アプリケーション

- ✅ **包括的CRUD操作**: 作成、読取、更新、削除機能
- ✅ **バリデーション**: カスタムバリデータと包括的な入力検証
- ✅ **エラーハンドリング**: グローバル例外処理とユーザーフレンドリーなエラーメッセージ
- ✅ **デュアルインターフェース**: REST APIとThymeleaf Webインターフェース
- ✅ **セキュリティ**: CSRF保護、SQLインジェクション対策、XSS保護
- ✅ **監視**: Actuator、Prometheus、Grafanaによる包括的監視
- ✅ **テスト**: 単体・統合・エンドツーエンドテストの完全なスイート
- ✅ **本番対応**: Docker、環境別設定、パフォーマンス最適化

### AIDAエージェントシステム

- 🔄 **段階的開発**: 要件 → 設計 → タスク → 実装の構造化ワークフロー
- 📋 **仕様書駆動**: 要件→設計→タスク→実装の完全な仕様書チェーン
- 🌐 **日本語対応**: GitHubワークフロー（Issue、PR）の日本語サポート
- 🤖 **AI駆動**: Claude Codeとの統合による自動化された開発支援

## トラブルシューティング

### よくある問題

1. **ポート競合**: 8080ポートが使用中の場合
   ```bash
   mvn spring-boot:run -Dserver.port=8081
   ```

2. **Java バージョン**: Java 17が必要
   ```bash
   java --version
   mvn -version
   ```

3. **Docker メモリ**: 最低4GB RAM推奨
   ```bash
   docker stats
   ```

4. **ログ確認**: 
   ```bash
   # 開発環境
   tail -f todo-app-springboot/logs/todo-app.log
   
   # Docker環境
   docker-compose logs -f todo-app
   ```

## 貢献

1. このリポジトリをフォーク
2. 機能ブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'Add some amazing feature'`)
4. ブランチにプッシュ (`git push origin feature/amazing-feature`)
5. プルリクエストを開く

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。詳細は [LICENSE](LICENSE) ファイルを参照してください。

## サポート

- **Issue**: [GitHub Issues](https://github.com/your-org/aidd-agents/issues)
- **ドキュメント**: `docs/` ディレクトリ内の各種ドキュメント
- **設定ガイド**: `CLAUDE.md` でClaude Codeでの使用方法を確認