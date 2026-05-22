package com.domus.server;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
            .andExpect(jsonPath("$.data.roles[0]").exists())
            .andExpect(jsonPath("$.data.permissions[0]").exists());
    }

    @Test
    void unauthenticatedUserCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void residentCannotAccessVisitsWithoutPermission() throws Exception {
        String residentToken = loginAndExtractToken("residente@domus.cl", "Domus123!");

        mockMvc.perform(get("/api/v1/visits")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateUpdateAndDeactivateUsers() throws Exception {
        String adminToken = loginAndExtractToken("admin@domus.cl", "Domus123!");
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");

        String createBody = """
            {
              "firstName": "Javier",
              "lastName": "Administrador",
              "email": "javier.admin@domus.cl",
              "password": "Domus123!",
              "role": "CONSERJERIA"
            }
            """;

        String createResponse = mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("javier.admin@domus.cl"))
            .andExpect(jsonPath("$.data.roles[0]").value("CONSERJERIA"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String userId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("role", "CONSERJERIA")
                .param("active", "true")
                .param("search", "javier"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(userId));

        String updateBody = """
            {
              "firstName": "Javiera",
              "lastName": "Administradora",
              "role": "RESIDENTE"
            }
            """;

        mockMvc.perform(put("/api/v1/users/" + userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.firstName").value("Javiera"))
            .andExpect(jsonPath("$.data.roles[0]").value("RESIDENTE"));

        mockMvc.perform(patch("/api/v1/users/" + userId + "/deactivate")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", "Bearer " + adminToken)
                .param("entityType", "USER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].entityId").value(userId))
            .andExpect(jsonPath("$.data[0].action").value("STATUS_CHANGE"))
            .andExpect(jsonPath("$.data[1].action").value("UPDATE"))
            .andExpect(jsonPath("$.data[2].action").value("CREATE"));
    }

    @Test
    void adminCanReviewAuditLogsAndResidentCannotAccessThem() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");
        String adminToken = loginAndExtractToken("admin@domus.cl", "Domus123!");
        String residentToken = loginAndExtractToken("residente@domus.cl", "Domus123!");

        String createVisitBody = """
            {
              "visitorName": "Andrea Leon",
              "visitorDocument": "11.222.333-4",
              "visitorPhone": "+56922223333",
              "vehiclePlate": "AAAA11",
              "residentUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "residentName": "Rocio Residente",
              "unitLabel": "Depto 804",
              "blockLabel": "Torre A",
              "observations": "Auditoria de visitas.",
              "registrationType": "MANUAL_CONSERJERIA"
            }
            """;

        mockMvc.perform(post("/api/v1/visits")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createVisitBody))
            .andExpect(status().isOk());

        String auditResponse = mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", "Bearer " + adminToken)
                .param("entityType", "VISIT")
                .param("action", "CREATE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].entityType").value("VISIT"))
            .andExpect(jsonPath("$.data[0].action").value("CREATE"))
            .andExpect(jsonPath("$.data[0].actor.email").value("conserjeria@domus.cl"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String auditLogId = objectMapper.readTree(auditResponse).path("data").get(0).path("id").asText();

        mockMvc.perform(get("/api/v1/audit-logs/" + auditLogId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(auditLogId))
            .andExpect(jsonPath("$.data.summary").isNotEmpty());

        mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isForbidden());
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

    @Test
    void conciergeCanCreateUpdateAndDeactivateResident() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");

        String unitBody = """
            {
              "unitCode": "804",
              "blockLabel": "Torre A",
              "floorNumber": 8,
              "observations": "Unidad para pruebas de residentes.",
              "residentIds": []
            }
            """;

        String unitResponse = mockMvc.perform(post("/api/v1/units")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String unitId = objectMapper.readTree(unitResponse).path("data").path("id").asText();

        String createBody = """
            {
              "firstName": "Claudia",
              "lastName": "Mendez",
              "documentNumber": "22.333.444-5",
              "email": "claudia.mendez@domus.cl",
              "phone": "+56999998888",
              "residentType": "ARRENDATARIO",
              "linkedUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "unitId": "%s"
            }
            """.formatted(unitId);

        String createResponse = mockMvc.perform(post("/api/v1/residents")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.firstName").value("Claudia"))
            .andExpect(jsonPath("$.data.active").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String residentId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/residents")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(get("/api/v1/residents/" + residentId)
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.unit.unitCode").value("804"));

        String updateBody = """
            {
              "firstName": "Claudia",
              "lastName": "Mendez Soto",
              "documentNumber": "22.333.444-5",
              "email": "claudia.mendez@domus.cl",
              "phone": "+56999998888",
              "residentType": "PROPIETARIO",
              "linkedUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "unitId": "%s"
            }
            """.formatted(unitId);

        mockMvc.perform(put("/api/v1/residents/" + residentId)
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.lastName").value("Mendez Soto"))
            .andExpect(jsonPath("$.data.residentType").value("PROPIETARIO"));

        String deactivateBody = """
            {
              "active": false
            }
            """;

        mockMvc.perform(patch("/api/v1/residents/" + residentId + "/status")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(deactivateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void conciergeCanCreateUpdateAndDeactivateUnit() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");

        String createBody = """
            {
              "unitCode": "1202",
              "blockLabel": "Torre B",
              "floorNumber": 12,
              "observations": "Unidad orientada al norte.",
              "residentIds": []
            }
            """;

        String createResponse = mockMvc.perform(post("/api/v1/units")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.unitCode").value("1202"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String unitId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/units")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(get("/api/v1/units/" + unitId)
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.blockLabel").value("Torre B"));

        String updateBody = """
            {
              "unitCode": "1202",
              "blockLabel": "Torre B",
              "floorNumber": 13,
              "observations": "Unidad actualizada.",
              "residentIds": []
            }
            """;

        mockMvc.perform(put("/api/v1/units/" + unitId)
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.floorNumber").value(13));

        String deactivateBody = """
            {
              "active": false
            }
            """;

        mockMvc.perform(patch("/api/v1/units/" + unitId + "/status")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(deactivateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void conciergeCanViewOperationalDashboard() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");

        String unitBody = """
            {
              "unitCode": "1501",
              "blockLabel": "Torre C",
              "floorNumber": 15,
              "observations": "Unidad para dashboard.",
              "residentIds": []
            }
            """;

        String unitResponse = mockMvc.perform(post("/api/v1/units")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String unitId = objectMapper.readTree(unitResponse).path("data").path("id").asText();

        String residentBody = """
            {
              "firstName": "Paula",
              "lastName": "Rivas",
              "documentNumber": "33.444.555-6",
              "email": "paula.rivas@domus.cl",
              "phone": "+56977776666",
              "residentType": "OCUPANTE",
              "linkedUserId": null,
              "unitId": "%s"
            }
            """.formatted(unitId);

        mockMvc.perform(post("/api/v1/residents")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(residentBody))
            .andExpect(status().isOk());

        String visitBody = """
            {
              "visitorName": "Luciano Perez",
              "visitorDocument": "9.876.543-2",
              "visitorPhone": "+56955554444",
              "vehiclePlate": "ZXCV98",
              "residentUserId": null,
              "residentName": "Paula Rivas",
              "unitLabel": "1501",
              "blockLabel": "Torre C",
              "observations": "Visita para pruebas del panel.",
              "registrationType": "MANUAL_CONSERJERIA"
            }
            """;

        mockMvc.perform(post("/api/v1/visits")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitBody))
            .andExpect(status().isOk());

        String packageBody = """
            {
              "description": "Caja de supermercado",
              "senderName": "Despacho Express",
              "packageType": "DELIVERY",
              "residentUserId": null,
              "residentName": "Paula Rivas",
              "unitLabel": "1501",
              "blockLabel": "Torre C",
              "receivedByName": "Mario Porteria",
              "observations": "Ingreso para dashboard."
            }
            """;

        mockMvc.perform(post("/api/v1/packages")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(packageBody))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/concierge/dashboard")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.metrics.pendingVisits").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.data.metrics.pendingPackages").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.data.metrics.activeResidents").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.data.metrics.activeUnits").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.data.recentActivity[0].type").exists())
            .andExpect(jsonPath("$.data.generatedAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/concierge/recent-activity")
                .header("Authorization", "Bearer " + conciergeToken)
                .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].type").exists())
            .andExpect(jsonPath("$.data[0].route").exists());
    }

    @Test
    void adminCanViewAdministrativeDashboardAndConciergeCannot() throws Exception {
        String adminToken = loginAndExtractToken("admin@domus.cl", "Domus123!");
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");

        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.metrics.totalUsers").value(greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.data.metrics.activeUsers").value(greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.data.metrics.activeResidents").value(greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.data.metrics.unreadNotifications").value(greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.data.recentActivity[0].action").exists())
            .andExpect(jsonPath("$.data.generatedAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/admin/recent-activity")
                .header("Authorization", "Bearer " + adminToken)
                .param("limit", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].summary").isNotEmpty());

        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void conciergeCanCreateUpdateAndChangeParkingStatus() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");
        String adminToken = loginAndExtractToken("admin@domus.cl", "Domus123!");

        String unitBody = """
            {
              "unitCode": "210",
              "blockLabel": "Torre D",
              "floorNumber": 2,
              "observations": "Unidad para pruebas de estacionamiento.",
              "residentIds": []
            }
            """;

        String unitResponse = mockMvc.perform(post("/api/v1/units")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String unitId = objectMapper.readTree(unitResponse).path("data").path("id").asText();

        String residentBody = """
            {
              "firstName": "Mariela",
              "lastName": "Saez",
              "documentNumber": "44.555.666-7",
              "email": "mariela.saez@domus.cl",
              "phone": "+56966665555",
              "residentType": "PROPIETARIO",
              "linkedUserId": null,
              "unitId": "%s"
            }
            """.formatted(unitId);

        String residentResponse = mockMvc.perform(post("/api/v1/residents")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(residentBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String residentId = objectMapper.readTree(residentResponse).path("data").path("id").asText();

        String createBody = """
            {
              "spotCode": "E-12",
              "parkingType": "RESIDENTE",
              "occupancyStatus": "DISPONIBLE",
              "unitId": "%s",
              "residentId": "%s",
              "vehiclePlate": null,
              "observations": "Estacionamiento cubierto."
            }
            """.formatted(unitId, residentId);

        String createResponse = mockMvc.perform(post("/api/v1/parking")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.spotCode").value("E-12"))
            .andExpect(jsonPath("$.data.parkingType").value("RESIDENTE"))
            .andExpect(jsonPath("$.data.occupancyStatus").value("DISPONIBLE"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String parkingId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/parking")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(get("/api/v1/parking/" + parkingId)
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.unit.unitCode").value("210"))
            .andExpect(jsonPath("$.data.resident.firstName").value("Mariela"));

        String updateBody = """
            {
              "spotCode": "E-12",
              "parkingType": "RESIDENTE",
              "occupancyStatus": "OCUPADO",
              "unitId": "%s",
              "residentId": "%s",
              "vehiclePlate": "JKL123",
              "observations": "Vehiculo asignado al residente."
            }
            """.formatted(unitId, residentId);

        mockMvc.perform(put("/api/v1/parking/" + parkingId)
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.occupancyStatus").value("OCUPADO"))
            .andExpect(jsonPath("$.data.vehiclePlate").value("JKL123"));

        String statusBody = """
            {
              "active": false,
              "occupancyStatus": "DISPONIBLE"
            }
            """;

        mockMvc.perform(patch("/api/v1/parking/" + parkingId + "/status")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active").value(false))
            .andExpect(jsonPath("$.data.occupancyStatus").value("DISPONIBLE"));

        mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", "Bearer " + adminToken)
                .param("entityType", "PARKING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].entityType").value("PARKING"))
            .andExpect(jsonPath("$.data[0].entityId").value(parkingId))
            .andExpect(jsonPath("$.data[0].action").value("STATUS_CHANGE"))
            .andExpect(jsonPath("$.data[0].actor.email").value("conserjeria@domus.cl"))
            .andExpect(jsonPath("$.data[1].action").value("UPDATE"))
            .andExpect(jsonPath("$.data[2].action").value("CREATE"));
    }

    @Test
    void conciergeCanCreateUpdateAndChangeStorageStatus() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");
        String adminToken = loginAndExtractToken("admin@domus.cl", "Domus123!");

        String unitBody = """
            {
              "unitCode": "310",
              "blockLabel": "Torre E",
              "floorNumber": 3,
              "observations": "Unidad para pruebas de bodega.",
              "residentIds": []
            }
            """;

        String unitResponse = mockMvc.perform(post("/api/v1/units")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String unitId = objectMapper.readTree(unitResponse).path("data").path("id").asText();

        String createBody = """
            {
              "storageCode": "B-03",
              "storageType": "MEDIANA",
              "occupancyStatus": "DISPONIBLE",
              "unitId": "%s",
              "observations": "Bodega interior."
            }
            """.formatted(unitId);

        String createResponse = mockMvc.perform(post("/api/v1/storages")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.storageCode").value("B-03"))
            .andExpect(jsonPath("$.data.storageType").value("MEDIANA"))
            .andExpect(jsonPath("$.data.occupancyStatus").value("DISPONIBLE"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String storageId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/storages")
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(get("/api/v1/storages/" + storageId)
                .header("Authorization", "Bearer " + conciergeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.unit.unitCode").value("310"));

        String updateBody = """
            {
              "storageCode": "B-03",
              "storageType": "GRANDE",
              "occupancyStatus": "OCUPADA",
              "unitId": "%s",
              "observations": "Bodega ampliada."
            }
            """.formatted(unitId);

        mockMvc.perform(put("/api/v1/storages/" + storageId)
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.storageType").value("GRANDE"))
            .andExpect(jsonPath("$.data.occupancyStatus").value("OCUPADA"));

        String statusBody = """
            {
              "active": false,
              "occupancyStatus": "DISPONIBLE"
            }
            """;

        mockMvc.perform(patch("/api/v1/storages/" + storageId + "/status")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active").value(false))
            .andExpect(jsonPath("$.data.occupancyStatus").value("DISPONIBLE"));

        mockMvc.perform(get("/api/v1/audit-logs")
                .header("Authorization", "Bearer " + adminToken)
                .param("entityType", "STORAGE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].entityType").value("STORAGE"))
            .andExpect(jsonPath("$.data[0].entityId").value(storageId))
            .andExpect(jsonPath("$.data[0].action").value("STATUS_CHANGE"))
            .andExpect(jsonPath("$.data[0].actor.email").value("conserjeria@domus.cl"))
            .andExpect(jsonPath("$.data[1].action").value("UPDATE"))
            .andExpect(jsonPath("$.data[2].action").value("CREATE"));
    }

    @Test
    void usersCanExchangeMessagesAndMarkThemAsRead() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");
        String residentToken = loginAndExtractToken("residente@domus.cl", "Domus123!");

        String sendBody = """
            {
              "recipientUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "content": "Hola, tienes una encomienda pendiente en conserjeria."
            }
            """;

        String sendResponse = mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(sendBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ENVIADO"))
            .andExpect(jsonPath("$.data.content").value("Hola, tienes una encomienda pendiente en conserjeria."))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String messageId = objectMapper.readTree(sendResponse).path("data").path("id").asText();
        String conversationId = objectMapper.readTree(sendResponse).path("data").path("conversationId").asText();

        mockMvc.perform(get("/api/v1/conversations")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(conversationId))
            .andExpect(jsonPath("$.data[0].unreadCount").value(greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/conversations/" + conversationId)
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.messages[0].id").value(messageId))
            .andExpect(jsonPath("$.data.otherParticipant.email").value("conserjeria@domus.cl"));

        mockMvc.perform(get("/api/v1/messages")
                .header("Authorization", "Bearer " + residentToken)
                .param("conversationId", conversationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(messageId));

        mockMvc.perform(patch("/api/v1/messages/" + messageId + "/read")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("LEIDO"))
            .andExpect(jsonPath("$.data.readAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/messages/contacts")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());
    }

    @Test
    void residentCanListAndReadGeneratedNotifications() throws Exception {
        String conciergeToken = loginAndExtractToken("conserjeria@domus.cl", "Domus123!");
        String residentToken = loginAndExtractToken("residente@domus.cl", "Domus123!");

        String packageBody = """
            {
              "description": "Caja de documentos",
              "senderName": "Courier Central",
              "packageType": "PAQUETE",
              "residentUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "residentName": "Rocio Residente",
              "unitLabel": "Depto 804",
              "blockLabel": "Torre A",
              "receivedByName": "Ana Porteria",
              "observations": "Notificacion por encomienda."
            }
            """;

        mockMvc.perform(post("/api/v1/packages")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(packageBody))
            .andExpect(status().isOk());

        String visitBody = """
            {
              "visitorName": "Luis Paredes",
              "visitorDocument": "18.222.333-4",
              "visitorPhone": "+56911113333",
              "vehiclePlate": "MNOP45",
              "residentUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "residentName": "Rocio Residente",
              "unitLabel": "Depto 804",
              "blockLabel": "Torre A",
              "observations": "Notificacion por visita.",
              "registrationType": "MANUAL_CONSERJERIA"
            }
            """;

        mockMvc.perform(post("/api/v1/visits")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitBody))
            .andExpect(status().isOk());

        String messageBody = """
            {
              "recipientUserId": "bb4f8752-3baa-46fb-934b-54cc2d9d2003",
              "content": "Tienes novedades en el edificio."
            }
            """;

        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + conciergeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(messageBody))
            .andExpect(status().isOk());

        String notificationsResponse = mockMvc.perform(get("/api/v1/notifications")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].type").exists())
            .andExpect(jsonPath("$.data[0].route").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String notificationId = objectMapper.readTree(notificationsResponse).path("data").get(0).path("id").asText();

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.unreadCount").value(greaterThanOrEqualTo(3)));

        mockMvc.perform(patch("/api/v1/notifications/" + notificationId + "/read")
                .header("Authorization", "Bearer " + residentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.read").value(true))
            .andExpect(jsonPath("$.data.readAt").isNotEmpty());
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
