package org.example.devopslearning.services;

import java.io.InputStream;

public interface S3Service {
    String upload(InputStream data, String key) throws Exception;
}
