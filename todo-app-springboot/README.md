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

## API エンドポイント

```
GET    /api/todos          - Todo一覧取得
GET    /api/todos/{id}     - Todo詳細取得
POST   /api/todos          - Todo作成
PUT    /api/todos/{id}     - Todo更新
DELETE /api/todos/{id}     - Todo削除
GET    /api/todos/search   - Todo検索
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
- [ ] データモデルとデータベース基盤の実装 (TASK-002)
- [ ] リポジトリ層の実装 (TASK-003)
- [ ] データ転送オブジェクトとマッパーの実装 (TASK-004)
- [ ] サービス層の実装 (TASK-005)
- [ ] 例外処理とエラーハンドリングの実装 (TASK-006)
- [ ] REST APIコントローラーの実装 (TASK-007)
- [ ] Webコントローラーの実装 (TASK-008)
- [ ] テスト実装 (TASK-009+)

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。