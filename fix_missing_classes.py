import os
import re
import shutil

src_dir = "backend-springboot/src/main/java/com/ralsei"
dst_dir = "staff-service/src/main/java/com/ralsei/staff"

def find_file(name, root):
    for dirpath, _, filenames in os.walk(root):
        for f in filenames:
            if f == name + ".java":
                return os.path.join(dirpath, f)
    return None

missing_classes = set()
with open("staff-service/mvn_errors.log", "r") as f:
    for line in f:
        match = re.search(r'symbol:\s*class\s+(\w+)', line)
        if match:
            missing_classes.add(match.group(1))

print(f"Found {len(missing_classes)} missing classes to copy.")

for cls in missing_classes:
    src_file = find_file(cls, src_dir)
    if src_file:
        rel_path = os.path.relpath(src_file, src_dir)
        dst_file = os.path.join(dst_dir, rel_path)
        os.makedirs(os.path.dirname(dst_file), exist_ok=True)
        with open(src_file, "r") as f:
            content = f.read()
        content = content.replace("package com.ralsei.", "package com.ralsei.staff.")
        content = content.replace("import com.ralsei.", "import com.ralsei.staff.")
        with open(dst_file, "w") as f:
            f.write(content)
        print(f"Copied {cls}")
    else:
        print(f"Not found: {cls}")
