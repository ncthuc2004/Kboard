# Project Rules & Guidelines - Kaius Keyboard

## 1. App Versioning & Release (BẮT BUỘC)
- **Luôn tăng version khi build bản mới**:
  - Mỗi khi thay đổi code, sửa lỗi hoặc thêm tính năng và xuất file APK cho người dùng, **BẮT BUỘC** phải tăng `versionCode` và `versionName` trong file [build.gradle.kts](file:///t:/Kaius_Inc/K_Keyboard/app/build.gradle.kts):
    - `versionCode`: Số nguyên tăng dần (+1 mỗi lần build: 1 -> 2 -> 3 -> 4...).
    - `versionName`: Chuẩn Semantic Versioning (ví dụ: `1.0.0` -> `1.1.0` -> `1.1.1`...).
- **Hiển thị Version trực quan trên giao diện (UI)**:
  - Header chính (`LandscapeUnifiedHeader`): Luôn hiển thị nhãn phiên bản bên cạnh logo, ví dụ `KAIUS v1.1.0`.
  - Thanh trạng thái bàn phím ảo (`KaiusImeService`): Hiển thị `⌨️ Kaius v1.1.0` để người dùng xác nhận đúng bản đang chạy trong app clone trên máy nhận.
- **Xuất APK ra thư mục gốc**:
  - Sau khi build thành công `./gradlew assembleDebug`, luôn sao chép file APK từ `app/build/outputs/apk/debug/app-debug.apk` ra thư mục gốc: `t:\Kaius_Inc\K_Keyboard\Kaius_Keyboard.apk`.

## 2. Quản lý Git Local
- **Commit Git sau mỗi tác vụ**:
  - Người dùng yêu cầu nghiêm ngặt: Thực hiện xong bất kỳ tính năng, fix bug hoặc build mới nào đều phải commit git local (`git commit -m "..."`) đầy đủ, không để sót uncommitted changes.

## 3. Kiến trúc & Trải nghiệm (UX/UI)
- **Khóa xoay ngang cảm biến (`sensorLandscape`)**:
  - Bàn phím hoạt động ở chế độ nằm ngang với giao diện tràn viền cạnh-đến-cạnh (Edge-to-Edge).
- **Layout chuẩn 75% PC Keyboard**:
  - Đầy đủ 6 hàng: Hàng F1-F12, Esc, Del, các ký tự đặc biệt, phím bổ trợ Ctrl, Alt, Win, Shift, mũi tên 4 hướng.
  - Phím tắt nhanh Macro trên thanh công cụ: `Ctrl+C`, `Ctrl+V`, `Ctrl+A`, `Ctrl+Z`, `Alt+Tab`, `Win`.
- **Đè phím lặp ký tự (Hardware Key Repeat)**:
  - Nhấn giữ quá 380ms sẽ tự động lặp ký tự liên tục chu kỳ 45ms (~22 ký tự/giây) như phím vật lý.
- **Bộ gõ Tiếng Việt Telex tích hợp**:
  - `VietnameseTelexEngine` xử lý dấu tự nhiên trực tiếp trong `KaiusImeService` cho máy nhận mà không cần cài thêm bàn phím bên thứ 3.
- **Không dùng AI Slop**:
  - Giữ phong cách cơ học tối giản, màu sắc công nghệ dark mode sắc nét, không dùng gradient lòe loẹt hoặc icon placeholder giả.
