import os
import shutil
import re
from pathlib import Path

MONO_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/backend-springboot/src/main/java/com/ralsei"
STAFF_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/staff-service/src/main/java/com/ralsei/staff"

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
    content = re.sub(r'package com\.ralsei', 'package com.ralsei.staff', content)
    # Update imports
    content = re.sub(r'import com\.ralsei\.', 'import com.ralsei.staff.', content)

    # Refactors for entities & repos
    if src.name == "Trip.java":
        content = re.sub(r'@ManyToOne[^\n]*\n\s*@JoinColumn\(name = "coachId"[^\)]*\)\s*\n\s*private Coach coach;',
                         '// [MICROSERVICE-REFACTOR]: Removed @ManyToOne Coach. Use scalar coachId + FeignClient.\n    // coachId is retained as int scalar field.', content)
    elif src.name == "CargoTicket.java":
        content = re.sub(r'@OneToOne[^\n]*\n\s*private Payment payment;',
                         '// [MICROSERVICE-REFACTOR]: Removed Payment entity relationship. Payment is managed in Customer Service.', content)
    elif src.name == "TripRepository.java":
        content = content.replace("JOIN FETCH t.coach c", "")
        content = content.replace("JOIN c.coachType", "")

    with open(dst, "w", encoding="utf-8") as f:
        f.write(content)

def main():
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

    models = [
        ("model/Staff.java", "model/Staff.java"),
        ("model/Trip.java", "model/Trip.java"),
        ("model/Route.java", "model/Route.java"),
        ("model/RouteStop.java", "model/RouteStop.java"),
        ("model/CoachStop.java", "model/CoachStop.java"),
        ("model/TicketAgency.java", "model/TicketAgency.java"),
        ("model/CargoTicket.java", "model/CargoTicket.java"),
        ("model/CargoTicketDetail.java", "model/CargoTicketDetail.java"),
        ("model/CargoType.java", "model/CargoType.java"),
        ("model/CargoTypePrice.java", "model/CargoTypePrice.java"),
        ("model/enums/ProvinceEnum.java", "model/ProvinceEnum.java")
    ]

    repos = [
        ("repository/StaffRepository.java", "repository/StaffRepository.java"),
        ("repository/TripRepository.java", "repository/TripRepository.java"),
        ("repository/RouteRepository.java", "repository/RouteRepository.java"),
        ("repository/RouteStopRepository.java", "repository/RouteStopRepository.java"),
        ("repository/CoachStopRepository.java", "repository/CoachStopRepository.java"),
        ("repository/TicketAgencyRepository.java", "repository/TicketAgencyRepository.java"),
        ("repository/CargoTicketRepository.java", "repository/CargoTicketRepository.java"),
        ("repository/CargoTicketDetailRepository.java", "repository/CargoTicketDetailRepository.java"),
        ("repository/CargoTypeRepository.java", "repository/CargoTypeRepository.java"),
        ("repository/CargoTypePriceRepository.java", "repository/CargoTypePriceRepository.java")
    ]

    services = [
        ("service/StaffService.java", "service/StaffService.java"),
        ("service/impl/StaffServiceImpl.java", "service/impl/StaffServiceImpl.java"),
        ("service/StaffAccountService.java", "service/StaffAccountService.java"),
        ("service/impl/StaffAccountServiceImpl.java", "service/impl/StaffAccountServiceImpl.java"),
        ("service/TripService.java", "service/TripService.java"),
        ("service/impl/TripServiceImpl.java", "service/impl/TripServiceImpl.java"),
        ("service/RouteService.java", "service/RouteService.java"),
        ("service/impl/RouteServiceImpl.java", "service/impl/RouteServiceImpl.java"),
        ("service/RouteStopService.java", "service/RouteStopService.java"),
        ("service/impl/RouteStopServiceImpl.java", "service/impl/RouteStopServiceImpl.java"),
        ("service/CoachStopService.java", "service/CoachStopService.java"),
        ("service/impl/CoachStopServiceImpl.java", "service/impl/CoachStopServiceImpl.java"),
        ("service/TicketAgencyService.java", "service/TicketAgencyService.java"),
        ("service/impl/TicketAgencyServiceImpl.java", "service/impl/TicketAgencyServiceImpl.java"),
        ("service/CargoTicketService.java", "service/CargoTicketService.java"),
        ("service/impl/CargoTicketServiceImpl.java", "service/impl/CargoTicketServiceImpl.java"),
        ("service/CargoTypeService.java", "service/CargoTypeService.java"),
        ("service/impl/CargoTypeServiceImpl.java", "service/impl/CargoTypeServiceImpl.java"),
        ("service/CargoTypePriceService.java", "service/CargoTypePriceService.java"),
        ("service/impl/CargoTypePriceServiceImpl.java", "service/impl/CargoTypePriceServiceImpl.java"),
        ("service/GoongService.java", "service/GoongService.java"),
        ("service/impl/GoongServiceImpl.java", "service/impl/GoongServiceImpl.java")
    ]

    service_subdirs = [
        "service/cargoticket",
        "service/passengerticket",
        "service/passengerticket/impl",
        "service/staffrefund",
        "service/staffrefund/impl",
        "service/tripstaff",
        "service/tripstaff/impl"
    ]

    controllers = [
        ("controller/StaffController.java", "controller/StaffController.java"),
        ("controller/StaffAccountController.java", "controller/StaffAccountController.java"),
        ("controller/TripController.java", "controller/TripController.java"),
        ("controller/RouteController.java", "controller/RouteController.java"),
        ("controller/RouteStopController.java", "controller/RouteStopController.java"),
        ("controller/CoachStopController.java", "controller/CoachStopController.java"),
        ("controller/TicketAgencyController.java", "controller/TicketAgencyController.java"),
        ("controller/CargoTicketController.java", "controller/CargoTicketController.java"),
        ("controller/CargoTypeController.java", "controller/CargoTypeController.java"),
        ("controller/CargoTypePriceController.java", "controller/CargoTypePriceController.java"),
        ("controller/StaffPassengerTicketController.java", "controller/StaffPassengerTicketController.java"),
        ("controller/StaffRefundController.java", "controller/StaffRefundController.java"),
        ("controller/TripStaffController.java", "controller/TripStaffController.java"),
        ("controller/GoongController.java", "controller/GoongController.java")
    ]

    utils = [
        ("util/ProvinceMapper.java", "util/ProvinceMapper.java"),
        ("util/TimeForeCastUtility.java", "util/TimeForeCastUtility.java"),
        ("util/TripUtility.java", "util/TripUtility.java"),
        ("util/CargoVolumePolicy.java", "util/CargoVolumePolicy.java"),
        ("util/FreightCalculatorUtility.java", "util/FreightCalculatorUtility.java"),
        ("util/StringNormalize.java", "util/StringNormalize.java"),
        ("util/GetCurrentDateUtility.java", "util/GetCurrentDateUtility.java")
    ]

    for src, dst in shared + models + repos + services + controllers + utils:
        copy_and_refactor(f"{MONO_DIR}/{src}", f"{STAFF_DIR}/{dst}")

    for s_dir in service_subdirs:
        p = Path(f"{MONO_DIR}/{s_dir}")
        if p.exists():
            for f in p.glob("*.java"):
                copy_and_refactor(str(f), f"{STAFF_DIR}/{s_dir}/{f.name}")

    # Copy all DTOs
    dto_dirs = [
        "dto/request/staff", "dto/request/staffaccount", "dto/request/trip",
        "dto/request/CoachAndRouteStop", "dto/request/route", "dto/request/ticketagency",
        "dto/request/cargoticket", "dto/request/cargoticketdetail", "dto/request/cargotype",
        "dto/request/cargotypeprice", "dto/request/staffpassengerticket", "dto/request/staffrefund",
        "dto/request/tripstaff", "dto/request/goong", "dto/request/sePay",
        "dto/response/staff", "dto/response/trip", "dto/response/route", "dto/response/ticketagency",
        "dto/response/cargoticket", "dto/response/cargotype", "dto/response/cargotypeprice",
        "dto/response/staffpassengerticket", "dto/response/staffrefund", "dto/response/tripstaff",
        "dto/projection"
    ]
    for d in dto_dirs:
        d_path = Path(f"{MONO_DIR}/{d}")
        if d_path.exists():
            for f in d_path.glob("*.java"):
                copy_and_refactor(str(f), f"{STAFF_DIR}/{d}/{f.name}")

    # Generate FeignClients
    feign_dir = Path(f"{STAFF_DIR}/feign")
    feign_dir.mkdir(parents=True, exist_ok=True)
    
    feign_dir.joinpath("DriverServiceClient.java").write_text('''package com.ralsei.staff.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "driver-service")
public interface DriverServiceClient {
    @GetMapping("/api/internal/coaches/{coachId}")
    Object getCoachById(@PathVariable("coachId") Integer coachId);

    @GetMapping("/api/internal/coaches/available")
    Object getAvailableCoaches(@RequestParam("routeId") Integer routeId, @RequestParam("departureTime") String departureTime);
}
''', encoding="utf-8")

    feign_dir.joinpath("CustomerServiceClient.java").write_text('''package com.ralsei.staff.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    @GetMapping("/api/internal/passenger-tickets/count-by-trip/{tripId}")
    Integer countTicketsByTripId(@PathVariable("tripId") Integer tripId);

    @GetMapping("/api/internal/trip-seats/count-by-trip/{tripId}")
    Object getSeatAvailabilityByTripId(@PathVariable("tripId") Integer tripId);
}
''', encoding="utf-8")

    feign_dir.joinpath("AuthServiceClient.java").write_text('''package com.ralsei.staff.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/internal/accounts/{accountId}")
    Object getAccountById(@PathVariable("accountId") Integer accountId);
}
''', encoding="utf-8")

    # Generate InternalControllers
    internal_dir = Path(f"{STAFF_DIR}/controller/internal")
    internal_dir.mkdir(parents=True, exist_ok=True)

    internal_dir.joinpath("InternalTripController.java").write_text('''package com.ralsei.staff.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/trips")
@RequiredArgsConstructor
public class InternalTripController {
    // GET /{tripId} -> returns TripInfoDTO
    // GET /{tripId}/stops -> returns List<TripStopDTO>
    // POST /batch -> body: List<Integer> tripIds -> returns List<TripInfoDTO>
}
''', encoding="utf-8")

    internal_dir.joinpath("InternalStaffController.java").write_text('''package com.ralsei.staff.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/staff")
@RequiredArgsConstructor
public class InternalStaffController {
    // GET /{staffId} -> returns StaffInfoDTO
    // GET /by-account/{accountId} -> returns StaffInfoDTO
    // POST /batch -> body: List<Integer> staffIds -> returns List<StaffInfoDTO>
}
''', encoding="utf-8")

    internal_dir.joinpath("InternalRouteController.java").write_text('''package com.ralsei.staff.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/routes")
@RequiredArgsConstructor
public class InternalRouteController {
    // GET /{routeId} -> returns RouteInfoDTO
    // GET /{routeId}/stops -> returns List<RouteStopDTO>
}
''', encoding="utf-8")

    internal_dir.joinpath("InternalCoachStopController.java").write_text('''package com.ralsei.staff.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/coach-stops")
@RequiredArgsConstructor
public class InternalCoachStopController {
    // GET /{stopPointId} -> returns CoachStopInfoDTO
}
''', encoding="utf-8")

    print("Staff migration complete!")

if __name__ == "__main__":
    main()
