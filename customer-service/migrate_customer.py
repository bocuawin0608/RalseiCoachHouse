import os
import shutil
import re
from pathlib import Path

MONO_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/backend-springboot/src/main/java/com/ralsei"
CUST_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/customer-service/src/main/java/com/ralsei/customer"

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
    content = re.sub(r'package com\.ralsei', 'package com.ralsei.customer', content)
    # Update imports
    content = re.sub(r'import com\.ralsei\.', 'import com.ralsei.customer.', content)

    # Specific entity refactors
    if src.name == "TripSeat.java":
        content = re.sub(r'@ManyToOne[^\n]*\n\s*@JoinColumn\(name = "tripId"[^\)]*\)\s*\n\s*private Trip trip;',
                         '// [MICROSERVICE-REFACTOR]: Removed @ManyToOne Trip. Use scalar tripId + FeignClient.\n    @Column(name = "tripId", nullable = false)\n    private int tripId;', content)
        content = re.sub(r'@ManyToOne[^\n]*\n\s*@JoinColumn\(name = "seatId"[^\)]*\)\s*\n\s*private Seat seat;',
                         '// [MICROSERVICE-REFACTOR]: Removed @ManyToOne Seat. Use scalar seatId + FeignClient.\n    @Column(name = "seatId", nullable = false)\n    private int seatId;', content)
    elif src.name == "Payment.java":
        content = re.sub(r'@OneToOne[^\n]*\n\s*@JoinColumn\(name = "cargoTicketId"\)\s*\n\s*private CargoTicket cargoTicket;',
                         '// [MICROSERVICE-REFACTOR]: Removed @OneToOne CargoTicket. Use scalar cargoTicketId + FeignClient.\n    @Column(name = "cargoTicketId")\n    private Integer cargoTicketId;', content)
    elif src.name == "TripSeatRepository.java":
        content = content.replace("ts LEFT JOIN ts.seat s WHERE ts.trip.tripId = :tripId", "ts WHERE ts.tripId = :tripId")
        content = content.replace("JOIN FETCH ts.seat", "")

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
        ("model/Customer.java", "model/Customer.java"),
        ("model/PassengerTicket.java", "model/PassengerTicket.java"),
        ("model/PassengerTicketDetail.java", "model/PassengerTicketDetail.java"),
        ("model/AccompaniedChild.java", "model/AccompaniedChild.java"),
        ("model/TripSeat.java", "model/TripSeat.java"),
        ("model/Payment.java", "model/Payment.java"),
        ("model/Refund.java", "model/Refund.java"),
        ("model/Voucher.java", "model/Voucher.java"),
        ("model/enums/PassengerTicketStatus.java", "model/PassengerTicketStatus.java"),
        ("model/enums/PassengerTicketDetailStatus.java", "model/PassengerTicketDetailStatus.java"),
        ("model/enums/PassengerTicketMajorChangeType.java", "model/PassengerTicketMajorChangeType.java"),
        ("model/enums/PassengerPendingPaymentOutcome.java", "model/PassengerPendingPaymentOutcome.java"),
        ("model/enums/TripSeatStatus.java", "model/TripSeatStatus.java"),
        ("model/enums/RefundMethod.java", "model/RefundMethod.java"),
        ("model/enums/RefundStatus.java", "model/RefundStatus.java"),
        ("model/enums/VoucherType.java", "model/VoucherType.java")
    ]

    repos = [
        ("repository/CustomerRepository.java", "repository/CustomerRepository.java"),
        ("repository/PassengerTicketRepository.java", "repository/PassengerTicketRepository.java"),
        ("repository/PassengerTicketDetailRepository.java", "repository/PassengerTicketDetailRepository.java"),
        ("repository/AccompaniedChildRepository.java", "repository/AccompaniedChildRepository.java"),
        ("repository/TripSeatRepository.java", "repository/TripSeatRepository.java"),
        ("repository/PaymentRepository.java", "repository/PaymentRepository.java"),
        ("repository/RefundRepository.java", "repository/RefundRepository.java"),
        ("repository/VoucherRepository.java", "repository/VoucherRepository.java")
    ]

    services = [
        ("service/CustomerService.java", "service/CustomerService.java"),
        ("service/impl/CustomerServiceImpl.java", "service/impl/CustomerServiceImpl.java"),
        ("service/CustomerAccountService.java", "service/CustomerAccountService.java"),
        ("service/impl/CustomerAccountServiceImpl.java", "service/impl/CustomerAccountServiceImpl.java"),
        ("service/CustomerTicketHistoryService.java", "service/CustomerTicketHistoryService.java"),
        ("service/impl/CustomerTicketHistoryServiceImpl.java", "service/impl/CustomerTicketHistoryServiceImpl.java"),
        ("service/PaymentService.java", "service/PaymentService.java"),
        ("service/impl/PaymentServiceImpl.java", "service/impl/PaymentServiceImpl.java"),
        ("service/VoucherService.java", "service/VoucherService.java"),
        ("service/impl/VoucherServiceImpl.java", "service/impl/VoucherServiceImpl.java"),
        ("service/CargoOrderLookupService.java", "service/CargoOrderLookupService.java"),
        ("service/impl/CargoOrderLookupServiceImpl.java", "service/impl/CargoOrderLookupServiceImpl.java"),
        ("service/TransactionIdGenerator.java", "service/TransactionIdGenerator.java"),
        ("service/impl/SePayTransactionIdGeneratorImpl.java", "service/impl/SePayTransactionIdGeneratorImpl.java")
    ]

    # Service subpackages
    service_subdirs = [
        "service/passengerbooking",
        "service/passengerbooking/impl",
        "service/ticketgenerator",
        "service/ticketgenerator/impl",
        "service/notification",
        "service/notification/impl"
    ]

    controllers = [
        ("controller/CustomerController.java", "controller/CustomerController.java"),
        ("controller/CustomerAccountController.java", "controller/CustomerAccountController.java"),
        ("controller/CustomerTicketHistoryController.java", "controller/CustomerTicketHistoryController.java"),
        ("controller/PassengerBookingController.java", "controller/PassengerBookingController.java"),
        ("controller/PaymentController.java", "controller/PaymentController.java"),
        ("controller/VoucherController.java", "controller/VoucherController.java"),
        ("controller/CargoOrderLookupController.java", "controller/CargoOrderLookupController.java")
    ]

    utils = [
        ("util/PhoneNumberUtility.java", "util/PhoneNumberUtility.java"),
        ("util/EmailUtility.java", "util/EmailUtility.java"),
        ("util/FormatHandlerUtility.java", "util/FormatHandlerUtility.java"),
        ("util/PiiMaskingUtility.java", "util/PiiMaskingUtility.java"),
        ("util/RefundCallbackDataParser.java", "util/RefundCallbackDataParser.java"),
        ("util/SeatCodeUtility.java", "util/SeatCodeUtility.java")
    ]

    for src, dst in shared + models + repos + services + controllers + utils:
        copy_and_refactor(f"{MONO_DIR}/{src}", f"{CUST_DIR}/{dst}")

    for s_dir in service_subdirs:
        p = Path(f"{MONO_DIR}/{s_dir}")
        if p.exists():
            for f in p.glob("*.java"):
                copy_and_refactor(str(f), f"{CUST_DIR}/{s_dir}/{f.name}")

    # Copy all DTOs
    dto_dirs = [
        "dto/request/customer", "dto/request/passengerbooking", "dto/request/payment",
        "dto/request/voucher", "dto/request/sePay",
        "dto/response/customer", "dto/response/passengerbooking", "dto/response/payment",
        "dto/response/voucher", "dto/response/sePay",
        "dto/projection"
    ]
    for d in dto_dirs:
        d_path = Path(f"{MONO_DIR}/{d}")
        if d_path.exists():
            for f in d_path.glob("*.java"):
                copy_and_refactor(str(f), f"{CUST_DIR}/{d}/{f.name}")

    print("Customer migration complete!")

if __name__ == "__main__":
    main()
