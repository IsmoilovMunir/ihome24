package com.ihome24.ihome24.controller.admin.user;

import com.ihome24.ihome24.dto.request.user.ChangePasswordRequest;
import com.ihome24.ihome24.dto.request.user.UserRequest;
import com.ihome24.ihome24.dto.response.user.UserListResponse;
import com.ihome24.ihome24.dto.response.user.UserResponse;
import com.ihome24.ihome24.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/apps/users")
@RequiredArgsConstructor
public class AdminUserRestController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserListResponse> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy
    ) {
        UserListResponse response = userService.getUsers(q, role, plan, status, page, itemsPerPage, sortBy, orderBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, 
                                                    @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Изменение пароля пользователя
     * Отдельный endpoint для изменения только пароля без необходимости передавать все поля
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> changeUserPassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeUserPassword(id, request.getPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
