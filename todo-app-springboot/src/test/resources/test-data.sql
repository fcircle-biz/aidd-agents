-- Test data for development environment integration tests
-- This file provides sample data for testing development features

-- Clear existing data
DELETE FROM todo;

-- Insert test todos with various states and priorities
INSERT INTO todo (id, title, description, due_date, priority, status, created_at, updated_at) VALUES
(1, 'テストタスク1', 'これは開発環境のテスト用タスクです', '2024-12-31', 'HIGH', 'PENDING', '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'テストタスク2', '進行中のタスクのテストデータ', '2024-06-15', 'MEDIUM', 'IN_PROGRESS', '2024-01-02 11:00:00', '2024-01-02 11:00:00'),
(3, '完了タスク', '完了済みのタスクのテストデータ', '2024-01-10', 'LOW', 'COMPLETED', '2024-01-03 12:00:00', '2024-01-15 14:00:00'),
(4, '期限切れタスク', '期限が過ぎてしまったタスク', '2023-12-01', 'HIGH', 'PENDING', '2023-11-01 09:00:00', '2023-11-01 09:00:00'),
(5, '長期プロジェクト', '将来的な長期プロジェクトのタスク', '2025-03-31', 'MEDIUM', 'PENDING', '2024-01-05 13:00:00', '2024-01-05 13:00:00');

-- Reset the sequence to start from 6 for new insertions
ALTER SEQUENCE TODO_SEQ RESTART WITH 6;