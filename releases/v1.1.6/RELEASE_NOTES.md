# Kboard Release v1.1.6 (versionCode 8)

## Điểm mới trong phiên bản v1.1.6:
1. **Khắc phục triệt để lỗi logo bị lẹm viền (Icon Padding & Safe Zone):**
   - Biểu tượng bàn phím trắng được tinh chỉnh kích thước chuẩn xác về tỷ lệ **49%** (thay vì chiếm gần 70% như trước).
   - Tối ưu khoảng đệm an toàn (safe margins ~25% bốn phía) nằm trọn vẹn bên trong vòng tròn an toàn 66dp theo tiêu chuẩn Android Adaptive Icon.
   - Khi áp dụng trên MIUI, HyperOS, OneUI hay Pixel Launcher, biểu tượng bàn phím nằm cân đối tuyệt đối ở chính giữa squircle, không còn bất kỳ hiện tượng lẹm góc hay chạm viền bo cong.
2. **Đồng bộ toàn diện các chuẩn Icon:**
   - `ic_launcher.png`: Chuẩn squircle không viền trắng, lùi góc rộng rãi.
   - `ic_launcher_round.png`: Chuẩn tròn cắt chuẩn tâm.
   - `ic_launcher_background.png` & `ic_launcher_foreground.png`: Chuẩn Adaptive Icons tách lớp mượt mà.
   - `receiver_icon.ico`: Đồng bộ tỷ lệ cân đối cho ứng dụng nhận phím trên Windows.

## Tệp đính kèm trong thư mục:
* `Kboard_v1.1.6.apk`: Bản cài đặt Android (API 28+).
* `Kboard.apk`: File cài đặt chuẩn.
* `Kboard_Receiver.exe`: Bộ nhận phím độc lập cho máy tính Windows.
