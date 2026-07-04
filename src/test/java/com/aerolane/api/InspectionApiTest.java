package com.aerolane.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InspectionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void officerCanLogInspection() throws Exception {
        mockMvc.perform(post("/api/v1/inspections")
                        .with(httpBasic("officer", "officer123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "laneId": 1,
                                  "equipment": "XRAY_SCANNER",
                                  "result": "PASS",
                                  "notes": "logged over the API"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.equipment").value("XRAY_SCANNER"))
                .andExpect(jsonPath("$.inspectedBy").value("officer"));
    }

    @Test
    void missingFieldsComeBackAsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/inspections")
                        .with(httpBasic("officer", "officer123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"no lane, no equipment, no result\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.laneId").exists())
                .andExpect(jsonPath("$.fieldErrors.equipment").exists())
                .andExpect(jsonPath("$.fieldErrors.result").exists());
    }

    @Test
    void unknownEnumValueIsABadRequestNotAServerError() throws Exception {
        mockMvc.perform(post("/api/v1/inspections")
                        .with(httpBasic("officer", "officer123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"laneId\":1,\"equipment\":\"LASER_CANNON\",\"result\":\"PASS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unknownLaneIsACleanNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/inspections")
                        .with(httpBasic("officer", "officer123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"laneId\":999999,\"equipment\":\"XRAY_SCANNER\",\"result\":\"PASS\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownInspectionIdIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/inspections/999999")
                        .with(httpBasic("auditor", "auditor123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void resultFilterOnlyReturnsMatchingInspections() throws Exception {
        mockMvc.perform(get("/api/v1/inspections?result=FAIL")
                        .with(httpBasic("supervisor", "supervisor123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].result").value("FAIL"));
    }

    @Test
    void auditorCannotCreateInspections() throws Exception {
        mockMvc.perform(post("/api/v1/inspections")
                        .with(httpBasic("auditor", "auditor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"laneId\":1,\"equipment\":\"XRAY_SCANNER\",\"result\":\"PASS\"}"))
                .andExpect(status().isForbidden());
    }
}
