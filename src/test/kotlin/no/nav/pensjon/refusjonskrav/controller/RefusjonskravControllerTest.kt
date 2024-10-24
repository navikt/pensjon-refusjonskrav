package no.nav.pensjon.refusjonskrav.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.pensjon.refusjonskrav.config.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.OpprettRefusjonskravExceptions.*
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
import org.springframework.web.client.RestClientException
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
    fun `valid request response is 201 No Content`() {
        val request = Refusjonskrav("12345678901", "3010", 1234L, true, emptyList())

        val requestJson = jacksonObjectMapper().writeValueAsString(request)

        //samRestTemplate.postForEntity("/api/refusjonskrav", refusjonskrav, OpprettRefusjonskravResponse::class.java).body!!
        every {
            samRestTemplate.postForEntity(
                "/api/refusjonskrav/",
                request,
                OpprettRefusjonskravResponse::class.java
            )
        } returns ResponseEntity<OpprettRefusjonskravResponse>(OpprettRefusjonskravResponse(), HttpStatus.OK)

        mockMvc.post("/api/refusjonskrav/") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
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
                "/api/refusjonskrav/",
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

        mockMvc.post("/api/refusjonskrav/") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
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
                "/api/refusjonskrav/",
                request,
                OpprettRefusjonskravResponse::class.java
            )
        } returns
                ResponseEntity<OpprettRefusjonskravResponse>(
                    OpprettRefusjonskravResponse("No tpforhold exist", ELEMENT_FINNES_IKKE),
                    HttpStatus.OK
                )

        mockMvc.post("/api/refusjonskrav/") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
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
                "/api/refusjonskrav/",
                request,
                OpprettRefusjonskravResponse::class.java
            )
        } throws
                RestClientException("Unexpected exception")

        mockMvc.post("/api/refusjonskrav/") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andDo {
            print()
        }.andExpect {
            status {
                isInternalServerError()
                reason("Unexpected exception")
            }
        }
    }
}