# API Documentation - HelloQuizz System

## Tổng quan
Tài liệu này mô tả các API REST cần thiết cho hệ thống HelloQuizz. Tất cả API đều sử dụng JSON cho request/response body và chạy trên localhost.

**Base URL:** `http://localhost:3000/api`

**Authentication:** Sử dụng JWT token trong header `Authorization: Bearer <token>`

---

## 1. Authentication APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Đăng ký tài khoản mới | POST | `/auth/register` | `{"full_name": "string", "email": "string", "password": "string", "username": "string", "phone": "string", "gender": "Male/Female/Other", "date_of_birth": "YYYY-MM-DD", "school_name": "string"}` | `{"success": true, "message": "Đăng ký thành công", "user": {...}, "token": "jwt_token"}` |
| Đăng nhập | POST | `/auth/login` | `{"email": "string", "password": "string"}` | `{"success": true, "message": "Đăng nhập thành công", "user": {...}, "token": "jwt_token"}` |
| Đăng xuất | POST | `/auth/logout` | Không cần input (sử dụng token) | `{"success": true, "message": "Đăng xuất thành công"}` |
| Lấy thông tin user hiện tại | GET | `/auth/me` | Không cần input (sử dụng token) | `{"success": true, "user": {...}}` |

---

## 2. User Management APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Lấy thông tin profile | GET | `/users/profile` | Không cần input (sử dụng token) | `{"success": true, "user": {...}}` |
| Cập nhật profile | PUT | `/users/profile` | `{"full_name": "string", "phone": "string", "gender": "Male/Female/Other", "date_of_birth": "YYYY-MM-DD", "school_name": "string", "bio": "string"}` | `{"success": true, "message": "Cập nhật thành công", "user": {...}}` |
| Đổi mật khẩu | PUT | `/users/change-password` | `{"current_password": "string", "new_password": "string"}` | `{"success": true, "message": "Đổi mật khẩu thành công"}` |
| Upload avatar | POST | `/users/upload-avatar` | Form-data với file `avatar` | `{"success": true, "message": "Upload thành công", "avatar_url": "url"}` |

---

## 3. Exam Management APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Lấy danh sách đề thi | GET | `/exams?page=1&limit=10&subject=Toán học&difficulty=easy` | Query parameters | `{"success": true, "exams": [...], "pagination": {...}}` |
| Lấy chi tiết đề thi | GET | `/exams/{exam_id}` | Không cần input | `{"success": true, "exam": {...}}` |
| Tạo đề thi mới (Admin) | POST | `/exams` | `{"title": "string", "description": "string", "subject": "string", "total_questions": 50, "duration_minutes": 90, "difficulty": "easy/medium/hard", "pass_score": 5.0}` | `{"success": true, "message": "Tạo đề thi thành công", "exam": {...}}` |
| Cập nhật đề thi (Admin) | PUT | `/exams/{exam_id}` | `{"title": "string", "description": "string", "status": "published/draft/archived"}` | `{"success": true, "message": "Cập nhật thành công", "exam": {...}}` |
| Xóa đề thi (Admin) | DELETE | `/exams/{exam_id}` | Không cần input | `{"success": true, "message": "Xóa đề thi thành công"}` |
| Lấy danh sách đề thi theo môn học | GET | `/exams/subject/{subject}` | Không cần input | `{"success": true, "exams": [...]} ` |

---

## 4. Question Management APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Lấy câu hỏi của đề thi | GET | `/exams/{exam_id}/questions` | Không cần input | `{"success": true, "questions": [...], "exam": {...}}` |
| Tạo câu hỏi mới (Admin) | POST | `/exams/{exam_id}/questions` | `{"question_text": "string", "question_number": 1, "options": [{"key": "A", "text": "string", "is_correct": false}, ...], "explanation": "string"}` | `{"success": true, "message": "Tạo câu hỏi thành công", "question": {...}}` |
| Cập nhật câu hỏi (Admin) | PUT | `/questions/{question_id}` | `{"question_text": "string", "options": [...], "explanation": "string"}` | `{"success": true, "message": "Cập nhật thành công", "question": {...}}` |
| Xóa câu hỏi (Admin) | DELETE | `/questions/{question_id}` | Không cần input | `{"success": true, "message": "Xóa câu hỏi thành công"}` |

---

## 5. Taking Exam APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Bắt đầu thi | POST | `/exams/{exam_id}/start` | Không cần input | `{"success": true, "exam_session": {"session_id": "string", "exam": {...}, "started_at": "timestamp", "time_remaining": 5400}}` |
| Lấy câu hỏi tiếp theo | GET | `/exam-sessions/{session_id}/question/{question_number}` | Không cần input | `{"success": true, "question": {...}, "time_remaining": 5300}` |
| Trả lời câu hỏi | POST | `/exam-sessions/{session_id}/answer` | `{"question_id": 1, "selected_option": "A", "marked_for_review": false}` | `{"success": true, "message": "Đã lưu câu trả lời"}` |
| Đánh dấu xem lại | PUT | `/exam-sessions/{session_id}/mark-review/{question_id}` | `{"marked_for_review": true}` | `{"success": true, "message": "Đã đánh dấu"}` |
| Nộp bài thi | POST | `/exam-sessions/{session_id}/submit` | Không cần input | `{"success": true, "result": {...}, "message": "Nộp bài thành công"}` |
| Lấy tiến độ thi | GET | `/exam-sessions/{session_id}/progress` | Không cần input | `{"success": true, "progress": {"answered": 25, "marked": 5, "remaining": 20, "time_remaining": 3600}}` |

---

## 6. Results APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Lấy danh sách kết quả của user | GET | `/users/results?page=1&limit=10` | Query parameters | `{"success": true, "results": [...], "pagination": {...}}` |
| Lấy chi tiết kết quả | GET | `/results/{result_id}` | Không cần input | `{"success": true, "result": {...}, "answers": [...], "exam": {...}}` |
| Lấy kết quả theo đề thi | GET | `/exams/{exam_id}/results` | Query parameters | `{"success": true, "results": [...], "exam": {...}}` |
| Xem lại bài thi | GET | `/results/{result_id}/review` | Không cần input | `{"success": true, "result": {...}, "questions_with_answers": [...], "statistics": {...}}` |

---

## 7. Statistics APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Lấy thống kê cá nhân | GET | `/users/statistics` | Không cần input | `{"success": true, "statistics": {...}, "subject_stats": [...], "recent_results": [...]} ` |
| Lấy thống kê theo môn học | GET | `/users/statistics/subject/{subject}` | Không cần input | `{"success": true, "subject_statistics": {...}, "exam_history": [...]} ` |
| Thống kê hệ thống (Admin) | GET | `/admin/statistics` | Query parameters (date range) | `{"success": true, "system_stats": {...}, "daily_activity": [...], "charts_data": {...}}` |
| Báo cáo hoạt động hàng ngày (Admin) | GET | `/admin/daily-activity?date=2026-03-19` | Query parameters | `{"success": true, "activity": [...], "summary": {...}}` |

---

## 8. Admin User Management APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Lấy danh sách users (Admin) | GET | `/admin/users?page=1&limit=10&role=student&status=active` | Query parameters | `{"success": true, "users": [...], "pagination": {...}}` |
| Lấy chi tiết user (Admin) | GET | `/admin/users/{user_id}` | Không cần input | `{"success": true, "user": {...}, "statistics": {...}}` |
| Cập nhật user (Admin) | PUT | `/admin/users/{user_id}` | `{"status": "active/inactive/locked", "role": "student/admin"}` | `{"success": true, "message": "Cập nhật thành công", "user": {...}}` |
| Xóa user (Admin) | DELETE | `/admin/users/{user_id}` | Không cần input | `{"success": true, "message": "Xóa user thành công"}` |
| Reset mật khẩu user (Admin) | POST | `/admin/users/{user_id}/reset-password` | `{"new_password": "string"}` | `{"success": true, "message": "Reset mật khẩu thành công"}` |

---

## 9. File Upload APIs

| Mô tả | Method | URL | Input | Output |
|-------|--------|-----|-------|--------|
| Upload file đính kèm cho đề thi (Admin) | POST | `/exams/{exam_id}/upload-file` | Form-data với file | `{"success": true, "message": "Upload thành công", "file_url": "url"}` |
| Upload ảnh cho câu hỏi (Admin) | POST | `/questions/{question_id}/upload-image` | Form-data với file `image` | `{"success": true, "message": "Upload thành công", "image_url": "url"}` |

---

## Response Format Standards

### Success Response:
```json
{
  "success": true,
  "message": "Thao tác thành công",
  "data": {...},
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 100,
    "total_pages": 10
  }
}
```

### Error Response:
```json
{
  "success": false,
  "message": "Lỗi xảy ra",
  "error": "ERROR_CODE",
  "details": {...}
}
```

### Common Error Codes:
- `VALIDATION_ERROR`: Dữ liệu đầu vào không hợp lệ
- `UNAUTHORIZED`: Chưa đăng nhập hoặc token hết hạn
- `FORBIDDEN`: Không có quyền truy cập
- `NOT_FOUND`: Không tìm thấy tài nguyên
- `CONFLICT`: Xung đột dữ liệu (email đã tồn tại, v.v.)
- `INTERNAL_ERROR`: Lỗi server

---

## Authentication Notes

- Token được trả về trong response của `/auth/login` và `/auth/register`
- Gửi token trong header: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
- Token có thời hạn, cần refresh khi hết hạn
- Admin APIs yêu cầu role = 'admin' trong token

---

## Pagination

Các API trả về danh sách sử dụng pagination:
- `page`: Trang hiện tại (bắt đầu từ 1)
- `limit`: Số items per page (mặc định 10)
- `total`: Tổng số items
- `total_pages`: Tổng số trang

Example: `/exams?page=2&limit=20`