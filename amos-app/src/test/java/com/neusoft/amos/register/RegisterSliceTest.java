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

        // 软删：记录仍存在、active=false（置为失效，而非物理移除）
        mvc.perform(get("/api/register/function-criticalities/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        // 列表默认仍含该 degree（以 active=false 过滤可查到）
        mvc.perform(get("/api/register/function-criticalities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.degree == 'MAJ')]").isNotEmpty());
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

    @Test
    void paginationReturnsPageEnvelopeAndRespectsFilters() throws Exception {
        // 无分页参数时仍为向后兼容的 List（数组）
        mvc.perform(get("/api/register/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").exists());

        // 造 3 条 unit 用于分页
        for (String c : new String[]{"U-A", "U-B", "U-C"}) {
            mvc.perform(post("/api/register/units")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"" + c + "\",\"name\":\"Unit " + c + "\"}"))
                    .andExpect(status().isCreated());
        }

        // page=0&size=2 -> Spring Page 信封
        mvc.perform(get("/api/register/units").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(4))   // PCS + U-A/B/C
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));

        // q + 分页组合：仅匹配 U-*，totalElements=3
        mvc.perform(get("/api/register/units").param("q", "U-").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].code").value("U-A"));
    }

    @Test
    void locationPaginationRespectsInstallationFilterAtQueryLevel() throws Exception {
        // 在专用 installation 998 下造 3 条，验证查询级过滤后再分页（避开其它测试的 999）
        for (String c : new String[]{"P-L1", "P-L2", "P-L3"}) {
            mvc.perform(post("/api/register/locations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"installationId\":998,\"code\":\"" + c + "\",\"name\":\"" + c + "\"}"))
                    .andExpect(status().isCreated());
        }

        mvc.perform(get("/api/register/locations")
                        .param("installation", "998").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))   // 仅 998 下的 3 条，而非全部 location
                .andExpect(jsonPath("$.content.length()").value(2));
    }
}
