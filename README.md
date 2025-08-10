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

## AIDDエージェント

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
Input: システム要件仕様
Output: docs/specs/{project}/requirements.md
[使用例]
@aidd-step01-requirements 
システム名: product-management-system
システム概要: 商品の在庫管理を効率化するWebアプリケーション
主要機能リスト: 商品登録, 商品一覧, 商品検索, 商品編集, 商品削除
技術スタック: Streamlit, SQLModel, SQLite, pandas
ユーザータイプ: 管理者, システム管理者

# システム設計文書作成
Task: aidd-step02-design  
Input: requirements.mdファイルパス（オプション）
Output: docs/specs/{project}/design.md
[使用例]
@aidd-step02-design 
※inputが未入力の場合、docs/specs/{project}/requirements.mdを参照

# 実装タスク計画作成
Task: aidd-step03-task-plan
Input: requirements.md（オプション）,design.mdファイルパス（オプション）
Output: docs/specs/{project}/tasks.md
[使用例]
@aidd-step03-task-plan
※inputが未入力の場合、docs/specs/{project}/requirements.md,design.mdを参照

# コード実装
Task: aidd-step04-implementation
Input: tasks.mdファイルパス（オプション）、実装する特定のタスク
Output: 実装されたコード
[使用例]
@aidd-step04-implementation TASK-001

# GitHub Issue作成
Task: aidd-step05-issue-management
Input: バグ報告、機能要求
Output: 適切にフォーマットされたGitHub Issue
[使用例]
@aidd-step05-issue-management 不具合:○○でエラー

# PR作成・管理
Task: aidd-step06-pr-workflow
Input: 完了した作業内容（オプション）
Output: コミット、プッシュ、プルリクエスト作成
[使用例]
@aidd-step06-pr-workflow
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


## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。詳細は [LICENSE](LICENSE) ファイルを参照してください。

## サポート

- **Issue**: [GitHub Issues](https://github.com/your-org/aidd-agents/issues)
- **ドキュメント**: `docs/` ディレクトリ内の各種ドキュメント
- **設定ガイド**: `CLAUDE.md` でClaude Codeでの使用方法を確認