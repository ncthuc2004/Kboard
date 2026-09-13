# Kboard Release v1.1.7 (versionCode 9)

## Điểm mới trong phiên bản v1.1.7:
1. **Tối ưu hóa kết nối Bluetooth HID với Laptop / PC:**
   - Cập nhật SDP record định danh chính xác tên thiết bị là **Kboard** (thay vì Kaius Keyboard).
   - Bổ sung chỉ dẫn kết nối chi tiết ngay trong hộp thoại Bluetooth: Hướng dẫn người dùng xóa ghép đôi cũ trên Laptop và thực hiện kết nối từ Laptop (Add device) để Windows nhận diện chuẩn thiết bị ngoại vi Bàn phím HID.
   - Thêm thông báo chẩn đoán trực tiếp trong Log Console khi kết nối bị timeout (16s) do Windows từ chối kênh L2CAP HID.
2. **Khắc phục triệt để hiển thị Version trên toàn bộ giao diện:**
   - Màn hình Bàn phím: `KBOARD v1.1.7`
   - Màn hình Nhận phím: `RECEIVER v1.1.7`
   - Hộp thoại Cài đặt (⚙️): `Kboard v1.1.7 (Build 9)`
   - Thanh trạng thái Bàn phím ảo IME: `Kboard v1.1.7: Sẵn sàng nhận phím`
3. **Biểu tượng ứng dụng cân đối (49% Canvas, Safe Padding 25%):**
   - Đảm bảo hiển thị hoàn hảo trên MIUI / HyperOS / OneUI mà không bị lẹm góc.

## Tệp đính kèm trong thư mục:
* `Kboard_v1.1.7.apk`: Bản cài đặt Android (API 28+).
* `Kboard.apk`: File cài đặt chuẩn.
* `Kboard_Receiver.exe`: Bộ nhận phím độc lập cho máy tính Windows.
