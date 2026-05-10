package com.zoo.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zoo.admin.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final JsonStorageService storageService;
    private static final String FILE_NAME = "permissions.json";

    public List<Permission> list() {
        return storageService.loadData(FILE_NAME, new TypeReference<List<Permission>>() {}, new ArrayList<>());
    }

    public Permission getById(String id) {
        return list().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Permission> getByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return list().stream()
                .filter(p -> ids.contains(p.getId()))
                .collect(Collectors.toList());
    }

    public Permission create(Permission permission) {
        List<Permission> permissions = list();
        permission.setId(UUID.randomUUID().toString());
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permissions.add(permission);
        storageService.saveData(FILE_NAME, permissions);
        return permission;
    }

    public Permission update(Permission permission) {
        List<Permission> permissions = list();
        for (int i = 0; i < permissions.size(); i++) {
            if (permissions.get(i).getId().equals(permission.getId())) {
                permission.setUpdatedAt(LocalDateTime.now());
                permissions.set(i, permission);
                storageService.saveData(FILE_NAME, permissions);
                return permission;
            }
        }
        return null;
    }

    public boolean delete(String id) {
        List<Permission> permissions = list();
        boolean removed = permissions.removeIf(p -> p.getId().equals(id));
        if (removed) {
            storageService.saveData(FILE_NAME, permissions);
        }
        return removed;
    }
}
