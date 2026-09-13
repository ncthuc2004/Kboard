# Kaius Wi-Fi LAN Receiver

Bộ nhận phím qua Wi-Fi nội bộ / Hotspot tốc độ cao cho máy tính (Windows / Linux / Mac).

## Chạy trên Windows:
Không cần cài thêm bất kỳ thư viện nào (sử dụng trực tiếp Windows API `SendInput` thông qua `ctypes`).

```powershell
python pc_receiver.py
```

Khi chạy, script sẽ in ra địa chỉ IP của máy tính (ví dụ: `192.168.1.15:8964`).
Chỉ cần nhập IP này vào app trên điện thoại Redmi 10 (trong mục Cài đặt Wi-Fi), mọi thao tác gõ phím trên điện thoại sẽ được gửi thẳng tới máy tính với độ trễ cực thấp.
