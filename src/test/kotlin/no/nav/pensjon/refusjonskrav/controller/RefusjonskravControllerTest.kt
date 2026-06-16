package no.nav.pensjon.refusjonskrav.controller

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.nimbusds.jose.JOSEObjectType
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.RefusjonskravService
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptorBuilder
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.pen.PenClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.wiremock.spring.EnableWireMock
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@EnableMockOAuth2Server
@EnableWireMock
internal class RefusjonskravControllerTest {

    @MockkBean
    @Suppress("Unused")
    private lateinit var azureM2MTokenInterceptorBuilder: AzureM2MTokenInterceptorBuilder

    @MockkBean
    private lateinit var samAzureM2MTokenInterceptor: AzureM2MTokenInterceptor

    @MockkSpyBean
    private lateinit var samClient: SamClient

    @MockkBean
    private lateinit var tpAzureM2MTokenInterceptor: AzureM2MTokenInterceptor

    @MockkSpyBean
    private lateinit var tpClient: TpClient

    @MockkBean
    private lateinit var osAzureM2MTokenInterceptor: AzureM2MTokenInterceptor

    @MockkSpyBean
    private lateinit var osClient: OsClient

    @MockkBean
    private lateinit var penAzureM2MTokenInterceptor: AzureM2MTokenInterceptor

    @MockkSpyBean
    private lateinit var penClient: PenClient

    @MockkBean
    private lateinit var refusjonskravService: RefusjonskravService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var server: MockOAuth2Server

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        every { samAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
        every { samAzureM2MTokenInterceptor.andThen(any()) } answers { callOriginal() }
        every { tpAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
        every { tpAzureM2MTokenInterceptor.andThen(any()) } answers { callOriginal() }
        every { penAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
        every { penAzureM2MTokenInterceptor.andThen(any()) } answers { callOriginal() }
        every { osAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
        every { osAzureM2MTokenInterceptor.andThen(any()) } answers { callOriginal() }
    }

    @Test
    fun `Valid request response is 201 No Content`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        stubFor(get("/actuator/health/readiness").willReturn(ok("ready")))
        every { refusjonskravService.behandleRefusjonskrav(request, null) } just runs

        mockMvc.post("/api/refusjonskrav") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            headers {
                setBearerAuth(mockEntraIdToken())
            }
        }.andDo { print() }.andExpect {
            status {
                isNoContent()
            }
        }

        verify { samClient.ping() }
        verify { tpClient.ping() }
        verify { osClient.ping() }
        verify { penClient.ping() }
    }

    @Test
    fun `Intercepts and captures orgno from Maskinporten token`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())
        val orgno = "12345678"

        stubFor(get("/actuator/health/readiness").willReturn(ok("ready")))
        every { refusjonskravService.behandleRefusjonskrav(request, orgno) } just runs

        mockMvc.post("/api/refusjonskrav") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            headers {
                setBearerAuth(mockMaskinportenToken(orgno))
            }
        }.andDo { print() }.andExpect {
            status {
                isNoContent()
            }
        }

        verify { samClient.ping() }
        verify { tpClient.ping() }
        verify { osClient.ping() }
        verify { penClient.ping() }
    }

    @Test
    fun `Responds 503 Service Unavailable when upstreams are not available`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        stubFor(get("/actuator/health/readiness").willReturn(notFound()))
        every { refusjonskravService.behandleRefusjonskrav(request, null) } just runs

        mockMvc.post("/api/refusjonskrav") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            headers {
                setBearerAuth(mockEntraIdToken())
            }
        }.andDo { print() }.andExpect {
            status {
                isServiceUnavailable()
            }
        }
    }

    @Test
    fun `test for ekstern bruk av ping`() {
        val token = mockEntraIdToken()

        stubFor(get("/actuator/health/readiness").willReturn(ok("ready")))
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
                content { string("true") }
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

    fun mockMaskinportenToken(orgno: String): String = token(
        issuerId = "maskinporten",
        audience = listOf("refusjonskrav-test", "tp"),
        claims = mapOf(
            "azp_name" to UUID.randomUUID().toString(),
            "idtyp" to "app",
            "azp_name" to "MockOAuth2Server",
            "scope" to "nav:pensjon/refusjonskrav",
            "consumer" to mapOf("ID" to "0192:$orgno")
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
