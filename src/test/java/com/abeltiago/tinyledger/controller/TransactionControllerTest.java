package com.abeltiago.tinyledger.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void depositThenBalance_overHttp() throws Exception {
        mockMvc.perform(post("/accounts/7/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"type": "DEPOSIT", "amountCents":1000}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountCents").value(1000));
        mockMvc.perform(get("/accounts/7/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(1000));

        mockMvc.perform(get("/accounts/999999/balance")).andExpect(status().isNotFound());
    }

}