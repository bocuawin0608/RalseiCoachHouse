package com.ralsei.util;

import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
@UtilityClass
//WORK IN PROGRESS 
/**
 * Provides utility helpers for format handler uti processing.
 */
public class FormatHandlerUtility {
    /**
     * Định dạng lại chuỗi đầu vào: loại bỏ khoảng trắng thừa, chuẩn hóa chữ hoa chữ thường, và đảm bảo dấu gạch ngang có khoảng trắng xung quanh
     * @param input Chuỗi đầu vào cần định dạng
     * @return Chuỗi đã được định dạng sạch sẽ, chuẩn hóa cho việc tìm kiếm và hiển thị
     */
    private static final Pattern MINECRAFT_COLOR_PATTERN = Pattern.compile("[§&][0-9a-fk-or]");
    private static final Pattern VIETNAMESE_CHARS_EXTENDED = Pattern.compile("[^a-zA-Z\\s\\-àÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬeEèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆiIìÌỉỈĩĨíÍịỊoOòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢuUùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰyYỳỲỷỶỹỸýÝỵYđĐ]");
    private static final Pattern MUST_HAVE_ACCENT_OR_DD = Pattern.compile("[àÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬeEèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆiIìÌỉỈĩĨíÍịỊoOòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢuUùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰyYỳỲỷỶỹỸýÝỵYđĐ]");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");
    private static final Pattern HYPHEN_SPACES_PATTERN = Pattern.compile("\\s*-\\s*");

    /**
     * Executes the format province name operation.
     *
     * @param input the value supplied for this operation
     *
     * @return the operation result
     */
    public static String formatProvinceName(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // 1. Dọn mã màu Minecraft bằng vòng lặp để tránh mã lồng nhau (&&bb)
        String cleanStr = input;
        String prev;
        do {
            prev = cleanStr;
            cleanStr = MINECRAFT_COLOR_PATTERN.matcher(cleanStr).replaceAll("");
        } while (!cleanStr.equals(prev));

        // 2. Xóa sạch mọi ký tự lạ ngoài hệ Latinh (Ả Rập, Do Thái, Emoji, Số, Ký tự đặc biệt)
        cleanStr = VIETNAMESE_CHARS_EXTENDED.matcher(cleanStr).replaceAll("");

        // 3. Tách chuỗi thành các cụm từ dựa trên khoảng trắng và dấu gạch ngang để thẩm định
        // Dùng kỹ thuật Regex Lookaround để giữ lại dấu gạch ngang khi split
        String[] tokens = cleanStr.split("(?<=\\s)|(?=\\s)|(?<=-)|(?=-)");
        StringBuilder validContent = new StringBuilder();

        for (String token : tokens) {
            String trimmedToken = token.trim();
            
            // Nếu là khoảng trắng hoặc dấu gạch ngang thì giữ lại làm cấu trúc
            if (token.equals("-") || token.isBlank()) {
                validContent.append(token);
                continue;
            }

            // Nếu là từ, bắt buộc phải có dấu tiếng Việt hoặc chữ Đ. Nếu không có -> Xóa sạch (Ngoại lai!)
            if (MUST_HAVE_ACCENT_OR_DD.matcher(trimmedToken).find()) {
                // Viết hoa chữ cái đầu tiên của từ hợp lệ
                String capitalized = trimmedToken.substring(0, 1).toUpperCase() + trimmedToken.substring(1).toLowerCase();
                validContent.append(capitalized);
            }
        }

        // 4. Chuẩn hóa khoảng trắng toàn cục và định dạng dấu gạch ngang
        String result = MULTIPLE_SPACES_PATTERN.matcher(validContent.toString()).replaceAll(" ").trim();
        result = HYPHEN_SPACES_PATTERN.matcher(result).replaceAll(" - ").trim();

        // Xử lý trường hợp sau khi xóa hết rác chỉ còn lại dấu gạch ngang trơ trọi "-"
        if (result.equals("-")) {
            return "";
        }

        return result;
    }
    String handleRouteInput(String route) {
        return formatProvinceName(route);
    }
}
