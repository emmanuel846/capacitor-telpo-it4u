package com.it4u.telpo.com;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public final class ReceiptQrHelper {

    private static final int THERMAL_QR_WIDTH = 240;

    private ReceiptQrHelper() {}

    public static Bitmap decodeBase64ToBitmap(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    public static Bitmap scaleForThermalPrinter(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        if (bitmap.getWidth() <= THERMAL_QR_WIDTH) {
            return bitmap;
        }

        float ratio = (float) THERMAL_QR_WIDTH / bitmap.getWidth();
        int targetHeight = Math.max(1, Math.round(bitmap.getHeight() * ratio));

        return Bitmap.createScaledBitmap(bitmap, THERMAL_QR_WIDTH, targetHeight, false);
    }

    public static byte[] bitmapToPngBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return new byte[0];
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
