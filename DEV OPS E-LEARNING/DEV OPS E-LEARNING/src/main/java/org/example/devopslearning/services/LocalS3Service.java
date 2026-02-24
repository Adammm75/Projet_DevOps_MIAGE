package org.example.devopslearning.services;

import org.example.devopslearning.config.AppConfig;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalS3Service implements S3Service {
    private final AppConfig appConfig;

    public LocalS3Service(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public String upload(InputStream data, String key) throws Exception {
        Path base = Path.of(appConfig.getBaseDir());
        Files.createDirectories(base);
        String filename = (key == null || key.isBlank()) ? UUID.randomUUID().toString() + ".bin"
                : key.replaceAll("[/\\\\]+", "_");
        Path dest = base.resolve(filename);
        try (FileOutputStream fos = new FileOutputStream(dest.toFile())) {
            data.transferTo(fos);
        }
        return dest.toAbsolutePath().toString();
    }
}
