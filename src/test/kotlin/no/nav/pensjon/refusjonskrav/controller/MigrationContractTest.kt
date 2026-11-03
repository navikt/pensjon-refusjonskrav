package no.nav.pensjon.refusjonskrav.controller

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.pensjon.refusjonskrav.config.MaskinportenValidator
import no.nav.pensjon.refusjonskrav.service.OpprettRefusjonskravExceptions
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["logging.config=classpath:logback-json.xml"]
)
@EnableMockOAuth2Server
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension::class)
internal class MigrationContractTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var server: MockOAuth2Server

    @Autowired
    private lateinit var samRestTemplate: RestTemplate

    @Autowired
    private lateinit var maskinportenValidator: MaskinportenValidator

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var azureM2MTokenInterceptor: AzureM2MTokenInterceptor

    private lateinit var sam: MockRestServiceServer
    private lateinit var tp: MockRestServiceServer

    private val request = """
        {
            "pid": "12345678901",
            "tpNr": "3010",
            "samId": 1234,
            "refusjonskrav": true,
            "periodisertBelopListe": [{
                "belop": 1234.5,
                "kravstillersRef": "ref-1",
                "datoFom": "2026-01-01T00:00:00",
                "datoTom": "2026-01-31T23:59:59"
            }]
        }
    """.trimIndent()

    @BeforeEach
    fun setUp() {
        sam = MockRestServiceServer.bindTo(samRestTemplate).build()
        val tpRestTemplate = ReflectionTestUtils.getField(maskinportenValidator, "restTemplate") as RestTemplate
        tp = MockRestServiceServer.bindTo(tpRestTemplate).build()
        every { azureM2MTokenInterceptor.andThen(any()) } answers { callOriginal() }
        every { azureM2MTokenInterceptor.intercept(any(), any(), any()) } answers {
            val outgoing = firstArg<HttpRequest>()
            outgoing.headers.setBearerAuth("sam-test-token")
            thirdArg<ClientHttpRequestExecution>().execute(outgoing, secondArg())
        }
    }

    @AfterEach
    fun verifyDownstreamRequests() {
        sam.verify()
        tp.verify()
    }

    @Test
    fun `missing tokens are rejected without downstream calls`() {
        mockMvc.get("/api/ping").andExpect { status { isUnauthorized() } }
        mockMvc.post("/api/refusjonskrav") {
            contentType = APPLICATION_JSON
            content = request
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `invalid tokens are rejected without downstream calls`() {
        val tokens = listOf(
            token(audience = listOf("wrong-audience")),
            token(expiry = -60),
            token(issuer = "unknown"),
            token(issuer = "maskinporten", claims = emptyMap()),
            token(issuer = "maskinporten", claims = mapOf("scope" to "wrong:scope")),
            token(issuer = "maskinporten", claims = mapOf("scope" to "nav:pensjon/refusjonskrav other:scope")),
            PlainJWT(
                JWTClaimsSet.Builder()
                    .issuer(server.issuerUrl("maskinporten").toString())
                    .claim("scope", "nav:pensjon/refusjonskrav")
                    .build()
            ).serialize()
        )
        tokens.forEach { invalidToken ->
            mockMvc.get("/api/ping") {
                headers { setBearerAuth(invalidToken) }
            }.andExpect { status { isUnauthorized() } }
            postRequest(invalidToken).andExpect { status { isUnauthorized() } }
        }
    }

    @Test
    fun `maskinporten validates organisation and preserves outgoing JSON and token isolation`() {
        tp.expect(requestTo("http://localhost/api/tpconfig/organisation/validate/3010_999888777"))
            .andExpect(headerDoesNotExist("Authorization"))
            .andRespond(withSuccess("true", APPLICATION_JSON))
        expectSamPing()
        val expectedJson = objectMapper.readTree(request) as ObjectNode
        val period = expectedJson["periodisertBelopListe"][0] as ObjectNode
        period.put("datoFom", epochMillis(LocalDateTime.of(2026, 1, 1, 0, 0)))
        period.put("datoTom", epochMillis(LocalDateTime.of(2026, 1, 31, 23, 59, 59)))
        sam.expect(requestTo("http://localhost/api/refusjonskrav"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer sam-test-token"))
            .andExpect(content().json(expectedJson.toString(), JsonCompareMode.STRICT))
            .andRespond(withSuccess("{}", APPLICATION_JSON))

        postRequest(maskinportenToken()).andExpect { status { isNoContent() } }
    }

    @ParameterizedTest
    @ValueSource(ints = [404, 500])
    fun `tp errors preserve response status and do not call SAM`(tpStatus: Int) {
        tp.expect(requestTo("http://localhost/api/tpconfig/organisation/validate/3010_999888777"))
            .andRespond(withStatus(HttpStatus.valueOf(tpStatus)))

        postRequest(maskinportenToken()).andExpect {
            status { isEqualTo(if (tpStatus == 404) 403 else 502) }
        }
    }

    @ParameterizedTest
    @EnumSource(OpprettRefusjonskravExceptions::class)
    fun `SAM error responses preserve status and message`(exception: OpprettRefusjonskravExceptions) {
        expectSamPing()
        sam.expect(requestTo("http://localhost/api/refusjonskrav"))
            .andRespond(withSuccess("""{"message":"sam-test-error","exceptionType":"${exception.name}"}""", APPLICATION_JSON))

        postRequest(token()).andExpect {
            status {
                isEqualTo(exception.status.value())
                reason("sam-test-error")
            }
        }
    }

    @Test
    fun `health remains available without exposing other actuator endpoints`() {
        mockMvc.get("/actuator/health").andExpect { status { isOk() } }
        mockMvc.get("/actuator/env").andExpect { status { isNotFound() } }
    }

    @Test
    fun `prometheus exposes JVM metrics`() {
        mockMvc.get("/actuator/prometheus").andExpect {
            status { isOk() }
            content { string(containsString("jvm_")) }
        }
    }

    @Test
    fun `JSON logging still masks personal identifiers`(output: CapturedOutput) {
        LoggerFactory.getLogger(javaClass).info("Logging contract: {}", "12345678901")

        assertTrue(output.out.contains("Logging contract: 123456*****"))
        assertFalse(output.out.contains("Logging contract: 12345678901"))
    }

    private fun expectSamPing() {
        sam.expect(requestTo("http://localhost/api/refusjonskrav/ping"))
            .andExpect(header("Authorization", "Bearer sam-test-token"))
            .andRespond(withSuccess("pong", APPLICATION_JSON))
    }

    private fun postRequest(bearerToken: String) = mockMvc.post("/api/refusjonskrav") {
        contentType = APPLICATION_JSON
        content = request
        headers { setBearerAuth(bearerToken) }
    }

    private fun maskinportenToken() = token(
        issuer = "maskinporten",
        claims = mapOf(
            "scope" to "nav:pensjon/refusjonskrav",
            "consumer" to mapOf("ID" to "0192:999888777")
        )
    )

    private fun token(
        issuer: String = "entraID",
        audience: List<String> = listOf("refusjonskrav-test"),
        claims: Map<String, Any> = emptyMap(),
        expiry: Long = 3600
    ): String = server.issueToken(
        issuerId = issuer,
        clientId = "test-client",
        tokenCallback = DefaultOAuth2TokenCallback(
            issuerId = issuer,
            audience = audience,
            claims = claims,
            expiry = expiry
        )
    ).serialize()

    private fun epochMillis(date: LocalDateTime) = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
