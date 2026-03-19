package com.ihome24.ihome24.service.user;

import com.ihome24.ihome24.dto.response.user.RoleResponse;
import com.ihome24.ihome24.dto.request.user.RoleUpsertRequest;
import com.ihome24.ihome24.entity.user.Permission;
import com.ihome24.ihome24.entity.user.Role;
import com.ihome24.ihome24.repository.user.PermissionRepository;
import com.ihome24.ihome24.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse createRole(RoleUpsertRequest request) {
        String name = normalizeName(request.getName());
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists: " + name);
        }

        Role role = Role.builder()
                .name(name)
                .displayName(normalizeDisplayName(request.getDisplayName(), name))
                .build();

        role.setPermissions(resolvePermissions(request.getPermissions()));
        role = roleRepository.save(role);

        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleUpsertRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        String name = normalizeName(request.getName());
        if (!name.equals(role.getName()) && roleRepository.existsByName(name)) {
            throw new RuntimeException("Role already exists: " + name);
        }

        role.setName(name);
        role.setDisplayName(normalizeDisplayName(request.getDisplayName(), name));
        role.setPermissions(resolvePermissions(request.getPermissions()));

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new RuntimeException("Role has users assigned and cannot be deleted");
        }
        roleRepository.delete(role);
    }

    private String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName, String fallbackName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return fallbackName;
        }
        return displayName.trim();
    }

    private List<Permission> resolvePermissions(List<String> permissionNames) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return List.of();
        }
        List<String> normalized = permissionNames.stream()
                .filter(p -> p != null && !p.isBlank())
                .map(String::trim)
                .toList();
        List<Permission> found = permissionRepository.findByNameIn(normalized);
        if (found.size() != normalized.size()) {
            throw new RuntimeException("Some permissions not found");
        }
        return found;
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .createdAt(role.getCreatedAt())
                .permissions(role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toList()))
                .userCount(role.getUsers() != null ? role.getUsers().size() : 0)
                .build();
    }
}
