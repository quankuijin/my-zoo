package com.zoo.admin.controller;

import com.zoo.admin.entity.Permission;
import com.zoo.admin.entity.Role;
import com.zoo.admin.entity.User;
import com.zoo.admin.service.PermissionService;
import com.zoo.admin.service.RoleService;
import com.zoo.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;

    private boolean isAdmin(String userId) {
        User user = userService.getById(userId);
        if (user == null || user.getRoleIds() == null) {
            return false;
        }
        for (String roleId : user.getRoleIds()) {
            Role role = roleService.getById(roleId);
            if (role != null && "ADMIN".equals(role.getCode())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermission(String userId, String permissionCode) {
        User user = userService.getById(userId);
        if (user == null || user.getRoleIds() == null) {
            return false;
        }
        for (String roleId : user.getRoleIds()) {
            Role role = roleService.getById(roleId);
            if (role != null && role.getPermissionIds() != null) {
                List<Permission> perms = permissionService.getByIds(role.getPermissionIds());
                for (Permission perm : perms) {
                    if (permissionCode.equals(perm.getCode())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ResponseEntity<Map<String, Object>> forbiddenResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return ResponseEntity.status(403).body(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        Map<String, Object> result = new HashMap<>();
        List<User> allUsers;
        
        if (isAdmin(userId)) {
            if (!hasPermission(userId, "USER_LIST")) {
                return forbiddenResponse("无权限查看用户列表");
            }
            allUsers = userService.list();
        } else {
            if (!hasPermission(userId, "USER_LIST")) {
                return forbiddenResponse("无权限查看用户");
            }
            User currentUser = userService.getById(userId);
            allUsers = currentUser != null ? Collections.singletonList(currentUser) : new ArrayList<>();
        }
        
        int total = allUsers.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<User> pageData = allUsers.subList(Math.max(0, start), Math.max(0, end));

        result.put("success", true);
        result.put("data", pageData);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", total);
        result.put("totalPages", totalPages);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) && !id.equals(userId)) {
            return forbiddenResponse("无权限查看其他用户信息");
        }
        
        User user = userService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("success", true);
            result.put("data", user);
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody User user, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "USER_CREATE")) {
            return forbiddenResponse("无权限创建用户");
        }
        
        Map<String, Object> result = new HashMap<>();
        User existing = userService.getByUsername(user.getUsername());
        if (existing != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(result);
        }
        User created = userService.create(user);
        result.put("success", true);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody User user, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId)) {
            if (!id.equals(userId)) {
                return forbiddenResponse("无权限修改其他用户信息");
            }
            if (!hasPermission(userId, "USER_EDIT")) {
                return forbiddenResponse("无权限修改用户");
            }
            user.setRoleIds(null);
        } else {
            if (!hasPermission(userId, "USER_EDIT")) {
                return forbiddenResponse("无权限修改用户");
            }
        }
        
        user.setId(id);
        Map<String, Object> result = new HashMap<>();
        User updated = userService.update(user);
        if (updated != null) {
            result.put("success", true);
            result.put("data", updated);
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "USER_DELETE")) {
            return forbiddenResponse("无权限删除用户");
        }
        
        Map<String, Object> result = new HashMap<>();
        boolean deleted = userService.delete(id);
        if (deleted) {
            result.put("success", true);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
    }
}
