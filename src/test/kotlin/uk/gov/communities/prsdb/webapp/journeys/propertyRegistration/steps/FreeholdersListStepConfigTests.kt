package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class FreeholdersListStepConfigTests {
    @Mock
    lateinit var mockState: FreeholdersDetailsState

    @Mock
    lateinit var urlParameterService: CollectionKeyParameterService

    @Mock
    lateinit var freeholderProvideDetailsStep: ProvideFreeholderDetailsStep

    @Mock
    lateinit var setStateForFreeholderEditStep: SetStateForFreeholderEditStep

    @Mock
    lateinit var confirmRemoveFreeholderStep: ConfirmRemoveFreeholderStep

    @Test
    fun `heading uses the joint message key when the freeholders are joint`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isJointFreeholder).thenReturn(true)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("registerProperty.freeholders.list.heading.joint", content["addAnotherTitle"])
    }

    @Test
    fun `heading uses the single message key when the freeholders are not joint`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isJointFreeholder).thenReturn(false)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("registerProperty.freeholders.list.heading.single", content["addAnotherTitle"])
    }

    @Test
    fun `heading parameter is the number of freeholders`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(3, content["optionalAddAnotherTitleParam"])
    }

    @Test
    fun `freeholder with a postal address has a card titled with their name and a multi-line postal address row`() {
        val stepConfig = setupStepConfig()

        val card = getCards(stepConfig)[0]

        assertEquals("James Hassan", card.title)
        assertEquals("registerProperty.freeholders.list.postalAddress", card.summaryList.single().fieldHeading)
        assertEquals(listOf("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"), card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `freeholder with an email has a card titled with their name and an email address row`() {
        val stepConfig = setupStepConfig()

        val card = getCards(stepConfig)[1]

        assertEquals("Billie Smith", card.title)
        assertEquals("registerProperty.freeholders.list.emailAddress", card.summaryList.single().fieldHeading)
        assertEquals("email@example.com", card.summaryList.single().fieldValue)
        assertNull(card.bodyTextKey)
    }

    @Test
    fun `freeholder without known details has an unknown freeholder card with body text and no rows`() {
        val stepConfig = setupStepConfig()

        val card = getCards(stepConfig)[2]

        assertEquals("registerProperty.freeholders.list.unknownFreeholder.title", card.title)
        assertEquals("registerProperty.freeholders.list.unknownFreeholder.body", card.bodyTextKey)
        assertEquals(emptyList<Any>(), card.summaryList)
    }

    @Test
    fun `cards have Change and Remove actions when there is more than one freeholder`() {
        val stepConfig = setupStepConfig()

        val cards = getCards(stepConfig)

        cards.forEach { card ->
            assertEquals(listOf("forms.links.change", "forms.links.remove"), card.actions!!.map { it.text })
        }
    }

    @Test
    fun `the only freeholder has no Remove action when removing the last member is not allowed`() {
        val stepConfig = setupStepConfig(freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = false)), stubRemoveStep = false)
        whenever(mockState.allowRemovingLastMember).thenReturn(false)

        val card = getCards(stepConfig).single()

        assertEquals(listOf("forms.links.change"), card.actions!!.map { it.text })
    }

    @Test
    fun `there are no cards and the heading parameter is zero when there are no freeholders in state`() {
        val stepConfig = setupStepConfig(freeholders = null, stubCardActionSteps = false)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(0, content["optionalAddAnotherTitleParam"])
        assertEquals(emptyList<SummaryCardViewModel>(), content["summaryCards"])
    }

    @Test
    fun `afterStepIsReached clears the freeholder being edited`() {
        val stepConfig = FreeholdersListStepConfig(urlParameterService)

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).editingFreeholderId = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCards(stepConfig: FreeholdersListStepConfig) =
        stepConfig.getStepSpecificContent(mockState)["summaryCards"] as List<SummaryCardViewModel>

    private fun setupStepConfig(
        freeholders: Map<Int, FreeholderDataModel>? = DEFAULT_FREEHOLDERS,
        stubCardActionSteps: Boolean = true,
        stubRemoveStep: Boolean = stubCardActionSteps,
    ): FreeholdersListStepConfig {
        val stepConfig = FreeholdersListStepConfig(urlParameterService)
        stepConfig.urlPath = FreeholdersListStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockState.freeholdersMap).thenReturn(freeholders)
        whenever(mockState.freeholderProvideDetailsStep).thenReturn(freeholderProvideDetailsStep)
        whenever(freeholderProvideDetailsStep.urlPath).thenReturn(ProvideFreeholderDetailsStep.ROUTE_SEGMENT)
        whenever(freeholderProvideDetailsStep.currentJourneyId).thenReturn("journey-123")
        whenever(freeholderProvideDetailsStep.isStepReachable).thenReturn(true)
        if (stubCardActionSteps) {
            whenever(mockState.setStateForFreeholderEditStep).thenReturn(setStateForFreeholderEditStep)
            whenever(setStateForFreeholderEditStep.urlPath).thenReturn(SetStateForFreeholderEditStep.ROUTE_SEGMENT)
            whenever(setStateForFreeholderEditStep.currentJourneyId).thenReturn("journey-123")
            whenever(setStateForFreeholderEditStep.isStepReachable).thenReturn(true)
            whenever(urlParameterService.createParameterPair(any())).thenAnswer {
                "freeholderId" to it.getArgument<Int>(0).toString()
            }
        }
        if (stubRemoveStep) {
            whenever(mockState.confirmRemoveFreeholderStep).thenReturn(confirmRemoveFreeholderStep)
            whenever(confirmRemoveFreeholderStep.urlPath).thenReturn(ConfirmRemoveFreeholderStep.ROUTE_SEGMENT)
            whenever(confirmRemoveFreeholderStep.currentJourneyId).thenReturn("journey-123")
            whenever(confirmRemoveFreeholderStep.isStepReachable).thenReturn(true)
        }
        return stepConfig
    }

    companion object {
        private val DEFAULT_FREEHOLDERS =
            mapOf(
                1 to
                    FreeholderDataModel(
                        detailsKnown = true,
                        name = "James Hassan",
                        address = AddressDataModel.fromManualAddressData("36 Sunnyside Lane", "Biggleswade", "SG18 9EJ"),
                    ),
                2 to FreeholderDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com"),
                3 to FreeholderDataModel(detailsKnown = false),
            )
    }
}
