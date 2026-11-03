package no.nav.pensjon.refusjonskrav.service.interceptor

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import no.nav.pensjon.refusjonskrav.config.RestConfig
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.getForObject
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals

@WireMockTest(httpPort = 8089)
internal class AzureM2MTokenInterceptorTest {
    @Test
    fun `real token client requests SAM scope and supplies the outgoing bearer token`() {
        val accessToken = PlainJWT(
            JWTClaimsSet.Builder().expirationTime(Date.from(Instant.now().plusSeconds(3600))).build()
        ).serialize()

        stubFor(
            post(urlEqualTo("/"))
                .withRequestBody(
                    equalToJson("""{"identity_provider":"entra_id","target":"api://sam/.default"}""")
                )
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON.toString())
                        .withBody("""{"access_token":"$accessToken","token_type":"Bearer","expires_in":3600}""")
                )
        )

        val interceptor = AzureM2MTokenInterceptor("api://sam/.default", "http://localhost:8089")
        val template = RestConfig("http://localhost", interceptor, RestTemplateMdcInterceptor()).samRestTemplate()
        val sam = MockRestServiceServer.bindTo(template).build()
        sam.expect(requestTo("http://localhost/api/refusjonskrav/ping"))
            .andExpect(header("Authorization", "Bearer $accessToken"))
            .andRespond(withSuccess("pong", APPLICATION_JSON))

        assertEquals("pong", template.getForObject<String>("/api/refusjonskrav/ping"))
        verify(
            1,
            postRequestedFor(urlEqualTo("/"))
                .withRequestBody(
                    equalToJson("""{"identity_provider":"entra_id","target":"api://sam/.default"}""")
                )
        )
        sam.verify()
    }
}
