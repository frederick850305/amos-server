package com.neusoft.amos.maintenance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.amos.maintenance.dto.ComponentCounterDto;
import com.neusoft.amos.maintenance.dto.ComponentDto;
import com.neusoft.amos.maintenance.dto.ComponentMeasurePointDto;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Counters / Measure Points 切片集成测试（H2 内存库，@ActiveProfiles("test")）。
 * 依赖 V10（C-TEST-10 装在 FN-ENG-01）+ V11（C-TEST-10/01 计数器、FN-ENG-01 功能计数器）种子。
 * 覆盖：读数更新写日志+当前值、function 同步、级联、Set Start、overview、logs、归属校验 400。
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class CounterSliceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Long componentId(String number) throws Exception {
        String body = mockMvc.perform(get("/api/maintenance/components").param("q", number))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<ComponentDto> list = objectMapper.readValue(body, new TypeReference<List<ComponentDto>>() {});
        return list.stream().filter(c -> number.equals(c.getNumber()))
                .map(ComponentDto::getId).findFirst()
                .orElseThrow(() -> new IllegalStateException("component not found: " + number));
    }

    private Long counterId(Long componentId, String code) throws Exception {
        String body = mockMvc.perform(get("/api/maintenance/components/" + componentId))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ComponentDto c = objectMapper.readValue(body, ComponentDto.class);
        return c.getComponentCounters().stream().filter(cc -> code.equals(cc.getCode()))
                .map(ComponentCounterDto::getId).findFirst()
                .orElseThrow(() -> new IllegalStateException("counter not found: " + code));
    }

    private double currentCounterValue(Long componentId, String code) throws Exception {
        String body = mockMvc.perform(get("/api/maintenance/components/" + componentId))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ComponentDto c = objectMapper.readValue(body, ComponentDto.class);
        return c.getComponentCounters().stream().filter(cc -> code.equals(cc.getCode()))
                .map(ComponentCounterDto::getCurrentValue).findFirst().orElse(0.0);
    }

    @Test
    void recordComponentCounterReading_writesLogAndUpdatesCurrentValue() throws Exception {
        Long cid = componentId("C-TEST-10");
        Long oid = counterId(cid, "OIL");

        mockMvc.perform(post("/api/maintenance/components/" + cid + "/counters/" + oid + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("newValue", 5.5, "readingDate", "2026-07-28"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentCounter.currentValue").value(5.5))
                .andExpect(jsonPath("$.log.newValue").value(5.5));

        // 当前值落库
        org.junit.jupiter.api.Assertions.assertEquals(5.5, currentCounterValue(cid, "OIL"), 0.0001);

        // 日志可查
        mockMvc.perform(get("/api/maintenance/counter-logs").param("component", "C-TEST-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'OIL')]").exists());
    }

    @Test
    void recordInstalledComponentCounter_syncsFunctionCounter() throws Exception {
        Long cid = componentId("C-TEST-10");
        Long hid = counterId(cid, "HRS");

        // 读 FN-ENG-01 功能 HRS 当前值（overview 携带 functionCounters）
        String ov = mockMvc.perform(get("/api/maintenance/counters/overview").param("component", "C-TEST-10"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode item = objectMapper.readTree(ov).get(0);
        double beforeFn = item.get("functionCounters").get(0).get("lastValue").asDouble();

        String resp = mockMvc.perform(post("/api/maintenance/components/" + cid + "/counters/" + hid + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("newValue", 1000.0, "readingDate", "2026-07-28"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode r = objectMapper.readTree(resp);
        double delta = r.get("log").get("delta").asDouble();
        double afterFn = r.get("functionCounter").get("lastValue").asDouble();

        org.junit.jupiter.api.Assertions.assertEquals(beforeFn + delta, afterFn, 0.0001);
    }

    @Test
    void recordSourceCounter_cascadesToDependentComponent() throws Exception {
        Long srcId = componentId("C-TEST-01");
        Long srcHid = counterId(srcId, "HRS");
        Long depId = componentId("C-TEST-10");

        mockMvc.perform(post("/api/maintenance/components/" + srcId + "/counters/" + srcHid + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("newValue", 1200.0, "readingDate", "2026-07-28"))))
                .andExpect(status().isOk());

        // 依赖组件 C-TEST-10 的 HRS（dependsOn=C-TEST-01）同步到 1200
        org.junit.jupiter.api.Assertions.assertEquals(1200.0, currentCounterValue(depId, "HRS"), 0.0001);
    }

    @Test
    void setStart_snapshotsBaselineAndResetsAverage() throws Exception {
        Long cid = componentId("C-TEST-10");
        Long oid = counterId(cid, "OIL");
        double before = currentCounterValue(cid, "OIL");

        mockMvc.perform(post("/api/maintenance/components/" + cid + "/counters/" + oid + "/set-start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentCounter.startValue").value(before))
                .andExpect(jsonPath("$.componentCounter.average").value(0));
    }

    @Test
    void overview_filtersByComponentAndInherits() throws Exception {
        mockMvc.perform(get("/api/maintenance/counters/overview").param("component", "C-TEST-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.componentNo == 'C-TEST-10')]").exists());

        // inherits=true：C-TEST-10 仅 HRS 有 dependsOn
        String body = mockMvc.perform(get("/api/maintenance/counters/overview")
                        .param("component", "C-TEST-10").param("inherits", "true"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        JsonNode item = node.get(0);
        org.junit.jupiter.api.Assertions.assertEquals(1, item.get("counters").size());
        org.junit.jupiter.api.Assertions.assertEquals("HRS", item.get("counters").get(0).get("code").asText());
    }

    @Test
    void reading_withCounterOfAnotherComponent_returns400() throws Exception {
        // 建两个独立组件，给 a 一个计数器，再用 b 的 id 去更新 a 的计数器 -> 400
        ComponentDto a = createComponent("C-NEG-A");
        ComponentCounterDto cc = new ComponentCounterDto();
        cc.setCode("HRS");
        cc.setDescription("Running Hours");
        cc.setUnit("h");
        a.setComponentCounters(List.of(cc));
        String body = mockMvc.perform(put("/api/maintenance/components/" + a.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(a)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ComponentDto a2 = objectMapper.readValue(body, ComponentDto.class);
        Long counterId = a2.getComponentCounters().get(0).getId();

        ComponentDto b = createComponent("C-NEG-B");
        mockMvc.perform(post("/api/maintenance/components/" + b.getId() + "/counters/" + counterId + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("newValue", 1.0))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/maintenance/components/" + a.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/maintenance/components/" + b.getId())).andExpect(status().isNoContent());
    }

    private ComponentDto createComponent(String number) throws Exception {
        ComponentDto dto = new ComponentDto();
        dto.setNumber(number);
        dto.setName("Neg " + number);
        dto.setInstallation("Traveller");
        dto.setDepartment("ER");
        dto.setStatus("Available");
        String body = mockMvc.perform(post("/api/maintenance/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, ComponentDto.class);
    }

    @Test
    void recordMeasurePoint_writesLogAndUpdatesValue() throws Exception {
        Long cid = componentId("C-TEST-10");
        String body = mockMvc.perform(get("/api/maintenance/components/" + cid)).andReturn()
                .getResponse().getContentAsString();
        ComponentDto c = objectMapper.readValue(body, ComponentDto.class);
        Long mpId = c.getComponentMeasurePoints().stream()
                .filter(mp -> "T-EXH".equals(mp.getCode())).map(ComponentMeasurePointDto::getId).findFirst()
                .orElseThrow(() -> new IllegalStateException("measure point not found"));

        mockMvc.perform(post("/api/maintenance/components/" + cid + "/measure-points/" + mpId + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", "430", "trend", "Up", "readingDate", "2026-07-28"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurePoint.value").value("430"))
                .andExpect(jsonPath("$.measurePoint.trend").value("Up"));

        mockMvc.perform(get("/api/maintenance/measure-logs").param("component", "C-TEST-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'T-EXH')]").exists());
    }
}
