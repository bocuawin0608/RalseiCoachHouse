package com.ralsei.util.mapper;

import com.ralsei.model.enums.ProvinceEnum;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class ProvinceMapper {

    // Khóa Class lại, không cho phép khởi tạo instance bừa bãi
    private ProvinceMapper() {}

    private static final Map<String, ProvinceEnum> UNACCENTED_LOOKUP = new HashMap<>();
    private static final Pattern MINECRAFT_COLOR_PATTERN = Pattern.compile("[§&][0-9a-fk-or]");
    private static final Pattern VIETNAMESE_VALID_CHARS = Pattern.compile("[^a-zA-Z\\s\\-àÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬeEèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆiIìÌỉỈĩĨíÍịỊoOòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢuUùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰyYỳỲỷỶỹỸýÝỵYđĐ]");

    static {
        for (ProvinceEnum province : ProvinceEnum.values()) {
            String unaccented = removeAccent(province.getDisplayName()).toLowerCase();
            UNACCENTED_LOOKUP.put(unaccented, province);
        }
    }

    /**
     * Hàm xử lý trung tâm: Biến đổi chuỗi "rác bẩn" thành Object Province chuẩn.
     * Trả về null nếu chuỗi vô nghĩa hoặc cố tình phá hoại.
     */
    /**
     * Executes the parse operation.
     *
     * @param input the value supplied for this operation
     *
     * @return the operation result
     */
    public static ProvinceEnum parse(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String clean = input;
        String prev;
        do {
            prev = clean;
            clean = MINECRAFT_COLOR_PATTERN.matcher(clean).replaceAll("");
        } while (!clean.equals(prev));

        clean = VIETNAMESE_VALID_CHARS.matcher(clean).replaceAll("");
        clean = clean.replaceAll("\\s+", " ").trim().toLowerCase();

        if (clean.isEmpty()) {
            return null;
        }

        String unaccentedInput = removeAccent(clean).toLowerCase();

        if (UNACCENTED_LOOKUP.containsKey(unaccentedInput)) {
            return UNACCENTED_LOOKUP.get(unaccentedInput);
        }

        ProvinceEnum bestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<String, ProvinceEnum> entry : UNACCENTED_LOOKUP.entrySet()) {
            int distance = getLevenshteinDistance(unaccentedInput, entry.getKey());
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = entry.getValue();
            }
        }

        // Ngưỡng Threshold: Sai lệch tối đa 3 ký tự
        if (minDistance <= 3 && bestMatch != null) {
            return bestMatch;
        }

        return null; // Quá sai lệch -> Rác phá hoại, từ chối nhận
    }

    private static String removeAccent(String src) {
        if (src == null) return "";
        String nfdNormalizedString = Normalizer.normalize(src, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("[đĐ]", "d");
    }

    private static int getLevenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}