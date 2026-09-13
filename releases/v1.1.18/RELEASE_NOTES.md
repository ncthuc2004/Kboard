# Kboard v1.1.18 Release Notes

## Cai tien va Sua loi

### 1. Phuc hoi 100% kien truc goc ban 14
- Giu nguyen toan bo tang truyen dan UDP, Bluetooth HID va co che go phim cua ban 14 on dinh.
- Tuyet doi khong co lenh KEYCODE_HOME, nut Win giu nguyen ma KEYCODE_META_LEFT goc khong bao gio gay thoat app clone.

### 2. Khac phuc cum phim Ctrl (Ctrl+A, Ctrl+V, Ctrl+C, Ctrl+X, Ctrl+Z)
- Khi co InputConnection trong app clone hay hop van ban:
  - Chi goi duy nhat performContextMenuAction (selectAll, paste, copy, cut, undo).
  - Loai bo viec ban tiep ma phim ky tu A, C, V tho phia sau, cham dut tinh trang bi de mat chu thanh ky tu 'a', 'c', 'v' khi bam nut Macro tren dau.
- Bo qua su kien ACTION_KEY_UP cho cac phim tat de khong bi nhan dup hay huy boi den.

### 3. Khac phuc nut Alt+Tab
- Chi goi lenh he thong KEYCODE_APP_SWITCH cua Android, khong ban phay them ma phim Tab phu de giao dien da nhiem khong bi giat hoac tu dong thoat.

### 4. Giam do tre nut Win
- Giam do tre truyen goi trong sendMacro tu 40ms xuong 20ms de nut Win phan hoi nhanh hon, khong bi cam giac cham/lag.

## Danh muc tep phat hanh
- Kboard_v1.1.18.apk: Ban cai dat APK Android v1.1.18 (versionCode 20, cai de truc tiep).
- Kboard.apk: Ban APK chuan tai thu muc goc.
- Kboard_Receiver.exe: Bo nhan Windows dong bo.
