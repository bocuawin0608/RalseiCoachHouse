import os
import re

for root, _, files in os.walk("staff-service/src/main/java"):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, "r") as f:
                content = f.read()
            
            # Find annotations followed by Javadoc and then public class/interface
            # It's easier to just move Javadoc /** ... */ that appears right before public class
            # to BEFORE the annotations.
            
            # Regex to find: (annotations) (javadoc) (public class|interface|enum)
            # Actually, I can just find Javadoc that is between annotations and class definition.
            # A simple way: find "/** ... */\npublic class" and move the /** ... */ to before the annotations.
            # But the annotations could be anything.
            
            # Let's just remove the Javadoc if it's right before `public class` and there are annotations before it.
            # Or just move all `public class` to the line immediately after the annotations, deleting the javadoc.
            # The regex for javadoc is /\*\*.*?\*/ with DOTALL.
            
            def replacer(match):
                annotations = match.group(1)
                javadoc = match.group(2)
                cls_decl = match.group(3)
                return f"{javadoc}\n{annotations}\n{cls_decl}"

            # We want to match one or more annotations like @Data\n@Builder...
            # then Javadoc /** ... */
            # then public class
            new_content = re.sub(r'((?:@[A-Za-z0-9_]+(?:\([^)]*\))?\s*)+)(/\*\*.*?\*/)\s*(public\s+(?:class|interface|enum)\s+[A-Za-z0-9_]+)', replacer, content, flags=re.DOTALL)
            
            if new_content != content:
                with open(filepath, "w") as f:
                    f.write(new_content)
                print(f"Fixed Javadoc placement in {filepath}")
