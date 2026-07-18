/**
 * Customer-facing booking terms shown on Step 2.
 * Kept aligned with enforced cancel/change rules in the backend (customer history + staff policy).
 */
export const BOOKING_TERMS_SECTIONS = [
    {
        title: '1. Phạm vi áp dụng',
        items: [
            'Áp dụng cho hành khách đặt vé, thanh toán và sử dụng dịch vụ vận chuyển hành khách trên hệ thống.',
            'Việc tiếp tục đặt vé và thanh toán được xem là hành khách đã đọc, hiểu và đồng ý các quy định dưới đây.',
        ],
    },
    {
        title: '2. Thông tin đặt vé',
        items: [
            'Thông tin hành khách cung cấp (họ tên, số điện thoại, email, điểm đón/trả, chuyến đi) là căn cứ thực hiện vận chuyển.',
            'Hành khách chịu trách nhiệm với thiệt hại phát sinh do cung cấp sai thông tin, không nghe điện thoại, không có mặt đúng giờ, hoặc đến sai điểm đón/trả.',
        ],
    },
    {
        title: '3. Có mặt trước giờ đón',
        items: [
            'Hành khách cần có mặt tại điểm đón trước thời điểm được hệ thống hướng dẫn để làm thủ tục / trung chuyển.',
            'Xe được quyền khởi hành đúng lịch. Trường hợp đến trễ, chỗ đã giữ có thể hết hiệu lực và nhà xe không đảm bảo việc giữ ghế hoặc hoàn tiền.',
        ],
    },
    {
        title: '4. Tự hủy vé trên hệ thống',
        items: [
            'Áp dụng khi khách tự hủy trên tài khoản (lịch sử vé), với vé đã thanh toán thành công và chưa check-in.',
            'Chỉ được hủy sau ít nhất 24 giờ kể từ thời điểm đặt vé.',
            'Chỉ được hủy trước giờ xuất bến ít nhất 5 giờ.',
            'Hoàn 100% giá đã thanh toán; yêu cầu hoàn tiền ghi nhận qua chuyển khoản ngân hàng và xử lý theo quy trình nhà xe.',
        ],
    },
    {
        title: '5. Hủy vé qua nhân viên nhà xe',
        items: [
            'Áp dụng khi khách liên hệ quầy vé / nhân viên hỗ trợ hủy hộ.',
            'Chỉ áp dụng với vé đã thanh toán, chưa check-in, và đã qua ít nhất 24 giờ kể từ lúc đặt.',
            'Trước giờ xuất bến trên 5 giờ: hoàn 100% giá đã thanh toán.',
            'Trong khoảng từ 3 giờ đến 5 giờ trước giờ xuất bến: vẫn được hỗ trợ hủy nhưng chỉ hoàn 50%.',
            'Dưới 3 giờ trước giờ xuất bến: không hỗ trợ hủy.',
            'Hoàn tiền xử lý theo quy trình nhà xe.',
        ],
    },
    {
        title: '6. Chính sách đổi vé / đổi hành trình',
        items: [
            'Khách hàng liên hệ nhà xe hoặc quầy vé để được hỗ trợ đổi ghế / đổi hành trình.',
            'Chỉ hỗ trợ khi còn ít nhất 3 giờ trước giờ khởi hành của chuyến xe.',
            'Mỗi vé chỉ được thực hiện một lần thao tác đổi chuyến hoặc hủy (theo quy định vận hành).',
            'Khi đổi hành trình, giá lựa chọn mới không được cao hơn số tiền đã thanh toán.',
            'Nhà xe có quyền từ chối hỗ trợ nếu chuyến đã kín ghế, hoặc vé đã check-in.',
        ],
    },
    {
        title: '7. Hành lý đi cùng',
        items: [
            'Hành lý xách tay gọn nhẹ do hành khách tự bảo quản trong phạm vi ghế/giường hoặc vị trí nhân viên hướng dẫn.',
            'Hành lý tiêu chuẩn cần giao cho nhân viên kiểm tra, dán tem và sắp xếp lên xe theo quy trình nhà xe.',
            'Nếu hành lý quá cỡ, quá nặng, cồng kềnh, nhiều kiện phải đăng ký diện hàng hóa / hành lý ký gửi: liên hệ quầy vé để nhân viên tạo đơn trên hệ thống và thanh toán theo biểu phí hiện hành.',
            'Hành lý tự ý để lên xe, không khai báo hoặc không được nhân viên tiếp nhận được xem là tự bảo quản; nhà xe không chịu trách nhiệm nếu thất lạc hoặc hư hỏng.',
            'Không gửi tiền mặt, trang sức, hoặc hàng cấm trong hành lý ký gửi. Nhà xe có quyền từ chối tiếp nhận nếu không đủ điều kiện an toàn hoặc vượt khả năng chứa của xe.',
            'Trong trường hợp chuyến xe khách đi không đủ sức chứa hành lý ký gửi / hàng hóa của khách, nhà xe có quyền điều chuyển hành lý ký gửi / hàng hóa của khách sang chuyến khác.',
        ],
    },
    {
        title: '8. Trẻ em đi kèm',
        items: [
            'Trẻ nhỏ đi kèm cần khai báo họ tên và năm sinh khi đặt vé.',
            'Hành khách đi cùng chịu trách nhiệm về an toàn và thông tin của trẻ trong suốt hành trình.',
        ],
    },
    {
        title: '9. Từ chối vận chuyển & bất khả kháng',
        items: [
            'Nhà xe có quyền từ chối phục vụ nếu hành khách gây rối, say xỉn, đe dọa an toàn, mang hàng cấm, hoặc không tuân thủ hướng dẫn an toàn.',
            'Trong các trường hợp đó, chi phí đã thanh toán có thể không được hoàn lại.',
            'Với sự cố ngoài kiểm soát (ùn tắc, thời tiết xấu, thiên tai, quy định cơ quan nhà nước…), nhà xe được quyền điều chỉnh giờ đi, điều chuyển xe hoặc trung chuyển phù hợp thực tế vận hành.',
        ],
    },
];
