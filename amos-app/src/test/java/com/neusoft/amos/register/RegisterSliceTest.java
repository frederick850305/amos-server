package com.neusoft.amos.register;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Registers 切片集成测试（H2 内存库，@ActiveProfiles("test") 关闭 postgres profile）。
 * 覆盖：CRUD、重复编码 409、q/status 搜索、软删（状态过滤排除）、location 层级与 installation 过滤、
 * function_criticality 的 active 布尔过滤。
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class RegisterSliceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void makersCrudDuplicateAndSoftDelete() throws Exception {
        String body = mvc.perform(post("/api/register/makers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"MK-NEW\",\"name\":\"New Maker\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();

        // q 搜索
        mvc.perform(get("/api/register/makers").param("q", "MK-NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MK-NEW"));

        // 重复编码 -> 409
        mvc.perform(post("/api/register/makers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"MK-NEW\"}"))
                .andExpect(status().isConflict());

        // 软删
        mvc.perform(delete("/api/register/makers/" + id)).andExpect(status().isNoContent());

        // 软删后 status=INACTIVE，ACTIVE 过滤应排除
        mvc.perform(get("/api/register/makers").param("status", "ACTIVE").param("q", "MK-NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void vendorsCrudAndSearch() throws Exception {
        String body = mvc.perform(post("/api/register/vendors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vendorNo\":\"V-NEW\",\"name\":\"New Vendor\",\"country\":\"Norway\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();

        mvc.perform(get("/api/register/vendors").param("q", "Norway"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vendorNo").value("V-NEW"));

        mvc.perform(put("/api/register/vendors/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vendorNo\":\"V-NEW\",\"name\":\"Updated\",\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        mvc.perform(delete("/api/register/vendors/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void locationHierarchyAndInstallationFilter() throws Exception {
        // 使用非种子安装地点 id（999），避免与 V5 种子（Traveller 下的 ER 位置）冲突
        String parent = mvc.perform(post("/api/register/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"installationId\":999,\"code\":\"L-PARENT\",\"name\":\"Parent\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long parentId = objectMapper.readTree(parent).get("id").asLong();

        mvc.perform(post("/api/register/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"installationId\":999,\"code\":\"L-CHILD\",\"name\":\"Child\",\"parentLocationId\":" + parentId + "}"))
                .andExpect(status().isCreated());

        // 按 installation 过滤（999 为本测试专用，避开种子 ER 位置）
        mvc.perform(get("/api/register/locations").param("installation", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // 按 parentId 过滤
        mvc.perform(get("/api/register/locations").param("parentId", String.valueOf(parentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("L-CHILD"));
    }

    @Test
    void functionCriticalityBooleanActiveFilter() throws Exception {
        String body = mvc.perform(post("/api/register/function-criticalities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"degree\":\"MAJ\",\"description\":\"Major\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();

        // status=true 走布尔过滤
        mvc.perform(get("/api/register/function-criticalities").param("status", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.degree == 'MAJ')].degree").value("MAJ"));

        // active 参数过滤
        mvc.perform(get("/api/register/function-criticalities").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.degree == 'MAJ')].degree").value("MAJ"));

        // 重复 degree -> 409
        mvc.perform(post("/api/register/function-criticalities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"degree\":\"MAJ\"}"))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/register/function-criticalities/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void simpleRegistersListAndCreate() throws Exception {
        for (String base : new String[]{"units", "currencies", "job-classes", "trades", "disciplines", "budget-codes"}) {
            mvc.perform(get("/api/register/" + base)).andExpect(status().isOk());
        }
        // budget-code 创建 + 唯一校验
        String b = mvc.perform(post("/api/register/budget-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"BG-NEW\",\"name\":\"New Budget\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(b).get("id").asLong();
        mvc.perform(post("/api/register/budget-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"BG-NEW\"}"))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/register/budget-codes/" + id)).andExpect(status().isNoContent());
    }
}
