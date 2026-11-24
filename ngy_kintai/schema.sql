-- 既存カレンダーの削除
DROP TABLE IF EXISTS calender;

-- 8. カレンダー機能テーブル (calendar) ※スペル修正
CREATE TABLE calendar (
    calendar_id SERIAL PRIMARY KEY,
    user_id VARCHAR(8) REFERENCES users(user_id),
    title VARCHAR(32) NOT NULL,
    detail VARCHAR(256),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP
);


CREATE DATABASE ngy_kintai;

-- 1. 役割管理テーブル (role)
CREATE TABLE role (
    role_id VARCHAR(10) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

-- 2. 部署管理テーブル (department)
CREATE TABLE department (
    department_id VARCHAR(10) PRIMARY KEY,
    department_name VARCHAR(50) NOT NULL
);

-- 3. 勤務地管理テーブル (work_location)
CREATE TABLE work_location (
    work_location_id VARCHAR(10) PRIMARY KEY,
    work_location_name VARCHAR(50) NOT NULL
);

-- 4. 出勤管理テーブル (work_status)
CREATE TABLE work_status (
    work_status_id VARCHAR(10) PRIMARY KEY,
    work_status_name VARCHAR(50) NOT NULL
);

-- 5. ユーザ管理テーブル (users) ※PostgreSQLの予約語回避のためテーブル名はusersにマッピング
CREATE TABLE users (
    user_id VARCHAR(8) PRIMARY KEY,
    user_name VARCHAR(32) NOT NULL,
    password VARCHAR(16) NOT NULL,
    email_address VARCHAR(128),
    phone_number VARCHAR(20),
    department_id VARCHAR(10) REFERENCES department(department_id),
    work_location_id VARCHAR(10) REFERENCES work_location(work_location_id),
    role_id VARCHAR(10) REFERENCES role(role_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 勤怠管理テーブル(保存) (attendance)
CREATE TABLE attendance (
    attendance_id SERIAL PRIMARY KEY,
    user_id VARCHAR(8) REFERENCES users(user_id),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    work_status_id VARCHAR(10) REFERENCES work_status(work_status_id)
);

-- 7. 勤怠管理テーブル(更新) (attendance_today)
CREATE TABLE attendance_today (
    user_id VARCHAR(8) PRIMARY KEY REFERENCES users(user_id),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    work_status_id VARCHAR(10) REFERENCES work_status(work_status_id)
);

-- 8. カレンダー機能テーブル (calender)
CREATE TABLE calender (
    calendar_id SERIAL PRIMARY KEY,
    user_id VARCHAR(8) REFERENCES users(user_id),
    title VARCHAR(32) NOT NULL,
    detail VARCHAR(256),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP
);

-- 9. メール機能テーブル (massege)
CREATE TABLE massege (
    mail_id SERIAL PRIMARY KEY,
    sender_user_id VARCHAR(8) REFERENCES users(user_id),
    receiver_user_id VARCHAR(8) REFERENCES users(user_id),
    subject VARCHAR(32) NOT NULL,
    body VARCHAR(256) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 就業合計管理テーブル (time_managiment)
CREATE TABLE time_managiment (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(8) REFERENCES users(user_id),
    work_date DATE NOT NULL,
    actual_work_minutes INT DEFAULT 0,
    overtime_minutes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. 月間就業合計管理テーブル (month_time_managiment)
CREATE TABLE month_time_managiment (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(8) REFERENCES users(user_id),
    target_year_month VARCHAR(7) NOT NULL,
    monthly_actual_minutes INT DEFAULT 0,
    monthly_overtime_minutes INT DEFAULT 0
);

-- 初期マスタデータ
INSERT INTO role VALUES ('R01', '管理者'), ('R02', '従業員');
INSERT INTO work_status VALUES ('W01', '出社'), ('W02', '在宅'), ('W03', '勤務外');
INSERT INTO department VALUES ('D01', '開発部'), ('D02', '営業部');
INSERT INTO work_location VALUES ('L01', '愛知県'), ('L02', '東京都'), ('L03', '大阪府');
INSERT INTO users (user_id, user_name, password, department_id, work_location_id, role_id)
VALUES ('00000001', '管理者太郎', 'admin123', 'D01', 'L01', 'R01'),
       ('00000002', '一般花子', 'user1234', 'D01', 'L01', 'R02');