package com.zoo.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zoo.admin.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final JsonStorageService storageService;
    private static final String FILE_NAME = "roles.json";

    public List<Role> list() {
        return storageService.loadData(FILE_NAME, new TypeReference<List<Role>>() {}, new ArrayList<>());
    }

    public Role getById(String id) {
        return list().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Role getByCode(String code) {
        return list().stream()
                .filter(r -> r.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public Role create(Role role) {
        List<Role> roles = list();
        role.setId(UUID.randomUUID().toString());
        role.setEnabled(true);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roles.add(role);
        storageService.saveData(FILE_NAME, roles);
        return role;
    }

    public Role update(Role role) {
        List<Role> roles = list();
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getId().equals(role.getId())) {
                role.setUpdatedAt(LocalDateTime.now());
                roles.set(i, role);
                storageService.saveData(FILE_NAME, roles);
                return role;
            }
        }
        return null;
    }

    public boolean delete(String id) {
        List<Role> roles = list();
        boolean removed = roles.removeIf(r -> r.getId().equals(id));
        if (removed) {
            storageService.saveData(FILE_NAME, roles);
        }
        return removed;
    }
}
