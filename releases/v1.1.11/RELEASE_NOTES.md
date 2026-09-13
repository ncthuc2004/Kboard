# Kboard v1.1.11 Release Notes

## Khac Phuc Triet De Su Co Ket Noi Wi-Fi Hotspot (Phat Mang)

### 1. Co che Quet Thong Minh Vuot Tuong Lua Android SoftAP
- **Nguyen nhan loi truoc do**: Khi may nhan phat Wi-Fi Hotspot (Diem phat song di dong), he dieu hanh Android (SoftAP / iptables) mac dinh chan hoac huy cac goi tin quang ba UDP Broadcast (255.255.255.255 va 192.168.43.255) tu thiet bi con gui len. Do do lenh quet phong thanh truoc day khong bao gio den duoc may nhan.
- **Giai phap trong v1.1.11**:
  - Tich hop cong nghe **Direct Unicast Probe**: Trong qua trinh quet LAN, may ban phim dong thoi ban goi tin truc tiep toi cong `192.168.43.1:8964` (IP may chu Hotspot mac dinh cua tat ca cac dong dien thoai Android) va cong Gateway cua mang ket noi hien tai.
  - Cac goi tin Unicast di thang vao may chu Hotspot ma khong he bi he thong chan, may nhan phan hoi lap tuc.

### 2. Tu Dong Nhan Dien & Uu Tien IP Hotspot (192.168.43.1)
- **Nguyen nhan loi IP di dong (4G/5G)**: Khi bat Hotspot, dien thoai nhan van dang bat Du lieu di dong (4G/5G). Thuat toan cu nhan dien nham card mang nha mang (`rmnet_data0` / `ccmni0` voi IP `10.x.x.x`), dan den viec man hinh Receiver bao sai IP va may ban phim khong the ket noi toi IP nha mang.
- **Giai phap trong v1.1.11**:
  - Thuat toan `getLocalIpAddress()` duoc viet lai toan dien: Uu tien hang dau cho dai IP `192.168.43.1` va cac card mang Wi-Fi/AP (`ap*`, `softap*`, `wlan*`).
  - Bo qua cac card nha mang 4G/5G khi dang co Wi-Fi / Hotspot.
  - Tich hop tien trinh tu dong cap nhat IP moi 2.5 giay: Nguoi dung mo app truoc roi moi bat Hotspot thi IP tren man hinh Receiver van tu dong nhay sang `192.168.43.1` ma khong can khoi dong lai app.

### 3. Phuc Hoi Tien Trinh Lang Nghe Hai Lop (LanServer & KaiusImeService)
- **Sua loi xung dot cong (BindException)**: Can chinh `reuseAddress = true` truoc khi bind port tren `LanServer`, giup giao dien Receiver va ban phim he thong `KaiusImeService` dong thoi hoat dong tro tru ma khong bi dung do.
- **Phan hoi Discovery truc tiep tu KaiusImeService**: Khi nguoi dung tren may nhan dang dung app khac (app clone, game, trinh duyet) ma khong mo man hinh Kboard, ban phim he thong `KaiusImeService` van tu dong phan hoi goi tin quet tim kiem tu may ban phim.

### 4. Phim Tat Ket Noi Nhanh Hotspot tren Thanh Cong Cu
- Bo sung nut bam nhanh `[Dung Hotspot]` ngay tren thanh `LanConnectionBar`: Nguoi dung chi can bam 1 cham la may ban phim tu dong dat dia chi nhan ve `192.168.43.1:8964` va ket noi lap tuc.

## Tep Phat Hanh
- Kboard_v1.1.11.apk (39.8 MB) - File APK gan tag phien ban.
- Kboard.apk (39.8 MB) - File APK tieu chuan.
- Kboard_Receiver.exe (8.28 MB) - Bo nhan Windows dong bo sieu nhe.
