package no.nav.pensjon.refusjonskrav.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.pensjon.refusjonskrav.config.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.OpprettRefusjonskravResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.web.client.RestTemplate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RefusjonskravControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var azureM2MTokenInterceptor: AzureM2MTokenInterceptor

    @MockkBean
    private lateinit var samRestTemplate: RestTemplate

    @Test
    fun `valid request respons is 200 OK`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        val requestJson = jacksonObjectMapper().writeValueAsString(request)

        //samRestTemplate.postForEntity("/api/refusjonskrav", refusjonskrav, OpprettRefusjonskravResponse::class.java).body!!
        every { samRestTemplate.postForEntity("/api/refusjonskrav/", request, OpprettRefusjonskravResponse::class.java)  } returns ResponseEntity<OpprettRefusjonskravResponse>(OpprettRefusjonskravResponse(false), HttpStatus.OK,)

        mockMvc.post("/api/refusjonskrav/") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andDo { print() } .andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

}