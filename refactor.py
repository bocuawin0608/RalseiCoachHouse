import os
import shutil

src_base = "/tmp/nhaxe/backend-springboot/src/main/java/com/ralsei"
dst_base = "/tmp/nhaxe/customer-service/src/main/java/com/ralsei/customer"

def copy_and_modify(src_rel_path, dst_rel_path, modifications=[]):
    src_path = os.path.join(src_base, src_rel_path)
    dst_path = os.path.join(dst_base, dst_rel_path)
    
    os.makedirs(os.path.dirname(dst_path), exist_ok=True)
    
    if not os.path.exists(src_path):
        print(f"Source not found: {src_path}")
        return
        
    with open(src_path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    # Standard package replacement
    content = content.replace("package com.ralsei.", "package com.ralsei.customer.")
    content = content.replace("import com.ralsei.", "import com.ralsei.customer.")
    
    for mod in modifications:
        content = content.replace(mod[0], mod[1])
        
    with open(dst_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Copied {src_rel_path} to {dst_rel_path}")

shared_components = {
    "model/BaseEntity.java": "model/BaseEntity.java",
    "exception/GlobalExceptionHandler.java": "exception/GlobalExceptionHandler.java",
    "exception/BusinessRuleException.java": "exception/BusinessRuleException.java",
    "exception/ResourceNotFoundException.java": "exception/ResourceNotFoundException.java",
    "exception/ErrorResponse.java": "exception/ErrorResponse.java",
    "config/WebConfig.java": "config/WebConfig.java",
    "security/JwtAuthenticationFilter.java": "security/JwtAuthenticationFilter.java",
    "security/JwtService.java": "security/JwtService.java",
    "security/JwtServiceImpl.java": "security/JwtServiceImpl.java",
    "security/SecurityConfig.java": "security/SecurityConfig.java",
}

for src, dst in shared_components.items():
    copy_and_modify(src, dst)
