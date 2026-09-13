@echo off
title Kboard - PC Wi-Fi Receiver
cls
echo ===================================================
echo         KBOARD - WI-FI RECEIVER CHO LAPTOP
echo ===================================================
echo.
echo Dang khoi dong receiver tren cong UDP 8964...
echo (Kboard se tu dong tim thay may tinh nay trong mang Wi-Fi)
echo.
python "%~dp0pc_receiver.py"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the chay Python. Vui long kiem tra xem Laptop da co Python chua.
    pause
)
