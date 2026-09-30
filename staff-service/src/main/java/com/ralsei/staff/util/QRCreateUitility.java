package com.ralsei.staff.util;

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

@Component
public class QRCreateUitility {

    private static final int QR_SIZE_PIXELS = 360;

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
