# Kaius Keyboard & Receiver (K_Keyboard)

Ứng dụng 2-trong-1 (Universal Single App): Biến điện thoại phụ **Redmi 10** thành bàn phím và điện thoại chính **Redmi Turbo 4** thành thiết bị nhận (Receiver) hoặc gõ phím trực tiếp sang **Laptop/PC**.

---

## ⚡ Kiến trúc Dual-Role (Chung 1 App trên cả 2 máy)

Khi mở app, bạn có thể chuyển đổi vai trò ngay tức thì:
- `[ ⌨️ Làm bàn phím ]`: Dành cho **Redmi 10** (hoặc bất kỳ máy nào muốn làm bàn phím).
- `[ 🖥️ Nhận bàn phím ]`: Dành cho **Redmi Turbo 4** (hoặc tablet/máy nhận khi dùng Wi-Fi LAN).

```text
                    KAIUS INPUT
                         │
              ┌──────────┴──────────┐
              │                     │
       📱 KEYBOARD MODE       📱 RECEIVER MODE
          (Redmi 10)           (Redmi Turbo 4)
              │                     │
       ┌──────┴──────┐              │
       │             │              │
   Bluetooth       Wi-Fi            │
      HID         (UDP LAN)         │
       │             └──────►───────┘
       ▼                            │
  Redmi Turbo 4 / Laptop      Nhận phím trực tiếp
  (Không cần cài app)        (Hiện phím, text buffer)
```

---

## 🚀 Hướng dẫn sử dụng thực tế

### 1. Cài đặt file APK (Cài cùng 1 file APK lên cả 2 máy)
Bản build APK debug nằm tại:
```
app/build/outputs/apk/debug/app-debug.apk
```
Cài đặt qua ADB:
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

### 2. Kịch bản 1: Bluetooth HID (Chuẩn nhất — Turbo 4 không cần mở app)
1. **Redmi 10**: Chọn `[ ⌨️ Làm bàn phím ]` -> Chọn `[ 🔵 BT HID ]` -> Bấm icon kính lúp để Discoverable.
2. **Redmi Turbo 4**: Vào Cài đặt Bluetooth của Android -> Quét và chọn **Kaius Keyboard** -> Pair.
3. Redmi Turbo 4 sẽ nhận diện Redmi 10 như bàn phím Bluetooth phần cứng thật. Không cần cài hay mở app trên Turbo 4!

---

### 3. Kịch bản 2: Wi-Fi LAN với Receiver Mode (Cả 2 máy mở app)
1. **Redmi Turbo 4**: Mở app -> Bấm `[ 🖥️ Nhận bàn phím ]`.
   - Máy sẽ tự động bật UDP Server trên cổng 8964 và phát tín hiệu LAN Discovery.
2. **Redmi 10**: Mở app -> Chọn `[ ⌨️ Làm bàn phím ]` -> Chọn `[ 🟢 Wi-Fi LAN ]`.
   - App sẽ **tự động quét và hiển thị**: `🟢 Redmi Turbo 4 (192.168.x.x)`.
   - Bấm **1 chạm vào tên máy** để kết nối ngay, không cần gõ IP thủ công!
3. Bấm phím trên Redmi 10 -> Màn hình Redmi Turbo 4 sẽ hiển thị phím bấm trực tiếp, modifier sáng đèn và nội dung văn bản gõ theo thời gian thực (hỗ trợ nút Copy tức thì).

---

### 4. Kịch bản 3: Gõ phím sang Laptop/PC qua Wi-Fi
1. Trên Laptop Windows, chạy receiver:
   ```powershell
   python receiver/pc_receiver.py
   ```
2. Trên Redmi 10 (chế độ Wi-Fi LAN), app sẽ tự tìm thấy `🟢 PC (<Tên máy tính>)`.
3. Bấm kết nối -> Bấm phím trên điện thoại sẽ được gõ trực tiếp vào các ứng dụng trên máy tính!

---

## 🛠️ Công cụ Live Diagnostics
Bấm biểu tượng `>_` trên thanh công cụ để mở/đóng Terminal chẩn đoán. Mọi sự kiện Bluetooth (registerApp, SDP callback, connection state, error report) đều hiển thị theo thời gian thực để chẩn đoán chính xác hành vi MIUI.
