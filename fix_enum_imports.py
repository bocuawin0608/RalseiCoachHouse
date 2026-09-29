import os

enums = [
    "CoachStatus",
    "PassengerTicketDetailStatus",
    "PassengerTicketMajorChangeType",
    "PassengerTicketStatus",
    "RefundMethod",
    "RefundStatus",
    "TripSeatStatus",
    "CoachType"
]

target_dir = "staff-service/src/main/java"

for root, _, files in os.walk(target_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, "r") as f:
                content = f.read()
            
            modified = False
            for e in enums:
                old_import = f"import com.ralsei.staff.model.{e};"
                new_import = f"import com.ralsei.staff.model.enums.{e};"
                if old_import in content:
                    content = content.replace(old_import, new_import)
                    modified = True
            
            if modified:
                with open(filepath, "w") as f:
                    f.write(content)
                print(f"Fixed imports in {filepath}")

