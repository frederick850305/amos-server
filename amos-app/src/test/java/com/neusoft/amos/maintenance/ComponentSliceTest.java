package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.amos.maintenance.dto.ChangeStatusRequest;
import com.neusoft.amos.maintenance.dto.ComponentCounterDto;
import com.neusoft.amos.maintenance.dto.ComponentDto;
import com.neusoft.amos.maintenance.dto.ComponentMeasurePointDto;
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
 * Components 切片集成测试（H2 内存库，@ActiveProfiles("test") 关闭 postgres profile）。
 * 覆盖：聚合创建 / 列表过滤 / 子表 id 复用 / change-status 写日志 / status-log 查询 / hierarchy 树。
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class ComponentSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void componentSliceFlow() throws Exception {
        // 1) 创建组件（含计数器 + 测点）
        ComponentDto dto = new ComponentDto();
        dto.setNumber("C-FLOW-1");
        dto.setTypeNumber("CT-ENG-1");
        dto.setName("Main Engine");
        dto.setStatus("Available");
        dto.setMaker("WART");
        dto.setInstallation("Traveller");
        dto.setDepartment("ER");
        dto.setFunctionNo("FN-1");

        ComponentCounterDto counter = new ComponentCounterDto();
        counter.setCode("RUN-HRS");
        counter.setDescription("Running Hours");
        counter.setUnit("h");
        counter.setCurrentValue(1200.0);
        dto.getComponentCounters().add(counter);

        ComponentMeasurePointDto mp = new ComponentMeasurePointDto();
        mp.setCode("TEMP");
        mp.setDescription("Exhaust Temp");
        mp.setUnit("C");
        mp.setTrend("Stable");
        dto.getComponentMeasurePoints().add(mp);

        String createdBody = mockMvc.perform(post("/api/maintenance/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("C-FLOW-1"))
                .andExpect(jsonPath("$.componentCounters[0].code").value("RUN-HRS"))
                .andExpect(jsonPath("$.componentMeasurePoints[0].code").value("TEMP"))
                .andReturn().getResponse().getContentAsString();

        ComponentDto created = objectMapper.readValue(createdBody, ComponentDto.class);
        Long id = created.getId();

        // 2) GET 回显子表
        mockMvc.perform(get("/api/maintenance/components/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentCounters[0].id").exists())
                .andExpect(jsonPath("$.dateCreated").exists());

        // 3) 列表过滤（installation / department / status）
        mockMvc.perform(get("/api/maintenance/components").param("installation", "Traveller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.number == 'C-FLOW-1')]").exists());
        mockMvc.perform(get("/api/maintenance/components").param("department", "ER"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/maintenance/components").param("status", "Available"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/maintenance/components").param("q", "Main"))
                .andExpect(status().isOk());

        // 4) 更新：子表按 id 复用（保留 RUN-HRS，新增 OIL-PRESS），不丢不重
        ComponentDto upd = objectMapper.readValue(createdBody, ComponentDto.class);
        upd.getStatus();
        upd.setName("Main Engine (Overhauled)");
        upd.getComponentCounters().get(0).setCurrentValue(1500.0);
        ComponentCounterDto newCounter = new ComponentCounterDto();
        newCounter.setCode("OIL-PRESS");
        newCounter.setDescription("Oil Pressure");
        newCounter.setUnit("bar");
        upd.getComponentCounters().add(newCounter);

        mockMvc.perform(put("/api/maintenance/components/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentCounters.length()").value(2))
                .andExpect(jsonPath("$.name").value("Main Engine (Overhauled)"));

        // 5) change-status：状态变更 + 写日志
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setNewStatus("In Use");
        req.setReason("Installed");
        req.setChangedBy("A. Admin");
        mockMvc.perform(post("/api/maintenance/components/" + id + "/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.updatedIds[0]").value(id));

        mockMvc.perform(get("/api/maintenance/components/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("In Use"));

        mockMvc.perform(get("/api/maintenance/components/" + id + "/status-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].oldStatus").value("Available"))
                .andExpect(jsonPath("$[0].newStatus").value("In Use"));

        // 6) archive 查询（初始为空，返回 200）
        mockMvc.perform(get("/api/maintenance/components/" + id + "/archive"))
                .andExpect(status().isOk());

        // 7) hierarchy：创建子组件后构成树
        ComponentDto child = new ComponentDto();
        child.setNumber("C-FLOW-1-1");
        child.setTypeNumber("CT-ENG-1");
        child.setName("Piston");
        child.setStatus("Available");
        child.setInstallation("Traveller");
        child.setDepartment("ER");
        child.setParentComponent("C-FLOW-1");
        String childBody = mockMvc.perform(post("/api/maintenance/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(child)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long childId = objectMapper.readValue(childBody, ComponentDto.class).getId();

        mockMvc.perform(get("/api/maintenance/components/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.number == 'C-FLOW-1')].children[?(@.number == 'C-FLOW-1-1')].id").exists());

        // 8) 清理
        mockMvc.perform(delete("/api/maintenance/components/" + childId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + id))
                .andExpect(status().isNoContent());
    }
}
