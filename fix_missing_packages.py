import os
import re
import shutil

src_dir = "backend-springboot/src/main/java/com/ralsei"
dst_dir = "staff-service/src/main/java/com/ralsei/staff"

missing_packages = set()
with open("staff-service/mvn_errors.log", "r") as f:
    for line in f:
        match = re.search(r'package (com\.ralsei\.staff\S*) does not exist', line)
        if match:
            missing_packages.add(match.group(1))

print(f"Found {len(missing_packages)} missing packages to copy.")

for pkg in missing_packages:
    # pkg is like com.ralsei.staff.dto.projection.trip
    rel_pkg = pkg.replace("com.ralsei.staff.", "")
    # corresponding src pkg is com.ralsei. + rel_pkg
    src_pkg_path = os.path.join(src_dir, rel_pkg.replace(".", "/"))
    dst_pkg_path = os.path.join(dst_dir, rel_pkg.replace(".", "/"))
    
    if os.path.exists(src_pkg_path) and os.path.isdir(src_pkg_path):
        for root, dirs, files in os.walk(src_pkg_path):
            for file in files:
                if file.endswith(".java"):
                    src_file = os.path.join(root, file)
                    rel_path = os.path.relpath(src_file, src_dir)
                    dst_file = os.path.join(dst_dir, rel_path)
                    
                    os.makedirs(os.path.dirname(dst_file), exist_ok=True)
                    with open(src_file, "r") as f_in:
                        content = f_in.read()
                    content = content.replace("package com.ralsei.", "package com.ralsei.staff.")
                    content = content.replace("import com.ralsei.", "import com.ralsei.staff.")
                    with open(dst_file, "w") as f_out:
                        f_out.write(content)
                    print(f"Copied {file} to {dst_file}")
    else:
        print(f"Package not found in src: {src_pkg_path}")

