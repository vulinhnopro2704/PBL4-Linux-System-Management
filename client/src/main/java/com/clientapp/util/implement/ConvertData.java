package com.clientapp.util.implement;

import com.clientapp.util.IConvertData;

public class ConvertData implements IConvertData {
    /**
     * Converts bytes to megabytes (MB).
     *
     * @param bytes The number of bytes.
     * @return The equivalent number of megabytes.
     */
    public Long bytesToMB(Long bytes) {
        return bytes / (1024 * 1024);
    }

    /**
     * Converts bytes to gigabytes (GB) with two decimal places.
     *
     * @param bytes The number of bytes.
     * @return The equivalent number of gigabytes as a formatted string.
     */
    public Long bytesToGB(Long bytes) {
        return (long) (bytes / (double) (1024 * 1024 * 1024));
    }
}
