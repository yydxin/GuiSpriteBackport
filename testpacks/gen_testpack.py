#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 GuiSpriteBackport 的测试用资源包（纯 Python，无需 PIL）。

产出：
  testpacks/gsb-testpack.zip                 1.21.1 能解析的 mcmeta（无 stretch_inner）
  testpacks/gsb-testpack-stretch-inner.zip   完全照抄 1.21.2 原版 mcmeta（含 stretch_inner），
                                             用来复现"1.21.1 解析不了该字段"的已知限制

两张 sprite 都是 100x100、border=9/10，和 1.21.2 原版 tooltip/background、tooltip/frame 的
尺寸与 gui.scaling 一致，这样在 1.21.2 里能正常显示的材质包，在 1.21.1 里会走到同一套九宫格逻辑。
"""

import json
import os
import struct
import zlib

OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)))
SIZE = 100


def write_png(path, pixels):
    """pixels: list rows of (r,g,b,a) tuples."""
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter: none
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        c = tag + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)  # 8bit RGBA
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


def make_background():
    px = [[(24, 28, 48, 230) for _ in range(SIZE)] for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            edge = min(x, y, SIZE - 1 - x, SIZE - 1 - y)
            if edge < 2:                      # 外圈亮青，用来确认边界没被拉伸
                px[y][x] = (0, 220, 220, 255)
            elif edge < 9:                    # nine-slice 的 border 区
                px[y][x] = (40, 70, 140, 240)
            else:                             # 中心区（会被平铺或拉伸）
                px[y][x] = (24, 28, 48, 230)
    # 四个角画 9x9 的品红方块：如果九宫格生效，角永远是 9x9 的方块；如果整图被拉伸，角会变形
    for cx, cy in ((0, 0), (SIZE - 9, 0), (0, SIZE - 9), (SIZE - 9, SIZE - 9)):
        for y in range(cy, cy + 9):
            for x in range(cx, cx + 9):
                if 2 <= min(x, y, SIZE - 1 - x, SIZE - 1 - y):
                    px[y][x] = (255, 0, 200, 255)
    return px


def make_frame():
    px = [[(0, 0, 0, 0) for _ in range(SIZE)] for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            edge = min(x, y, SIZE - 1 - x, SIZE - 1 - y)
            if edge < 3:
                px[y][x] = (120, 255, 80, 255)          # 亮绿描边
            elif edge < 10:
                px[y][x] = (0, 0, 0, 0)                 # frame 内部必须透明
    # 左上角放一个小三角标记，用来判断有没有镜像/错位
    for i in range(10):
        for j in range(10 - i):
            px[3 + i][3 + j] = (255, 255, 0, 255)
    return px


def build(zip_name, stretch_inner):
    import zipfile

    tmp = os.path.join(OUT_DIR, "_tmp_" + zip_name)
    os.makedirs(os.path.join(tmp, "assets/minecraft/textures/gui/sprites/tooltip"), exist_ok=True)

    with open(os.path.join(tmp, "pack.mcmeta"), "w", encoding="utf-8") as f:
        json.dump({"pack": {"pack_format": 42, "description": "GuiSpriteBackport test pack"}}, f, indent=4)

    sprite_dir = os.path.join(tmp, "assets/minecraft/textures/gui/sprites/tooltip")
    write_png(os.path.join(sprite_dir, "background.png"), make_background())
    write_png(os.path.join(sprite_dir, "frame.png"), make_frame())

    bg_meta = {"gui": {"scaling": {"type": "nine_slice", "width": 100, "height": 100, "border": 9}}}
    with open(os.path.join(sprite_dir, "background.png.mcmeta"), "w", encoding="utf-8") as f:
        json.dump(bg_meta, f, indent=4)

    frame_meta = {"gui": {"scaling": {"type": "nine_slice", "width": 100, "height": 100, "border": 10}}}
    if stretch_inner:
        frame_meta["gui"]["scaling"]["stretch_inner"] = True
    with open(os.path.join(sprite_dir, "frame.png.mcmeta"), "w", encoding="utf-8") as f:
        json.dump(frame_meta, f, indent=4)

    out = os.path.join(OUT_DIR, zip_name)
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        for root, _, files in os.walk(tmp):
            for name in files:
                full = os.path.join(root, name)
                z.write(full, os.path.relpath(full, tmp))
    import shutil
    shutil.rmtree(tmp)
    print("built", out)


if __name__ == "__main__":
    build("gsb-testpack.zip", stretch_inner=False)
    build("gsb-testpack-stretch-inner.zip", stretch_inner=True)
