package no.nav.pensjon.refusjonskrav.service.interceptor

import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import com.sun.net.httpserver.HttpServer
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.pensjon.refusjonskrav.config.RestConfig
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AzureM2MTokenInterceptorTest {
    @Test
    fun `real token client requests SAM scope and supplies the outgoing bearer token`() {
        val tokenRequest = AtomicReference<Map<String, String>>()
        val accessToken = PlainJWT(
            JWTClaimsSet.Builder().expirationTime(Date.from(Instant.now().plusSeconds(3600))).build()
        ).serialize()
        val tokenServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        tokenServer.createContext("/token") { exchange ->
            exchange.use {
                val parameters = it.requestBody.bufferedReader(UTF_8).readText()
                    .split("&")
                    .associate { parameter ->
                        val (key, value) = parameter.split("=", limit = 2)
                        URLDecoder.decode(key, UTF_8) to URLDecoder.decode(value, UTF_8)
                    }
                tokenRequest.set(parameters)
                val response = """{"access_token":"$accessToken","token_type":"Bearer","expires_in":3600}"""
                    .toByteArray(UTF_8)
                it.responseHeaders.add("Content-Type", APPLICATION_JSON.toString())
                it.sendResponseHeaders(200, response.size.toLong())
                it.responseBody.write(response)
            }
        }
        tokenServer.start()
        mockkStatic(AzureAdTokenClientBuilder::class)
        try {
            val builder = spyk(
                AzureAdTokenClientBuilder.builder()
                    .withClientId("test-client")
                    .withPrivateJwk(RSAKeyGenerator(2048).keyID("test-key").generate().toJSONString())
                    .withTokenEndpointUrl("http://127.0.0.1:${tokenServer.address.port}/token")
            )
            every { AzureAdTokenClientBuilder.builder() } returns builder
            every { builder.withNaisDefaults() } returns builder
            val interceptor = AzureM2MTokenInterceptor("api://sam/.default")
            val template = RestConfig("http://localhost", interceptor, RestTemplateMdcInterceptor()).samRestTemplate()
            val sam = MockRestServiceServer.bindTo(template).build()
            sam.expect(requestTo("http://localhost/api/refusjonskrav/ping"))
                .andExpect(header("Authorization", "Bearer $accessToken"))
                .andRespond(withSuccess("pong", APPLICATION_JSON))

            assertEquals("pong", template.getForObject("/api/refusjonskrav/ping", String::class.java))
            assertEquals("api://sam/.default", tokenRequest.get()["scope"])
            assertEquals("client_credentials", tokenRequest.get()["grant_type"])
            assertTrue(tokenRequest.get()["client_assertion"].orEmpty().isNotBlank())
            sam.verify()
        } finally {
            unmockkStatic(AzureAdTokenClientBuilder::class)
            tokenServer.stop(0)
        }
    }
}
