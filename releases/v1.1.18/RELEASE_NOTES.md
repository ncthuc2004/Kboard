# Kboard v1.1.18 - BAN LOI DA THU HOI (DEPRECATED)

> **CANH BAO: PHIEN BAN CO LOI - DA THU HOI KHONG SU DUNG.**
> Vui long cai dat ban on dinh chinh thuc tai [releases/v1.1.19/](../v1.1.19/) hoac file [Kboard.apk](../../Kboard.apk).

## Chi tiet loi tai ban v1.1.18
- **Loi chan su kien ban phim**: Ban 18 can thiep vao `KaiusImeService` chi goi `performContextMenuAction` ma khong tiep tuc gui `KeyEvent` du phong. Trong moi truong app clone va nhieu ung dung khong ho tro menu context, lenh nay bi ngat khien to hop phim Ctrl+A/C/V bi liet hoan toan ca khi go bang tay tren ban phim.
- **Khac phuc o ban v1.1.19**: Khoi phuc nguyen ban v14 cho `KaiusImeService` va tinh chinh trinh tu phan cung trong `InputEngine.sendMacro` (nhan giu Ctrl truoc 20ms roi moi nhan phim ky tu).
