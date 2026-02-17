package com.vhausler.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnRouteBetweenCzechAndItaly() throws Exception {
        mockMvc.perform(get("/routing/CZE/ITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route[0]").value("CZE"))
                .andExpect(jsonPath("$.route[1]").value("AUT"))
                .andExpect(jsonPath("$.route[2]").value("ITA"));
    }

    @Test
    void shouldReturnRouteBetweenPortugalAndRussia() throws Exception {
        mockMvc.perform(get("/routing/PRT/RUS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route[0]").value("PRT"))
                .andExpect(jsonPath("$.route[1]").value("ESP"))
                .andExpect(jsonPath("$.route[2]").value("FRA"))
                .andExpect(jsonPath("$.route[3]").value("DEU"))
                .andExpect(jsonPath("$.route[4]").value("POL"))
                .andExpect(jsonPath("$.route[5]").value("RUS"));
    }

    @Test
    void shouldReturnBadRequestWhenNoRouteExists() throws Exception {
        mockMvc.perform(get("/routing/USA/ITA"))
                .andExpect(status().isBadRequest());
    }
}

