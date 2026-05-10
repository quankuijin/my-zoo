package com.zoo.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class JsonStorageService {

    @Getter
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    private static final String DATA_DIR = "data";

    public JsonStorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (Exception e) {
            log.error("初始化数据目录失败", e);
        }
    }

    public <T> T loadFromResources(String filename, TypeReference<T> typeRef, T defaultValue) {
        try {
            ClassPathResource resource = new ClassPathResource(filename);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream();
                     InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    return objectMapper.readValue(reader, typeRef);
                }
            }
        } catch (Exception e) {
            log.warn("从resources加载文件失败: {}", filename, e);
        }
        return defaultValue;
    }

    public <T> T loadData(String filename, TypeReference<T> typeRef, T defaultValue) {
        String cacheKey = filename;
        if (cache.containsKey(cacheKey)) {
            return (T) cache.get(cacheKey);
        }
        try {
            Path path = Paths.get(DATA_DIR, filename);
            if (Files.exists(path)) {
                try (InputStream is = Files.newInputStream(path);
                     InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    T data = objectMapper.readValue(reader, typeRef);
                    cache.put(cacheKey, data);
                    return data;
                }
            }
            return loadFromResources(filename, typeRef, defaultValue);
        } catch (Exception e) {
            log.error("加载数据文件失败: {}", filename, e);
            return defaultValue;
        }
    }

    public <T> void saveData(String filename, T data) {
        try {
            Path path = Paths.get(DATA_DIR, filename);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (OutputStream os = Files.newOutputStream(path);
                 OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                objectMapper.writeValue(writer, data);
            }
            cache.put(filename, data);
        } catch (Exception e) {
            log.error("保存数据文件失败: {}", filename, e);
        }
    }

    public void clearCache(String filename) {
        cache.remove(filename);
    }
}
