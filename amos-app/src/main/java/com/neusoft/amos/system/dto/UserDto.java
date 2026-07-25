package com.neusoft.amos.system.dto;

import java.util.List;

public record UserDto(Long id, String username, String displayName, String status, List<RoleDto> roles) {
}
