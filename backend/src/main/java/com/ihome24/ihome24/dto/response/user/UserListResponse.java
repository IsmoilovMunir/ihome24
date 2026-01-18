package com.ihome24.ihome24.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponse {
    private List<UserResponse> users;
    private Long totalUsers;
    private Integer totalPages;
    private Integer page;
}
