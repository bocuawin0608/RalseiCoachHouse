package com.ralsei.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ralsei.dto.projection.AccountListProjection;
import com.ralsei.dto.request.account.AccountFilterRequest;
import com.ralsei.dto.response.account.AccountListResponse;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.AccountRoleRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.RoleRepository;
import com.ralsei.repository.StaffRepository;

/**
 * Unit tests covering AccountServiceImpl mapping, filtering, null-handling,
 * and AccountListProjection contract preservation.
 */
class AccountServiceImplTest {

    private AccountRepository accountRepo;
    private AccountRoleRepository accountRoleRepo;
    private RoleRepository roleRepo;
    private StaffRepository staffRepo;
    private CustomerRepository customerRepo;
    private PasswordEncoder passwordEncoder;
    private AccountServiceImpl accountService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        accountRepo = mock(AccountRepository.class);
        accountRoleRepo = mock(AccountRoleRepository.class);
        roleRepo = mock(RoleRepository.class);
        staffRepo = mock(StaffRepository.class);
        customerRepo = mock(CustomerRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        accountService = new AccountServiceImpl(
            accountRepo,
            accountRoleRepo,
            roleRepo,
            staffRepo,
            customerRepo,
            passwordEncoder
        );

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private AccountListProjection createProjection(
        Integer accountId,
        String username,
        String authProvider,
        Boolean isActive,
        String lastLogin,
        String roleNames,
        Integer staffId,
        String staffName,
        String staffPosition,
        String phone,
        String email,
        String createdAt,
        String customerName
    ) {
        AccountListProjection proj = mock(AccountListProjection.class);
        when(proj.getAccountId()).thenReturn(accountId);
        when(proj.getUsername()).thenReturn(username);
        when(proj.getAuthProvider()).thenReturn(authProvider);
        when(proj.getIsActive()).thenReturn(isActive);
        when(proj.getLastLogin()).thenReturn(lastLogin);
        when(proj.getRoleNames()).thenReturn(roleNames);
        when(proj.getStaffId()).thenReturn(staffId);
        when(proj.getStaffName()).thenReturn(staffName);
        when(proj.getStaffPosition()).thenReturn(staffPosition);
        when(proj.getPhone()).thenReturn(phone);
        when(proj.getEmail()).thenReturn(email);
        when(proj.getCreatedAt()).thenReturn(createdAt);
        when(proj.getCustomerName()).thenReturn(customerName);
        return proj;
    }

    @Test
    @DisplayName("Should map staff-linked account projection preserving all staff fields and null customerName")
    void testMapStaffAccountProjection() {
        AccountListProjection staffProj = createProjection(
            1, "staff_admin", "local", true,
            "2026-10-12 08:00:00", "ADMIN,MANAGER",
            101, "Nguyen Van A", "MANAGER",
            "0901234567", "staff.a@ralsei.com",
            "2026-10-01 10:00:00", null
        );

        when(accountRepo.findAllAccountList()).thenReturn(List.of(staffProj));

        Page<AccountListResponse> page = accountService.filterAccounts(null, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        AccountListResponse response = page.getContent().get(0);

        assertEquals(1, response.accountId());
        assertEquals("staff_admin", response.username());
        assertEquals("local", response.authProvider());
        assertTrue(response.isActive());
        assertEquals(LocalDateTime.of(2026, 10, 12, 8, 0, 0), response.lastLogin());
        assertEquals(List.of("ADMIN", "MANAGER"), response.roles());
        assertEquals(101, response.staffId());
        assertEquals("Nguyen Van A", response.staffName());
        assertEquals("MANAGER", response.staffPosition());
        assertEquals("0901234567", response.phone());
        assertEquals("staff.a@ralsei.com", response.email());
        assertEquals(LocalDateTime.of(2026, 10, 1, 10, 0, 0), response.createdAt());
        assertNull(response.customerName());
    }

    @Test
    @DisplayName("Should map customer-linked account projection preserving customerName and null staff fields")
    void testMapCustomerAccountProjection() {
        AccountListProjection customerProj = createProjection(
            2, "cust_0911", "firebase", true,
            "2026-10-11 15:30:00", "CUSTOMER",
            null, null, null,
            null, null,
            "2026-10-05 12:00:00", "Tran Thi B"
        );

        when(accountRepo.findAllAccountList()).thenReturn(List.of(customerProj));

        Page<AccountListResponse> page = accountService.filterAccounts(null, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        AccountListResponse response = page.getContent().get(0);

        assertEquals(2, response.accountId());
        assertEquals("cust_0911", response.username());
        assertEquals("firebase", response.authProvider());
        assertTrue(response.isActive());
        assertEquals(List.of("CUSTOMER"), response.roles());
        assertNull(response.staffId());
        assertNull(response.staffName());
        assertNull(response.staffPosition());
        assertNull(response.phone());
        assertNull(response.email());
        assertEquals("Tran Thi B", response.customerName());
    }

    @Test
    @DisplayName("Should gracefully handle unlinked account with null dates, null roles, and null associations")
    void testMapUnlinkedAccountWithNullFields() {
        AccountListProjection unlinkedProj = createProjection(
            3, "standalone_user", "local", null,
            null, null,
            null, null, null,
            null, null,
            null, null
        );

        when(accountRepo.findAllAccountList()).thenReturn(List.of(unlinkedProj));

        Page<AccountListResponse> page = accountService.filterAccounts(null, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        AccountListResponse response = page.getContent().get(0);

        assertEquals(3, response.accountId());
        assertEquals("standalone_user", response.username());
        assertFalse(response.isActive());
        assertNull(response.lastLogin());
        assertEquals(List.of(), response.roles());
        assertNull(response.staffId());
        assertNull(response.customerName());
        assertNull(response.createdAt());
    }

    @Test
    @DisplayName("Should handle malformed date strings without throwing exception")
    void testMalformedDateHandling() {
        AccountListProjection invalidDateProj = createProjection(
            4, "user_malformed_date", "local", true,
            "invalid-date-format", "CUSTOMER",
            null, null, null,
            null, null,
            "corrupted-timestamp", null
        );

        when(accountRepo.findAllAccountList()).thenReturn(List.of(invalidDateProj));

        Page<AccountListResponse> page = accountService.filterAccounts(null, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        AccountListResponse response = page.getContent().get(0);

        assertNull(response.lastLogin());
        assertNull(response.createdAt());
    }

    @Test
    @DisplayName("Should correctly filter by search term matching username, staffName, customerName, or role")
    void testFilterBySearchTerm() {
        AccountListProjection staff1 = createProjection(
            1, "duc_admin", "local", true, null, "ADMIN",
            10, "Doan Ngoc Duc", "ADMIN", "0123", "duc@test.com", null, null
        );
        AccountListProjection staff2 = createProjection(
            2, "driver_tom", "local", true, null, "DRIVER",
            11, "Tom Driver", "DRIVER", "0456", "tom@test.com", null, null
        );
        AccountListProjection customer = createProjection(
            3, "cust_anna", "firebase", true, null, "CUSTOMER",
            null, null, null, null, null, null, "Anna Smith"
        );

        when(accountRepo.findAllAccountList()).thenReturn(List.of(staff1, staff2, customer));

        // 1. Search matches staffName
        AccountFilterRequest searchStaff = new AccountFilterRequest("ngoc duc", null, null, null, null);
        Page<AccountListResponse> staffResult = accountService.filterAccounts(searchStaff, PageRequest.of(0, 10));
        assertEquals(1, staffResult.getTotalElements());
        assertEquals("duc_admin", staffResult.getContent().get(0).username());

        // 2. Search matches customerName
        AccountFilterRequest searchCust = new AccountFilterRequest("anna", null, null, null, null);
        Page<AccountListResponse> custResult = accountService.filterAccounts(searchCust, PageRequest.of(0, 10));
        assertEquals(1, custResult.getTotalElements());
        assertEquals("cust_anna", custResult.getContent().get(0).username());

        // 3. Search matches role
        AccountFilterRequest searchRole = new AccountFilterRequest("DRIVER", null, null, null, null);
        Page<AccountListResponse> roleResult = accountService.filterAccounts(searchRole, PageRequest.of(0, 10));
        assertEquals(1, roleResult.getTotalElements());
        assertEquals("driver_tom", roleResult.getContent().get(0).username());
    }

    @Test
    @DisplayName("Should filter by isActive, staffPosition, authProvider, and role")
    void testFilterByAttributes() {
        AccountListProjection acc1 = createProjection(
            1, "active_manager", "local", true, null, "MANAGER",
            10, "Manager Bob", "MANAGER", null, null, null, null
        );
        AccountListProjection acc2 = createProjection(
            2, "inactive_driver", "local", false, null, "DRIVER",
            11, "Driver Dan", "DRIVER", null, null, null, null
        );
        AccountListProjection acc3 = createProjection(
            3, "firebase_customer", "firebase", true, null, "CUSTOMER",
            null, null, null, null, null, null, "Cust Claire"
        );

        when(accountRepo.findAllAccountList()).thenReturn(List.of(acc1, acc2, acc3));

        // Filter by isActive = false
        AccountFilterRequest inactiveFilter = new AccountFilterRequest(null, null, false, null, null);
        Page<AccountListResponse> inactivePage = accountService.filterAccounts(inactiveFilter, PageRequest.of(0, 10));
        assertEquals(1, inactivePage.getTotalElements());
        assertEquals("inactive_driver", inactivePage.getContent().get(0).username());

        // Filter by staffPosition = MANAGER
        AccountFilterRequest posFilter = new AccountFilterRequest(null, null, null, "MANAGER", null);
        Page<AccountListResponse> posPage = accountService.filterAccounts(posFilter, PageRequest.of(0, 10));
        assertEquals(1, posPage.getTotalElements());
        assertEquals("active_manager", posPage.getContent().get(0).username());

        // Filter by authProvider = firebase
        AccountFilterRequest providerFilter = new AccountFilterRequest(null, null, null, null, "firebase");
        Page<AccountListResponse> providerPage = accountService.filterAccounts(providerFilter, PageRequest.of(0, 10));
        assertEquals(1, providerPage.getTotalElements());
        assertEquals("firebase_customer", providerPage.getContent().get(0).username());

        // Filter by role = CUSTOMER
        AccountFilterRequest roleFilter = new AccountFilterRequest(null, "CUSTOMER", null, null, null);
        Page<AccountListResponse> rolePage = accountService.filterAccounts(roleFilter, PageRequest.of(0, 10));
        assertEquals(1, rolePage.getTotalElements());
        assertEquals("firebase_customer", rolePage.getContent().get(0).username());
    }

    @Test
    @DisplayName("Should support pagination correctly")
    void testPagination() {
        AccountListProjection p1 = createProjection(1, "user1", "local", true, null, "CUSTOMER", null, null, null, null, null, null, null);
        AccountListProjection p2 = createProjection(2, "user2", "local", true, null, "CUSTOMER", null, null, null, null, null, null, null);
        AccountListProjection p3 = createProjection(3, "user3", "local", true, null, "CUSTOMER", null, null, null, null, null, null, null);

        when(accountRepo.findAllAccountList()).thenReturn(List.of(p1, p2, p3));

        Page<AccountListResponse> page0 = accountService.filterAccounts(null, PageRequest.of(0, 2));
        assertEquals(3, page0.getTotalElements());
        assertEquals(2, page0.getContent().size());
        assertEquals("user1", page0.getContent().get(0).username());
        assertEquals("user2", page0.getContent().get(1).username());

        Page<AccountListResponse> page1 = accountService.filterAccounts(null, PageRequest.of(1, 2));
        assertEquals(3, page1.getTotalElements());
        assertEquals(1, page1.getContent().size());
        assertEquals("user3", page1.getContent().get(0).username());

        Page<AccountListResponse> pageExceed = accountService.filterAccounts(null, PageRequest.of(5, 2));
        assertEquals(3, pageExceed.getTotalElements());
        assertTrue(pageExceed.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should serialize AccountListResponse with expected JSON field names")
    void testJsonSerializationContract() throws Exception {
        AccountListResponse response = new AccountListResponse(
            42,
            "test_user",
            "local",
            true,
            LocalDateTime.of(2026, 10, 12, 8, 30, 0),
            List.of("ADMIN", "STAFF"),
            99,
            "Staff Member",
            "TICKET_STAFF",
            "0987654321",
            "staff@ralsei.com",
            LocalDateTime.of(2026, 1, 1, 0, 0, 0),
            "Customer Alias"
        );

        String json = objectMapper.writeValueAsString(response);

        // Verify JSON keys match frontend contract
        assertTrue(json.contains("\"accountId\":42"));
        assertTrue(json.contains("\"username\":\"test_user\""));
        assertTrue(json.contains("\"authProvider\":\"local\""));
        assertTrue(json.contains("\"active\":true")); // @JsonProperty("active")
        assertTrue(json.contains("\"roles\":[\"ADMIN\",\"STAFF\"]"));
        assertTrue(json.contains("\"staffId\":99"));
        assertTrue(json.contains("\"staffName\":\"Staff Member\""));
        assertTrue(json.contains("\"staffPosition\":\"TICKET_STAFF\""));
        assertTrue(json.contains("\"phone\":\"0987654321\""));
        assertTrue(json.contains("\"email\":\"staff@ralsei.com\""));
        assertTrue(json.contains("\"customerName\":\"Customer Alias\""));
    }
}
