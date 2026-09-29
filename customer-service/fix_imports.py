import re
import os

def find_package(class_name):
    # Find file with the class name
    result = os.popen(f"find src/main/java/com/ralsei/customer -name '{class_name}.java'").read().strip().split('\n')
    for res in result:
        if res:
            with open(res, 'r') as f:
                for line in f:
                    if line.startswith('package '):
                        pkg = line.replace('package ', '').replace(';', '').strip()
                        return f"{pkg}.{class_name}"
    return None

error_file = "compile_errors5.txt"
with open(error_file, 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "cannot find symbol" in line and "[ERROR] /home/loliconhihi/" in line:
        filepath = line.split(":[")[0].replace("[ERROR] ", "").strip()
        
        # look ahead for symbol: class XYZ
        class_name = None
        for j in range(i+1, min(i+5, len(lines))):
            match = re.search(r'symbol:\s+class\s+(\w+)', lines[j])
            if match:
                class_name = match.group(1)
                break
        
        if class_name:
            full_class = find_package(class_name)
            if full_class:
                with open(filepath, 'r') as java_f:
                    java_code = java_f.read()
                
                import_stmt = f"import {full_class};\n"
                if import_stmt not in java_code:
                    print(f"Adding {import_stmt.strip()} to {filepath}")
                    # Insert after package
                    java_code = re.sub(r'(package .+;\n)', r'\1\n' + import_stmt, java_code)
                    with open(filepath, 'w') as java_f:
                        java_f.write(java_code)
