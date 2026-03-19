# -*- coding: utf-8 -*-
"""
ModelRouter Windows 一键启动程序
使用 SQLite 数据库，无需 Docker，双击即可启动
"""
import os
import sys
import subprocess
import webbrowser
import threading
import time
import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext

from version import CURRENT_VERSION, GITHUB_REPO, check_for_update, download_release

APP_URL = "http://localhost:20118"


def find_app_dir() -> str:
    """查找应用根目录（jar 与 data 所在位置）"""
    if getattr(sys, "frozen", False):
        base = os.path.dirname(sys.executable)
    else:
        base = os.path.dirname(os.path.abspath(__file__))
    # 向上查找包含 modelrouter.jar 或 backend/target 的目录
    for _ in range(5):
        if os.path.isfile(os.path.join(base, "modelrouter.jar")):
            return base
        if os.path.isfile(os.path.join(base, "modelrouter-backend.jar")):
            return base
        if os.path.isdir(os.path.join(base, "backend", "target")):
            return base
        parent = os.path.dirname(base)
        if parent == base:
            break
        base = parent
    return base


def find_jar(app_dir):
    """查找可用的 jar 文件"""
    candidates = [
        os.path.join(app_dir, "modelrouter.jar"),
        os.path.join(app_dir, "modelrouter-backend.jar"),
        os.path.join(app_dir, "backend", "target", "modelrouter-backend-0.1.0-preview.1.jar"),
    ]
    for p in candidates:
        if os.path.isfile(p):
            return p
    # 通配匹配 backend/target/modelrouter-backend-*.jar
    target_dir = os.path.join(app_dir, "backend", "target")
    if os.path.isdir(target_dir):
        for name in os.listdir(target_dir):
            if name.startswith("modelrouter-backend-") and name.endswith(".jar") and "SNAPSHOT" not in name.upper():
                return os.path.join(target_dir, name)
    return None


def find_java():
    """查找 java 可执行文件"""
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        candidates = [
            os.path.join(java_home, "bin", "java.exe"),
            os.path.join(java_home, "bin", "java"),
        ]
        for p in candidates:
            if os.path.isfile(p):
                return p
    # 尝试 PATH 中的 java
    try:
        subprocess.run(
            ["java", "-version"],
            capture_output=True,
            text=True,
            timeout=3,
        )
        return "java"
    except Exception:
        pass
    return None


def ensure_data_dir(app_dir):
    """确保 data 目录存在"""
    data_dir = os.path.join(app_dir, "data")
    os.makedirs(data_dir, exist_ok=True)
    return data_dir


def start_backend(app_dir, jar_path, log_callback):
    """启动后端进程"""
    java_exe = find_java()
    if not java_exe:
        log_callback("[错误] 未找到 Java，请安装 JDK 17+ 并设置 JAVA_HOME 或 PATH")
        return None

    if not jar_path or not os.path.isfile(jar_path):
        log_callback(f"[错误] 未找到 jar 文件，请先执行构建：cd backend && mvn package")
        log_callback(f"查找路径: {app_dir}")
        return None

    ensure_data_dir(app_dir)
    cmd = [
        java_exe,
        "-jar", jar_path,
        "--spring.profiles.active=sqlite",
    ]
    env = os.environ.copy()
    env["SPRING_PROFILES_ACTIVE"] = "sqlite"
    kwargs = dict(
        cwd=app_dir,
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if sys.platform == "win32":
        kwargs["creationflags"] = subprocess.CREATE_NO_WINDOW
    proc = subprocess.Popen(cmd, **kwargs)
    return proc


def main():
    app_dir = find_app_dir()
    jar_path = find_jar(app_dir)

    root = tk.Tk()
    root.title("ModelRouter 一键启动")
    root.geometry("560x400")
    root.resizable(True, True)
    root.minsize(450, 350)

    main_frame = ttk.Frame(root, padding=16)
    main_frame.pack(fill=tk.BOTH, expand=True)

    title_row = ttk.Frame(main_frame)
    title_row.pack(pady=(0, 12))
    ttk.Label(title_row, text="ModelRouter", font=("Segoe UI", 16, "bold")).pack(side=tk.LEFT)
    ttk.Label(title_row, text=f" v{CURRENT_VERSION}", foreground="gray").pack(side=tk.LEFT)
    update_btn_holder = [None]

    def on_update_available(latest: str, release_url: str, download_url: str):
        def do_update():
            app_dir = find_app_dir()
            result = download_release(download_url, app_dir)
            if result:
                messagebox.showinfo("下载完成", f"已保存到 {result}\n请先停止服务，再将新 jar 覆盖为 modelrouter.jar 后重启。")
            else:
                webbrowser.open(release_url)
                messagebox.showinfo("手动下载", f"请从 GitHub 下载最新版本 v{latest}\n已打开 Release 页面。")

        btn = ttk.Button(title_row, text=f"更新到 v{latest}", command=do_update)
        btn.pack(side=tk.RIGHT, padx=(8, 0))
        update_btn_holder[0] = btn

    def run_update_check():
        try:
            latest, release_url, download_url = check_for_update()
            if latest:
                root.after(0, lambda: on_update_available(latest, release_url, download_url or release_url))
        except Exception:
            pass

    threading.Thread(target=run_update_check, daemon=True).start()

    urls_frame = ttk.LabelFrame(main_frame, text="服务地址", padding=12)
    urls_frame.pack(fill=tk.X, pady=(0, 12))

    def add_url_row(parent, label: str, url: str):
        row = ttk.Frame(parent)
        row.pack(fill=tk.X, pady=4)
        ttk.Label(row, text=f"{label}:", width=8, anchor="w").pack(side=tk.LEFT)
        ttk.Label(row, text=url, foreground="#0066cc").pack(side=tk.LEFT, padx=(0, 8))
        ttk.Button(row, text="打开", command=lambda: webbrowser.open(url)).pack(side=tk.RIGHT)

    add_url_row(urls_frame, "管理界面", APP_URL)

    ttk.Button(main_frame, text="打开管理界面", command=lambda: webbrowser.open(APP_URL)).pack(pady=(0, 12))

    log_frame = ttk.LabelFrame(main_frame, text="运行日志", padding=8)
    log_frame.pack(fill=tk.BOTH, expand=True, pady=(0, 12))
    log = scrolledtext.ScrolledText(log_frame, height=10, wrap=tk.WORD, state=tk.DISABLED, font=("Consolas", 9))
    log.pack(fill=tk.BOTH, expand=True)

    def append_log(text: str):
        log.config(state=tk.NORMAL)
        log.insert(tk.END, text + "\n")
        log.see(tk.END)
        log.config(state=tk.DISABLED)

    proc = [None]  # 用列表以便在闭包中修改

    def read_stdout():
        if proc[0] and proc[0].stdout:
            for line in iter(proc[0].stdout.readline, ""):
                if line:
                    root.after(0, lambda l=line: append_log(l.rstrip()))

    def start_services():
        nonlocal proc
        if proc[0] is not None:
            append_log("服务已在运行中")
            return
        append_log("正在启动 ModelRouter (SQLite 模式)...")
        root.update()
        proc[0] = start_backend(app_dir, jar_path, append_log)
        if proc[0]:
            append_log("后端进程已启动，等待就绪...")
            t = threading.Thread(target=read_stdout, daemon=True)
            t.start()
            def open_browser():
                time.sleep(5)
                try:
                    webbrowser.open(APP_URL)
                except Exception:
                    pass
            threading.Thread(target=open_browser, daemon=True).start()
        else:
            append_log("启动失败，请检查上方错误信息")

    def stop_services():
        if proc[0] is not None:
            proc[0].terminate()
            proc[0] = None
            append_log("已发送停止信号")
        else:
            append_log("当前无运行中的服务")

    def on_closing():
        if proc[0] is not None:
            if messagebox.askokcancel("退出", "ModelRouter 仍在运行，确定要退出并停止服务吗？"):
                proc[0].terminate()
                root.destroy()
        else:
            root.destroy()

    root.protocol("WM_DELETE_WINDOW", on_closing)

    btn_frame = ttk.Frame(main_frame)
    btn_frame.pack(fill=tk.X)
    ttk.Button(btn_frame, text="启动服务", command=start_services).pack(side=tk.LEFT, padx=(0, 8))
    ttk.Button(btn_frame, text="停止服务", command=stop_services).pack(side=tk.LEFT)

    root.after(300, start_services)
    root.mainloop()


if __name__ == "__main__":
    main()
