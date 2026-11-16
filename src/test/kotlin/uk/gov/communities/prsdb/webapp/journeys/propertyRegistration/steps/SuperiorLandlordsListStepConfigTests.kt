package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.SuperiorLandlordSummaryCardHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class SuperiorLandlordsListStepConfigTests {
    @Mock
    lateinit var mockState: SuperiorLandlordsDetailsState

    @Mock
    lateinit var urlParameterService: CollectionKeyParameterService

    @Mock
    lateinit var superiorLandlordProvideDetailsStep: ProvideSuperiorLandlordDetailsStep

    @Mock
    lateinit var setStateForSuperiorLandlordEditStep: SetStateForSuperiorLandlordEditStep

    @Mock
    lateinit var confirmRemoveSuperiorLandlordStep: ConfirmRemoveSuperiorLandlordStep

    @Test
    fun `heading uses the superior landlords list heading message key`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("registerProperty.superiorLandlords.list.heading", content["addAnotherTitle"])
    }

    @Test
    fun `heading parameter is the number of superior landlords`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(3, content["optionalAddAnotherTitleParam"])
    }

    @Test
    fun `cards have Change and Remove actions`() {
        val stepConfig = setupStepConfig()

        val cards = getCards(stepConfig)

        assertEquals(
            listOf("James Hassan", "Billie Smith", "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title"),
            cards.map { it.title },
        )
        cards.forEach { card ->
            assertEquals(listOf("forms.links.change", "forms.links.remove"), card.actions!!.map { it.text })
        }
    }

    @Test
    fun `afterStepIsReached clears the superior landlord being edited`() {
        val stepConfig = SuperiorLandlordsListStepConfig(urlParameterService, SuperiorLandlordSummaryCardHelper())

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).editingSuperiorLandlordId = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCards(stepConfig: SuperiorLandlordsListStepConfig) =
        stepConfig.getStepSpecificContent(mockState)["summaryCards"] as List<SummaryCardViewModel>

    private fun setupStepConfig(): SuperiorLandlordsListStepConfig {
        val stepConfig = SuperiorLandlordsListStepConfig(urlParameterService, SuperiorLandlordSummaryCardHelper())
        stepConfig.urlPath = SuperiorLandlordsListStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockState.superiorLandlordsMap).thenReturn(DEFAULT_SUPERIOR_LANDLORDS)
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(superiorLandlordProvideDetailsStep)
        whenever(superiorLandlordProvideDetailsStep.urlPath).thenReturn(ProvideSuperiorLandlordDetailsStep.ROUTE_SEGMENT)
        whenever(superiorLandlordProvideDetailsStep.currentJourneyId).thenReturn("journey-123")
        whenever(superiorLandlordProvideDetailsStep.isStepReachable).thenReturn(true)
        whenever(mockState.setStateForSuperiorLandlordEditStep).thenReturn(setStateForSuperiorLandlordEditStep)
        whenever(setStateForSuperiorLandlordEditStep.urlPath).thenReturn(SetStateForSuperiorLandlordEditStep.ROUTE_SEGMENT)
        whenever(setStateForSuperiorLandlordEditStep.currentJourneyId).thenReturn("journey-123")
        whenever(setStateForSuperiorLandlordEditStep.isStepReachable).thenReturn(true)
        whenever(mockState.confirmRemoveSuperiorLandlordStep).thenReturn(confirmRemoveSuperiorLandlordStep)
        whenever(confirmRemoveSuperiorLandlordStep.urlPath).thenReturn(ConfirmRemoveSuperiorLandlordStep.ROUTE_SEGMENT)
        whenever(confirmRemoveSuperiorLandlordStep.currentJourneyId).thenReturn("journey-123")
        whenever(confirmRemoveSuperiorLandlordStep.isStepReachable).thenReturn(true)
        whenever(urlParameterService.createParameterPair(any())).thenAnswer {
            "superiorLandlordId" to it.getArgument<Int>(0).toString()
        }
        return stepConfig
    }

    companion object {
        private val DEFAULT_SUPERIOR_LANDLORDS =
            mapOf(
                1 to
                    SuperiorLandlordDataModel(
                        detailsKnown = true,
                        name = "James Hassan",
                        address = AddressDataModel.fromManualAddressData("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"),
                    ),
                2 to SuperiorLandlordDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com"),
                3 to SuperiorLandlordDataModel(detailsKnown = false),
            )
    }
}
