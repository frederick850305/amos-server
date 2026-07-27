package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.amos.maintenance.dto.ComponentTypeCounterDefDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeMeasurePointDefDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeRelatedTypeDto;
import com.neusoft.amos.maintenance.dto.ComponentTypeStockTypeDto;
import com.neusoft.amos.maintenance.dto.RegisterComponentRequest;
import com.neusoft.amos.stock.StockType;
import com.neusoft.amos.stock.StockTypeRepository;
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

    @Autowired
    private StockTypeRepository stockTypeRepository;

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

    /**
     * 业务键解析：按 relatedTypeNumber / stockTypeNo 关联，并持久化 alternativeNo。
     */
    @Test
    void relatedAndStockTypeBusinessKeys() throws Exception {
        // 种子：一个备件类型，供 stockTypeNo 解析
        StockType st = new StockType();
        st.setStockTypeNo("ST-TEST-1");
        st.setDescription("Test Stock");
        st = stockTypeRepository.save(st);

        // 被关联的类型 CT-REL-1
        ComponentTypeDto rel = new ComponentTypeDto();
        rel.setTypeNumber("CT-REL-1");
        rel.setName("Related Type");
        rel.setStatus("Active");
        String relBody = mockMvc.perform(post("/api/maintenance/component-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rel)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ComponentTypeDto relCreated = objectMapper.readValue(relBody, ComponentTypeDto.class);

        // 主类型：按 typeNumber 关联 + 按 stockTypeNo 挂备件（含 alternativeNo）
        ComponentTypeDto dto = new ComponentTypeDto();
        dto.setTypeNumber("CT-KEY-1");
        dto.setName("Key Type");
        dto.setStatus("Active");

        ComponentTypeRelatedTypeDto rd = new ComponentTypeRelatedTypeDto();
        rd.setRelatedTypeNumber("CT-REL-1");
        dto.getRelatedTypes().add(rd);

        ComponentTypeStockTypeDto sd = new ComponentTypeStockTypeDto();
        sd.setStockTypeNo("ST-TEST-1");
        sd.setAlternativeNo("ALT-1");
        sd.setQuantity(2.0);
        dto.getStockTypeLinks().add(sd);

        String createdBody = mockMvc.perform(post("/api/maintenance/component-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relatedTypes[0].relatedTypeNumber").value("CT-REL-1"))
                .andExpect(jsonPath("$.relatedTypes[0].relatedTypeName").value("Related Type"))
                .andExpect(jsonPath("$.stockTypeLinks[0].stockTypeNo").value("ST-TEST-1"))
                .andExpect(jsonPath("$.stockTypeLinks[0].alternativeNo").value("ALT-1"))
                .andReturn().getResponse().getContentAsString();

        ComponentTypeDto created = objectMapper.readValue(createdBody, ComponentTypeDto.class);
        Long id = created.getId();

        // GET 回显验证
        mockMvc.perform(get("/api/maintenance/component-types/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relatedTypes[0].relatedTypeNumber").value("CT-REL-1"))
                .andExpect(jsonPath("$.stockTypeLinks[0].alternativeNo").value("ALT-1"));

        // 清理（先删主类型解除外键，再删被关联类型）
        mockMvc.perform(delete("/api/maintenance/component-types/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/component-types/" + relCreated.getId()))
                .andExpect(status().isNoContent());
    }
}
