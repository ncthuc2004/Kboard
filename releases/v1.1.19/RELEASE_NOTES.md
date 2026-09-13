# Kboard v1.1.19 Release Notes

## Cai tien va Sua loi

### 1. Phuc hoi 100% kien truc goc ban 14 cho KaiusImeService
- File `KaiusImeService.kt` duoc khoi phuc nguyen ban 100% tu commit ban 14 (`6b51eb3`).
- Giu nguyen co che injectKeyEvent ket hop performContextMenuAction goc da hoat dong rat tot khi go tu ban phim.
- Khong co bat ky can thiep nao vao ma KEYCODE_HOME, dam bao khong bao gio bi thoat app clone.

### 2. Xu ly dung ban chat cho cum nut Macro Ctrl tren dau (Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+Z, Alt+Tab)
- Nguyen nhan o ban 14: Khi go tren ban phim ao, nguoi dung cham nut Ctrl truoc (kich hoat giu Ctrl trong he thong Android), sau do moi bam phim A -> hop thoai chon tat ca hoat dong chuan. Tuy nhien khi bam nut tat Macro tren dau o ban 14, he thong goi truc tiep ma phim ma chua tung gui lenh nhan giu Ctrl xuong he thong, khien Android khong nhan dien duoc Ctrl dang giu va coi do la go ky tu 'a', 'c', 'v'.
- Khac phuc: Tinh chinh `sendMacro` trong `InputEngine.kt` theo dung trinh tu phan cung:
  1. Gui lenh de giu phim bo tro (Ctrl/Alt) truoc.
  2. Gui tiep ma phim can thuc hien (A, C, V, Z, Tab) trong khi phim bo tro dang duoc giu.
  3. Nha phim ky tu, sau do moi nha phim bo tro va xoa report.
- Nho do, cum nut Macro tren dau hoat dong chinh xac nhu khi thao tac tay tren layout ban phim.

### 3. Giam do tre nut Win
- Giam do tre truyen goi trong `sendMacro` tu 40ms xuong con 15ms, giup thao tac nhan nut Win phan hoi nhanh, khong con bi cam giac cham/lag.

## Danh muc tep phat hanh
- Kboard_v1.1.19.apk: Ban cai dat APK Android v1.1.19 (versionCode 21, cai de truc tiep).
- Kboard.apk: Ban APK chuan tai thu muc goc va thu muc release.
- Kboard_Receiver.exe: Bo nhan Windows dong bo.
