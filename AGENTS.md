# Project Rules & Guidelines - Kboard (Kaius Keyboard)

## 1. App Versioning & Dedicated Release Directory (BẮT BUỘC)
- **Luôn tăng version khi build bản mới**:
  - Mỗi khi thay đổi code, sửa lỗi hoặc thêm tính năng và xuất file APK cho người dùng, **BẮT BUỘC** phải tăng `versionCode` và `versionName` trong file [build.gradle.kts](file:///t:/Kaius_Inc/K_Keyboard/app/build.gradle.kts):
    - `versionCode`: Số nguyên tăng dần (+1 mỗi lần build: 1 -> 2 -> 3 -> 4 -> 5 -> 6...).
    - `versionName`: Chuẩn Semantic Versioning (ví dụ: `1.0.0` -> `1.1.0` -> `1.1.4`...).
- **Mỗi phiên bản một thư mục riêng biệt (`releases/v<versionName>/`)**:
  - Người dùng yêu cầu nghiêm ngặt: **Từ bản v1.1.4 về sau, mỗi version mới phát hành phải được lưu trữ trong một thư mục riêng biệt** `releases/v<versionName>/` (ví dụ: `releases/v1.1.4/`).
  - Trong thư mục của từng phiên bản phải chứa đầy đủ:
    1. `Kboard_v<versionName>.apk` (file APK gắn tag phiên bản).
    2. `Kboard.apk` (file APK chuẩn).
    3. `Kaius_Receiver.exe` (bộ nhận Windows đồng bộ).
  - Duy trì duy nhất file APK chuẩn ở thư mục gốc: `Kboard.apk`.
- **Hiển thị Version trực quan trên giao diện (UI)**:
  - Header chính (`LandscapeUnifiedHeader`): Luôn hiển thị nhãn phiên bản bên cạnh logo, ví dụ `KBOARD v1.1.4`.
  - Thanh trạng thái bàn phím ảo (`KaiusImeService`): Hiển thị `Kboard v1.1.4` để người dùng xác nhận đúng bản đang chạy trong app clone trên máy nhận.

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
- **Nghiêm cấm dùng icon Emoji (🔍, ⌨️, 🖥️, ⚡, 🇻🇳...)**:
  - Toàn bộ giao diện người dùng, thanh trạng thái, nút bấm và tài liệu phải giữ phong cách công nghệ cơ học tối giản (Minimalist Industrial/Cyberpunk), dùng nhãn chữ rõ ràng hoặc Material Vector Icons, tuyệt đối không dùng emoji hình vẽ.
