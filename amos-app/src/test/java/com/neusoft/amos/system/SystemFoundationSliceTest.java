package com.neusoft.amos.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SystemFoundationSliceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndToken() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(body).get("data");
        return data.get("token").asText();
    }

    @Test
    void loginReturnsTokenAndScopes() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(body).get("data");
        org.junit.jupiter.api.Assertions.assertTrue(data.get("token").asText().length() > 10);
        org.junit.jupiter.api.Assertions.assertEquals(1, data.get("roles").size());
        org.junit.jupiter.api.Assertions.assertEquals(1, data.get("scopes").get("installations").size());
    }

    @Test
    void loginWrongPasswordFails() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void installationsAndDepartmentsLoad() throws Exception {
        mvc.perform(get("/api/system/installations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("Traveller"));
        mvc.perform(get("/api/system/departments").param("installation", "Traveller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ER"));
    }

    @Test
    void meScopesAndOptionsRoundtrip() throws Exception {
        String token = loginAndToken();
        mvc.perform(get("/api/system/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
        mvc.perform(get("/api/system/me/scopes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installations[0].code").value("Traveller"));
        mvc.perform(put("/api/system/me/options")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"theme\",\"value\":\"light\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("light"));
        mvc.perform(get("/api/system/me/options").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("light"));
    }

    @Test
    void meWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/system/me")).andExpect(status().isUnauthorized());
    }
}
