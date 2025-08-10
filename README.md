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

詳細については、[todo-app-springboot/README.md](todo-app-springboot/README.md) を参照してください。

## クイックスタート

### Todo管理アプリケーションの起動

Todo管理アプリケーションの詳細な起動手順、開発環境構築、テスト実行については、[todo-app-springboot/README.md](todo-app-springboot/README.md) を参照してください。


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

## .claude/agents ディレクトリの使用方法

`.claude/agents/` ディレクトリには、AIDAエージェントシステムの各段階に対応するエージェント定義ファイルを配置します。

### エージェントファイル構造

```
.claude/agents/
├── aidd-step01-requirements.md  # 要件定義書作成エージェント
├── aidd-step02-design.md         # システム設計文書作成エージェント
├── aidd-step03-task-plan.md      # 実装タスク計画エージェント
├── aidd-step04-implementation.md # コード実装エージェント
├── aidd-step05-issue-management.md # GitHub Issue管理エージェント
└── aidd-step06-pr-workflow.md    # PR作成・管理エージェント
```

### エージェントの使用方法

各エージェントは、Claude Code環境でTaskツールを使用して呼び出すことができます：

```bash
# 要件定義書作成
Task: aidd-step01-requirements
Input: 初期仕様、システム概要
Output: docs/specs/{system-name}/requirements.md

# システム設計文書作成
Task: aidd-step02-design  
Input: requirements.mdファイルパス
Output: docs/specs/{system-name}/design.md

# 実装タスク計画作成
Task: aidd-step03-task-plan
Input: design.mdファイルパス
Output: docs/specs/{system-name}/tasks.md

# コード実装
Task: aidd-step04-implementation
Input: 仕様書ファイル、実装する特定のタスク
Output: 実装されたコード

# GitHub Issue作成（日本語）
Task: aidd-step05-issue-management
Input: バグ報告、機能要求
Output: 適切にフォーマットされたGitHub Issue

# PR作成・管理（日本語）
Task: aidd-step06-pr-workflow
Input: 完了した作業内容
Output: コミット、プッシュ、プルリクエスト作成
```

### AIDD開発フロー

1. **要件分析**: 初期仕様から正式な要件定義書を作成
2. **システム設計**: 要件定義書から包括的な設計文書を作成
3. **タスク計画**: 設計文書から詳細な実装タスクリストを生成
4. **実装**: 仕様書に基づいて実際のコードを実装
5. **Issue管理**: 開発過程で発生した問題をGitHub Issueとして管理
6. **PR管理**: 完了した作業をプルリクエストとして統合

## 特徴


### AIDAエージェントシステム

- 🔄 **段階的開発**: 要件 → 設計 → タスク → 実装の構造化ワークフロー
- 📋 **仕様書駆動**: 要件→設計→タスク→実装の完全な仕様書チェーン
- 🌐 **日本語対応**: GitHubワークフロー（Issue、PR）の日本語サポート
- 🤖 **AI駆動**: Claude Codeとの統合による自動化された開発支援


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