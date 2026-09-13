# Kboard v1.1.10 Release Notes

## Diem Moi & Nang Cap Chinh

### 1. Bo go Tieng Viet Telex doc quyen cau hinh tren May Ban Phim
- Tuan thu chat che nguyen tac he thong: Bo go Tieng Viet Telex chi duoc bat/tat va dieu chinh duy nhat tren thiet bi dong vai tro Ban Phim (Kboard Sender).
- May nhan (Receiver / KaiusImeService) chi giu vai tro hien thi huy hieu trang thai doc quyen [VI] / [EN], dong bo lap tuc theo goi lenh UDP tu ban phim ma khong hien thi bat ky nut bam gay nham lan nao tren may nhan.
- Tren ban phim: Nguoi dung co the chuyen nhanh che do VI / EN ngay tren QuickMacroBar hoac trong bang Keyboard Settings Dialog (Tab 0).

### 2. Che do nghi tiet kiem pin (Standby / Sleep Mode)
- Nut nguon tich hop truc tiep tren thanh tieu de Landscape Unified Header:
  - Trang thai Xanh luc: Ban phim dang hoat dong (Active).
  - Trang thai Vang ho phach: Ban phim dang o che do nghi (Standby).
- Khi chuyen sang che do Standby:
  - Tu dong ha co `FLAG_KEEP_SCREEN_ON` de man hinh dien thoai co the tu dong tat theo thoi gian cho cua he thong.
  - Tam dung phat song quang ba LAN Discovery de tiet kiem toi da dung luong pin va bang thong mang Wi-Fi.
  - Lop phu StandbyOverlay bao ve chong cham nham phim khi de dien thoai trong tui hoac tren mat ban.
  - Cung cap 3 thao tac nhanh ngay tren overlay:
    + [ BAT LAI BAN PHIM ]: Kich hoat lai ban phim lap tuc.
    + [ Doi Ban Phim Thuong (Gboard) ]: Mo nhanh trinh chon phuong thuc nhap he thong de chuyen ve ban phim hang ngay.
    + [ Ve Man Hinh Chinh ]: An ung dung xuong chay nen.

### 3. Don dep ma nguon & Toi uu tai nguyen
- Loai bo cac tep hinh anh du thua, xoa bo nho dem va tep rac de giam kich thuoc goi cai dat.
- Toi uu hoa dong bo StateFlow giua ViewModel va Jetpack Compose UI.

## Tep Phat Hanh
- Kboard_v1.1.10.apk (39.8 MB) - File APK gan tag phien ban.
- Kboard.apk (39.8 MB) - File APK tieu chuan.
- Kboard_Receiver.exe (8.28 MB) - Bo nhan Windows dong bo sieu nhe.
