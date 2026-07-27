package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.amos.maintenance.dto.ChangeStatusRequest;
import com.neusoft.amos.maintenance.dto.ComponentDto;
import com.neusoft.amos.maintenance.dto.FunctionCounterDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Functions 切片集成测试（H2 内存库，@ActiveProfiles("test") 关闭 postgres profile）。
 * 覆盖：聚合创建 / 列表过滤 / 计数器 id 复用 / change-status（空 function 允许、装有组件拒绝）/
 * install-component 联动组件 / remove-component 联动组件 / rotation-log。
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class FunctionSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private FunctionDto createFunction(String no, String desc, String installation, String department,
                                       String parent, String criticality) throws Exception {
        FunctionDto dto = new FunctionDto();
        dto.setFunctionNo(no);
        dto.setDescription(desc);
        dto.setInstallation(installation);
        dto.setDepartment(department);
        dto.setParentFunctionNo(parent);
        dto.setCriticality(criticality);
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
    void functionSliceFlow() throws Exception {
        // 1) 创建功能位置（含计数器）
        FunctionCounterDto counter = new FunctionCounterDto();
        counter.setCode("RH");
        counter.setDescription("Running Hours");
        counter.setUnit("h");
        counter.setLastValue(java.math.BigDecimal.valueOf(48230));
        FunctionDto fn = createFunction("FN-SLICE-1", "Main Engine", "Traveller", "ER", "", "High");
        fn.getFunctionCounters().add(counter);
        String updated = mockMvc.perform(put("/api/maintenance/functions/" + fn.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.functionCounters[0].id").exists())
                .andExpect(jsonPath("$.functionCounters[0].code").value("RH"))
                .andReturn().getResponse().getContentAsString();
        fn = objectMapper.readValue(updated, FunctionDto.class);

        // 2) 子功能位置（parentFunctionNo = FN-SLICE-1）
        FunctionDto child = createFunction("FN-SLICE-2", "Aux Boiler", "Traveller", "ER", "FN-SLICE-1", "High");

        // 3) 列表过滤
        mockMvc.perform(get("/api/maintenance/functions").param("installation", "Traveller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.functionNo == 'FN-SLICE-1')]").exists());
        mockMvc.perform(get("/api/maintenance/functions").param("criticality", "High"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/maintenance/functions").param("q", "Main"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/maintenance/functions").param("parentFunctionNo", "FN-SLICE-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.functionNo == 'FN-SLICE-2')]").exists());

        // 4) change-status：空 function 允许；级联仅作用于无组件的子节点
        ChangeStatusRequest csr = new ChangeStatusRequest();
        csr.setNewStatus("Scrapped");
        csr.setCascadeSubFunctions(true);
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(csr)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        mockMvc.perform(get("/api/maintenance/functions/" + fn.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Scrapped"));
        // 复位，便于安装
        ChangeStatusRequest reset = new ChangeStatusRequest();
        reset.setNewStatus("In Use");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reset)))
                .andExpect(status().isOk());

        // 5) 安装组件 → 联动 component 状态 / 写轮次与历史
        ComponentDto comp = createComponent("C-SLICE-1");
        InstallComponentRequest install = new InstallComponentRequest();
        install.setComponentNumber("C-SLICE-1");
        install.setDetails("Initial install");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/install-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(install)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installedComponentId").value("C-SLICE-1"));
        // component 状态联动
        mockMvc.perform(get("/api/maintenance/components/" + comp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.functionNo").value("FN-SLICE-1"))
                .andExpect(jsonPath("$.status").value("In Use"))
                .andExpect(jsonPath("$.location").value("Engine Room"));
        // rotation-log 含 Installed
        mockMvc.perform(get("/api/maintenance/functions/" + fn.getId() + "/rotation-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action == 'Installed')]").exists());

        // 6) 装有组件 → change-status 拒绝（400）
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(csr)))
                .andExpect(status().isBadRequest());

        // 7) 拆卸组件 → 联动回退 + 写 Removed
        RemoveComponentRequest remove = new RemoveComponentRequest();
        remove.setNewLocation("Store Room");
        remove.setStatus("Scrapped");
        remove.setDetails("End of life");
        mockMvc.perform(post("/api/maintenance/functions/" + fn.getId() + "/remove-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remove)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installedComponentId").isEmpty());
        mockMvc.perform(get("/api/maintenance/components/" + comp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.functionNo").isEmpty())
                .andExpect(jsonPath("$.status").value("Scrapped"))
                .andExpect(jsonPath("$.location").value("Store Room"));
        mockMvc.perform(get("/api/maintenance/functions/" + fn.getId() + "/rotation-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action == 'Removed')]").exists());

        // 8) 清理
        mockMvc.perform(delete("/api/maintenance/functions/" + child.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/functions/" + fn.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + comp.getId()))
                .andExpect(status().isNoContent());
    }
}
