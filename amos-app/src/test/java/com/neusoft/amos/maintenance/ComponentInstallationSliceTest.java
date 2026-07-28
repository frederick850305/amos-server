package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.amos.maintenance.dto.ComponentDto;
import com.neusoft.amos.maintenance.dto.FunctionDto;
import com.neusoft.amos.maintenance.dto.InstallComponentRequest;
import com.neusoft.amos.maintenance.dto.RemoveComponentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Component Installation 切片集成测试（H2 内存库，@ActiveProfiles("test")）。
 * 覆盖模块 06 新增/补强行为：
 * - 安装 → 写 component status log（旧→In Use，reason=Installed on {fn}）
 * - 拆卸 → 写 component status log（旧→指定状态/默认 Available，reason=Removed from {fn}）
 * - 已装其他 function 的组件再安装 → 400
 * - 同组件重复装同 function → 400
 * - 对空 function 拆卸 → 400
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class ComponentInstallationSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private FunctionDto createFunction(String no) throws Exception {
        FunctionDto dto = new FunctionDto();
        dto.setFunctionNo(no);
        dto.setDescription("Function " + no);
        dto.setInstallation("Traveller");
        dto.setDepartment("ER");
        dto.setStatus("In Use");
        dto.setLocation("Engine Room");
        String body = mockMvc.perform(post("/api/maintenance/functions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, FunctionDto.class);
    }

    private ComponentDto createComponent(String number) throws Exception {
        ComponentDto dto = new ComponentDto();
        dto.setNumber(number);
        dto.setName("Comp " + number);
        dto.setStatus("Available");
        dto.setInstallation("Traveller");
        dto.setDepartment("ER");
        dto.setFunctionNo("");
        String body = mockMvc.perform(post("/api/maintenance/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, ComponentDto.class);
    }

    @Test
    void installWritesStatusLog() throws Exception {
        FunctionDto fn = createFunction("FN-CI-1");
        ComponentDto comp = createComponent("C-CI-1");

        InstallComponentRequest install = new InstallComponentRequest();
        install.setComponentNumber("C-CI-1");
        install.setDetails("Initial install");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/install-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(install)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installedComponentId").value("C-CI-1"));

        // 安装写入 status log：Available -> In Use
        mockMvc.perform(get("/api/maintenance/components/" + comp.getId() + "/status-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.newStatus == 'In Use' && @.reason == 'Installed on FN-CI-1')]").exists());

        // 清理
        RemoveComponentRequest remove = new RemoveComponentRequest();
        remove.setStatus("Available");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/remove-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remove)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/maintenance/functions/" + fn.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + comp.getId())).andExpect(status().isNoContent());
    }

    @Test
    void removeWritesStatusLog() throws Exception {
        FunctionDto fn = createFunction("FN-CI-2");
        ComponentDto comp = createComponent("C-CI-2");

        InstallComponentRequest install = new InstallComponentRequest();
        install.setComponentNumber("C-CI-2");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/install-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(install)))
                .andExpect(status().isOk());

        // 拆卸指定状态 Scrapped
        RemoveComponentRequest remove = new RemoveComponentRequest();
        remove.setNewLocation("Store Room");
        remove.setStatus("Scrapped");
        remove.setDetails("End of life");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/remove-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remove)))
                .andExpect(status().isOk());

        // 拆卸写入 status log：In Use -> Scrapped
        mockMvc.perform(get("/api/maintenance/components/" + comp.getId() + "/status-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.newStatus == 'Scrapped' && @.reason == 'Removed from FN-CI-2')]").exists());
        mockMvc.perform(get("/api/maintenance/components/" + comp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Scrapped"))
                .andExpect(jsonPath("$.location").value("Store Room"));

        // 清理
        mockMvc.perform(delete("/api/maintenance/functions/" + fn.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + comp.getId())).andExpect(status().isNoContent());
    }

    @Test
    void removeDefaultsToAvailable() throws Exception {
        FunctionDto fn = createFunction("FN-CI-3");
        ComponentDto comp = createComponent("C-CI-3");

        InstallComponentRequest install = new InstallComponentRequest();
        install.setComponentNumber("C-CI-3");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/install-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(install)))
                .andExpect(status().isOk());

        // 不指定状态，应回落 Available
        RemoveComponentRequest remove = new RemoveComponentRequest();
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/remove-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remove)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/maintenance/components/" + comp.getId() + "/status-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.newStatus == 'Available' && @.reason == 'Removed from FN-CI-3')]").exists());

        // 清理
        mockMvc.perform(delete("/api/maintenance/functions/" + fn.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + comp.getId())).andExpect(status().isNoContent());
    }

    @Test
    void installOnAlreadyInstalledComponentRejected() throws Exception {
        FunctionDto fnA = createFunction("FN-CI-A");
        FunctionDto fnB = createFunction("FN-CI-B");
        ComponentDto comp = createComponent("C-CI-X");

        InstallComponentRequest installA = new InstallComponentRequest();
        installA.setComponentNumber("C-CI-X");
        mockMvc.perform(post("/api/maintenance/functions/" + fnA.getId() + "/install-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(installA)))
                .andExpect(status().isOk());

        // 已装在 FN-CI-A，再装到 FN-CI-B -> 400
        InstallComponentRequest installB = new InstallComponentRequest();
        installB.setComponentNumber("C-CI-X");
        mockMvc.perform(post("/api/maintenance/functions/" + fnB.getId() + "/install-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(installB)))
                .andExpect(status().isBadRequest());

        // 同组件重复装同 function -> 400
        mockMvc.perform(post("/api/maintenance/functions/" + fnA.getId() + "/install-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(installA)))
                .andExpect(status().isBadRequest());

        // 清理
        RemoveComponentRequest remove = new RemoveComponentRequest();
        mockMvc.perform(post("/api/maintenance/functions/" + fnA.getId() + "/remove-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remove)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/maintenance/functions/" + fnA.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/functions/" + fnB.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + comp.getId())).andExpect(status().isNoContent());
    }

    @Test
    void removeOnEmptyFunctionRejected() throws Exception {
        FunctionDto fn = createFunction("FN-CI-E");
        ComponentDto comp = createComponent("C-CI-E");

        // 空 function 拆卸 -> 400
        RemoveComponentRequest remove = new RemoveComponentRequest();
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/remove-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remove)))
                .andExpect(status().isBadRequest());

        // 清理
        mockMvc.perform(delete("/api/maintenance/functions/" + fn.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + comp.getId())).andExpect(status().isNoContent());
    }
}
