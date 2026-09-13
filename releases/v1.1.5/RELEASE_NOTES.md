# Kboard Release v1.1.5 (versionCode 7)

## Điểm mới trong phiên bản v1.1.5:
1. **Chuyển nút bật/tắt Telex về cài đặt và thanh công cụ Bàn phím:**
   - Thay vì phải chỉnh trên máy nhận (InputMethodService của máy nhận), nay người dùng có thể chuyển đổi linh hoạt chế độ gõ **VI (Telex)** <-> **EN (Tiếng Anh)** trực tiếp ngay trên bàn phím đang gõ.
2. **Nút gõ tắt nhanh `[ VI ]` / `[ EN ]` trên Macro Bar:**
   - Nằm ngay đầu thanh công cụ Quick Macro Bar phía trên hàng phím số, chuyển đổi 1 chạm cực kỳ tiện lợi khi cần gõ mật khẩu, URL hoặc văn bản tiếng Việt.
3. **Mục Cài đặt Bàn phím (Keyboard Settings Dialog):**
   - Tích hợp mục cấu hình **Bộ gõ Tiếng Việt Telex** chi tiết trong hộp thoại cài đặt (icon bánh răng ⚙️ trên thanh header), lưu trạng thái tự động vào SharedPreferences.
4. **Tự động đồng bộ cấu hình Telex tới máy nhận:**
   - Giao thức Wi-Fi LAN truyền trạng thái Telex kèm theo từng gói phím và thông qua gói điều khiển `ACTION_SET_TELEX`.
   - Máy nhận tự động nhận diện và cập nhật huy hiệu ngôn ngữ `[ VI ]` / `[ EN ]` theo thời gian thực mà người dùng không cần thao tác gì thêm trên máy nhận.

## Tệp đính kèm trong thư mục:
* `Kboard_v1.1.5.apk`: Bản cài đặt Android (API 28+).
* `Kboard.apk`: File cài đặt chuẩn.
* `Kboard_Receiver.exe`: Bộ nhận phím độc lập cho máy tính Windows (đồng bộ thương hiệu Kboard).
