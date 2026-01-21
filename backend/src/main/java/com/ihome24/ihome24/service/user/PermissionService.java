package com.ihome24.ihome24.service.user;

import com.ihome24.ihome24.dto.response.user.PermissionListResponse;
import com.ihome24.ihome24.dto.response.user.PermissionResponse;
import com.ihome24.ihome24.entity.user.Permission;
import com.ihome24.ihome24.repository.user.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public PermissionListResponse getPermissions(String q, Integer page, Integer itemsPerPage, 
                                                 String sortBy, String orderBy) {
        Pageable pageable = createPageable(page, itemsPerPage, sortBy, orderBy);
        
        Page<Permission> permissionPage = permissionRepository.findPermissionsWithFilters(q, pageable);
        
        return PermissionListResponse.builder()
                .permissions(permissionPage.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .totalPermissions(permissionPage.getTotalElements())
                .build();
    }

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .createdAt(permission.getCreatedAt())
                .assignedTo(permission.getRoles().stream()
                        .map(role -> role.getName().toLowerCase())
                        .collect(Collectors.toList()))
                .build();
    }

    private Pageable createPageable(Integer page, Integer itemsPerPage, String sortBy, String orderBy) {
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (itemsPerPage != null && itemsPerPage > 0) ? itemsPerPage : 10;

        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(orderBy) ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, sortBy);
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
