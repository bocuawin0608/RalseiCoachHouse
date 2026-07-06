package com.ralsei.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Converts an existing boarding-security token into a PNG QR image.
 *
 * <p>The utility deliberately does not create or persist tokens. Token lifecycle
 * belongs to the payment service, while this class has the single responsibility
 * of rendering a token for an authorized customer.</p>
 */
@Component
public class QRCreateUitility {

    private static final int QR_SIZE_PIXELS = 360;

    /**
     * Renders a boarding token as a high-error-correction PNG image.
     *
     * @param boardingToken opaque token stored on the passenger ticket detail
     * @return PNG file contents
     * @throws IllegalArgumentException when the persisted token is missing
     * @throws IllegalStateException when the QR encoder cannot render the token
     */
    public byte[] createPng(String boardingToken) {
        if (boardingToken == null || boardingToken.isBlank()) {
            throw new IllegalArgumentException("Vé chưa có mã QR hợp lệ.");
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.CHARACTER_SET, "UTF-8",
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = new QRCodeWriter().encode(
                boardingToken,
                BarcodeFormat.QR_CODE,
                QR_SIZE_PIXELS,
                QR_SIZE_PIXELS,
                hints
            );
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Không thể tạo ảnh QR cho vé.", exception);
        }
    }
}
