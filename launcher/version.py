# -*- coding: utf-8 -*-
"""
版本配置与 GitHub 更新检查
发布前请同步 backend/pom.xml 中的版本
"""
import json
import os
import re
import urllib.error
import urllib.request

CURRENT_VERSION = "0.1.0-preview.1"
GITHUB_REPO = "modelrouter/modelrouter-app"  # 格式: owner/repo
GITHUB_API = f"https://api.github.com/repos/{GITHUB_REPO}/releases/latest"


def _parse_version(v: str) -> tuple:
    """解析版本号为可比较的元组，如 '0.1.0-preview.1' -> (0, 1, 0, -1, 1)"""
    v = v.strip().lstrip("vV")
    main_part = re.split(r"[-+]", v)[0]
    parts = main_part.split(".")
    result = []
    for p in parts:
        try:
            result.append(int(p))
        except ValueError:
            result.append(0)
    # 补全到至少 3 位
    while len(result) < 3:
        result.append(0)
    # 预发布后缀：preview.1 -> (-1, 1)
    suffix = re.search(r"[-+](.+)", v)
    if suffix:
        suf = suffix.group(1).lower()
        if "preview" in suf:
            num = re.search(r"(\d+)", suf)
            result.extend([-1, int(num.group(1)) if num else 0])
        elif "beta" in suf:
            num = re.search(r"(\d+)", suf)
            result.extend([-2, int(num.group(1)) if num else 0])
        elif "alpha" in suf:
            num = re.search(r"(\d+)", suf)
            result.extend([-3, int(num.group(1)) if num else 0])
        else:
            result.extend([0, 0])
    else:
        result.extend([0, 0])
    return tuple(result)


def _version_less(a: str, b: str) -> bool:
    """判断版本 a 是否小于版本 b"""
    return _parse_version(a) < _parse_version(b)


def check_for_update():
    """
    检查 GitHub 是否有新版本
    返回: (latest_version: str|None, release_url: str|None, download_url: str|None)
         有更新时返回版本号、Release 页面、jar 下载链接；无更新返回 (None, None, None)
    """
    try:
        req = urllib.request.Request(
            GITHUB_API,
            headers={"Accept": "application/vnd.github.v3+json", "User-Agent": "ModelRouter-Launcher"},
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode())
    except (urllib.error.URLError, urllib.error.HTTPError, json.JSONDecodeError, OSError):
        return None, None, None

    tag = data.get("tag_name")
    html_url = data.get("html_url")
    if not tag or not html_url:
        return None, None, None

    latest = tag.strip().lstrip("vV")
    if not _version_less(CURRENT_VERSION, latest):
        return None, None, None

    asset = _find_jar_asset(data.get("assets", []))
    download_url = asset.get("browser_download_url") if asset else None
    return latest, html_url, download_url


def _find_jar_asset(assets: list) -> dict | None:
    """从 release assets 中查找 jar 文件"""
    for a in assets:
        name = a.get("name", "").lower()
        url = a.get("browser_download_url")
        if url and name.endswith(".jar") and "modelrouter" in name and "sources" not in name:
            return a
    return None


def download_release(download_url: str, app_dir: str, progress_callback=None) -> str | None:
    """
    从 download_url 下载 jar 到 app_dir/updates/
    返回: 下载后的 jar 路径，失败返回 None
    """
    if not download_url or ".jar" not in download_url:
        return None

    updates_dir = os.path.join(app_dir, "updates")
    os.makedirs(updates_dir, exist_ok=True)
    # 从 URL 提取文件名，或使用默认名
    filename = download_url.split("/")[-1].split("?")[0] or "modelrouter-latest.jar"
    if not filename.endswith(".jar"):
        filename = "modelrouter-latest.jar"
    dest = os.path.join(updates_dir, filename)

    try:
        req = urllib.request.Request(download_url, headers={"User-Agent": "ModelRouter-Launcher"})
        with urllib.request.urlopen(req, timeout=60) as resp:
            total = int(resp.headers.get("Content-Length", 0))
            downloaded = 0
            with open(dest, "wb") as f:
                while True:
                    chunk = resp.read(8192)
                    if not chunk:
                        break
                    f.write(chunk)
                    downloaded += len(chunk)
                    if progress_callback and total > 0:
                        pct = min(100, downloaded * 100 // total)
                        progress_callback(pct)
    except Exception:
        if os.path.isfile(dest):
            try:
                os.remove(dest)
            except OSError:
                pass
        return None

    return dest
