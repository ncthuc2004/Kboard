# Kaius Keyboard (K_Keyboard)

Biến điện thoại phụ **Redmi 10** (Android 11 / MIUI 12.5, Bluetooth 5.1) thành bàn phím phần cứng cho **Redmi Turbo 4** hoặc **Laptop/PC**.

---

## ⚡ Kiến trúc Dual-Transport

Hệ thống được thiết kế độc lập 3 tầng:
1. **UI Layer (Jetpack Compose)**: Bàn phím chuẩn PC đầy đủ (Esc, F1–F12, số, QWERTY, Caps, Shift, Ctrl, Alt, Win, Space, mũi tên điều hướng), thanh macro nhanh và bảng chẩn đoán sự kiện trực tiếp (Live Diagnostics Console).
2. **Input Engine**: Quản lý trạng thái phím, hỗ trợ phím giữ (hold-to-press), phím gõ (tap), sticky modifiers (`Ctrl`, `Shift`, `Alt`, `Win`) và macro phím tắt.
3. **Transport Layer**:
   - **Mode 1 — Bluetooth HID (Ưu tiên)**: Sử dụng Android `BluetoothHidDevice` API, đăng ký profile HID Keyboard chuẩn. Thiết bị nhận (Redmi Turbo 4, Laptop, TV) nhận diện Redmi 10 như một bàn phím Bluetooth thật mà không cần cài thêm app nhận.
   - **Mode 2 — Wi-Fi LAN / Hotspot (Dự phòng & Tốc độ cao)**: Truyền gói tin UDP siêu nhẹ qua mạng nội bộ hoặc qua trực tiếp Wi-Fi Hotspot của máy đích, độ trễ < 5ms, không bị ảnh hưởng bởi chính sách Bluetooth của MIUI.

---

## 🚀 Hướng dẫn sử dụng

### 1. Cài đặt lên Redmi 10
Bản build APK debug nằm tại:
```
app/build/outputs/apk/debug/app-debug.apk
```
Cài đặt qua ADB:
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Sử dụng Mode 1: Bluetooth HID (Chuẩn)
1. Mở app **Kaius Keyboard** trên Redmi 10.
2. Cấp quyền Bluetooth khi được hỏi. App sẽ tự động giữ sáng màn hình (`FLAG_KEEP_SCREEN_ON`) để MIUI không đóng profile HID.
3. Bấm biểu tượng 🔍 (Pairing) trên góc phải để đưa Redmi 10 vào trạng thái Discoverable.
4. Trên **Redmi Turbo 4** (hoặc Laptop):
   - Mở Cài đặt Bluetooth -> Quét thiết bị mới.
   - Chọn **Kaius Keyboard** và chấp nhận Pair.
5. Sau khi kết nối, trạng thái trên app sẽ chuyển sang màu xanh lá: `Đã kết nối: <Tên thiết bị>`.
6. Bấm phím bất kỳ trên Redmi 10 để gõ phím sang máy nhận!

### 3. Sử dụng Mode 2: Wi-Fi LAN / Hotspot (Dự phòng)
1. Trên thanh công cụ, bấm chuyển sang nút **[ 🟢 Wi-Fi LAN ]**.
2. Kết nối Redmi 10 vào Wi-Fi chung hoặc vào Hotspot của máy nhận.
3. Bấm biểu tượng bánh răng ⚙️ để nhập IP đích:
   - Nếu Redmi 10 kết nối vào Hotspot của Turbo 4: Chọn preset `192.168.43.1`.
   - Nếu test trên Laptop Windows: Chạy script receiver trên laptop:
     ```powershell
     python receiver/pc_receiver.py
     ```
     Nhập IP hiện trên màn hình máy tính vào app và bấm **Lưu & Kết nối**.

---

## 🛠️ Công cụ Live Diagnostics
Bấm biểu tượng `>_` trên thanh công cụ để mở/đóng Terminal chẩn đoán. Mọi sự kiện Bluetooth (registerApp, SDP callback, connection state, error report) đều hiển thị theo thời gian thực để chẩn đoán chính xác hành vi MIUI.
