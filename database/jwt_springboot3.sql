-- ============================================================
-- BT10: Spring Boot JWT Authentication
-- Database: jwt_springboot3
-- Dành cho: SQL Server (SSMS)
-- Tác giả  : 24110257
-- Ngày     : 29-09-2026
-- ============================================================

-- ============================================================
-- BƯỚC 1: Tạo Database
-- ============================================================
USE master;
GO

IF NOT EXISTS (
    SELECT name FROM sys.databases WHERE name = N'jwt_springboot3'
)
BEGIN
    CREATE DATABASE jwt_springboot3
        COLLATE Vietnamese_CI_AS;
    PRINT N'Database jwt_springboot3 đã được tạo thành công.';
END
ELSE
BEGIN
    PRINT N'Database jwt_springboot3 đã tồn tại, bỏ qua bước tạo.';
END
GO

-- ============================================================
-- BƯỚC 2: Chọn Database
-- ============================================================
USE jwt_springboot3;
GO

-- ============================================================
-- BƯỚC 3: Tạo bảng users
-- (Hibernate ddl-auto=update cũng sẽ tự tạo khi chạy app)
-- ============================================================
IF NOT EXISTS (
    SELECT * FROM sysobjects WHERE name='users' AND xtype='U'
)
BEGIN
    CREATE TABLE users (
        id          BIGINT          IDENTITY(1,1)   NOT NULL,
        full_name   NVARCHAR(100)                   NOT NULL,
        email       NVARCHAR(150)                   NOT NULL,
        password    NVARCHAR(255)                   NOT NULL,   -- BCrypt hash
        created_at  DATETIME2       DEFAULT GETDATE(),
        updated_at  DATETIME2       DEFAULT GETDATE(),

        CONSTRAINT PK_users         PRIMARY KEY (id),
        CONSTRAINT UQ_users_email   UNIQUE      (email)
    );
    PRINT N'Bảng users đã được tạo thành công.';
END
ELSE
BEGIN
    PRINT N'Bảng users đã tồn tại, bỏ qua bước tạo.';
END
GO

-- ============================================================
-- BƯỚC 4: Dữ liệu mẫu (mật khẩu đều là "123456" - BCrypt hash)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@iotstar.vn')
BEGIN
    INSERT INTO users (full_name, email, password)
    VALUES (N'Nguyen Van A', 'admin@iotstar.vn',
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
END

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'user@iotstar.vn')
BEGIN
    INSERT INTO users (full_name, email, password)
    VALUES (N'Tran Thi B', 'user@iotstar.vn',
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
END

PRINT N'Dữ liệu mẫu đã được chèn (nếu chưa tồn tại).';
GO

-- ============================================================
-- BƯỚC 5: Kiểm tra kết quả
-- ============================================================
SELECT * FROM users;
GO
