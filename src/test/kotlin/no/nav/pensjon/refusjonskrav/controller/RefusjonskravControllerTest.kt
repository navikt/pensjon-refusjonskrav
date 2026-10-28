package no.nav.pensjon.refusjonskrav.controller

import com.nimbusds.jose.JOSEObjectType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.domain.Refusjonstrekk
import no.nav.pensjon.refusjonskrav.service.OpprettRefusjonskravExceptions.ALLEREDE_REGISTRERT_ELLER_UTENFOR_FRIST
import no.nav.pensjon.refusjonskrav.service.OpprettRefusjonskravExceptions.ELEMENT_FINNES_IKKE
import no.nav.pensjon.refusjonskrav.service.OpprettRefusjonskravResponse
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureMockMvc
internal class RefusjonskravControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var server: MockOAuth2Server

    @MockkBean
    private lateinit var azureM2MTokenInterceptor: AzureM2MTokenInterceptor

    @MockkBean
    private lateinit var samRestTemplate: RestTemplate

    @Test
    fun `JSON preserves request fields and serializes period dates as epoch milliseconds`() {
        val input = """
            {
                "pid": "12345678901",
                "tpNr": "3010",
                "samId": 1234,
                "refusjonskrav": true,
                "periodisertBelopListe": [{
                    "belop": 1234.5,
                    "kravstillersRef": "ref-1",
                    "datoFom": "2026-01-01T00:00:00.000",
                    "datoTom": "2026-01-31T23:59:59.000"
                }]
            }
        """.trimIndent()
        val fom = LocalDateTime.of(2026, 1, 1, 0, 0)
        val tom = LocalDateTime.of(2026, 1, 31, 23, 59, 59)
        val expected = Refusjonskrav(
            "12345678901", "3010", 1234L, true,
            listOf(Refusjonstrekk(1234.5, "ref-1", fom, tom))
        )

        val decoded = objectMapper.readValue(input, Refusjonskrav::class.java)

        assertEquals(expected, decoded)
        val expectedJson = objectMapper.readTree(input)
        val period = expectedJson["periodisertBelopListe"][0] as ObjectNode
        period.put("datoFom", fom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        period.put("datoTom", tom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        assertEquals(expectedJson, objectMapper.readTree(objectMapper.writeValueAsString(decoded)))
    }

    @Test
    fun `valid request response is 201 No Content`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        val requestJson = jacksonObjectMapper().writeValueAsString(request)

        //samRestTemplate.postForEntity("/api/refusjonskrav", refusjonskrav, OpprettRefusjonskravResponse::class.java).body!!
        every {
            samRestTemplate.postForEntity(
                "/api/refusjonskrav",
                request,
                OpprettRefusjonskravResponse::class.java
            )
        } returns ResponseEntity<OpprettRefusjonskravResponse>(OpprettRefusjonskravResponse(), HttpStatus.OK)

        every {
            samRestTemplate.getForEntity("/api/refusjonskrav/ping", String::class.java)
        } returns ResponseEntity<String>(
            "pong",
            HttpStatus.OK
        )

        mockMvc.post("/api/refusjonskrav") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
            headers {
                setBearerAuth(mockEntraIdToken())
            }
        }.andDo { print() }.andExpect {
            status {
                isNoContent()
            }
        }
    }

    @Test
    fun `ALLEREDE_REGISTRERT_ELLER_UTENFOR_FRIST response is 409 Conflict`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        val requestJson = jacksonObjectMapper().writeValueAsString(request)

        //samRestTemplate.postForEntity("/api/refusjonskrav", refusjonskrav, OpprettRefusjonskravResponse::class.java).body!!
        every {
            samRestTemplate.postForEntity(
                "/api/refusjonskrav",
                request,
                OpprettRefusjonskravResponse::class.java
            )
        } returns
                ResponseEntity<OpprettRefusjonskravResponse>(
                    OpprettRefusjonskravResponse(
                        "Message with id = ${request.samId} has already been answered or time limit is exceeded.",
                        ALLEREDE_REGISTRERT_ELLER_UTENFOR_FRIST
                    ),
                    HttpStatus.OK
                )

        every {
            samRestTemplate.getForEntity("/api/refusjonskrav/ping", String::class.java)
        } returns ResponseEntity<String>(
            "pong",
            HttpStatus.OK
        )

        mockMvc.post("/api/refusjonskrav") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
            headers {
                setBearerAuth(mockEntraIdToken())
            }
        }.andDo {
            print()
        }.andExpect {
            status {
                isConflict()
                reason("Message with id = 1234 has already been answered or time limit is exceeded.")
            }
        }
    }

    @Test
    fun `ELEMENT_FINNES_IKKE response is 404 Not Found`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        val requestJson = jacksonObjectMapper().writeValueAsString(request)

        //samRestTemplate.postForEntity("/api/refusjonskrav", refusjonskrav, OpprettRefusjonskravResponse::class.java).body!!
        every {
            samRestTemplate.postForEntity(
                "/api/refusjonskrav",
                request,
                OpprettRefusjonskravResponse::class.java
            )
        } returns
                ResponseEntity<OpprettRefusjonskravResponse>(
                    OpprettRefusjonskravResponse("No tpforhold exist", ELEMENT_FINNES_IKKE),
                    HttpStatus.NOT_FOUND
                )

        every {
            samRestTemplate.getForEntity("/api/refusjonskrav/ping", String::class.java)
        } returns ResponseEntity<String>(
            "pong",
            HttpStatus.OK
        )

        mockMvc.post("/api/refusjonskrav") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
            headers {
                setBearerAuth(mockEntraIdToken())
            }
        }.andDo {
            print()
        }.andExpect {
            status {
                isNotFound()
                reason("No tpforhold exist")
            }
        }
    }

    @Test
    fun `Unexpected exception mot sam is 500 Internal Server Error`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        val requestJson = jacksonObjectMapper().writeValueAsString(request)

        //samRestTemplate.postForEntity("/api/refusjonskrav", refusjonskrav, OpprettRefusjonskravResponse::class.java).body!!
        every {
            samRestTemplate.postForEntity(
                "/api/refusjonskrav",
                request,
                OpprettRefusjonskravResponse::class.java
            )
        } throws
                RestClientException("Unexpected exception")

        every {
            samRestTemplate.getForEntity("/api/refusjonskrav/ping", String::class.java)
        } returns ResponseEntity<String>(
            "pong",
            HttpStatus.OK
        )

        mockMvc.post("/api/refusjonskrav") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
            headers {
                setBearerAuth(mockEntraIdToken())
            }
        }.andDo {
            print()
        }.andExpect {
            status {
                isBadGateway()
                reason("Unexpected exception")
            }
        }
    }

    @Test
    fun `test for ekstern bruk av ping`() {
        val token = mockEntraIdToken()

        mockMvc.get("/api/ping") {
            contentType = MediaType.APPLICATION_JSON
            headers {
                setBearerAuth(token)
                contentType = MediaType.APPLICATION_JSON
            }
        }.andDo {
            print()
        }.andExpect {
            status {
                isOk()
                content { true }
            }
        }

    }

    fun mockEntraIdToken(): String = token(
        issuerId = "entraID",
        audience = listOf("refusjonskrav-test", "tp"),
        claims = mapOf(
            "azp_name" to UUID.randomUUID().toString(),
            "idtyp" to "app",
            "azp_name" to "MockOAuth2Server",
        )
    )

    private fun token(
        issuerId: String,
        audience: List<String>,
        subject: String = UUID.randomUUID().toString(),
        claims: Map<String, Any>): String {

        return server.issueToken(
            issuerId = issuerId,
            clientId = "test-client",
            tokenCallback = DefaultOAuth2TokenCallback(
                issuerId = issuerId,
                typeHeader = JOSEObjectType.JWT.type,
                audience = audience,
                subject = subject,
                claims = claims,
                expiry = 3322L
            )
        ).serialize()
    }

}
