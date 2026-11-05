package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
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
    fun `superior landlord with a postal address has a card titled with their name and a multi-line postal address row`() {
        val stepConfig = setupStepConfig()

        val card = getCards(stepConfig)[0]

        assertEquals("James Hassan", card.title)
        assertEquals("registerProperty.superiorLandlords.list.postalAddress", card.summaryList.single().fieldHeading)
        assertEquals(listOf("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"), card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `superior landlord with an email has a card titled with their name and an email address row`() {
        val stepConfig = setupStepConfig()

        val card = getCards(stepConfig)[1]

        assertEquals("Billie Smith", card.title)
        assertEquals("registerProperty.superiorLandlords.list.emailAddress", card.summaryList.single().fieldHeading)
        assertEquals("email@example.com", card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `superior landlord without known details has an unknown superior landlord card with body text and no rows`() {
        val stepConfig = setupStepConfig()

        val card = getCards(stepConfig)[2]

        assertEquals("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title", card.title)
        assertEquals("registerProperty.superiorLandlords.list.unknownSuperiorLandlord.body", card.bodyTextKey)
        assertEquals(emptyList<Any>(), card.summaryList)
    }

    @Test
    fun `cards have Change and Remove actions`() {
        val stepConfig = setupStepConfig()

        val cards = getCards(stepConfig)

        cards.forEach { card ->
            assertEquals(listOf("forms.links.change", "forms.links.remove"), card.actions!!.map { it.text })
        }
    }

    @Test
    fun `getStepSpecificContent throws when a freeholder with known details has no name`() {
        // Arrange
        val stepConfig =
            setupStepConfig(
                superiorLandlords = mapOf(1 to SuperiorLandlordDataModel(detailsKnown = true, email = "email@example.com")),
                stubAddAnotherStep = false,
            )

        // Act, Assert
        assertThrows<PrsdbWebException> { stepConfig.getStepSpecificContent(mockState) }
    }

    @Test
    fun `getStepSpecificContent throws when a freeholder with known details has no email or postal address`() {
        // Arrange
        val stepConfig =
            setupStepConfig(
                superiorLandlords = mapOf(1 to SuperiorLandlordDataModel(detailsKnown = true, name = "Billie Smith")),
                stubAddAnotherStep = false,
            )

        // Act, Assert
        assertThrows<PrsdbWebException> { stepConfig.getStepSpecificContent(mockState) }
    }

    @Test
    fun `afterStepIsReached clears the superior landlord being edited`() {
        val stepConfig = SuperiorLandlordsListStepConfig(urlParameterService)

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).editingSuperiorLandlordId = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCards(stepConfig: SuperiorLandlordsListStepConfig) =
        stepConfig.getStepSpecificContent(mockState)["summaryCards"] as List<SummaryCardViewModel>

    private fun setupStepConfig(
        superiorLandlords: Map<Int, SuperiorLandlordDataModel>? = DEFAULT_SUPERIOR_LANDLORDS,
        stubCardActionSteps: Boolean = true,
        stubAddAnotherStep: Boolean = true,
    ): SuperiorLandlordsListStepConfig {
        val stepConfig = SuperiorLandlordsListStepConfig(urlParameterService)
        stepConfig.urlPath = SuperiorLandlordsListStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockState.superiorLandlordsMap).thenReturn(superiorLandlords)
        if (stubAddAnotherStep) {
            whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(superiorLandlordProvideDetailsStep)
            whenever(superiorLandlordProvideDetailsStep.urlPath).thenReturn(ProvideSuperiorLandlordDetailsStep.ROUTE_SEGMENT)
            whenever(superiorLandlordProvideDetailsStep.currentJourneyId).thenReturn("journey-123")
            whenever(superiorLandlordProvideDetailsStep.isStepReachable).thenReturn(true)
        }
        if (stubCardActionSteps) {
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
