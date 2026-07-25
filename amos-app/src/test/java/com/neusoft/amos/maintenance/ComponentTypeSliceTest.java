package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.amos.maintenance.dto.ComponentTypeCounterDefDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeMeasurePointDefDto;
import com.neusoft.amos.maintenance.dto.RegisterComponentRequest;
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
 * Component Types 切片集成测试（H2 内存库，由 @ActiveProfiles 关闭 postgres profile）。
 * 覆盖：聚合创建 / 列表过滤 / 重复 typeNumber 拒绝 / 子集合 orphanRemoval / register-component。
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class ComponentTypeSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void componentTypeSliceFlow() throws Exception {
        ComponentTypeDto dto = new ComponentTypeDto();
        dto.setTypeNumber("CT-TEST-1");
        dto.setName("Test Type");
        dto.setMaker("WART");
        dto.setClassCode("ENG");
        dto.setStatus("Active");

        ComponentTypeCounterDefDto counter = new ComponentTypeCounterDefDto();
        counter.setCode("RUN-HRS");
        counter.setDescription("Running Hours");
        counter.setUnit("h");
        counter.setSortOrder(1);
        dto.getCounters().add(counter);

        ComponentTypeMeasurePointDefDto mp = new ComponentTypeMeasurePointDefDto();
        mp.setCode("TEMP");
        mp.setDescription("Temperature");
        mp.setTrend("Up");
        mp.setUnit("C");
        mp.setSortOrder(1);
        dto.getMeasurePointDefs().add(mp);

        // 1) 创建聚合（含计数器定义 + 测点定义）
        String createdBody = mockMvc.perform(post("/api/maintenance/component-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeNumber").value("CT-TEST-1"))
                .andExpect(jsonPath("$.counters[0].code").value("RUN-HRS"))
                .andExpect(jsonPath("$.measurePointDefs[0].code").value("TEMP"))
                .andReturn().getResponse().getContentAsString();

        ComponentTypeDto created = objectMapper.readValue(createdBody, ComponentTypeDto.class);
        Long id = created.getId();

        // 2) 列表按 maker 过滤
        mockMvc.perform(get("/api/maintenance/component-types").param("maker", "WART"))
                .andExpect(status().isOk());

        // 3) 重复 typeNumber 应被拒绝（400）
        ComponentTypeDto dup = new ComponentTypeDto();
        dup.setTypeNumber("CT-TEST-1");
        dup.setName("dup");
        mockMvc.perform(post("/api/maintenance/component-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isBadRequest());

        // 4) 更新：移除测点定义，验证 orphanRemoval 删除子行（计数器保留）
        created.getMeasurePointDefs().clear();
        mockMvc.perform(put("/api/maintenance/component-types/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurePointDefs.length()").value(0))
                .andExpect(jsonPath("$.counters.length()").value(1));

        // 5) register-component 创建 Component 并复制计数器
        RegisterComponentRequest reg = new RegisterComponentRequest();
        reg.setNumber("C-TEST-1");
        reg.setName("Test Component");
        reg.setLocation("Engine Room");
        reg.setDepartment("Engine Room");
        reg.setInstallation("Traveller");
        reg.setSerialNo("SN-T1");
        mockMvc.perform(post("/api/maintenance/component-types/" + id + "/register-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("C-TEST-1"))
                .andExpect(jsonPath("$.status").value("Available"))
                .andExpect(jsonPath("$.typeNumber").value("CT-TEST-1"));

        // 清理
        mockMvc.perform(delete("/api/maintenance/component-types/" + id))
                .andExpect(status().isNoContent());
    }
}
