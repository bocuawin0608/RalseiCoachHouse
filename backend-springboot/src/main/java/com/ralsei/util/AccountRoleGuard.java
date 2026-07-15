package com.ralsei.util;

import java.util.Collection;
import java.util.Set;

import com.ralsei.exception.BusinessRuleException;

/**
 * Guards role assignment so customer accounts and staff accounts stay on their own side.
 * <ul>
 *   <li>Customer → only {@code CUSTOMER}</li>
 *   <li>Staff (ADMIN / MANAGER / TICKET_STAFF / TRIP_STAFF) → only those staff roles</li>
 * </ul>
 */
public final class AccountRoleGuard {

    public static final String CUSTOMER_ROLE = "CUSTOMER";

    public static final Set<String> STAFF_ROLES = Set.of(
        "ADMIN",
        "MANAGER",
        "TICKET_STAFF",
        "TRIP_STAFF"
    );

    private enum AccountSide {
        CUSTOMER,
        STAFF,
        UNKNOWN
    }

    private AccountRoleGuard() {
    }

    /**
     * Returns whether the staff role is active.
     *
     * @param roleName the value supplied for this operation
     *
     * @return {@code true} if the staff role is active; otherwise {@code false}
     */
    public static boolean isStaffRole(String roleName) {
        return roleName != null && STAFF_ROLES.contains(roleName);
    }

    /**
     * Returns whether the customer role is active.
     *
     * @param roleName the value supplied for this operation
     *
     * @return {@code true} if the customer role is active; otherwise {@code false}
     */
    public static boolean isCustomerRole(String roleName) {
        return CUSTOMER_ROLE.equals(roleName);
    }

    /**
     * Validates that {@code newRoleNames} are allowed for the target account.
     *
     * @param linkedToCustomer whether a customer profile is linked to the account
     * @param linkedToStaff    whether a staff profile is linked to the account
     * @param currentRoleNames roles currently assigned (fallback when no profile link)
     * @param newRoleNames     roles about to be assigned
     */
    public static void validateRoleAssignment(
            boolean linkedToCustomer,
            boolean linkedToStaff,
            Collection<String> currentRoleNames,
            Collection<String> newRoleNames
    ) {
        if (linkedToCustomer && linkedToStaff) {
            throw new BusinessRuleException(
                "Tài khoản đang liên kết cả khách hàng và nhân viên — không thể phân quyền."
            );
        }

        if (newRoleNames == null || newRoleNames.isEmpty()) {
            return;
        }

        boolean assigningCustomer = newRoleNames.stream().anyMatch(AccountRoleGuard::isCustomerRole);
        boolean assigningStaff = newRoleNames.stream().anyMatch(AccountRoleGuard::isStaffRole);
        boolean assigningUnknown = newRoleNames.stream()
            .anyMatch(name -> !isCustomerRole(name) && !isStaffRole(name));

        if (assigningUnknown) {
            throw new BusinessRuleException("Role không hợp lệ trong hệ thống!");
        }

        if (assigningCustomer && assigningStaff) {
            throw new BusinessRuleException(
                "Không thể gán đồng thời role khách hàng và role nhân viên!"
            );
        }

        AccountSide side = resolveSide(linkedToCustomer, linkedToStaff, currentRoleNames);

        if (side == AccountSide.CUSTOMER && assigningStaff) {
            throw new BusinessRuleException(
                "Tài khoản khách hàng chỉ được giữ role CUSTOMER, không được đổi sang role nhân viên!"
            );
        }

        if (side == AccountSide.STAFF && assigningCustomer) {
            throw new BusinessRuleException(
                "Tài khoản nhân viên chỉ được đổi trong các role nội bộ (ADMIN, MANAGER, TICKET_STAFF, TRIP_STAFF), không được đổi sang CUSTOMER!"
            );
        }

        if (side == AccountSide.CUSTOMER) {
            boolean onlyCustomer = newRoleNames.stream().allMatch(AccountRoleGuard::isCustomerRole);
            if (!onlyCustomer) {
                throw new BusinessRuleException(
                    "Tài khoản khách hàng chỉ được giữ role CUSTOMER!"
                );
            }
        }

        if (side == AccountSide.STAFF) {
            boolean onlyStaff = newRoleNames.stream().allMatch(AccountRoleGuard::isStaffRole);
            if (!onlyStaff) {
                throw new BusinessRuleException(
                    "Tài khoản nhân viên chỉ được gán các role: ADMIN, MANAGER, TICKET_STAFF, TRIP_STAFF!"
                );
            }
        }
    }

    /** Staff onboard/update may only assign internal staff roles. */
    public static void validateStaffOnlyRoles(Collection<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new BusinessRuleException("Phải chọn ít nhất một role nhân viên!");
        }
        boolean onlyStaff = roleNames.stream().allMatch(AccountRoleGuard::isStaffRole);
        if (!onlyStaff) {
            throw new BusinessRuleException(
                "Nhân viên chỉ được gán các role: ADMIN, MANAGER, TICKET_STAFF, TRIP_STAFF!"
            );
        }
    }

    private static AccountSide resolveSide(
            boolean linkedToCustomer,
            boolean linkedToStaff,
            Collection<String> currentRoleNames
    ) {
        if (linkedToCustomer) {
            return AccountSide.CUSTOMER;
        }
        if (linkedToStaff) {
            return AccountSide.STAFF;
        }

        if (currentRoleNames != null) {
            boolean hasStaff = currentRoleNames.stream().anyMatch(AccountRoleGuard::isStaffRole);
            boolean hasCustomer = currentRoleNames.stream().anyMatch(AccountRoleGuard::isCustomerRole);
            if (hasStaff && !hasCustomer) {
                return AccountSide.STAFF;
            }
            if (hasCustomer && !hasStaff) {
                return AccountSide.CUSTOMER;
            }
        }

        return AccountSide.UNKNOWN;
    }
}
