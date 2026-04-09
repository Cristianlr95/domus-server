package com.domus.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DomusServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginReturnsJwtAndCurrentUser() throws Exception {
        String loginBody = """
            {
              "email": "admin@domus.cl",
              "password": "Domus123!"
            }
            """;

        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.user.email").value("admin@domus.cl"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String token = responseBody.replaceAll(".*\\\"accessToken\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("admin@domus.cl"))
            .andExpect(jsonPath("$.data.roles[0]").exists());
    }

    @Test
    void unauthenticatedUserCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void conciergeCanCreateListAndUpdateVisitStatus() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");

        String createBody = """
            {
              "visitorName": "Mario Soto",
              "visitorDocument": "12.345.678-9",
              "visitorPhone": "+56911112222",
              "vehiclePlate": "ABCD11",
              "residentUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "residentName": "Rocio Residente",
              "unitLabel": "Depto 804",
              "blockLabel": "Torre A",
              "observations": "Entrega de llaves y visita breve.",
              "registrationType": "MANUAL_CONSERJERIA"
            }
            """;

        String createResponse = mockMvc.perform(post("/api/v1/visits")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.visitorName").value("Mario Soto"))
            .andExpect(jsonPath("$.data.status").value("PENDIENTE"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String visitId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/visits")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(get("/api/v1/visits/" + visitId)
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.unitLabel").value("Depto 804"));

        String checkInBody = """
            {
              "status": "INGRESADA"
            }
            """;

        mockMvc.perform(patch("/api/v1/visits/" + visitId + "/status")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkInBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("INGRESADA"))
            .andExpect(jsonPath("$.data.entryAt").isNotEmpty());

        String finishBody = """
            {
              "status": "FINALIZADA"
            }
            """;

        mockMvc.perform(patch("/api/v1/visits/" + visitId + "/status")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(finishBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("FINALIZADA"))
            .andExpect(jsonPath("$.data.exitAt").isNotEmpty());
    }

    @Test
    void conciergeCanRegisterNotifyAndDeliverPackage() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");

        String createBody = """
            {
              "description": "Paquete de farmacia",
              "senderName": "Farmacia Central",
              "packageType": "PAQUETE",
              "residentUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "residentName": "Rocio Residente",
              "unitLabel": "Depto 804",
              "blockLabel": "Torre A",
              "receivedByName": "Ana Porteria",
              "observations": "Entregado por courier externo."
            }
            """;

        String createResponse = mockMvc.perform(post("/api/v1/packages")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.description").value("Paquete de farmacia"))
            .andExpect(jsonPath("$.data.status").value("RECIBIDA"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String packageId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/packages")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(get("/api/v1/packages/" + packageId)
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.unitLabel").value("Depto 804"));

        String notifyBody = """
            {
              "status": "NOTIFICADA"
            }
            """;

        mockMvc.perform(patch("/api/v1/packages/" + packageId + "/status")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(notifyBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("NOTIFICADA"));

        String deliverBody = """
            {
              "deliveredToName": "Rocio Residente"
            }
            """;

        mockMvc.perform(patch("/api/v1/packages/" + packageId + "/deliver")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(deliverBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ENTREGADA"))
            .andExpect(jsonPath("$.data.deliveredToName").value("Rocio Residente"))
            .andExpect(jsonPath("$.data.deliveredAt").isNotEmpty());
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        String loginBody = """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);

        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode dataNode = objectMapper.readTree(responseBody).path("data");
        return dataNode.path("accessToken").asText();
    }
}
