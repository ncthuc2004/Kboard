#!/usr/bin/env python3
"""
Kaius Keyboard - Wi-Fi LAN Receiver (Windows/Linux/Mac)
Ultra-low latency UDP receiver that translates phone packets into native keystrokes.
On Windows, uses native ctypes SendInput (zero external dependencies required!).
"""

import socket
import json
import sys
import ctypes
from ctypes import wintypes
import time

PORT = 8964

# Windows SendInput setup
if sys.platform == "win32":
    PUL = ctypes.POINTER(ctypes.c_ulong)
    class KeyBdInput(ctypes.Structure):
        _fields_ = [
            ("wVk", wintypes.WORD),
            ("wScan", wintypes.WORD),
            ("dwFlags", wintypes.DWORD),
            ("time", wintypes.DWORD),
            ("dwExtraInfo", PUL)
        ]

    class HardwareInput(ctypes.Structure):
        _fields_ = [("uMsg", wintypes.DWORD), ("wParamL", wintypes.WORD), ("wParamH", wintypes.WORD)]

    class MouseInput(ctypes.Structure):
        _fields_ = [
            ("dx", wintypes.LONG), ("dy", wintypes.LONG), ("mouseData", wintypes.DWORD),
            ("dwFlags", wintypes.DWORD), ("time", wintypes.DWORD), ("dwExtraInfo", PUL)
        ]

    class Input_I(ctypes.Union):
        _fields_ = [("ki", KeyBdInput), ("mi", MouseInput), ("hi", HardwareInput)]

    class Input(ctypes.Structure):
        _fields_ = [("type", wintypes.DWORD), ("ii", Input_I)]

    KEYEVENTF_KEYUP = 0x0002
    INPUT_KEYBOARD = 1

    # HID usage code to Windows Virtual Key mapping
    HID_TO_VK = {
        0x04: 0x41,  # A
        0x05: 0x42,  # B
        0x06: 0x43,  # C
        0x07: 0x44,  # D
        0x08: 0x45,  # E
        0x09: 0x46,  # F
        0x0A: 0x47,  # G
        0x0B: 0x48,  # H
        0x0C: 0x49,  # I
        0x0D: 0x4A,  # J
        0x0E: 0x4B,  # K
        0x0F: 0x4C,  # L
        0x10: 0x4D,  # M
        0x11: 0x4E,  # N
        0x12: 0x4F,  # O
        0x13: 0x50,  # P
        0x14: 0x51,  # Q
        0x15: 0x52,  # R
        0x16: 0x53,  # S
        0x17: 0x54,  # T
        0x18: 0x55,  # U
        0x19: 0x56,  # V
        0x1A: 0x57,  # W
        0x1B: 0x58,  # X
        0x1C: 0x59,  # Y
        0x1D: 0x5A,  # Z
        0x1E: 0x31,  # 1
        0x1F: 0x32,  # 2
        0x20: 0x33,  # 3
        0x21: 0x34,  # 4
        0x22: 0x35,  # 5
        0x23: 0x36,  # 6
        0x24: 0x37,  # 7
        0x25: 0x38,  # 8
        0x26: 0x39,  # 9
        0x27: 0x30,  # 0
        0x28: 0x0D,  # Enter
        0x29: 0x1B,  # Esc
        0x2A: 0x08,  # Backspace
        0x2B: 0x09,  # Tab
        0x2C: 0x20,  # Space
        0x2D: 0xBD,  # -
        0x2E: 0xBB,  # =
        0x2F: 0xDB,  # [
        0x30: 0xDD,  # ]
        0x31: 0xDC,  # \
        0x33: 0xBA,  # ;
        0x34: 0xDE,  # '
        0x35: 0xC0,  # `
        0x36: 0xBC,  # ,
        0x37: 0xBE,  # .
        0x38: 0xBF,  # /
        0x39: 0x14,  # Caps Lock
        0x3A: 0x70,  # F1
        0x3B: 0x71,  # F2
        0x3C: 0x72,  # F3
        0x3D: 0x73,  # F4
        0x3E: 0x74,  # F5
        0x3F: 0x75,  # F6
        0x40: 0x76,  # F7
        0x41: 0x77,  # F8
        0x42: 0x78,  # F9
        0x43: 0x79,  # F10
        0x44: 0x7A,  # F11
        0x45: 0x7B,  # F12
        0x4C: 0x2E,  # Delete
        0x4F: 0x27,  # Right Arrow
        0x50: 0x25,  # Left Arrow
        0x51: 0x28,  # Down Arrow
        0x52: 0x26,  # Up Arrow
    }

    VK_SHIFT = 0x10
    VK_CONTROL = 0x11
    VK_MENU = 0x12  # Alt
    VK_LWIN = 0x5B

    user32 = ctypes.windll.user32

    def send_key_event(vk, is_up):
        scan = user32.MapVirtualKeyW(vk, 0)
        extra = ctypes.c_ulong(0)
        ii_ = Input_I()
        flags = KEYEVENTF_KEYUP if is_up else 0
        ii_.ki = KeyBdInput(vk, scan, flags, 0, ctypes.pointer(extra))
        x = Input(ctypes.c_ulong(INPUT_KEYBOARD), ii_)
        user32.SendInput(1, ctypes.pointer(x), ctypes.sizeof(x))

    def apply_modifiers(mod_mask, is_up):
        if mod_mask & 0x01 or mod_mask & 0x10:  # Ctrl
            send_key_event(VK_CONTROL, is_up)
        if mod_mask & 0x02 or mod_mask & 0x20:  # Shift
            send_key_event(VK_SHIFT, is_up)
        if mod_mask & 0x04 or mod_mask & 0x40:  # Alt
            send_key_event(VK_MENU, is_up)
        if mod_mask & 0x08 or mod_mask & 0x80:  # Win
            send_key_event(VK_LWIN, is_up)
else:
    def send_key_event(vk, is_up):
        pass
    def apply_modifiers(mod_mask, is_up):
        pass
    HID_TO_VK = {}

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(('10.255.255.255', 1))
        ip = s.getsockname()[0]
    except Exception:
        ip = '127.0.0.1'
    finally:
        s.close()
    return ip

def main():
    ip = get_local_ip()
    print("=" * 60)
    print("  KAIUS KEYBOARD - WI-FI LAN RECEIVER")
    print(f"  Listening on: {ip}:{PORT}")
    print("  Enter this IP in your Redmi 10 app to connect.")
    print("=" * 60)

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind(("0.0.0.0", PORT))

    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = json.loads(data.decode("utf-8"))
            action = msg.get("a")

            if action == "discover":
                announce_data = json.dumps({
                    "a": "announce",
                    "name": f"PC ({socket.gethostname()})",
                    "ip": ip,
                    "port": PORT
                }).encode("utf-8")
                sock.sendto(announce_data, addr)
                print(f"[DISCOVERY] Replied to {addr[0]}")
                continue

            if action == "ping":
                print(f"[PING] from {addr[0]}")
                continue

            code = msg.get("k", 0)
            mod = msg.get("m", 0)
            vk = HID_TO_VK.get(code, 0)

            print(f"[{action.upper()}] code=0x{code:02X} vk=0x{vk:02X} mod=0x{mod:02X} from {addr[0]}")

            if vk != 0:
                if action == "down":
                    apply_modifiers(mod, is_up=False)
                    send_key_event(vk, is_up=False)
                elif action == "up":
                    send_key_event(vk, is_up=True)
                    apply_modifiers(mod, is_up=True)
                elif action == "tap":
                    apply_modifiers(mod, is_up=False)
                    send_key_event(vk, is_up=False)
                    time.sleep(0.02)
                    send_key_event(vk, is_up=True)
                    apply_modifiers(mod, is_up=True)

        except KeyboardInterrupt:
            print("\nShutting down receiver.")
            break
        except Exception as e:
            print(f"Error: {e}")

if __name__ == "__main__":
    main()
