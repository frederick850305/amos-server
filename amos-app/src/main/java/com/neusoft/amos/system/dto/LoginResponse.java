package com.neusoft.amos.system.dto;

import java.util.List;

public record LoginResponse(String token, String username, String displayName, List<RoleDto> roles, ScopeResponse scopes) {
}
