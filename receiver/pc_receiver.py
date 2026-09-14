#!/usr/bin/env python3
"""
Kboard - PC Wi-Fi LAN Receiver (Windows Desktop GUI)
Ultra-low latency UDP receiver that translates phone packets into native Windows keystrokes.
Features a modern Cyberpunk / Industrial Dark Mode desktop GUI, real-time event logs,
network status, and direct SendInput injection.
"""

import os
import sys
import time
import json
import socket
import threading
import queue
import ctypes
from ctypes import wintypes

try:
    import tkinter as tk
    from tkinter import ttk
    from PIL import Image, ImageTk
except ImportError as e:
    print(f"Missing UI dependency: {e}")
    sys.exit(1)

# Enable High DPI scaling on Windows
if sys.platform == "win32":
    try:
        ctypes.windll.shcore.SetProcessDpiAwareness(1)
    except Exception:
        pass

PORT = 8964
APP_VERSION = "v1.1.19"

# Resource helper for PyInstaller bundled executable
def get_resource_path(relative_path):
    if hasattr(sys, '_MEIPASS'):
        return os.path.join(sys._MEIPASS, relative_path)
    return os.path.join(os.path.dirname(os.path.abspath(__file__)), relative_path)

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
        0x4A: 0x24,  # Home
        0x4B: 0x21,  # Page Up
        0x4C: 0x2E,  # Delete
        0x4D: 0x23,  # End
        0x4E: 0x22,  # Page Down
        0x4F: 0x27,  # Right Arrow
        0x50: 0x25,  # Left Arrow
        0x51: 0x28,  # Down Arrow
        0x52: 0x26,  # Up Arrow
    }

    HID_KEY_NAMES = {
        0x04: "A", 0x05: "B", 0x06: "C", 0x07: "D", 0x08: "E", 0x09: "F",
        0x0A: "G", 0x0B: "H", 0x0C: "I", 0x0D: "J", 0x0E: "K", 0x0F: "L",
        0x10: "M", 0x11: "N", 0x12: "O", 0x13: "P", 0x14: "Q", 0x15: "R",
        0x16: "S", 0x17: "T", 0x18: "U", 0x19: "V", 0x1A: "W", 0x1B: "X",
        0x1C: "Y", 0x1D: "Z", 0x1E: "1", 0x1F: "2", 0x20: "3", 0x21: "4",
        0x22: "5", 0x23: "6", 0x24: "7", 0x25: "8", 0x26: "9", 0x27: "0",
        0x28: "Enter", 0x29: "Esc", 0x2A: "Backspace", 0x2B: "Tab",
        0x2C: "Space", 0x2D: "-", 0x2E: "=", 0x2F: "[", 0x30: "]",
        0x31: "\\", 0x33: ";", 0x34: "'", 0x35: "`", 0x36: ",",
        0x37: ".", 0x38: "/", 0x39: "Caps Lock",
        0x3A: "F1", 0x3B: "F2", 0x3C: "F3", 0x3D: "F4", 0x3E: "F5", 0x3F: "F6",
        0x40: "F7", 0x41: "F8", 0x42: "F9", 0x43: "F10", 0x44: "F11", 0x45: "F12",
        0x4A: "Home", 0x4B: "Page Up", 0x4C: "Delete", 0x4D: "End", 0x4E: "Page Down",
        0x4F: "Right", 0x50: "Left", 0x51: "Down", 0x52: "Up"
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

    current_modifiers = 0

    def sync_modifiers(target_mod):
        global current_modifiers
        target_ctrl = bool(target_mod & 0x11)
        current_ctrl = bool(current_modifiers & 0x11)
        if target_ctrl and not current_ctrl:
            send_key_event(VK_CONTROL, is_up=False)
        elif not target_ctrl and current_ctrl:
            send_key_event(VK_CONTROL, is_up=True)

        target_shift = bool(target_mod & 0x22)
        current_shift = bool(current_modifiers & 0x22)
        if target_shift and not current_shift:
            send_key_event(VK_SHIFT, is_up=False)
        elif not target_shift and current_shift:
            send_key_event(VK_SHIFT, is_up=True)

        target_alt = bool(target_mod & 0x44)
        current_alt = bool(current_modifiers & 0x44)
        if target_alt and not current_alt:
            send_key_event(VK_MENU, is_up=False)
        elif not target_alt and current_alt:
            send_key_event(VK_MENU, is_up=True)

        target_win = bool(target_mod & 0x88)
        current_win = bool(current_modifiers & 0x88)
        if target_win and not current_win:
            send_key_event(VK_LWIN, is_up=False)
        elif not target_win and current_win:
            send_key_event(VK_LWIN, is_up=True)

        current_modifiers = target_mod

    def release_all_modifiers():
        global current_modifiers
        if current_modifiers & 0x11:
            send_key_event(VK_CONTROL, is_up=True)
        if current_modifiers & 0x22:
            send_key_event(VK_SHIFT, is_up=True)
        if current_modifiers & 0x44:
            send_key_event(VK_MENU, is_up=True)
        if current_modifiers & 0x88:
            send_key_event(VK_LWIN, is_up=True)
        current_modifiers = 0
else:
    def send_key_event(vk, is_up): pass
    def sync_modifiers(target_mod): pass
    def release_all_modifiers(): pass
    HID_TO_VK = {}
    HID_KEY_NAMES = {}

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

def format_modifiers(mod):
    parts = []
    if mod & 0x11: parts.append("Ctrl")
    if mod & 0x44: parts.append("Alt")
    if mod & 0x22: parts.append("Shift")
    if mod & 0x88: parts.append("Win")
    return "+".join(parts)


class KboardReceiverApp:
    def __init__(self, root):
        self.root = root
        self.root.title(f"Kboard Receiver {APP_VERSION}")
        self.root.geometry("580x640")
        self.root.minsize(520, 560)
        self.root.configure(bg="#0B0E14")

        # Window icon
        icon_path = get_resource_path("receiver_icon.ico")
        if os.path.exists(icon_path):
            try:
                self.root.iconbitmap(icon_path)
            except Exception:
                pass

        self.event_queue = queue.Queue()
        self.is_running = True
        self.packet_count = 0
        self.connected_senders = set()
        self.local_ip = get_local_ip()

        self._build_ui()
        self._start_udp_thread()
        self._poll_queue()

    def _build_ui(self):
        # Top Header Bar
        header_frame = tk.Frame(self.root, bg="#121722", padx=16, pady=12, highlightthickness=1, highlightbackground="#1F293D")
        header_frame.pack(fill=tk.X, padx=14, pady=(14, 8))

        # Logo Image & Title Row
        logo_row = tk.Frame(header_frame, bg="#121722")
        logo_row.pack(fill=tk.X)

        logo_path = get_resource_path("kboard_512.png")
        if os.path.exists(logo_path):
            try:
                pil_img = Image.open(logo_path).resize((40, 40), Image.Resampling.LANCZOS)
                self.logo_photo = ImageTk.PhotoImage(pil_img)
                logo_lbl = tk.Label(logo_row, image=self.logo_photo, bg="#121722")
                logo_lbl.pack(side=tk.LEFT, padx=(0, 10))
            except Exception:
                pass

        title_col = tk.Frame(logo_row, bg="#121722")
        title_col.pack(side=tk.LEFT, fill=tk.Y)

        title_top = tk.Frame(title_col, bg="#121722")
        title_top.pack(anchor=tk.W)

        brand_lbl = tk.Label(title_top, text="KBOARD RECEIVER", font=("Segoe UI", 13, "bold"), fg="#F8FAFC", bg="#121722")
        brand_lbl.pack(side=tk.LEFT)

        ver_lbl = tk.Label(title_top, text=f" {APP_VERSION} ", font=("Segoe UI", 8, "bold"), fg="#00E5FF", bg="#1E293B", padx=4, pady=1)
        ver_lbl.pack(side=tk.LEFT, padx=(8, 0))

        sub_lbl = tk.Label(title_col, text="BỘ NHẬN PHÍM WI-FI LAN CHO LAPTOP/PC", font=("Segoe UI", 8), fg="#94A3B8", bg="#121722")
        sub_lbl.pack(anchor=tk.W, pady=(2, 0))

        # Status & Connection Banner
        conn_frame = tk.Frame(self.root, bg="#121722", padx=16, pady=12, highlightthickness=1, highlightbackground="#1F293D")
        conn_frame.pack(fill=tk.X, padx=14, pady=6)

        status_row = tk.Frame(conn_frame, bg="#121722")
        status_row.pack(fill=tk.X)

        self.status_dot = tk.Label(status_row, text="●", font=("Segoe UI", 12), fg="#10B981", bg="#121722")
        self.status_dot.pack(side=tk.LEFT)

        self.status_text = tk.Label(status_row, text=" ĐANG LẮNG NGHE CỔNG UDP 8964", font=("Segoe UI", 9, "bold"), fg="#10B981", bg="#121722")
        self.status_text.pack(side=tk.LEFT)

        ip_box = tk.Frame(conn_frame, bg="#0B0E14", padx=12, pady=8, highlightthickness=1, highlightbackground="#1F293D")
        ip_box.pack(fill=tk.X, pady=(10, 6))

        ip_lbl_col = tk.Frame(ip_box, bg="#0B0E14")
        ip_lbl_col.pack(side=tk.LEFT)

        tk.Label(ip_lbl_col, text="ĐỊA CHỈ IP MÁY TÍNH", font=("Segoe UI", 8, "bold"), fg="#64748B", bg="#0B0E14").pack(anchor=tk.W)
        self.ip_val_lbl = tk.Label(ip_lbl_col, text=f"{self.local_ip}:{PORT}", font=("Consolas", 14, "bold"), fg="#00E5FF", bg="#0B0E14")
        self.ip_val_lbl.pack(anchor=tk.W, pady=(2, 0))

        copy_btn = tk.Button(
            ip_box, text="Sao chép IP", font=("Segoe UI", 9, "bold"),
            bg="#1E293B", fg="#F8FAFC", activebackground="#00E5FF", activeforeground="#0B0E14",
            relief=tk.FLAT, padx=12, pady=6, cursor="hand2", command=self._copy_ip
        )
        copy_btn.pack(side=tk.RIGHT)

        tip_lbl = tk.Label(
            conn_frame,
            text="Mở Kboard trên điện thoại -> Chọn [Wi-Fi LAN] -> Nhập IP trên hoặc bấm [Quét LAN].",
            font=("Segoe UI", 8), fg="#94A3B8", bg="#121722", wraplength=520, justify=tk.LEFT
        )
        tip_lbl.pack(anchor=tk.W, pady=(4, 0))

        # Metrics Bar
        metrics_frame = tk.Frame(self.root, bg="#0B0E14")
        metrics_frame.pack(fill=tk.X, padx=14, pady=4)

        self.packets_lbl = tk.Label(metrics_frame, text="Gói tin: 0", font=("Consolas", 9), fg="#94A3B8", bg="#0B0E14")
        self.packets_lbl.pack(side=tk.LEFT)

        self.last_key_lbl = tk.Label(metrics_frame, text="Phím gần nhất: [Chờ kết nối]", font=("Consolas", 9), fg="#00E5FF", bg="#0B0E14")
        self.last_key_lbl.pack(side=tk.RIGHT)

        # Log Console Card
        log_card = tk.Frame(self.root, bg="#121722", padx=12, pady=10, highlightthickness=1, highlightbackground="#1F293D")
        log_card.pack(fill=tk.BOTH, expand=True, padx=14, pady=6)

        log_top_row = tk.Frame(log_card, bg="#121722")
        log_top_row.pack(fill=tk.X, pady=(0, 6))

        tk.Label(log_top_row, text="NHẬT KÝ SỰ KIỆN PHÍM (REAL-TIME)", font=("Segoe UI", 9, "bold"), fg="#94A3B8", bg="#121722").pack(side=tk.LEFT)

        clear_btn = tk.Button(
            log_top_row, text="Xóa nhật ký", font=("Segoe UI", 8),
            bg="#1E293B", fg="#94A3B8", activebackground="#334155", activeforeground="#F8FAFC",
            relief=tk.FLAT, padx=8, pady=2, cursor="hand2", command=self._clear_logs
        )
        clear_btn.pack(side=tk.RIGHT)

        # Log Text Box
        self.log_text = tk.Text(
            log_card, bg="#07090E", fg="#E2E8F0", font=("Consolas", 9),
            relief=tk.FLAT, highlightthickness=1, highlightbackground="#1F293D",
            wrap=tk.WORD, state=tk.DISABLED
        )
        self.log_text.pack(fill=tk.BOTH, expand=True)

        # Tag configurations for syntax highlighting
        self.log_text.tag_config("TIME", foreground="#64748B")
        self.log_text.tag_config("DOWN", foreground="#10B981")
        self.log_text.tag_config("UP", foreground="#64748B")
        self.log_text.tag_config("TAP", foreground="#00E5FF")
        self.log_text.tag_config("DISC", foreground="#F59E0B")
        self.log_text.tag_config("MOD", foreground="#818CF8")

        # Bottom Controls
        bottom_bar = tk.Frame(self.root, bg="#0B0E14", padx=14, pady=8)
        bottom_bar.pack(fill=tk.X)

        self.always_on_top_var = tk.BooleanVar(value=False)
        ontop_chk = tk.Checkbutton(
            bottom_bar, text="Luôn hiển thị trên cùng (Always on top)",
            variable=self.always_on_top_var, command=self._toggle_on_top,
            font=("Segoe UI", 9), fg="#94A3B8", bg="#0B0E14",
            activebackground="#0B0E14", activeforeground="#F8FAFC", selectcolor="#1E293B"
        )
        ontop_chk.pack(side=tk.LEFT)

        author_lbl = tk.Label(bottom_bar, text="Kaius Ecosystem", font=("Segoe UI", 8), fg="#475569", bg="#0B0E14")
        author_lbl.pack(side=tk.RIGHT)

        self._append_log("Khởi động Receiver thành công.", "DISC")
        self._append_log(f"Lắng nghe tại {self.local_ip}:{PORT}. Sẵn sàng nhận phím.", "DISC")

    def _toggle_on_top(self):
        self.root.attributes("-topmost", self.always_on_top_var.get())

    def _copy_ip(self):
        self.root.clipboard_clear()
        self.root.clipboard_append(self.local_ip)
        self.ip_val_lbl.config(text=f"{self.local_ip} [ĐÃ COPY!]")
        self.root.after(1500, lambda: self.ip_val_lbl.config(text=f"{self.local_ip}:{PORT}"))

    def _clear_logs(self):
        self.log_text.config(state=tk.NORMAL)
        self.log_text.delete("1.0", tk.END)
        self.log_text.config(state=tk.DISABLED)

    def _append_log(self, text, tag="DOWN"):
        timestamp = time.strftime("%H:%M:%S")
        self.log_text.config(state=tk.NORMAL)
        self.log_text.insert(tk.END, f"[{timestamp}] ", "TIME")
        self.log_text.insert(tk.END, f"{text}\n", tag)
        self.log_text.see(tk.END)
        self.log_text.config(state=tk.DISABLED)

    def _start_udp_thread(self):
        self.udp_thread = threading.Thread(target=self._udp_receiver_worker, daemon=True)
        self.udp_thread.start()

    def _udp_receiver_worker(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            sock.bind(("0.0.0.0", PORT))
        except Exception as e:
            self.event_queue.put(("ERROR", f"Không thể mở cổng {PORT}: {e}", "DOWN"))
            return

        while self.is_running:
            try:
                data, addr = sock.recvfrom(1024)
                msg = json.loads(data.decode("utf-8"))
                action = msg.get("a")

                if action == "discover":
                    announce_data = json.dumps({
                        "a": "announce",
                        "name": f"PC ({socket.gethostname()})",
                        "ip": self.local_ip,
                        "port": PORT
                    }).encode("utf-8")
                    sock.sendto(announce_data, addr)
                    self.event_queue.put(("DISCOVER", f"Nhận diện thiết bị từ {addr[0]}", "DISC"))
                    continue

                if action == "ping":
                    self.event_queue.put(("PING", f"Ping từ {addr[0]}", "DISC"))
                    continue

                if action == "report":
                    mod = msg.get("m", 0)
                    sync_modifiers(mod)
                    continue

                code = msg.get("k", 0)
                mod = msg.get("m", 0)
                vk = HID_TO_VK.get(code, 0)
                key_name = HID_KEY_NAMES.get(code, f"Key(0x{code:02X})")
                mod_str = format_modifiers(mod)
                combo = f"{mod_str}+{key_name}" if mod_str else key_name

                if vk != 0:
                    if action == "down":
                        sync_modifiers(mod)
                        send_key_event(vk, is_up=False)
                        self.event_queue.put(("KEY_EVENT", f"[DOWN] {combo} (vk=0x{vk:02X}) từ {addr[0]}", "DOWN", combo))
                    elif action == "up":
                        send_key_event(vk, is_up=True)
                        sync_modifiers(mod)
                        self.event_queue.put(("KEY_EVENT", f"[UP]   {combo}", "UP", combo))
                    elif action == "tap":
                        sync_modifiers(mod)
                        send_key_event(vk, is_up=False)
                        time.sleep(0.015)
                        send_key_event(vk, is_up=True)
                        release_all_modifiers()
                        self.event_queue.put(("KEY_EVENT", f"[TAP]  {combo} từ {addr[0]}", "TAP", combo))
                else:
                    if action == "down":
                        sync_modifiers(mod)
                        self.event_queue.put(("KEY_EVENT", f"[DOWN] Phím bổ trợ: {mod_str}", "MOD", mod_str))
                    elif action == "up":
                        sync_modifiers(mod)
                        self.event_queue.put(("KEY_EVENT", f"[UP]   Phím bổ trợ: {mod_str}", "UP", mod_str))
                    elif action == "tap":
                        sync_modifiers(mod)
                        time.sleep(0.02)
                        release_all_modifiers()
                        self.event_queue.put(("KEY_EVENT", f"[TAP]  Phím bổ trợ: {mod_str}", "TAP", mod_str))

            except Exception as e:
                pass

        try:
            sock.close()
        except Exception:
            pass

    def _poll_queue(self):
        try:
            while True:
                item = self.event_queue.get_nowait()
                ev_type = item[0]
                text = item[1]
                tag = item[2]

                self.packet_count += 1
                self.packets_lbl.config(text=f"Gói tin: {self.packet_count}")

                if len(item) > 3:
                    combo = item[3]
                    self.last_key_lbl.config(text=f"Phím gần nhất: [{combo}]")

                self._append_log(text, tag)
        except queue.Empty:
            pass

        self.root.after(40, self._poll_queue)


def main():
    root = tk.Tk()
    app = KboardReceiverApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
