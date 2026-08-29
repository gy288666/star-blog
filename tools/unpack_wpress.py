#!/usr/bin/env python3
"""Unpack an All-in-One WP Migration .wpress archive.

Entry layout (header 4377 bytes, then content):
  [0, 255)    filename (null-padded)
  [255, 269)  content size (ASCII decimal, null-padded)
  [269, 281)  mtime
  [281, 4377) directory prefix
"""
import sys, os

HEADER_SIZE = 4377

def unpack(path, outdir, only=None):
    os.makedirs(outdir, exist_ok=True)
    n = 0
    with open(path, "rb") as f:
        while True:
            header = f.read(HEADER_SIZE)
            if not header or not header.rstrip(b"\0"):
                break
            name = header[0:255].split(b"\0")[0].decode("utf-8", "replace")
            size = int(header[255:269].split(b"\0")[0])
            prefix = header[281:HEADER_SIZE].split(b"\0")[0].decode("utf-8", "replace")
            data = f.read(size)
            rel = (prefix + "/" + name).replace("\\", "/").lstrip("./") if prefix not in (".", "") else name.replace("\\", "/")
            if only and not any(p in rel for p in only):
                n += 1
                continue
            dest = os.path.join(outdir, rel.replace("/", os.sep))
            os.makedirs(os.path.dirname(dest) or outdir, exist_ok=True)
            with open(dest, "wb") as w:
                w.write(data)
            n += 1
            if n % 500 == 0:
                print(f"... {n} entries")
    print(f"done, entries scanned: {n}")

if __name__ == "__main__":
    src, out = sys.argv[1], sys.argv[2]
    unpack(src, out, sys.argv[3:] or None)
