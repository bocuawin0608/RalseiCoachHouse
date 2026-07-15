package com.ralsei.service.staffrefund;

import java.time.LocalDate;

import com.ralsei.dto.request.staffrefund.StaffRefundCompleteRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.staffrefund.StaffRefundDetailResponse;
import com.ralsei.dto.response.staffrefund.StaffRefundListItemResponse;

/**
 * Manager workflows for reviewing and completing passenger refund requests.
 */
/**
 * Provides the business service contract for staff refund.
 */
public interface StaffRefundService {

    /**
     * Searches passenger refunds with newest requests first.
     *
     * @param status optional refund status filter
     * @param ticketCode optional ticket code prefix filter
     * @param phone optional customer phone filter
     * @param createdFrom optional inclusive created-date lower bound
     * @param createdTo optional inclusive created-date upper bound
     * @param page zero-based page index
     * @param size page size
     * @return paginated refund list items
     */
    PagedResponse<StaffRefundListItemResponse> searchPassengerRefunds(
        String status,
        String ticketCode,
        String phone,
        LocalDate createdFrom,
        LocalDate createdTo,
        int page,
        int size
    );

    /**
     * Loads one passenger refund for manager review.
     *
     * @param refundId refund identifier
     * @return resolved refund detail
     */
    StaffRefundDetailResponse getPassengerRefundDetail(int refundId);

    /**
     * Confirms that a pending passenger refund was paid out and notifies the customer.
     *
     * @param accountId authenticated manager account identifier
     * @param refundId refund identifier
     * @param request payout confirmation payload
     * @return updated refund detail
     */
    StaffRefundDetailResponse completePassengerRefund(
        Integer accountId,
        int refundId,
        StaffRefundCompleteRequest request
    );
}
