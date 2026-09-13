# Kboard (KAIUS KEYBOARD & RECEIVER)

<div align="center">

![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Windows-00E5FF?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Bluetooth](https://img.shields.io/badge/Bluetooth-HID%20Profile-0075FF?style=for-the-badge&logo=bluetooth&logoColor=white)
![Network](https://img.shields.io/badge/LAN-UDP%20Auto--Discovery-00C853?style=for-the-badge)
![License](https://img.shields.io/badge/Version-v1.1.11-FF6D00?style=for-the-badge)

**Giải pháp biến điện thoại thông minh thành Bàn phím cơ ảo siêu mượt, độ trễ cực thấp cho Máy tính, Laptop và Điện thoại khác.**

<br/>

[![Download Android APK](https://img.shields.io/badge/Tải_ngay-Kboard.apk_(Android)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/ncthuc2004/Kboard/releases/download/v1.1.11/Kboard.apk)
[![Download Windows Receiver](https://img.shields.io/badge/Tải_ngay-Kboard_Receiver.exe_(Windows)-0078D6?style=for-the-badge&logo=windows&logoColor=white)](https://github.com/ncthuc2004/Kboard/releases/download/v1.1.11/Kboard_Receiver.exe)
[![View All Releases](https://img.shields.io/badge/Tất_cả_phiên_bản-GitHub_Releases-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ncthuc2004/Kboard/releases)

<br/>

**Tác giả:** [Nguyễn Công Thức (Kaius)](https://github.com/ncthuc2004) • **Phiên bản mới nhất:** [v1.1.11 Release](https://github.com/ncthuc2004/Kboard/releases/tag/v1.1.11)

</div>

---

## 1. Giới thiệu tổng quan

**Kaius Keyboard** là hệ thống emulated input thế hệ mới với kiến trúc **Dual-Role (2-trong-1)** và **Dual-Transport (Bluetooth HID + Wi-Fi LAN)**:
- Biến một chiếc điện thoại phụ thành **bàn phím rời chuẩn 75% PC Keyboard** với đầy đủ các phím điều hướng, hàng phím chức năng F1-F12, phím số, phím bổ trợ Ctrl/Alt/Win/Shift và thanh phím tắt macro.
- Hỗ trợ gõ trực tiếp vào **Máy tính/Laptop Windows** (không cần cài driver rườm rà) hoặc gửi phím sang một **Điện thoại khác** để điều khiển, chơi game, hoặc nhập liệu tốc độ cao vào các **ứng dụng clone (App Clone / Dual Apps)**.

---

## 2. Tính năng công nghệ nổi bật

### Layout chuẩn 75% PC Mechanical Keyboard
* **Tối ưu hóa xoay ngang (sensorLandscape)**: Giao diện tràn viền cạnh-đến-cạnh (Edge-to-Edge) tối ưu cho thao tác 2 ngón cái, thiết kế cơ học Dark Mode sắc nét, không bị chạm nhầm.
* **Đầy đủ 6 hàng phím PC**: Đầy đủ `Esc`, hàng `F1` - `F12`, phím `Del`, dãy số & ký tự đặc biệt, phím điều hướng `▲ ▼ ◄ ►`, `Tab`, `Caps Lock`, `Enter`, `Backspace`, `Ctrl`, `Alt`, `Win`, `Shift`.
* **Hardware Key Repeat (Đè phím lặp ký tự)**: Tích hợp engine lặp phím phần cứng — nhấn giữ phím quá 380ms sẽ kích hoạt chu kỳ lặp siêu tốc 45ms (~22 ký tự/giây) hệt như bàn phím cơ thật.
* **Thanh Macro 1 chạm**: Tích hợp sẵn các tổ hợp phím phổ biến: `Ctrl+C`, `Ctrl+V`, `Ctrl+A`, `Ctrl+Z`, `Alt+Tab`, `Win`.

### Công nghệ truyền tải kép (Dual-Transport)
* **Bluetooth HID Device Profile**:
  * Đóng vai trò là một thiết bị ngoại vi USB HID Keyboard chuẩn quốc tế.
  * Nếu thiết bị gửi hỗ trợ Bluetooth HID Device, máy nhận (PC, Laptop, Smartphone) sẽ nhận diện trực tiếp như bàn phím Bluetooth thật mà **hoàn toàn không cần cài bất kỳ phần mềm nào trên máy nhận**.
* **Wi-Fi LAN UDP Bridge (Độ trễ < 1ms)**:
  * Hoạt động thông qua mạng Wi-Fi nội bộ bằng giao thức UDP phân gói siêu nhẹ trên cổng 8964.
  * **Tự động dò tìm thiết bị (Zero-Config Auto Discovery)**: Thiết bị gửi tự động quét và phát hiện các máy nhận đang trực tuyến trong mạng LAN, kết nối tức thì chỉ với 1 chạm.

### Bộ gõ Tiếng Việt Telex & Bàn phím ảo hệ thống (Android IME)
* **Tích hợp sẵn KaiusImeService**:
  * Hoạt động như một bộ gõ hệ thống (Input Method Service) trên điện thoại nhận.
  * Cho phép gõ phím trực tiếp vào **App Clone, ứng dụng chat, game giả lập** mà không bị giới hạn bởi sandbox bảo mật của Android.
* **Vietnamese Telex Engine nguyên bản**:
  * Tự động nhận diện và xử lý dấu tiếng Việt theo chuẩn Telex (`aa` -> `â`, `aw` -> `ă`, `dd` -> `đ`, `s/f/r/x/j` bỏ dấu tự nhiên).
  * Nút chuyển đổi nhanh `[ VI ]` / `[ EN ]` trực tiếp trên bàn phím (Macro Bar) và trong hộp thoại Cài đặt Bàn phím, tự động đồng bộ tới máy nhận theo thời gian thực.

### Windows Companion Receiver (Kboard_Receiver.exe)
* Standalone Receiver cho Windows (~8.2MB), không cần cài đặt Python hay driver phức tạp.
* Sử dụng Windows API `SendInput` kết hợp phần cứng scancode `user32.MapVirtualKeyW`, mô phỏng chính xác tín hiệu bàn phím vật lý cho mọi tựa game và ứng dụng Windows.

---

## 3. Sơ đồ kiến trúc hệ thống

```text
                           ┌───────────────────────────────┐
                           │      KAIUS KEYBOARD APP       │
                           │   (Giao diện 75% PC Layout)   │
                           └───────────────┬───────────────┘
                                           │
                    ┌──────────────────────┴──────────────────────┐
                    │                                             │
         [ Bluetooth HID Mode ]                         [ Wi-Fi LAN UDP Mode ]
         Profile HID Device (SDP)                       Cổng UDP 8964 + Auto Discovery
                    │                                             │
        ┌───────────┴───────────┐                         ┌───────┴───────┐
        ▼                       ▼                         ▼               ▼
┌──────────────┐        ┌──────────────┐           ┌──────────────┐ ┌──────────────┐
│   LAPTOP /   │        │  ĐIỆN THOẠI  │           │  ĐIỆN THOẠI  │ │   WINDOWS    │
│   MÁY TÍNH   │        │     NHẬN     │           │  (KAIUS IME) │ │   RECEIVER   │
│(Nhận như kbd │        │(Pair trực    │           │(Gõ App Clone,│ │ (SendInput   │
│  Bluetooth)  │        │ tiếp qua BT) │           │ game, Telex) │ │  Scancodes)  │
└──────────────┘        └──────────────┘           └──────────────┘ └──────────────┘
```

---

## 4. Tải về & Hướng dẫn sử dụng

### Bảng tải về trực tiếp (Downloads)

| Nền tảng | Tệp cài đặt | Nút tải trực tiếp (1-Click) | Ghi chú |
| :--- | :--- | :---: | :--- |
| **Android** | `Kboard.apk` | [**Tải Kboard.apk**](https://github.com/ncthuc2004/Kboard/releases/download/v1.1.4/Kboard.apk) | Bản cài đặt cho điện thoại làm bàn phím hoặc điện thoại nhận |
| **Windows** | `Kboard_Receiver.exe` | [**Tải Windows Receiver**](https://github.com/ncthuc2004/Kboard/releases/download/v1.1.4/Kboard_Receiver.exe) | Chạy ngay trên Laptop/PC Windows (không cần cài đặt driver) |
| **Lịch sử** | Mọi phiên bản | [**Xem GitHub Releases**](https://github.com/ncthuc2004/Kboard/releases) | Kho lưu trữ toàn bộ các phiên bản v1.1.4, v1.1.3... |

---

### Kịch bản 1: Kết nối Wi-Fi LAN sang Điện thoại nhận (Hỗ trợ App Clone)
1. **Trên điện thoại nhận**:
   * Vào **Cài đặt** -> **Hệ thống & Bàn phím** -> Bật kích hoạt **Bàn phím Kboard LAN**.
   * Mở app Kboard -> Chọn vai trò `[ Nhận phím ]`.
   * Mở ứng dụng cần gõ (App clone, game, trình duyệt...) và chọn bàn phím ảo là **Kboard LAN**.
2. **Trên điện thoại làm bàn phím**:
   * Mở app -> Chọn `[ Bàn phím ]` -> Chọn tab `[ Wi-Fi LAN ]`.
   * App sẽ tự động quét và hiện tên máy nhận trên thanh kết nối. Bấm vào tên máy để kết nối ngay.
   * Gõ chữ, phím tắt `Ctrl+C`, `Ctrl+V`, `Ctrl+A` sẽ được truyền trực tiếp vào app clone với độ trễ gần như bằng 0.

---

### Kịch bản 2: Kết nối Wi-Fi LAN sang Máy tính / Laptop Windows
1. Trên máy tính Windows: Khởi chạy file `Kboard_Receiver.exe` (hoặc chạy lệnh `python receiver/pc_receiver.py`).
2. Trên điện thoại: Chọn chế độ `[ Wi-Fi LAN ]` -> Bấm nút **[ Quét LAN ]** -> Chọn thiết bị PC vừa tìm thấy.
3. Bắt đầu gõ phím, điều khiển trình chiếu, chơi game hoặc gõ văn bản từ xa.

---

### Kịch bản 3: Kết nối Bluetooth HID trực tiếp
*(Áp dụng cho các dòng điện thoại Android có hỗ trợ Bluetooth HID Device Profile ở cấp phần cứng)*
1. Trên điện thoại làm bàn phím: Chọn tab `[ Bluetooth HID ]` -> Bấm nút phát hiện (`Discoverable`).
2. Trên thiết bị nhận (Laptop hoặc điện thoại khác): Mở Bluetooth máy -> Dò tìm thiết bị tên **KAIUS** hoặc tên điện thoại của bạn -> Bấm **Ghép đôi (Pair)**.
3. Sau khi kết nối thành công, điện thoại sẽ trở thành bàn phím không dây độc lập.

---

## 5. Công nghệ & Ngôn ngữ sử dụng

* **Android:** Kotlin 1.9+, Jetpack Compose, Coroutines Flow, AndroidX Lifecycle, Android IME (`InputMethodService`).
* **Networking:** Java NIO DatagramChannel & DatagramSocket UDP, Broadcast Discovery protocol.
* **Bluetooth:** Android Bluetooth HID Device API (`BluetoothHidDevice`), SDP Keyboard Descriptor.
* **Windows Host:** Python 3 / PyInstaller standalone exe, Windows Win32 API (`SendInput`, `MapVirtualKeyW`).

---

## 6. Tác giả
 
* **Kỹ sư phát triển:** **Nguyễn Công Thức (Kaius)**
* **GitHub:** [https://github.com/ncthuc2004](https://github.com/ncthuc2004)
* **Dự án:** *Kaius Keyboard & Receiver Ecosystem*

---

<div align="center">
  <i>Được phát triển với tinh thần kỹ thuật chính xác, tối ưu hóa phần cứng và trải nghiệm người dùng không thỏa hiệp.</i>
</div>
