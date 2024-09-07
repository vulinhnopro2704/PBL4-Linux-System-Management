package com.clientapp.util;

public interface IConvertData {
    /**
     * Converts bytes to megabytes (MB).
     *
     * @param bytes The number of bytes.
     * @return The equivalent number of megabytes.
     */
    public Long bytesToMB(Long bytes);

    /**
     * Converts bytes to gigabytes (GB) with two decimal places.
     *
     * @param bytes The number of bytes.
     * @return The equivalent number of gigabytes as a formatted string.
     */
    public Long bytesToGB(Long bytes);
}
