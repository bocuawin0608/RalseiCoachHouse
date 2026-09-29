import os
import re

src_file = "backend-springboot/src/main/java/com/ralsei/exception/GlobalExceptionHandler.java"
dst_file = "staff-service/src/main/java/com/ralsei/staff/exception/GlobalExceptionHandler.java"

with open(src_file, "r") as f:
    content = f.read()

content = content.replace("package com.ralsei.exception;", "package com.ralsei.staff.exception;")
content = content.replace("import com.ralsei.", "import com.ralsei.staff.")
content = content.replace("import org.springframework.security.authorization.AuthorizationDeniedException;", "import org.springframework.security.access.AccessDeniedException;")
content = content.replace("AuthorizationDeniedException", "AccessDeniedException")

# Find handleHandlerMethodValidation method and replace its body
# It starts with: public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
method_signature = "public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex,\n            HttpServletRequest request) {"
dummy_body = """
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Invalid parameter")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
"""

if method_signature in content:
    # I'll just split on this and find the matching closing brace. Actually, simpler:
    pass

# Or simpler:
content = re.sub(r'public ResponseEntity<ErrorResponse> handleHandlerMethodValidation.*?return new ResponseEntity<>\(response, HttpStatus\.BAD_REQUEST\);\n    \}', 
                 method_signature + dummy_body, content, flags=re.DOTALL)


with open(dst_file, "w") as f:
    f.write(content)
