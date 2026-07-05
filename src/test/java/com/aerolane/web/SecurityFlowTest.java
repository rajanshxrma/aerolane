package com.aerolane.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies the security posture end to end against the seeded demo users:
 * browser requests bounce to the login page, API requests get proper status
 * codes, and role boundaries hold on both URL rules and method security.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousBrowserRequestRedirectsToLogin() throws Exception {
        // a real browser sends Accept: text/html — that's what routes anonymous
        // traffic to the login redirect instead of the API's 401 entry point
        mockMvc.perform(get("/").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void anonymousApiRequestGetsCleanUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/lanes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void officerCanReadLanesOverApi() throws Exception {
        mockMvc.perform(get("/api/v1/lanes").with(httpBasic("officer", "officer123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void officerCannotChangeLaneStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/lanes/1")
                        .with(httpBasic("officer", "officer123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void supervisorCanChangeLaneStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/lanes/1")
                        .with(httpBasic("supervisor", "supervisor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MAINTENANCE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    void auditorCanReadReports() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary").with(httpBasic("auditor", "auditor123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInspections").isNumber());
    }

    @Test
    void officerCannotReadReports() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary").with(httpBasic("officer", "officer123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void wrongPasswordIsRejectedOnApi() throws Exception {
        mockMvc.perform(get("/api/v1/lanes").with(httpBasic("officer", "not-the-password")))
                .andExpect(status().isUnauthorized());
    }
}
