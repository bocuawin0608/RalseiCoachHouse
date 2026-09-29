import os
import re

# Account
account_file = "staff-service/src/main/java/com/ralsei/staff/model/Account.java"
with open(account_file, "r") as f:
    content = f.read()

methods = """
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
"""
if "isCredentialsNonExpired" not in content:
    content = content.replace("public Collection<? extends GrantedAuthority> getAuthorities() {", methods + "\n    public Collection<? extends GrantedAuthority> getAuthorities() {")
with open(account_file, "w") as f:
    f.write(content)

# CargoTicketServiceImpl
cts_file = "staff-service/src/main/java/com/ralsei/staff/service/impl/CargoTicketServiceImpl.java"
with open(cts_file, "r") as f:
    content = f.read()
content = content.replace("com.ralsei.dto.response.cargoticketdetail.", "com.ralsei.staff.dto.response.cargoticketdetail.")
with open(cts_file, "w") as f:
    f.write(content)

# TripStaffPassengerServiceImpl
tspsi_file = "staff-service/src/main/java/com/ralsei/staff/service/tripstaff/impl/TripStaffPassengerServiceImpl.java"
with open(tspsi_file, "r") as f:
    content = f.read()
content = re.sub(r'PassengerTicketStaffPolicy\.canTransfer\((.*?), (.*?)\)', r'PassengerTicketStaffPolicy.canTransfer(\1, java.time.LocalDate.now())', content)
with open(tspsi_file, "w") as f:
    f.write(content)

# TripStaffRepository return type
tsr_file = "staff-service/src/main/java/com/ralsei/staff/repository/TripStaffRepository.java"
with open(tsr_file, "r") as f:
    content = f.read()
content = content.replace("java.util.List<Object> findPassengersForTrip", "java.util.List<com.ralsei.staff.dto.projection.tripstaff.PassengerBoardingProjection> findPassengersForTrip")
with open(tsr_file, "w") as f:
    f.write(content)

