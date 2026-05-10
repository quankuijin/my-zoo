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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;
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
        
        if (!isAdmin(userId) || !hasPermission(userId, "ROLE_LIST")) {
            return forbiddenResponse("无权限查看角色");
        }
        
        List<Role> allRoles = roleService.list();
        int total = allRoles.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Role> pageData = allRoles.subList(Math.max(0, start), Math.max(0, end));

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
    public ResponseEntity<Map<String, Object>> listAll() {
        List<Role> allRoles = roleService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", allRoles);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "ROLE_LIST")) {
            return forbiddenResponse("无权限查看角色");
        }
        
        Role role = roleService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (role != null) {
            result.put("success", true);
            result.put("data", role);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Role role, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "ROLE_CREATE")) {
            return forbiddenResponse("无权限创建角色");
        }
        
        Map<String, Object> result = new HashMap<>();
        Role existing = roleService.getByCode(role.getCode());
        if (existing != null) {
            result.put("success", false);
            result.put("message", "角色编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        Role created = roleService.create(role);
        result.put("success", true);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody Role role, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        
        if (!isAdmin(userId) || !hasPermission(userId, "ROLE_EDIT")) {
            return forbiddenResponse("无权限编辑角色");
        }
        
        role.setId(id);
        Role updated = roleService.update(role);
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
        
        if (!isAdmin(userId) || !hasPermission(userId, "ROLE_DELETE")) {
            return forbiddenResponse("无权限删除角色");
        }
        
        boolean deleted = roleService.delete(id);
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
