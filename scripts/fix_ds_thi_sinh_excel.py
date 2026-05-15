#!/usr/bin/env python3
"""
Script sửa file Excel danh sách thí sinh: đặt giá trị `CCCD` bằng `Số báo danh` (mặc định).
Hỗ trợ chế độ đảo ngược: đặt `Số báo danh` bằng `CCCD`.

Usage:
  python scripts/fix_ds_thi_sinh_excel.py input.xlsx [--sheet SHEET] [--mode cccd_from_sbd|sbd_from_cccd] [--output out.xlsx]

Examples:
  python scripts/fix_ds_thi_sinh_excel.py "ds thi sinh.xlsx"
  python scripts/fix_ds_thi_sinh_excel.py "ds thi sinh.xlsx" --mode sbd_from_cccd --output corrected.xlsx

Lưu ý: script sẽ tạo bản sao lưu của file gốc nếu không cung cấp `--output`.
"""

import argparse
import shutil
import sys
from datetime import datetime
from pathlib import Path

import pandas as pd


CANDIDATE_SBD = [
    "số báo danh",
    "so bao danh",
    "sbd",
    "sốbao danh",
    "sobd",
]
CANDIDATE_CCCD = [
    "cccd",
    "cmnd",
    "căn cước",
    "can cuoc",
    "cmnd/cccd",
]


def find_column(cols, candidates):
    lower = [c.lower().strip() for c in cols]
    for cand in candidates:
        for i, name in enumerate(lower):
            # relax matching: remove spaces and compare
            if name == cand or name.replace(" ", "") == cand.replace(" ", ""):
                return cols[i]
            # also try contains
            if cand in name:
                return cols[i]
    return None


def backup_file(path: Path) -> Path:
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    bak = path.with_name(path.stem + f".bak.{ts}" + path.suffix)
    shutil.copy2(path, bak)
    return bak


def main():
    p = argparse.ArgumentParser(description="Fix CCCD / SBD trong file Excel danh sách thí sinh")
    p.add_argument("input", help="Đường dẫn tới file Excel (ví dụ: ds thi sinh.xlsx)")
    p.add_argument("--sheet", default=0, help="Tên hoặc chỉ số sheet (mặc định: 0)")
    p.add_argument("--mode", choices=["cccd_from_sbd", "sbd_from_cccd"], default="cccd_from_sbd",
                   help="cccd_from_sbd: gán CCCD = Số báo danh (mặc định). sbd_from_cccd: gán SBD = CCCD")
    p.add_argument("--output", help="Đường dẫn file đầu ra (nếu không cung cấp, sẽ ghi đè file gốc sau khi tạo bản sao lưu)")
    args = p.parse_args()

    input_path = Path(args.input)
    if not input_path.exists():
        print(f"File không tồn tại: {input_path}")
        sys.exit(1)

    try:
        df = pd.read_excel(input_path, sheet_name=args.sheet, dtype=str, engine="openpyxl")
    except Exception as e:
        print("Không thể đọc file Excel:", e)
        sys.exit(1)

    cols = list(df.columns)
    sbd_col = find_column(cols, CANDIDATE_SBD)
    cccd_col = find_column(cols, CANDIDATE_CCCD)

    print("Cột phát hiện trong file:")
    for c in cols:
        print(" -", c)
    print()

    if args.mode == "cccd_from_sbd":
        if sbd_col is None:
            print("Không tìm thấy cột SBD trong file. Vui lòng kiểm tra tên cột.")
            sys.exit(1)
        # Nếu không có cccd_col, tạo cột mới tên 'CCCD'
        if cccd_col is None:
            cccd_col = "CCCD"
            df[cccd_col] = ""
            print("Không tìm thấy cột CCCD, sẽ tạo cột mới 'CCCD'.")

        before_nonnull = df[cccd_col].notna().sum() if cccd_col in df.columns else 0
        df[cccd_col] = df[sbd_col].fillna("").astype(str).str.strip()
        after_nonnull = df[cccd_col].notna().sum()
        print(f"Đã gán `{cccd_col}` = `{sbd_col}` cho {len(df)} dòng. Trước: {before_nonnull} giá trị không rỗng; Sau: {after_nonnull}.")

    else:  # sbd_from_cccd
        if cccd_col is None:
            print("Không tìm thấy cột CCCD trong file. Vui lòng kiểm tra tên cột.")
            sys.exit(1)
        if sbd_col is None:
            sbd_col = "Số báo danh"
            df[sbd_col] = ""
            print("Không tìm thấy cột Số báo danh, sẽ tạo cột mới 'Số báo danh'.")

        before_nonnull = df[sbd_col].notna().sum() if sbd_col in df.columns else 0
        df[sbd_col] = df[cccd_col].fillna("").astype(str).str.strip()
        after_nonnull = df[sbd_col].notna().sum()
        print(f"Đã gán `{sbd_col}` = `{cccd_col}` cho {len(df)} dòng. Trước: {before_nonnull} giá trị không rỗng; Sau: {after_nonnull}.")

    # Lưu file: nếu output được cung cấp thì lưu ra đó, ngược lại backup rồi ghi đè
    if args.output:
        out_path = Path(args.output)
        out_path.parent.mkdir(parents=True, exist_ok=True)
        df.to_excel(out_path, index=False, engine="openpyxl")
        print(f"Đã lưu file sửa vào: {out_path}")
    else:
        bak = backup_file(input_path)
        try:
            df.to_excel(input_path, index=False, engine="openpyxl")
            print(f"Đã sao lưu bản gốc sang: {bak}")
            print(f"Đã ghi đè file gốc: {input_path}")
        except Exception as e:
            print("Lỗi khi lưu file:", e)
            # nếu lưu thất bại, khôi phục từ backup
            if bak.exists():
                shutil.copy2(bak, input_path)
                print("Đã khôi phục file gốc từ bản sao lưu.")
            sys.exit(1)


if __name__ == "__main__":
    main()
