# Kboard Release v1.1.8 (Build 10)

## Diem moi trong phien ban v1.1.8:
1. **Tu dong xoa sach cac may nhan offline (Eviction TTL 6s)**:
   - Co che do tim UDP nay duoc bo sung luong don rac dinh ky (chay moi 2 giay).
   - Neu mot thiet bi nhan roi khoi mang Wi-Fi, tat ung dung hoac dong PC, sau 6 giay (tuong duong 2 chu ky phat song probe khong co phan hoi), the thiet bi se tu dong bien mat khoi thanh ket noi, khong con bi treo tren giao dien.
2. **Lam moi danh sach tuc thi khi bam [Quet LAN]**:
   - Khi nguoi dung bam nut `[Quet LAN]`, toan bo danh sach cu duoc xoa sach ngay lap tuc (`emptyList()`).
   - Phat broadcast probe tuc thi den tat ca giao dien mang va dia chi `255.255.255.255`.
   - Chi nhung thiet bi dang online va phan hoi moi hien thi tro lai tren man hinh.
3. **Chi bao truc quan khi dang quet**:
   - Hieu ung xoay bieu tuong Refresh va dong trang thai "Dang tim may nhan trong mang LAN..." trong luc do tim mang.
4. **Toi uu bo nho Gradle JVM**:
   - Dieu chinh `-Xmx2048m` on dinh, tranh tran bo nho native heap tren he thong Windows.

## File phat hanh kem theo:
* `Kboard_v1.1.8.apk`: Ban cai dat Android (API 28+).
* `Kboard.apk`: Ban cai dat Android tieu chuan dong bo nhat.
* `Kboard_Receiver.exe`: Bo nhan doc lap cho Windows (x86_64).
