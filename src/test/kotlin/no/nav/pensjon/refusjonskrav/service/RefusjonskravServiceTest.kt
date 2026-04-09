package no.nav.pensjon.refusjonskrav.service

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import io.mockk.verify
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.domain.Refusjonstrekk
import no.nav.pensjon.refusjonskrav.domain.TrekkType
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptorBuilder
import no.nav.pensjon.refusjonskrav.service.kafka.LukkVedtakMeldingProducer
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.pen.PenClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.*
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.VedtakStatus.BESVART
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.ForholdDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.OrdningDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.PersonDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.Ytelse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.web.WebAppConfiguration
import java.time.LocalDate
import java.time.LocalDateTime

@EmbeddedKafka
@EnableMockOAuth2Server
@SpringBootTest
@WireMockTest(httpPort = 8080)
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefusjonskravServiceTest {

    @MockkBean
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

    @MockkSpyBean
    private lateinit var lukkVedtakMeldingProducer: LukkVedtakMeldingProducer

    @Autowired
    private lateinit var refusjonskravService: RefusjonskravService

    @BeforeAll
    fun setup() {
        every { samAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
        every { tpAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
        every { penAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
        every { osAzureM2MTokenInterceptor.intercept(any(), any(), any()) }.answers { it.invocation.run {
            (args[2] as ClientHttpRequestExecution).execute(args[0] as HttpRequest, args[1] as ByteArray)
        } }
    }

    @Test
    fun `Behandle refusjonkrav med standard trekktype`() {
        val person = PersonDto(
            forhold = setOf(ForholdDto(
                ytelser = setOf(Ytelse(
                    innmeldtFom = LocalDate.of(2001, 1, 1),
                    ytelseKode = "ALDER"
                ))
            ))
        )
        val ordning = OrdningDto(
            navn = "",
            tpNr = "3110",
            orgNr = "12345678",
            tssId = "8000012345",
        )
        val melding = Melding(
            samId = 1234L,
            vedtak = Vedtak(
                samVedtakId = 1234L,
                person = "12345678910",
                fagomrade = Fagomrade.PEN,
                fagVedtakId = 4321L,
                vedtakStatus = VedtakStatus.SENDT,
                art = ArtTypeCode.ALDER,
                alleMeldingerBesvart = false,
                dateFom = LocalDate.of(2001, 1, 1),
                dateTom = LocalDate.of(2021, 1, 1),
            ),
            datoSvart = null,
            tpNr = ordning.tpNr,
            refusjonskrav = null,
            meldingStatus = MeldingStatus.SENDT
        )
        val updatedMelding = melding.copy(
            meldingStatus = MeldingStatus.BESVART,
            datoSvart = LocalDate.now(),
            vedtak = melding.vedtak.copy(
                alleMeldingerBesvart = true
            )
        )
        val refusjonskrav = Refusjonskrav(
            samId = melding.samId,
            tpNr = melding.tpNr,
            pid = melding.vedtak.person,
            refusjonskrav = true,
            periodisertBelopListe = listOf(Refusjonstrekk(
                belop = 10000.0,
                kravstillersRef = "Bogus",
                datoFom = LocalDateTime.of(2002, 1, 1, 0, 0),
                datoTom = LocalDateTime.of(2003, 1, 1, 0, 0)
            )),
        )
        stubFor(get("/api/melding/${melding.samId}").willReturn(okForJson(melding)))
        stubFor(get("/api/tpconfig/organisation/validate/${melding.tpNr}_${ordning.orgNr}").willReturn(okForJson(true)))
        stubFor(post("/api/hendelse").willReturn(ok()))
        stubFor(patch("/api/melding/${melding.samId}/status").willReturn(okForJson(updatedMelding)))
        stubFor(get("/api/finnForholdForBruker?fnr=${melding.vedtak.person}&tpnr=${melding.tpNr}").willReturn(okForJson(person)))
        stubFor(get("/api/ordning?tpnr=${ordning.tpNr}").willReturn(okForJson(ordning)))
        stubFor(post("/api/nav-cons-sto-sam-trekk/opprettAndreTrekk").willReturn(ok()))
        stubFor(post("/api/vedtak/${updatedMelding.vedtak.fagVedtakId}/mottaSamhandlerSvar").willReturn(ok()))
        stubFor(patch("/api/vedtak/${updatedMelding.vedtak.samVedtakId}"))

        refusjonskravService.behandleRefusjonskrav(
            refusjonskrav = refusjonskrav,
            orgno = ordning.orgNr
        )

        verify { samClient.hentMelding(melding.samId) }
        verify { tpClient.validateTpnr(melding.tpNr, ordning.orgNr) }
        verify { samClient.opprettHendelse(melding.vedtak.person, melding.tpNr) }
        verify { samClient.updateMelding(
            melding,
            refusjonskrav.refusjonskrav,
            any(),
            MeldingStatus.BESVART
        ) }
        verify { tpClient.getYtelser(updatedMelding.vedtak.person, updatedMelding.tpNr) }
        verify { tpClient.getTssEksternId(ordning.tpNr) }
        verify { osClient.opprettAndreTrekk(match { req ->
            req.andreTrekkList.size == 1
                    && req.andreTrekkList.all {
                        it.debitorOffnr == updatedMelding.vedtak.person
                                && it.trekktypeKode == TrekkType.RAPE
                                && it.endringsInfo.kildeId == ordning.tssId
                                && it.tssEksternId == "80000434937"
                                && it.kreditorRef == refusjonskrav.periodisertBelopListe.first().kravstillersRef
                    }
        }) }
        verify { penClient.lukkVedtak(updatedMelding.vedtak) }
        verify { samClient.oppdaterVedtak(updatedMelding.vedtak, BESVART) }
    }
}
