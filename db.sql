-- Cơ sở dữ liệu cho hệ thống HelloQuizz

CREATE DATABASE IF NOT EXISTS helloquizz;
USE helloquizz;

-- Bảng lưu thông tin người dùng
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của người dùng',
  full_name VARCHAR(255) NOT NULL COMMENT 'Họ và tên đầy đủ',
  email VARCHAR(255) UNIQUE NOT NULL COMMENT 'Email đăng nhập',
  password_hash VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã mã hóa',
  username VARCHAR(100) UNIQUE NOT NULL COMMENT 'Tên đăng nhập',
  phone VARCHAR(20) COMMENT 'Số điện thoại',
  gender ENUM('Male', 'Female', 'Other') COMMENT 'Giới tính',
  date_of_birth DATE COMMENT 'Ngày sinh',
  school_name VARCHAR(255) COMMENT 'Tên trường học',
  bio TEXT COMMENT 'Tiểu sử cá nhân',
  avatar_url VARCHAR(255) COMMENT 'Đường dẫn ảnh đại diện',
  role ENUM('student', 'admin') DEFAULT 'student' COMMENT 'Vai trò: học sinh hoặc quản trị viên',
  status ENUM('active', 'inactive', 'locked') DEFAULT 'active' COMMENT 'Trạng thái tài khoản',
  last_login_at TIMESTAMP COMMENT 'Thời gian đăng nhập cuối cùng',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo tài khoản',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật cuối cùng'
);

-- Bảng lưu thông tin đề thi
CREATE TABLE exams (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của đề thi',
  title VARCHAR(255) NOT NULL COMMENT 'Tiêu đề đề thi',
  description TEXT COMMENT 'Mô tả đề thi',
  subject VARCHAR(100) NOT NULL COMMENT 'Môn học',
  subject_color VARCHAR(50) COMMENT 'Màu sắc đại diện môn học',
  total_questions INT NOT NULL COMMENT 'Tổng số câu hỏi',
  duration_minutes INT NOT NULL COMMENT 'Thời gian làm bài (phút)',
  difficulty ENUM('easy', 'medium', 'hard') NOT NULL COMMENT 'Độ khó',
  pass_score DECIMAL(5,2) DEFAULT 5.0 COMMENT 'Điểm qua môn',
  status ENUM('published', 'draft', 'archived') DEFAULT 'draft' COMMENT 'Trạng thái đề thi',
  attempt_count INT DEFAULT 0 COMMENT 'Số lần thi',
  average_score DECIMAL(5,2) COMMENT 'Điểm trung bình',
  banner_color VARCHAR(50) COMMENT 'Màu banner',
  created_by_admin_id INT COMMENT 'ID quản trị viên tạo đề',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
  FOREIGN KEY (created_by_admin_id) REFERENCES users(id)
);

-- Bảng lưu câu hỏi trong đề thi
CREATE TABLE questions (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của câu hỏi',
  exam_id INT NOT NULL COMMENT 'ID đề thi chứa câu hỏi',
  question_text TEXT NOT NULL COMMENT 'Nội dung câu hỏi',
  question_number INT NOT NULL COMMENT 'Số thứ tự câu hỏi',
  correct_option_index INT NOT NULL COMMENT 'Chỉ số đáp án đúng (0-3 tương ứng A-D)',
  explanation TEXT COMMENT 'Giải thích đáp án',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
  FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
  UNIQUE KEY unique_exam_question (exam_id, question_number)
);

-- Bảng lưu các lựa chọn cho câu hỏi
CREATE TABLE options (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của lựa chọn',
  question_id INT NOT NULL COMMENT 'ID câu hỏi',
  option_key ENUM('A', 'B', 'C', 'D') NOT NULL COMMENT 'Khóa lựa chọn',
  option_text VARCHAR(500) NOT NULL COMMENT 'Nội dung lựa chọn',
  is_correct BOOLEAN DEFAULT FALSE COMMENT 'Có phải đáp án đúng không',
  FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
  UNIQUE KEY unique_question_option (question_id, option_key)
);

-- Bảng lưu kết quả thi của người dùng
CREATE TABLE results (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của kết quả',
  user_id INT NOT NULL COMMENT 'ID người dùng',
  exam_id INT NOT NULL COMMENT 'ID đề thi',
  score DECIMAL(5,2) NOT NULL COMMENT 'Điểm số đạt được',
  max_score DECIMAL(5,2) DEFAULT 10.0 COMMENT 'Điểm tối đa',
  correct_count INT NOT NULL COMMENT 'Số câu đúng',
  wrong_count INT NOT NULL COMMENT 'Số câu sai',
  total_questions INT NOT NULL COMMENT 'Tổng số câu hỏi',
  time_spent_seconds INT NOT NULL COMMENT 'Thời gian làm bài (giây)',
  started_at TIMESTAMP COMMENT 'Thời gian bắt đầu thi',
  completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian hoàn thành',
  pass BOOLEAN DEFAULT FALSE COMMENT 'Đậu hay rớt (được tính toán từ code)', 
  percentile INT COMMENT 'Phần trăm xếp hạng',
  class_average DECIMAL(5,2) COMMENT 'Điểm trung bình lớp',
  class_highest DECIMAL(5,2) COMMENT 'Điểm cao nhất lớp',
  ranking INT COMMENT 'Xếp hạng',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
  INDEX idx_user_exam (user_id, exam_id),
  INDEX idx_created_at (created_at)
);

-- Bảng lưu chi tiết câu trả lời của người dùng
CREATE TABLE answers (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của câu trả lời',
  result_id INT NOT NULL COMMENT 'ID kết quả thi',
  question_id INT NOT NULL COMMENT 'ID câu hỏi',
  user_selected_option_index INT COMMENT 'Chỉ số lựa chọn của người dùng',
  correct_option_index INT NOT NULL COMMENT 'Chỉ số đáp án đúng',
  is_correct BOOLEAN NOT NULL COMMENT 'Đúng hay sai',
  marked_for_review BOOLEAN DEFAULT FALSE COMMENT 'Đánh dấu xem lại',
  time_spent_seconds INT COMMENT 'Thời gian trả lời câu hỏi (giây)',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
  FOREIGN KEY (result_id) REFERENCES results(id) ON DELETE CASCADE,
  FOREIGN KEY (question_id) REFERENCES questions(id),
  INDEX idx_result_id (result_id),
  INDEX idx_question_id (question_id)
);

-- Bảng lưu thống kê của người dùng
CREATE TABLE user_statistics (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của thống kê',
  user_id INT UNIQUE NOT NULL COMMENT 'ID người dùng',
  total_exams_taken INT DEFAULT 0 COMMENT 'Tổng số đề thi đã làm',
  average_score DECIMAL(5,2) COMMENT 'Điểm trung bình',
  highest_score DECIMAL(5,2) COMMENT 'Điểm cao nhất',
  total_correct INT DEFAULT 0 COMMENT 'Tổng số câu đúng',
  total_wrong INT DEFAULT 0 COMMENT 'Tổng số câu sai',
  total_time_spent_hours INT DEFAULT 0 COMMENT 'Tổng thời gian làm bài (giờ)',
  ranking INT COMMENT 'Xếp hạng',
  streak_days INT DEFAULT 0 COMMENT 'Số ngày liên tiếp làm bài',
  last_exam_date DATE COMMENT 'Ngày làm bài cuối cùng',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng lưu thống kê theo môn học của người dùng
CREATE TABLE subject_statistics (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của thống kê môn học',
  user_id INT NOT NULL COMMENT 'ID người dùng',
  subject VARCHAR(100) NOT NULL COMMENT 'Tên môn học',
  total_exams INT DEFAULT 0 COMMENT 'Tổng số đề thi môn này',
  average_score DECIMAL(5,2) COMMENT 'Điểm trung bình môn này',
  highest_score DECIMAL(5,2) COMMENT 'Điểm cao nhất môn này',
  last_exam_date DATE COMMENT 'Ngày thi môn này cuối cùng',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY unique_user_subject (user_id, subject)
);

-- Bảng lưu thống kê hệ thống cho quản trị viên
CREATE TABLE admin_statistics (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của thống kê hệ thống',
  stat_date DATE COMMENT 'Ngày thống kê',
  total_users INT COMMENT 'Tổng số người dùng',
  active_users INT COMMENT 'Số người dùng hoạt động',
  locked_users INT COMMENT 'Số người dùng bị khóa',
  new_registrations INT COMMENT 'Số đăng ký mới',
  total_exams INT COMMENT 'Tổng số đề thi',
  published_exams INT COMMENT 'Số đề thi đã xuất bản',
  draft_exams INT COMMENT 'Số đề thi nháp',
  archived_exams INT COMMENT 'Số đề thi đã lưu trữ',
  total_attempts INT COMMENT 'Tổng số lần thi',
  average_score DECIMAL(5,2) COMMENT 'Điểm trung bình hệ thống',
  pass_rate_percent DECIMAL(5,2) COMMENT 'Tỷ lệ đậu (%)',
  peak_hour INT COMMENT 'Giờ cao điểm',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
  UNIQUE KEY unique_date (stat_date)
);

-- Bảng lưu hoạt động hàng ngày
CREATE TABLE daily_activity (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID duy nhất của hoạt động',
  activity_date DATE NOT NULL COMMENT 'Ngày hoạt động',
  hour_of_day INT COMMENT 'Giờ trong ngày (0-23)',
  exam_attempts INT DEFAULT 0 COMMENT 'Số lần thi trong giờ đó',
  active_users INT DEFAULT 0 COMMENT 'Số người dùng hoạt động',
  total_score_sum DECIMAL(10,2) COMMENT 'Tổng điểm trong giờ đó',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
  UNIQUE KEY unique_date_hour (activity_date, hour_of_day)
);


-- Chèn dữ liệu mẫu

-- Người dùng mẫu
INSERT INTO users (full_name, email, password_hash, username, phone, gender, date_of_birth, school_name, bio, role, status) VALUES
('Nguyễn Văn A', 'nguyenvana@example.com', '$2b$10$hashedpassword1', 'nguyenvana', '0123456789', 'Male', '2005-05-15', 'THPT Nguyễn Du', 'Học sinh chăm chỉ', 'student', 'active'),
('Trần Thị B', 'tranthib@example.com', '$2b$10$hashedpassword2', 'tranthib', '0987654321', 'Female', '2004-08-20', 'THPT Lê Hồng Phong', 'Thích toán học', 'student', 'active'),
('Admin Chính', 'admin@helloquizz.com', '$2b$10$hashedpassword3', 'admin', '0111111111', 'Male', '1990-01-01', NULL, 'Quản trị viên hệ thống', 'admin', 'active'),
('Lê Văn C', 'levanc@example.com', '$2b$10$hashedpassword4', 'levanc', '0222222222', 'Male', '2003-12-10', 'THPT Trần Phú', 'Học sinh giỏi', 'student', 'active'),
('Phạm Thị D', 'phamthid@example.com', '$2b$10$hashedpassword5', 'phamthid', '0333333333', 'Female', '2006-03-25', 'THPT Marie Curie', 'Yêu thích khoa học', 'student', 'active');

-- Đề thi mẫu
INSERT INTO exams (title, description, subject, subject_color, total_questions, duration_minutes, difficulty, pass_score, status, attempt_count, average_score, banner_color, created_by_admin_id) VALUES
('Đề thi THPT Quốc gia 2025 — Toán', 'Đề thi thử THPT Quốc gia môn Toán học năm 2025', 'Toán học', '#FF6B6B', 50, 90, 'hard', 5.0, 'published', 1284, 7.2, '#FF6B6B', 3),
('Đề thi THPT Quốc gia 2025 — Lý', 'Đề thi thử THPT Quốc gia môn Vật lý năm 2025', 'Vật lý', '#4ECDC4', 40, 75, 'medium', 5.0, 'published', 892, 6.8, '#4ECDC4', 3),
('Đề thi THPT Quốc gia 2025 — Hóa', 'Đề thi thử THPT Quốc gia môn Hóa học năm 2025', 'Hóa học', '#45B7D1', 35, 60, 'medium', 5.0, 'published', 654, 7.1, '#45B7D1', 3),
('Đề thi thử môn Sinh học', 'Đề thi thử môn Sinh học cơ bản', 'Sinh học', '#96CEB4', 30, 45, 'easy', 5.0, 'draft', 0, NULL, '#96CEB4', 3),
('Đề thi thử môn Ngữ văn', 'Đề thi thử môn Ngữ văn THPT', 'Ngữ văn', '#FFEAA7', 25, 120, 'medium', 5.0, 'published', 432, 8.2, '#FFEAA7', 3);

-- Câu hỏi mẫu cho đề thi Toán
INSERT INTO questions (exam_id, question_text, question_number, correct_option_index, explanation) VALUES
(1, 'Cho ma trận A = [[2,1],[1,3]]. Hãy tính định thức det(A)?', 1, 0, 'det(A) = 2*3 - 1*1 = 6 - 1 = 5'),
(1, 'Tìm nghiệm của phương trình x² - 5x + 6 = 0?', 2, 1, 'Nghiệm là x = 2 hoặc x = 3'),
(1, 'Tính giới hạn lim(x→0) (sin(x)/x)?', 3, 2, 'Giới hạn bằng 1');

-- Lựa chọn cho câu hỏi 1
INSERT INTO options (question_id, option_key, option_text, is_correct) VALUES
(1, 'A', '5', TRUE),
(1, 'B', '6', FALSE),
(1, 'C', '4', FALSE),
(1, 'D', '7', FALSE);

-- Lựa chọn cho câu hỏi 2
INSERT INTO options (question_id, option_key, option_text, is_correct) VALUES
(2, 'A', 'x = 1, x = 6', FALSE),
(2, 'B', 'x = 2, x = 3', TRUE),
(2, 'C', 'x = -2, x = -3', FALSE),
(2, 'D', 'x = 0, x = 5', FALSE);

-- Lựa chọn cho câu hỏi 3
INSERT INTO options (question_id, option_key, option_text, is_correct) VALUES
(3, 'A', '0', FALSE),
(3, 'B', '∞', FALSE),
(3, 'C', '1', TRUE),
(3, 'D', 'Không tồn tại', FALSE);

-- Kết quả thi mẫu
INSERT INTO results (user_id, exam_id, score, correct_count, wrong_count, total_questions, time_spent_seconds, started_at, completed_at, percentile, ranking) VALUES
(1, 1, 8.5, 42, 8, 50, 4474, '2026-03-18 14:00:00', '2026-03-18 15:14:34', 73, 124),
(1, 2, 7.2, 28, 12, 40, 3600, '2026-03-17 10:00:00', '2026-03-17 11:00:00', 65, 234),
(2, 1, 9.2, 46, 4, 50, 4200, '2026-03-18 16:00:00', '2026-03-18 17:10:00', 85, 45),
(4, 3, 6.8, 24, 11, 35, 3200, '2026-03-16 09:00:00', '2026-03-16 09:53:20', 58, 312);

-- Chi tiết câu trả lời mẫu
INSERT INTO answers (result_id, question_id, user_selected_option_index, correct_option_index, is_correct, time_spent_seconds) VALUES
(1, 1, 0, 0, TRUE, 120),
(1, 2, 1, 1, TRUE, 95),
(1, 3, 2, 2, TRUE, 110),
(2, 1, 0, 0, TRUE, 130),
(3, 1, 0, 0, TRUE, 125),
(3, 2, 1, 1, TRUE, 100),
(3, 3, 2, 2, TRUE, 115);

-- Thống kê người dùng mẫu
INSERT INTO user_statistics (user_id, total_exams_taken, average_score, highest_score, total_correct, total_wrong, total_time_spent_hours, ranking, streak_days, last_exam_date) VALUES
(1, 32, 7.8, 9.5, 1240, 480, 48, 124, 7, '2026-03-18'),
(2, 28, 8.2, 9.8, 1120, 280, 42, 89, 12, '2026-03-18'),
(4, 15, 6.5, 8.0, 525, 225, 25, 456, 3, '2026-03-16');

-- Thống kê theo môn học mẫu
INSERT INTO subject_statistics (user_id, subject, total_exams, average_score, highest_score, last_exam_date) VALUES
(1, 'Toán học', 12, 8.5, 9.5, '2026-03-18'),
(1, 'Vật lý', 8, 7.2, 8.5, '2026-03-17'),
(1, 'Hóa học', 6, 6.8, 7.8, '2026-03-15'),
(2, 'Toán học', 10, 8.8, 9.8, '2026-03-18'),
(2, 'Ngữ văn', 5, 8.5, 9.2, '2026-03-17');

-- Thống kê hệ thống mẫu
INSERT INTO admin_statistics (stat_date, total_users, active_users, locked_users, new_registrations, total_exams, published_exams, draft_exams, archived_exams, total_attempts, average_score, pass_rate_percent, peak_hour) VALUES
('2026-03-19', 12842, 8241, 142, 24, 2341, 1820, 321, 200, 1284, 6.84, 74.3, 19),
('2026-03-18', 12818, 8217, 140, 18, 2338, 1815, 323, 200, 1156, 6.78, 73.8, 20),
('2026-03-17', 12800, 8199, 138, 22, 2335, 1810, 325, 200, 1089, 6.72, 72.9, 18);

-- Hoạt động hàng ngày mẫu
INSERT INTO daily_activity (activity_date, hour_of_day, exam_attempts, active_users, total_score_sum) VALUES
('2026-03-19', 7, 120, 89, 840.5),
('2026-03-19', 9, 380, 245, 2580.2),
('2026-03-19', 11, 280, 198, 1910.8),
('2026-03-19', 13, 420, 312, 2870.4),
('2026-03-19', 15, 680, 456, 4620.6),
('2026-03-19', 17, 920, 623, 6280.8),
('2026-03-19', 19, 1280, 845, 8700.2),
('2026-03-19', 21, 840, 567, 5710.4);