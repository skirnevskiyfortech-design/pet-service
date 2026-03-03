package com.example.petservice.dto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record FileData(
        String fileName,
        String contentType,
        long size,
        byte[] data
) {
    public InputStream toInputStream() {
        return new ByteArrayInputStream(data);
    }
}