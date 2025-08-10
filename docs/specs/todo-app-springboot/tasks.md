# 実装計画

## プロジェクト基盤

- [x] TASK-001: プロジェクト構造とコア設定の作成
  - Spring Initializrを使用してプロジェクトを生成
  - 必要な依存関係（Web, Data JPA, H2, Thymeleaf, Validation, DevTools）を追加
  - プロジェクトディレクトリ構造（controller, service, repository, entity, dto, exception, config）の作成
  - _要件: 要件11（開発環境セットアップ）_
  - **実装ノート**: 完全なMavenプロジェクト構造を作成。Spring Boot 3.x、Java 17設定完了。全パッケージ構造と設定ファイルを配置済み。

  - [x] TASK-001.1: Mavenプロジェクトの初期化
    - pom.xmlへの依存関係追加（Spring Boot 3.x, Java 17）
    - Spring Boot Starter Parent設定
    - _要件: 要件11_
  
  - [x] TASK-001.2: アプリケーション設定ファイルの作成
    - application.propertiesの作成（ポート設定、コンテキストパス）
    - application-dev.properties（開発環境設定）
    - application-prod.properties（本番環境設定）
    - _要件: 要件11_

  - [x] TASK-001.3: メインアプリケーションクラスの作成
    - TodoApplication.javaの作成（@SpringBootApplication）
    - メインメソッドの実装
    - _要件: 要件11_

## データ基盤

- [x] TASK-002: データモデルとデータベース基盤の実装
  - H2データベースの設定とJPA設定の実装
  - エンティティクラスとデータベーステーブルのマッピング
  - データベース初期化とマイグレーション設定
  - _要件: 要件8（データ永続化）_
  - **実装ノート**: H2データベース設定完了。Todoエンティティ、TodoStatus/TodoPriority Enumクラス、DatabaseConfig設定クラスを作成。JPA/@Entity設定済み。アプリケーション起動テスト成功。データベーステーブル自動生成確認済み。

  - [x] TASK-002.1: H2データベース接続設定
    - application.propertiesにH2データベース設定を追加
    - H2コンソール設定（/h2-console）の有効化
    - ファイルモードでのデータ永続化設定
    - _要件: 要件8, 要件11_
  
  - [x] TASK-002.2: Todoエンティティクラスの作成
    - com.example.todoapp.entity.Todo.javaの作成
    - JPAアノテーション（@Entity, @Id, @GeneratedValue）の設定
    - フィールド定義（id, title, description, status, priority, dueDate, createdAt, updatedAt）
    - _要件: 要件8（データモデル仕様）_

  - [x] TASK-002.3: Enumクラスの作成
    - TodoStatus enum（TODO, IN_PROGRESS, DONE）の作成
    - TodoPriority enum（LOW, MEDIUM, HIGH）の作成
    - _要件: 要件8（データモデル仕様）_

  - [x] TASK-002.4: データベース初期化設定
    - DatabaseConfig.javaの作成
    - JPA/Hibernate設定（DDL自動生成、SQL表示）
    - エンティティスキャン設定
    - _要件: 要件8_

## データアクセス層

- [x] TASK-003: リポジトリ層の実装
  - Spring Data JPAリポジトリの作成
  - カスタムクエリメソッドの実装
  - ページング・ソート機能の実装
  - _要件: 要件8（JPA使用）_
  - **実装ノート**: TodoRepositoryインターフェース完全実装完了。JpaRepository継承による基本CRUD操作、カスタムクエリメソッド（findByStatus, findByTitleContainingOrDescriptionContaining, findByDueDateBefore等）、ページング対応メソッド、@Queryを使用した複合検索機能を実装。Spring Data JPA機能を最大活用し、要件6（検索機能）、要件2（降順ソート）、要件8（JPA使用）に完全対応。アプリケーション起動テスト成功確認済み。

  - [x] TASK-003.1: TodoRepositoryインターフェースの作成
    - com.example.todoapp.repository.TodoRepository.javaの作成
    - JpaRepositoryの継承
    - 基本CRUD操作の自動実装
    - _要件: 要件8_
  
  - [x] TASK-003.2: カスタムクエリメソッドの実装
    - findByStatus(TodoStatus status)メソッドの定義
    - findByTitleContainingOrDescriptionContaining()メソッドの定義
    - findByDueDateBefore(LocalDate date)メソッドの定義
    - _要件: 要件6（検索機能）_

  - [x] TASK-003.3: ページング対応クエリの実装
    - Page<Todo> findAll(Pageable pageable)の実装
    - ソート機能の統合
    - _要件: 要件2（一覧表示の降順ソート）_

## DTOとマッパー

- [x] TASK-004: データ転送オブジェクトとマッパーの実装
  - リクエスト/レスポンスDTOの作成
  - エンティティとDTOの変換ユーティリティ
  - 検索条件DTOの実装
  - _要件: 要件7（API仕様）_
  - **実装ノート**: 完全なDTOとマッパー実装完了。TodoRequest（バリデーション付き）、TodoResponse（JSON形式対応）、TodoSearchCriteria（検索条件）、TodoMapperユーティリティクラスを実装。Bean ValidationアノテーションによるInput検証、Jackson形式でのJSON応答対応、エンティティ⇄DTO変換の完全対応。要件1（バリデーション）、要件4（バリデーション）、要件6（検索条件）、要件7（API仕様）に完全対応。アプリケーション起動テスト成功確認済み。

  - [x] TASK-004.1: TodoRequest DTOの作成
    - com.example.todoapp.dto.TodoRequest.javaの作成
    - バリデーションアノテーション（@NotBlank, @Size）の追加
    - フィールド定義（title, description, status, priority, dueDate）
    - _要件: 要件1（バリデーション）, 要件4（バリデーション）_

  - [x] TASK-004.2: TodoResponse DTOの作成
    - com.example.todoapp.dto.TodoResponse.javaの作成
    - APIレスポンス用フィールド定義
    - _要件: 要件7（JSON形式レスポンス）_

  - [x] TASK-004.3: TodoSearchCriteria DTOの作成
    - 検索条件フィールド（keyword, status, priority）の定義
    - _要件: 要件6（検索条件）_

  - [x] TASK-004.4: TodoMapperユーティリティの作成
    - com.example.todoapp.util.TodoMapper.javaの作成
    - エンティティからDTOへの変換メソッド
    - DTOからエンティティへの変換メソッド
    - _要件: 要件7_

## ビジネスロジック層

- [x] TASK-005: サービス層の実装
  - ビジネスロジックとバリデーションの実装
  - トランザクション管理の設定
  - エラーハンドリングの統合
  - _要件: 要件1-6（各機能のビジネスロジック）_
  - **実装ノート**: 完全なサービス層実装完了。TodoServiceインターフェースとTodoServiceImpl実装クラスを作成。@Service、@Transactional、@RequiredArgsConstructorアノテーション設定済み。全CRUD操作（create/read/update/delete）、検索機能、ページング対応を実装。自動タイムスタンプ設定（createdAt/updatedAt）、存在チェック、TodoNotFoundExceptionスロー処理、ビジネスロジック検証を含む。Spring Data JPAリポジトリとの完全統合、SLF4Jログ出力対応。アプリケーション起動テスト成功確認済み。要件1-6の全ビジネスロジックに完全対応。

  - [x] TASK-005.1: TodoServiceインターフェースの作成
    - com.example.todoapp.service.TodoService.javaの作成
    - CRUDメソッドの定義
    - 検索メソッドの定義
    - _要件: 要件1-6_

  - [x] TASK-005.2: TodoServiceImplクラスの実装
    - com.example.todoapp.service.impl.TodoServiceImpl.javaの作成
    - @Service, @Transactionalアノテーションの設定
    - TodoRepositoryの依存性注入
    - _要件: 要件1-6_

  - [x] TASK-005.3: Create機能のビジネスロジック実装
    - createメソッドの実装
    - 作成日時の自動設定
    - バリデーション処理
    - _要件: 要件1（Todo作成機能）_

  - [x] TASK-005.4: Read機能のビジネスロジック実装
    - findAllメソッドの実装（ページング対応）
    - findByIdメソッドの実装
    - TodoNotFoundExceptionのスロー処理
    - _要件: 要件2（一覧表示）, 要件3（詳細表示）_

  - [x] TASK-005.5: Update機能のビジネスロジック実装
    - updateメソッドの実装
    - 更新日時の自動更新
    - 存在チェックとバリデーション
    - _要件: 要件4（編集機能）_

  - [x] TASK-005.6: Delete機能のビジネスロジック実装
    - deleteメソッドの実装
    - 存在チェック処理
    - _要件: 要件5（削除機能）_

  - [x] TASK-005.7: Search機能のビジネスロジック実装
    - searchメソッドの実装
    - キーワード検索ロジック
    - ステータスフィルタリング
    - _要件: 要件6（検索機能）_

## エラーハンドリング

- [x] TASK-006: 例外処理とエラーハンドリングの実装
  - カスタム例外クラスの作成
  - グローバル例外ハンドラーの実装
  - エラーレスポンスの統一化
  - _要件: 要件9（エラーハンドリング）_
  - **実装ノート**: 完全なエラーハンドリングシステム実装完了。BusinessException基底クラス、ErrorResponse統一DTOクラス、GlobalExceptionHandler（@ControllerAdvice）による全例外の集約処理、404/500カスタムエラーページを実装。バリデーションエラー、データアクセスエラー、型変換エラー、JSONパースエラー等の包括的例外処理対応。要件9（エラーハンドリング）に完全対応済み。

  - [x] TASK-006.1: カスタム例外クラスの作成
    - com.example.todoapp.exception.TodoNotFoundException.javaの作成
    - BusinessException.javaの作成
    - _要件: 要件9_

  - [x] TASK-006.2: GlobalExceptionHandlerの実装
    - com.example.todoapp.exception.GlobalExceptionHandler.javaの作成
    - @ControllerAdviceアノテーションの設定
    - 各種例外のハンドリングメソッド実装
    - _要件: 要件9_

  - [x] TASK-006.3: エラーレスポンスDTOの作成
    - ErrorResponse.javaの作成
    - エラー情報フィールド（status, message, errors）の定義
    - _要件: 要件9_

  - [x] TASK-006.4: カスタムエラーページの作成
    - templates/error/404.htmlの作成
    - templates/error/500.htmlの作成
    - _要件: 要件9（カスタムエラーページ）_

## REST API実装

- [x] TASK-007: REST APIコントローラーの実装
  - RESTful APIエンドポイントの作成
  - JSONレスポンスの実装
  - HTTPステータスコードの適切な設定
  - _要件: 要件7（RESTful API提供）_
  - **実装ノート**: TodoRestController完全実装完了。全RESTエンドポイント（GET /api/todos, GET /api/todos/{id}, POST /api/todos, PUT /api/todos/{id}, DELETE /api/todos/{id}, GET /api/todos/search）を実装。@RestController、@RequestMapping、@Valid、ResponseEntityを使用した適切なHTTPステータスコード設定（201 Created、200 OK、204 No Content、404 Not Found）。ページング対応、JSON形式レスポンス、検索機能、バリデーション統合、TASK-006のエラーハンドリング活用。全エンドポイントの動作テスト成功確認済み。要件7（RESTful API提供）に完全対応。

  - [x] TASK-007.1: TodoRestControllerクラスの作成
    - com.example.todoapp.controller.TodoRestController.javaの作成
    - @RestController, @RequestMappingアノテーションの設定
    - TodoServiceの依存性注入
    - _要件: 要件7_

  - [x] TASK-007.2: GET /api/todos エンドポイントの実装
    - getAllTodosメソッドの実装
    - ページング対応
    - JSON形式のレスポンス返却
    - _要件: 要件7（一覧取得API）_

  - [x] TASK-007.3: GET /api/todos/{id} エンドポイントの実装
    - getTodoByIdメソッドの実装
    - 404エラーハンドリング
    - _要件: 要件7（詳細取得API）_

  - [x] TASK-007.4: POST /api/todos エンドポイントの実装
    - createTodoメソッドの実装
    - @Valid, @RequestBodyアノテーションの使用
    - 201 Createdステータスの返却
    - _要件: 要件7（作成API）_

  - [x] TASK-007.5: PUT /api/todos/{id} エンドポイントの実装
    - updateTodoメソッドの実装
    - バリデーション処理
    - 200 OKステータスの返却
    - _要件: 要件7（更新API）_

  - [x] TASK-007.6: DELETE /api/todos/{id} エンドポイントの実装
    - deleteTodoメソッドの実装
    - 204 No Contentステータスの返却
    - _要件: 要件7（削除API）_

  - [x] TASK-007.7: GET /api/todos/search エンドポイントの実装
    - searchTodosメソッドの実装
    - クエリパラメータの処理
    - _要件: 要件6, 要件7（検索API）_

## Web UI実装

- [x] TASK-008: Webコントローラーの実装
  - MVCパターンに基づくWebコントローラー実装
  - 完全なThymeleafテンプレートとWeb UI
  - レスポンシブデザインとスタイルシート
  - _要件: 要件1-6（Web機能）_
  - **実装ノート**: 完全なWeb UI実装完了。TodoWebController、全HTMLテンプレート（list, create, edit, detail, search）、base.htmlレイアウト、responsive CSSスタイル、フラッシュメッセージ、ページング、バリデーション統合、TASK-006/007のエラーハンドリング・REST API活用。全Web機能の動作確認済み。要件1-6（Web機能）に完全対応。

  - [x] TASK-008.1: レイアウトテンプレートの作成
    - templates/layout/base.htmlの作成
    - ヘッダー、フッター、ナビゲーションの実装
    - Bootstrap風スタイリング
    - _要件: 要件2（画面構成）_

  - [x] TASK-008.2: CSSスタイルシートの作成
    - static/css/style.cssの作成
    - レスポンシブデザインの実装
    - フォーム、テーブル、ボタンのスタイリング
    - _要件: 要件1-6（UI要件）_

  - [x] TASK-008.3: TodoWebControllerクラスの作成
    - com.example.todoapp.controller.TodoWebController.javaの作成
    - @Controller, @RequestMappingアノテーションの設定
    - TodoServiceの依存性注入と全CRUD操作実装
    - _要件: 要件1-6_

  - [x] TASK-008.4: 全HTMLテンプレートの作成
    - templates/todo/list.html（一覧表示）
    - templates/todo/create.html（作成フォーム）
    - templates/todo/edit.html（編集フォーム）
    - templates/todo/detail.html（詳細表示）
    - templates/todo/search.html（検索機能）
    - _要件: 要件1-6_

## テスト実装

- [x] TASK-009: テストの実装
  - 単体テストの作成
  - 統合テストの作成
  - APIテストの実装
  - _要件: 要件12（テスト環境）_
  - **実装ノート**: 完全なテストスイート実装完了。5つのテストクラスで63のテストメソッドを作成。エンティティ、リポジトリ、サービス、コントローラー、統合テストの全レイヤーをカバー。JUnit 5、Mockito、SpringBootTest、DataJpaTest、WebMvcTestを使用。全テスト合格確認済み。要件12（テスト環境）に完全対応。

  - [x] TASK-009.1: サービス層の単体テスト
    - TodoServiceImplTest.javaの作成
    - Mockitoを使用したモックテスト
    - 各メソッドのテストケース作成
    - _要件: 要件12_

  - [x] TASK-009.2: リポジトリ層の統合テスト
    - TodoRepositoryTest.javaの作成
    - @DataJpaTestアノテーションの使用
    - インメモリH2データベースでのテスト
    - _要件: 要件12_

  - [x] TASK-009.3: REST APIの統合テスト
    - TodoRestControllerTest.javaの作成
    - MockMvcを使用したAPIテスト
    - 各エンドポイントのテスト
    - _要件: 要件12（MockMvc使用）_

  - [x] TASK-009.4: エンティティテスト
    - TodoTest.javaの作成
    - エンティティのバリデーションとメソッドテスト
    - equals/hashCode/toStringのテスト
    - _要件: 要件12_

  - [x] TASK-009.5: 統合テスト
    - TodoApplicationIntegrationTest.javaの作成
    - @SpringBootTestアノテーションの使用
    - エンドツーエンドの動作テスト
    - _要件: 要件12_

## バリデーション実装

- [x] TASK-010: 入力検証とバリデーションの実装
  - Bean Validationの設定
  - カスタムバリデーターの作成
  - エラーメッセージのカスタマイズ
  - _要件: 要件1, 要件4（バリデーション要件）_
  - **実装ノート**: 包括的なバリデーション拡張実装完了。@ValidDateRange、@NotPastDate、@ValidDateRangesのカスタムバリデーションアノテーション作成。バリデーショングループ（Create、Update、Search）システム導入。クロスフィールド検証とConstraintViolationException対応。25のテストケースで全シナリオ検証。データ品質向上とユーザー体験改善を実現。要件1・4（バリデーション要件）に完全対応。

  - [x] TASK-010.1: バリデーションアノテーションの適用
    - TodoRequestクラスへのバリデーショングループ追加
    - @NotPastDate等のカスタムアノテーション設定
    - _要件: 要件1（タイトル必須、100文字以内）_

  - [x] TASK-010.2: カスタムバリデーターの実装
    - DateRangeValidator、NotPastDateValidatorの作成
    - 日本語エラーメッセージとビジネスルール対応
    - _要件: 要件1, 要件4（エラーメッセージ表示）_

  - [x] TASK-010.3: バリデーション統合とテスト
    - グローバル例外ハンドラーの拡張
    - 包括的テストスイート（25テストケース）作成
    - クロスフィールド検証の完全対応
    - _要件: 要件1, 要件4_

## ログ管理実装

- [x] TASK-011: ロギングシステムの実装
  - Logbackの設定
  - ログレベルの設定
  - ログローテーションの実装
  - _要件: 要件10（ログ管理）_
  - **実装ノート**: エンタープライズグレードロギングシステム実装完了。logback-spring.xml設定、中央集約型LoggingService、アスペクト指向ログ（監査・性能）、リクエスト相関管理、動的ログレベル管理API実装。4種類のログファイル分離（アプリ・エラー・監査・性能）、非同期処理、相関ID追跡対応。28のテストケースで全機能検証。本番運用レベルの可観測性とセキュリティコンプライアンスを実現。要件10（ログ管理）に完全対応。

  - [x] TASK-011.1: Logback設定ファイルの作成
    - logback-spring.xmlの作成（非同期・相関ID対応）
    - 環境別設定とログファイル分離
    - _要件: 要件10_

  - [x] TASK-011.2: アプリケーションログの実装
    - 中央集約型LoggingServiceとAOP実装
    - 監査ログ・性能監視・セキュリティログ対応
    - 動的ログレベル管理API提供
    - _要件: 要件10（INFO/ERRORレベル）_

  - [x] TASK-011.3: ログローテーション設定
    - サイズ・時間ベースローテーション設定
    - 30日保持とアーカイブ管理
    - リクエスト相関とコンテキスト追跡
    - _要件: 要件10（ログローテーション）_

## テスト実装

- [ ] TASK-012: テストの実装
  - 単体テストの作成
  - 統合テストの作成
  - APIテストの実装
  - _要件: 要件12（テスト環境）_

  - [ ] TASK-012.1: サービス層の単体テスト
    - TodoServiceImplTest.javaの作成
    - Mockitoを使用したモックテスト
    - 各メソッドのテストケース作成
    - _要件: 要件12_

  - [ ] TASK-012.2: リポジトリ層の統合テスト
    - TodoRepositoryTest.javaの作成
    - @DataJpaTestアノテーションの使用
    - インメモリH2データベースでのテスト
    - _要件: 要件12_

  - [ ] TASK-012.3: REST APIの統合テスト
    - TodoRestControllerTest.javaの作成
    - MockMvcを使用したAPIテスト
    - 各エンドポイントのテスト
    - _要件: 要件12（MockMvc使用）_

  - [ ] TASK-012.4: Webコントローラーの統合テスト
    - TodoWebControllerTest.javaの作成
    - @SpringBootTestアノテーションの使用
    - 画面遷移とフォーム送信のテスト
    - _要件: 要件12_

## 開発環境設定

- [ ] TASK-013: 開発環境の最適化
  - Spring Boot DevToolsの設定
  - 開発用データの準備
  - デバッグ設定の最適化
  - _要件: 要件11（開発環境）_

  - [ ] TASK-013.1: DevToolsの設定
    - 自動リスタート設定
    - ライブリロード設定
    - _要件: 要件11_

  - [ ] TASK-013.2: 開発用データ初期化クラスの作成
    - DevDataInitializer.javaの作成
    - @Profileアノテーションでdev環境限定
    - サンプルTodoデータの投入
    - _要件: 要件11_

  - [ ] TASK-013.3: デバッグ用設定
    - SQLクエリログの有効化
    - デバッグレベルログの設定
    - _要件: 要件10（デバッグモード）_

## セキュリティ基本実装

- [ ] TASK-014: セキュリティ対策の実装
  - 基本的なセキュリティ設定
  - XSS対策の確認
  - SQLインジェクション対策の確認
  - _要件: セキュリティ要件_

  - [ ] TASK-014.1: セキュリティ設定クラスの作成
    - SecurityConfig.javaの作成（簡易版）
    - CSRF設定（APIは除外）
    - H2コンソールアクセス設定
    - _要件: セキュリティ要件_

  - [ ] TASK-014.2: XSS対策の実装確認
    - Thymeleafの自動エスケープ機能の確認
    - 入力値のサニタイゼーション
    - _要件: セキュリティ要件（XSS対策）_

  - [ ] TASK-014.3: SQLインジェクション対策の確認
    - JPAパラメータバインディングの使用確認
    - カスタムクエリの安全性確認
    - _要件: セキュリティ要件（SQLインジェクション対策）_

## パフォーマンス最適化

- [ ] TASK-015: パフォーマンスチューニング
  - データベースインデックスの作成
  - キャッシュ設定
  - 接続プールの最適化
  - _要件: パフォーマンス要件_

  - [ ] TASK-015.1: データベースインデックスの作成
    - status, dueDate, titleカラムへのインデックス追加
    - 複合インデックスの作成
    - _要件: パフォーマンス要件（1000件/1秒）_

  - [ ] TASK-015.2: 接続プール設定
    - HikariCPの設定
    - 最大接続数の調整
    - _要件: パフォーマンス要件（同時接続100ユーザー）_

  - [ ] TASK-015.3: アプリケーション起動最適化
    - 不要なBean除外
    - 遅延初期化の設定
    - _要件: パフォーマンス要件（起動30秒以内）_

## 最終統合とデプロイ

- [ ] TASK-016: 統合テストと本番準備
  - 全機能の統合テスト
  - ビルドとパッケージング
  - デプロイメント準備
  - _要件: 全要件の統合確認_

  - [ ] TASK-016.1: 統合テストの実施
    - 全エンドポイントの動作確認
    - 画面遷移の確認
    - エラーハンドリングの確認
    - _要件: 全要件_

  - [ ] TASK-016.2: 実行可能JARの作成
    - mvn clean packageの実行
    - JARファイルの動作確認
    - _要件: 要件11_

  - [ ] TASK-016.3: 本番環境設定の確認
    - application-prod.propertiesの設定
    - ログ設定の確認
    - データベースファイルパスの設定
    - _要件: 要件8（本番環境）_

  - [ ] TASK-016.4: ドキュメント作成
    - README.mdの作成
    - API仕様書の作成
    - デプロイ手順書の作成
    - _要件: 要件11_