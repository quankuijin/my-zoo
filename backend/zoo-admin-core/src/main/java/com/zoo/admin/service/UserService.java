package com.zoo.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zoo.admin.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JsonStorageService storageService;
    private static final String FILE_NAME = "users.json";

    public List<User> list() {
        return storageService.loadData(FILE_NAME, new TypeReference<List<User>>() {}, new ArrayList<>());
    }

    public User getById(String id) {
        return list().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public User getByUsername(String username) {
        return list().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public User create(User user) {
        List<User> users = list();
        user.setId(UUID.randomUUID().toString());
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        users.add(user);
        storageService.saveData(FILE_NAME, users);
        return user;
    }

    public User update(User user) {
        List<User> users = list();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                user.setUpdatedAt(LocalDateTime.now());
                users.set(i, user);
                storageService.saveData(FILE_NAME, users);
                return user;
            }
        }
        return null;
    }

    public boolean delete(String id) {
        List<User> users = list();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) {
            storageService.saveData(FILE_NAME, users);
        }
        return removed;
    }
}
