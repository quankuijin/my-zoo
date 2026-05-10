package com.zoo.admin.service;

import com.zoo.admin.entity.Permission;
import com.zoo.admin.entity.Role;
import com.zoo.admin.entity.User;
import com.zoo.admin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(String username, String password) {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!user.getEnabled()) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", buildUserInfo(user));
        result.put("roles", getUserRoles(user));
        result.put("permissions", getUserPermissions(user));

        return result;
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("name", user.getName());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        return info;
    }

    private List<String> getUserRoles(User user) {
        List<String> roleCodes = new ArrayList<>();
        if (user.getRoleIds() == null || user.getRoleIds().isEmpty()) {
            return roleCodes;
        }
        for (String roleId : user.getRoleIds()) {
            Role role = roleService.getById(roleId);
            if (role != null) {
                roleCodes.add(role.getCode());
            }
        }
        return roleCodes;
    }

    private List<String> getUserPermissions(User user) {
        Set<String> permissionCodes = new HashSet<>();
        if (user.getRoleIds() == null || user.getRoleIds().isEmpty()) {
            return new ArrayList<>(permissionCodes);
        }
        for (String roleId : user.getRoleIds()) {
            Role role = roleService.getById(roleId);
            if (role != null && role.getPermissionIds() != null) {
                List<Permission> perms = permissionService.getByIds(role.getPermissionIds());
                for (Permission perm : perms) {
                    permissionCodes.add(perm.getCode());
                }
            }
        }
        return new ArrayList<>(permissionCodes);
    }
}
