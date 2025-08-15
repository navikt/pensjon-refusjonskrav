package no.nav.pensjon.refusjonskrav.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.nimbusds.jose.JOSEObjectType
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.OpprettRefusjonskravResponse
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.*

@SpringBootTest(classes = [TestConfig::class], properties = ["spring.main.allow-bean-definition-overriding=true"])
@EnableMockOAuth2Server
@AutoConfigureMockMvc
@WireMockTest(httpPort = 9090)
class RefusjonskravControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var server: MockOAuth2Server

    @Test
    fun `run integration test from incoming post to call wiremock sam`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())
        val requestJson = jacksonObjectMapper().writeValueAsString(request)

        stubFor(
    get("/api/refusjonskrav/ping")
            .willReturn(
                aResponse().withStatus(204)
            )
        )

        stubFor(
            post("/api/refusjonskrav")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jacksonObjectMapper().writeValueAsString(OpprettRefusjonskravResponse()))
                )
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

@TestConfiguration
class TestConfig(
    @Value("\${sam.url}")
    private val samUrl: String,) {

    @Bean
    fun samRestTemplate(): RestTemplate = RestTemplateBuilder()
        .rootUri(samUrl)
        .connectTimeout(Duration.ofSeconds(20))
        .readTimeout(Duration.ofSeconds(20))
        .build()
}
