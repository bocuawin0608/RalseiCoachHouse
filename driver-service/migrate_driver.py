import os
import shutil
import re
from pathlib import Path

MONO_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/backend-springboot/src/main/java/com/ralsei"
DRIVER_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/driver-service/src/main/java/com/ralsei/driver"

def copy_and_refactor(src_path_str, dst_path_str):
    src = Path(src_path_str)
    dst = Path(dst_path_str)
    if not src.exists():
        print(f"Skipping missing file: {src}")
        return
    dst.parent.mkdir(parents=True, exist_ok=True)
    
    with open(src, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Update package
    content = re.sub(r'package com\.ralsei', 'package com.ralsei.driver', content)
    # Update imports
    content = re.sub(r'import com\.ralsei\.', 'import com.ralsei.driver.', content)

    # Entity refactors
    if src.name == "Coach.java":
        # Remove @ManyToOne Route route
        content = re.sub(r'@ManyToOne[^\n]*\n\s*@JoinColumn\(name = "routeId"\)\s*\n\s*private Route route;', 
                         '// [MICROSERVICE-REFACTOR]: Removed @ManyToOne Route. Use routeId + FeignClient to Staff Service.\n    @Column(name = "routeId")\n    private Integer routeId;', content)
    elif src.name == "Seat.java":
        # Remove List<TripSeat>
        content = re.sub(r'@Builder\.Default\s*\n\s*@OneToMany[^\n]*\n\s*private List<TripSeat> tripSeats[^;]*;', 
                         '// [MICROSERVICE-REFACTOR]: Removed TripSeat relationship (TripSeat is in Customer Service)', content)
    elif src.name == "CoachRepository.java":
        # Remove route join in entity graph if present
        content = content.replace('"route", "coachType"', '"coachType"')

    with open(dst, "w", encoding="utf-8") as f:
        f.write(content)

def main():
    # Shared
    shared = [
        ("model/BaseEntity.java", "model/BaseEntity.java"),
        ("exception/GlobalExceptionHandler.java", "exception/GlobalExceptionHandler.java"),
        ("exception/BusinessRuleException.java", "exception/BusinessRuleException.java"),
        ("exception/ResourceNotFoundException.java", "exception/ResourceNotFoundException.java"),
        ("dto/response/ErrorResponse.java", "dto/response/ErrorResponse.java"),
        ("config/WebConfig.java", "config/WebConfig.java"),
        ("config/SecurityConfig.java", "config/SecurityConfig.java"),
        ("security/JwtAuthenticationFilter.java", "security/JwtAuthenticationFilter.java")
    ]

    # Models
    models = [
        ("model/Coach.java", "model/Coach.java"),
        ("model/CoachType.java", "model/CoachType.java"),
        ("model/CoachTypePrice.java", "model/CoachTypePrice.java"),
        ("model/Seat.java", "model/Seat.java"),
        ("model/CoachStatusLog.java", "model/CoachStatusLog.java"),
        ("model/enums/CoachStatus.java", "model/CoachStatus.java"),
        ("model/enums/CoachTypePriceStatus.java", "model/CoachTypePriceStatus.java")
    ]

    # Repos
    repos = [
        ("repository/CoachRepository.java", "repository/CoachRepository.java"),
        ("repository/CoachTypeRepository.java", "repository/CoachTypeRepository.java"),
        ("repository/CoachTypePriceRepository.java", "repository/CoachTypePriceRepository.java"),
        ("repository/SeatRepository.java", "repository/SeatRepository.java"),
        ("repository/CoachStatusLogRepository.java", "repository/CoachStatusLogRepository.java")
    ]

    # Services
    services = [
        ("service/CoachService.java", "service/CoachService.java"),
        ("service/impl/CoachServiceImpl.java", "service/impl/CoachServiceImpl.java"),
        ("service/CoachTypeService.java", "service/CoachTypeService.java"),
        ("service/impl/CoachTypeServiceImpl.java", "service/impl/CoachTypeServiceImpl.java")
    ]

    # Controllers
    controllers = [
        ("controller/CoachController.java", "controller/CoachController.java"),
        ("controller/CoachTypeController.java", "controller/CoachTypeController.java")
    ]

    # Utils
    utils = [
        ("util/LicensePlateUtility.java", "util/LicensePlateUtility.java"),
        ("util/validation/CoachValidationPatterns.java", "util/validation/CoachValidationPatterns.java"),
        ("util/validation/MaxCurrentYear.java", "util/validation/MaxCurrentYear.java"),
        ("util/validation/MaxCurrentYearValidator.java", "util/validation/MaxCurrentYearValidator.java")
    ]

    for src, dst in shared + models + repos + services + controllers + utils:
        copy_and_refactor(f"{MONO_DIR}/{src}", f"{DRIVER_DIR}/{dst}")

    # Copy all DTOs in coach and coachtype
    dto_dirs = [
        "dto/request/coach", "dto/request/coachtype",
        "dto/response/coach", "dto/response/coachtype",
        "dto/projection"
    ]
    for d in dto_dirs:
        d_path = Path(f"{MONO_DIR}/{d}")
        if d_path.exists():
            for f in d_path.glob("*.java"):
                copy_and_refactor(str(f), f"{DRIVER_DIR}/{d}/{f.name}")

    print("Driver migration complete!")

if __name__ == "__main__":
    main()
