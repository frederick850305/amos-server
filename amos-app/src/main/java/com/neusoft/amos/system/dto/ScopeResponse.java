package com.neusoft.amos.system.dto;

import java.util.List;

public record ScopeResponse(List<ScopeInstallation> installations) {

    public record ScopeDepartment(String code, String name) {
    }

    public record ScopeInstallation(String code, String name, List<ScopeDepartment> departments) {
    }
}
