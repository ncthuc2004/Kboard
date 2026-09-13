# Kboard Release v1.1.9 (Build 11)

## Diem moi trong phien ban v1.1.9:
1. **Don dep sach cac thanh phan thua va tep rac**:
   - Xoa bo anh logo khong su dung trong app (`app_logo.png`) giup goi APK nhe va toi uu.
   - Don dep thu muc build trung gian va cache PyInstaller.
2. **Trung tam Cai dat Ban phim toan dien (Modular Keyboard Settings Panel)**:
   - Bo sung nut Cai dat (Settings) truc quan luon hien dien tren thanh Header trong ca che do Bluetooth lan Wi-Fi LAN.
   - Giao diện chia 5 danh muc chuyen sau:
     * **Bo go & Ngon ngu (Language & Typing)**: Chon nhanh Tieng Viet (Telex) hoac English (Chuan quoc te), cong tac bat/tat Telex truc tiep tren may chay phim va tu dong dong bo tuc thi toi may nhan (PC / Android receiver).
     * **Den nen RGB (Backlight Themes)**: 5 chu de mau sac co hoc hien dai:
       - Dark Industrial (Mac dinh - Titan toi gian)
       - Cyberpunk Neon (Cyan & Magenta phat sang)
       - Matrix Green (Xanh phosphor ma tran)
       - RGB Chroma Wave (Song cau vong bien thien uyen chuyen)
       - Retro Amber (Cam hoai niem may tinh thap nien 80)
       - Hieu ung Reactive Key Glow: vien phim bung sang ruc ro khi nhan cham.
     * **Phan hoi Rung & Am thanh (Haptics & Sound)**: Rung xuc giac co hoc mo phong switch phím (3 muc: Nhe 12ms / Vua 22ms / Manh 35ms), tuy chon am thanh click phím co.
     * **Toc do lap phim co hoc (Hardware Key Repeat)**: Tuy chinh thoi gian tre bat dau (250ms / 380ms / 500ms) va toc do lap ky tu (25ms - 40 ky tu/s / 45ms / 70ms).
     * **Mang LAN & IP**: Cau hinh IP/Port may nhan, goi y nhanh IP Hotspot, nut gui goi Ping kiem tra mang.

## File phat hanh kem theo:
* `Kboard_v1.1.9.apk`: Ban cai dat Android (API 28+).
* `Kboard.apk`: Ban cai dat Android tieu chuan dong bo nhat.
* `Kboard_Receiver.exe`: Bo nhan doc lap cho Windows (x86_64).
