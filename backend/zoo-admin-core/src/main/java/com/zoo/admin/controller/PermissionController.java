package com.zoo.admin.controller;

import com.zoo.admin.entity.Permission;
import com.zoo.admin.entity.Role;
import com.zoo.admin.entity.User;
import com.zoo.admin.service.PermissionService;
import com.zoo.admin.service.RoleService;
import com.zoo.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PermissionController {

    private final PermissionService permissionService;
    private final UserService userService;
    private final RoleService roleService;

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
        
        if (!isAdmin(userId) || !hasPermission(userId, "PERMISSION_LIST")) {
            return forbiddenResponse("无权限查看权限");
        }
        
        List<Permission> allPerms = permissionService.list();
        int total = allPerms.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Permission> pageData = allPerms.subList(Math.max(0, start), Math.max(0, end));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", pageData);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", total);
        result.put("totalPages", totalPages);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> listAll(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "PERMISSION_LIST")) {
            return forbiddenResponse("无权限查看权限");
        }
        
        List<Permission> allPerms = permissionService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", allPerms);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "PERMISSION_LIST")) {
            return forbiddenResponse("无权限查看权限");
        }
        
        Permission permission = permissionService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (permission != null) {
            result.put("success", true);
            result.put("data", permission);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Permission permission, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "PERMISSION_CREATE")) {
            return forbiddenResponse("无权限创建权限");
        }
        
        Map<String, Object> result = new HashMap<>();
        Permission created = permissionService.create(permission);
        result.put("success", true);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody Permission permission, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "PERMISSION_EDIT")) {
            return forbiddenResponse("无权限编辑权限");
        }
        
        permission.setId(id);
        Permission updated = permissionService.update(permission);
        Map<String, Object> result = new HashMap<>();
        if (updated != null) {
            result.put("success", true);
            result.put("data", updated);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "PERMISSION_DELETE")) {
            return forbiddenResponse("无权限删除权限");
        }
        
        boolean deleted = permissionService.delete(id);
        Map<String, Object> result = new HashMap<>();
        if (deleted) {
            result.put("success", true);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
