package com.altermax;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.altermax.competitor.api.CompetitorCatalog;
import com.altermax.competitor.api.CompetitorDetails;
import com.altermax.competitor.api.CompetitorSpecification;
import com.altermax.competitor.api.CompetitorSummary;
import com.altermax.shared.error.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AltermaxIntegrationTest {
    private static final String JWT_SECRET =
            "YWx0ZXJtYXgtY2hhdmUtZGVtb25zdHJhY2FvLWFjYWRlbWljYS0yMDI2LXNlZ3VyYQ==";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CompetitorCatalog competitors;

    String login(String username, String password) throws Exception {
        return objectMapper
                .readTree(
                        mockMvc.perform(
                                        post("/api/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                        objectMapper.writeValueAsString(
                                                                Map.of(
                                                                        "username",
                                                                        username,
                                                                        "password",
                                                                        password))))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString())
                .get("accessToken")
                .asText();
    }

    String expiredUserToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("user")
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_SECRET)))
                .compact();
    }

    @BeforeEach
    void mockCompetitor() {
        when(competitors.search(anyString()))
                .thenReturn(
                        List.of(
                                new CompetitorSummary(
                                        "ext-1", "Toyota", "Corolla", null, "Altis", 2025)));
        when(competitors.getDetails("ext-1"))
                .thenReturn(
                        new CompetitorDetails(
                                "ext-1",
                                "Toyota",
                                "Corolla",
                                null,
                                "Altis",
                                2025,
                                List.of(
                                        new CompetitorSpecification("power_hp", 103, "kW"),
                                        new CompetitorSpecification(
                                                "transmission", "automatic", null))));
    }

    @Test
    @Order(1)
    void loginValidoEInvalido() throws Exception {
        login("user", "user123");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"user\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @Order(2)
    void tokenAusenteInvalidoEExpiradoRetornam401Padronizado() throws Exception {
        mockMvc.perform(get("/api/ford-vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").isArray());
        mockMvc.perform(get("/api/ford-vehicles").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(
                        get("/api/ford-vehicles")
                                .header("Authorization", "Bearer " + expiredUserToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @Order(3)
    void userListaConsultaFordEPesquisaConcorrente() throws Exception {
        String token = login("user", "user123");

        mockMvc.perform(get("/api/ford-vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Ford"));
        mockMvc.perform(get("/api/ford-vehicles/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specifications").isArray());
        mockMvc.perform(
                        get("/api/competitors/search?q=Corolla")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].externalId").value("ext-1"));
    }

    @Test
    @Order(4)
    void userRecebe403EmAdministracao() throws Exception {
        String token = login("user", "user123");

        mockMvc.perform(delete("/api/ford-vehicles/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @Order(5)
    void adminCadastraAtualizaEExcluiComLocation() throws Exception {
        String token = login("admin", "admin123");
        String vehicleRequestJson =
                "{\"model\":\"Bronco Sport\",\"variant\":\"Wildtrak\",\"modelYear\":2026,"
                        + "\"market\":\"BR\",\"specifications\":[{\"key\":\"power_hp\","
                        + "\"label\":\"Potencia\",\"category\":\"performance\",\"value\":\"253\","
                        + "\"unit\":\"hp\"}]}";

        var createResponse =
                mockMvc.perform(
                                post("/api/ford-vehicles")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(vehicleRequestJson))
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andReturn();
        long vehicleId =
                objectMapper
                        .readTree(createResponse.getResponse().getContentAsString())
                        .get("id")
                        .asLong();
        String updatedRequestJson = vehicleRequestJson.replace("Wildtrak", "Badlands");

        mockMvc.perform(
                        put("/api/ford-vehicles/" + vehicleId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatedRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variant").value("Badlands"));
        mockMvc.perform(
                        delete("/api/ford-vehicles/" + vehicleId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(
                        get("/api/ford-vehicles/" + vehicleId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FORD_VEHICLE_NOT_FOUND"));
    }

    @Test
    @Order(6)
    void requestInvalidoRetorna400() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(
                        post("/api/ford-vehicles")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(7)
    void metodoEMidiaIncompativeisRetornamStatusRestCoerentes() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(
                        post("/api/ford-vehicles/1")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
        mockMvc.perform(
                        post("/api/ford-vehicles")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.TEXT_PLAIN)
                                .content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    @Order(8)
    void comparacaoPreservaSchemaNormalizaEGeraHistorico() throws Exception {
        String token = login("user", "user123");
        String comparisonRequestJson =
                "{\"fordVehicleId\":1,\"competitorExternalId\":\"ext-1\","
                        + "\"attributes\":[\"power_hp\",\"torque_nm\",\"transmission\"]}";

        var comparisonResponse =
                mockMvc.perform(
                                post("/api/comparisons")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(comparisonRequestJson))
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(
                                jsonPath("$.vehicles[0].specifications.torque_nm.available")
                                        .value(true))
                        .andExpect(
                                jsonPath("$.vehicles[1].specifications.power_hp.value")
                                        .value(138.13))
                        .andExpect(
                                jsonPath("$.vehicles[1].specifications.torque_nm.available")
                                        .value(false))
                        .andExpect(
                                jsonPath("$.vehicles[1].specifications.torque_nm.value")
                                        .doesNotExist())
                        .andReturn();
        long comparisonId =
                objectMapper
                        .readTree(comparisonResponse.getResponse().getContentAsString())
                        .get("comparisonId")
                        .asLong();

        mockMvc.perform(
                        get("/api/comparison-history/" + comparisonId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultSnapshot.vehicles").isArray());
        mockMvc.perform(get("/api/comparison-history").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(comparisonId));
    }

    @Test
    @Order(9)
    void atributoNaoSuportadoRetorna422() throws Exception {
        String token = login("user", "user123");

        mockMvc.perform(
                        post("/api/comparisons")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"fordVehicleId\":1,"
                                                + "\"competitorExternalId\":\"ext-1\","
                                                + "\"attributes\":[\"x\"]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ATTRIBUTE_NOT_SUPPORTED"));
    }

    @Test
    @Order(10)
    void falhaExternaRetorna503Padronizado() throws Exception {
        when(competitors.search("falha"))
                .thenThrow(
                        new ApiException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "EXTERNAL_API_UNAVAILABLE",
                                "Indisponivel"));
        String token = login("user", "user123");

        mockMvc.perform(
                        get("/api/competitors/search?q=falha")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("EXTERNAL_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.path").value("/api/competitors/search"));
    }

    @Test
    @Order(11)
    void rotaDeAtributosRemovidaRetorna404Padronizado() throws Exception {
        String removedPath = "/api/specification-" + "attributes";
        String token = login("user", "user123");

        mockMvc.perform(get(removedPath).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @Order(12)
    void swaggerPublicoNaoDocumentaRotaNemControllerRemovidos() throws Exception {
        String removedPath = "/api/specification-" + "attributes";
        String removedTag = "specification-attribute-" + "controller";
        String errorSchemaPath =
                "$.paths['/api/comparisons'].post.responses['503']"
                        + ".content['application/json'].schema['$ref']";
        String errorExampleCodePath = "$.components.schemas.ApiError.properties.code.example";

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andExpect(jsonPath("$.components.schemas.ApiError").exists())
                .andExpect(jsonPath(errorSchemaPath).value("#/components/schemas/ApiError"))
                .andExpect(jsonPath(errorExampleCodePath).value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.paths['" + removedPath + "']").doesNotExist())
                .andExpect(content().string(not(containsString(removedTag))));
    }
}
