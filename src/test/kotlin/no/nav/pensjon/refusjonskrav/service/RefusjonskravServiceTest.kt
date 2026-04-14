package no.nav.pensjon.refusjonskrav.service

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import io.mockk.verify
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.domain.Refusjonstrekk
import no.nav.pensjon.refusjonskrav.domain.TrekkType
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.CouldNotCloseVedtakException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.FutureTrekkOnRunningVedtakException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.MeldingBesvartException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.MismatchedPidException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.MismatchedTpnrException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.OrdningForbiddenException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.TrekkEndAfterVedtakException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.TrekkEndBeforeStartException
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.TrekkStartBeforeVedtakException
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptorBuilder
import no.nav.pensjon.refusjonskrav.service.kafka.LukkVedtakMeldingProducer
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.pen.PenClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.*
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.VedtakStatus.BESVART
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.VedtakStatus.IKKE_OVERFORT_PEN
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.ForholdDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.OrdningDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.PersonDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.Ytelse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.web.WebAppConfiguration
import java.time.LocalDate

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
        runTest(TestCase(
            fagomrade = Fagomrade.PEN
        ))
    }

    @Test
    fun `Behandle refusjonkrav med RS trekktype`() {
        runTest(TestCase(
            fagomrade = Fagomrade.PEN,
            trekktype = TrekktypePattern.RS
        ))
    }

    @Test
    fun `Behandle refusjonkrav med RPTS trekktype`() {
        runTest(TestCase(
            fagomrade = Fagomrade.PEN,
            trekktype = TrekktypePattern.RPTS
        ))
    }

    @Test
    fun `Behandle refusjonkrav med feil orgno`() {
        runTestFailing<OrdningForbiddenException>(TestCase(
            orgno = "00000000",
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav med feil tpnr`() {
        runTestFailing<MismatchedTpnrException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav med feil pid`() {
        runTestFailing<MismatchedPidException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav ved besvart melding`() {
        runTestFailing<MeldingBesvartException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav med trekk foer vedtak`() {
        runTestFailing<TrekkStartBeforeVedtakException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav med trekk avsluttet foer vedtak`() {
        runTestFailing<TrekkEndBeforeStartException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav med trekk etter vedtak`() {
        runTestFailing<TrekkEndAfterVedtakException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav med fremtidig trekk for loepende vedtak`() {
        runTestFailing<FutureTrekkOnRunningVedtakException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    @Test
    fun `Behandle refusjonkrav kunne ikke avsluttes`() {
        runTestFailing<CouldNotCloseVedtakException>(TestCase(
            fagomrade = Fagomrade.PEN,
        ))
    }

    private fun runTest(testCase: TestCase) = runTestFailing<Nothing?>(testCase)

    private inline fun <reified T: RefusjonskravErrorResponseException?> runTestFailing(testCase: TestCase) {
        val orgno = testCase.orgno ?: CORRECT_ORGNO

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
                dateTom = if (T::class == FutureTrekkOnRunningVedtakException::class) null else LocalDate.of(2021, 1, 1),
            ),
            datoSvart = null,
            tpNr = testCase.trekktype.tpnr,
            refusjonskrav = null,
            meldingStatus = if (T::class == MeldingBesvartException::class) MeldingStatus.BESVART else MeldingStatus.SENDT
        )
        val refusjonskrav = buildRefusjonskrav<T>(melding)

        val updatedMelding = setupStubs<T>(melding, testCase, orgno, refusjonskrav.periodisertBelopListe)

        if (null is T) refusjonskravService.behandleRefusjonskrav(
            refusjonskrav = refusjonskrav,
            orgno = testCase.orgno
        ) else assertThrows(T::class.java) {
            refusjonskravService.behandleRefusjonskrav(
                refusjonskrav = refusjonskrav,
                orgno = testCase.orgno
            )
        }

        verify<T>(melding, testCase, refusjonskrav, updatedMelding)
    }

    private fun Melding.asBesvart(alleMeldingerBesvart: Boolean): Melding = copy(
        meldingStatus = MeldingStatus.BESVART,
        datoSvart = LocalDate.now(),
        vedtak = vedtak.copy(
            alleMeldingerBesvart = alleMeldingerBesvart
        )
    )

    private inline fun <reified T: RefusjonskravErrorResponseException?> buildRefusjonskrav(melding: Melding): Refusjonskrav = Refusjonskrav(
        samId = melding.samId,
        tpNr = if (T::class == MismatchedTpnrException::class) "0000" else melding.tpNr,
        pid = if (T::class == MismatchedPidException::class) "00000000000" else melding.vedtak.person,
        refusjonskrav = true,
        periodisertBelopListe = listOf(
            Refusjonstrekk(
                belop = 10000.0,
                kravstillersRef = "Bogus",
                datoFom = melding.vedtak.trekkFom<T>(),
                datoTom = melding.vedtak.trekkTom<T>(),
            )
        ),
    )

    private inline fun <reified T: RefusjonskravErrorResponseException?> Vedtak.trekkFom() = when(T::class) {
        TrekkStartBeforeVedtakException::class -> dateFom.minusMonths(1)
        else -> dateFom
    }.atStartOfDay()

    private inline fun <reified T: RefusjonskravErrorResponseException?> Vedtak.trekkTom() = when(T::class) {
        TrekkEndBeforeStartException::class -> dateFom.minusMonths(1)
        TrekkEndAfterVedtakException::class -> dateTom!!.plusMonths(1)
        FutureTrekkOnRunningVedtakException::class -> LocalDate.now().plusMonths(3)
        else -> dateTom ?: LocalDate.now()
    }.atStartOfDay()

    private inline fun <reified T : RefusjonskravErrorResponseException?> verify(
        melding: Melding,
        testCase: TestCase,
        refusjonskrav: Refusjonskrav,
        updatedMelding: Melding?
    ) {
        verify { samClient.hentMelding(melding.samId) }
        if (testCase.orgno != null) verify { tpClient.validateTpnr(melding.tpNr, testCase.orgno) }
        when (T::class) {
            OrdningForbiddenException::class, MismatchedPidException::class,
            MismatchedTpnrException::class, MeldingBesvartException::class,
            TrekkStartBeforeVedtakException::class, TrekkEndBeforeStartException::class,
            TrekkEndAfterVedtakException::class, FutureTrekkOnRunningVedtakException::class -> return
        }

        if (refusjonskrav.periodisertBelopListe.isNotEmpty())
            verify { samClient.opprettHendelse(melding.vedtak.person, melding.tpNr) }
        else {
            verify { samClient.updateMelding(melding, false, any(), MeldingStatus.BESVART) }
            return
        }

        verify {
            samClient.updateMelding(
                melding,
                refusjonskrav.refusjonskrav,
                any(),
                MeldingStatus.BESVART
            )
        }

        assertNotNull(updatedMelding)
        verify { tpClient.getYtelser(updatedMelding.vedtak.person, updatedMelding.tpNr) }
        verify { tpClient.getTssEksternId(testCase.trekktype.tpnr) }
        verify {
            osClient.opprettAndreTrekk(match { req ->
                req.andreTrekkList.size == 1 && req.andreTrekkList.all {
                    it.debitorOffnr == updatedMelding.vedtak.person
                            && it.trekktypeKode == testCase.trekktype.alderType
                            && it.endringsInfo.kildeId == CORRECT_TSS_ID
                            && it.tssEksternId == testCase.trekktype.tssId
                            && it.kreditorRef == refusjonskrav.periodisertBelopListe.first().kravstillersRef
                }
            })
        }
        verify { penClient.lukkVedtak(updatedMelding.vedtak) }
        verify { samClient.oppdaterVedtak(updatedMelding.vedtak, if (T::class == CouldNotCloseVedtakException::class) IKKE_OVERFORT_PEN else BESVART) }
    }

    private inline fun <reified T: RefusjonskravErrorResponseException?> setupStubs(
        melding: Melding,
        testCase: TestCase,
        orgno: String,
        periodisertBelopListe: List<Refusjonstrekk>
    ): Melding? {

        stubFor(get("/api/melding/${melding.samId}").willReturnOk(melding))
        if (testCase.maskinporten) {
            if (testCase.orgno == CORRECT_ORGNO) stubFor(
                get("/api/tpconfig/organisation/validate/${melding.tpNr}_${CORRECT_ORGNO}")
                    .willReturnOk(true)
            )
            else stubFor(
                get("/api/tpconfig/organisation/validate/${melding.tpNr}_${testCase.orgno}")
                    .willReturn(responseDefinition().withStatus(404))
            )
        }
        when (T::class) {
            OrdningForbiddenException::class, MismatchedPidException::class,
            MismatchedTpnrException::class, MeldingBesvartException::class -> return null
        }

        if (periodisertBelopListe.isNotEmpty()) stubFor(post("/api/hendelse").willReturnOk())
        else {
            stubFor(patch("/api/melding/${melding.samId}/status").willReturnOk(melding))
            return null
        }

        val updatedMelding = melding.asBesvart(testCase.alleMeldingerBesvart)
        stubFor(patch("/api/melding/${melding.samId}/status").willReturnOk(updatedMelding))
        if (periodisertBelopListe.isEmpty()) return updatedMelding

        stubFor(
            get(urlPathMatching("/api/finnForholdForBruker(.*)"))
                .withQueryParam("fnr", melding.vedtak.person)
                .withQueryParam("tpnr", melding.tpNr)
                .willReturnOk(
                    PersonDto(
                        forhold = setOf(ForholdDto(
                            ytelser = setOf(Ytelse(
                                innmeldtFom = LocalDate.of(2001, 1, 1),
                                ytelseKode = "ALDER"
                            ))
                        ))
                    )
                )
        )


        val ordning = OrdningDto(
            navn = "",
            tpNr = testCase.trekktype.tpnr,
            orgNr = orgno,
            tssId = CORRECT_TSS_ID,
        )

        stubFor(get("/api/ordning?tpnr=${ordning.tpNr}").willReturnOk(ordning))
        stubFor(post("/api/nav-cons-sto-sam-trekk/opprettAndreTrekk").willReturnOk())
        if (T::class == CouldNotCloseVedtakException::class) stubFor(post("/api/vedtak/${updatedMelding.vedtak.fagVedtakId}/mottaSamhandlerSvar")
            .willReturn(responseDefinition().withStatus(500)))
            else stubFor(post("/api/vedtak/${updatedMelding.vedtak.fagVedtakId}/mottaSamhandlerSvar").willReturnOk())
        stubFor(patch("/api/vedtak/${updatedMelding.vedtak.samVedtakId}").willReturnOk())
        return updatedMelding
    }

    private data class TestCase(
        val orgno: String? = CORRECT_ORGNO,
        val fagomrade: Fagomrade,
        val trekktype: TrekktypePattern = TrekktypePattern.DEFAULT,
        val alleMeldingerBesvart: Boolean = true,
    ) {
        val maskinporten = orgno != null
    }

    private enum class TrekktypePattern(val tpnr: String, val alderType: TrekkType, val tssId: String) {
        DEFAULT(DEFAULT_TPNR, TrekkType.RAPE, DEFAULT_TSS_ID),
        RS(RS_TPNR, TrekkType.RS30, RS_TSS_ID),
        RPTS(RPTS_TPNR, TrekkType.RPTS, RPTS_TSS_ID);
    }

    private fun MappingBuilder.withQueryParam(key: String, value: String) = withQueryParam(key, equalTo(value))
    private fun MappingBuilder.willReturnOk(o: Any? = null) = willReturn(if (o == null) ok() else okForJson(o))

    companion object {
        private const val CORRECT_ORGNO = "12345678"
        private const val CORRECT_TSS_ID = "80000434937"
        private const val DEFAULT_TPNR = "3110"
        private const val DEFAULT_TSS_ID = "80000434937"
        private const val RS_TPNR = "3010"
        private const val RS_TSS_ID = "80000435771"
        private const val RPTS_TPNR = "3100"
        private const val RPTS_TSS_ID = "80000427899"
    }
}
