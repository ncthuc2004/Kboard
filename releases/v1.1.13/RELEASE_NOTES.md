# Kboard v1.1.13 Release Notes

## Khac Phuc Loi Phim Bo Tro (Ctrl, Win, Alt, Shift) & Phim Tat Tren May Nhan Clone

### 1. Dong Bo Trang Thai Phim Bo Tro (InputEngine & LanServer)
- **Truyen goi tin tuc thi**: Khi cham bat hoac tat cac phim bo tro `Ctrl`, `Win`, `Alt`, `Shift` tren ban phim may gui, ung dung lap tuc truyen goi tin cap nhat trang thai qua kenh Wi-Fi LAN.
- **Hien thi ten phim truc quan**:
  - Tren man hinh may nhan (App clone / LanServer), cac su kien phim bo tro duoc dinh danh ro rang la `Ctrl`, `Win`, `Alt`, `Shift` thay vi hien thi chu `NONE` gay hieu nham.
  - Ho tro hien thi to hop phim bo tro dong thoi nhu `Ctrl+Shift`, `Ctrl+Alt`.
- **Khong in de ky tu khi dang giu phim bo tro**:
  - Bo dem van ban tich luy tren may nhan tu dong loai bo viec ghep ky tu khi cac phim bo tro nhu `Ctrl`, `Alt`, `Win` dang duoc kich hoat.

### 2. Nang Cap Ban Phim Ao May Nhan (KaiusImeService)
- **Ho tro phim Win / Meta**:
  - Cham phim `Win` tren may ban phim se kich hoat ve man hinh chinh (`KEYCODE_HOME`) hoac mo App Launcher tren may Android nhan.
  - To hop `Win+D` tro ve man hinh chinh (Show Desktop).
  - To hop `Win+E` hoac `Win+F` mo trinh quan ly tep tin (Files Explorer).
  - To hop `Win+B` mo trinh duyet Web mac dinh.
- **Ho tro phim tat Alt+Tab**:
  - To hop `Alt+Tab` tren may nhan Android se kich hoat trinh chuyen doi da nhiem ung dung gan day (`KEYCODE_APP_SWITCH`).
- **Toi uu cac phim tat Ctrl+C, Ctrl+V, Ctrl+A, Ctrl+X, Ctrl+Z**:
  - Thuc hien truc tiep qua API `performContextMenuAction`, loai bo viec gui thua ky tu tho gay loi go de mat noi dung van ban tren Android.
- **Khong bi chan phim khi chua focus o nhap lieu**:
  - Khi chua co con tro nhap van ban (vi du nguoi dung dang o giao dien Receiver hoac man hinh chinh), he thong van tu dong dieu phoi cac phim he thong (Home, Recent Apps, mui ten, Backspace, Enter).

### 3. Trinh Nhan Windows (pc_receiver.py & Kboard_Receiver.exe)
- **Theo doi trang thai bo tro chuyen nghiep (Stateful Modifier Tracking)**:
  - Khac phuc loi `if vk != 0` truoc day lam bo qua cac su kien bam nut `Win` tren thanh Macro Bar.
  - Bam nut `Win` tren thanh Macro Bar hoac ban phim gio day mo truc tiep Start Menu tren Windows.
  - Tu dong dong bo chinh xac giua phim bo tro dang bat/tat tren dien thoai va ban phim ao cua Windows.

### 4. Tep Phat Hanh
- Kboard_v1.1.13.apk (39.8 MB) - File APK gan tag phien ban.
- Kboard.apk (39.8 MB) - File APK tieu chuan.
- Kboard_Receiver.exe (8.28 MB) - Bo nhan Windows dong bo sieu nhe.
