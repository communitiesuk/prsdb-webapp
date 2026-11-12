package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.FreeholderSummaryCardHelper
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
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isJointFreeholder).thenReturn(true)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("registerProperty.freeholders.list.heading.joint", content["addAnotherTitle"])
    }

    @Test
    fun `heading uses the non-joint message key when the freeholders are not joint`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isJointFreeholder).thenReturn(false)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("registerProperty.freeholders.list.heading.nonJoint", content["addAnotherTitle"])
    }

    @Test
    fun `heading parameter is the number of freeholders`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(3, content["optionalAddAnotherTitleParam"])
    }

    @Test
    fun `cards have Change and Remove actions when there is more than one freeholder`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val cards = getCards(stepConfig)

        // Assert
        assertEquals(
            listOf("James Hassan", "Billie Smith", "registerProperty.freeholders.list.unknownFreeholder.title"),
            cards.map { it.title },
        )
        cards.forEach { card ->
            assertEquals(listOf("forms.links.change", "forms.links.remove"), card.actions!!.map { it.text })
        }
    }

    @Test
    fun `the only freeholder has no Remove action when removing the last member is not allowed`() {
        // Arrange
        val stepConfig = setupStepConfig(freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = false)), stubRemoveStep = false)
        whenever(mockState.allowRemovingLastMember).thenReturn(false)

        // Act
        val card = getCards(stepConfig).single()

        // Assert
        assertEquals(listOf("forms.links.change"), card.actions!!.map { it.text })
    }

    @Test
    fun `the only freeholder has Change and Remove actions when removing the last member is allowed`() {
        // Arrange
        val stepConfig = setupStepConfig(freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = false)))
        whenever(mockState.allowRemovingLastMember).thenReturn(true)

        // Act
        val card = getCards(stepConfig).single()

        // Assert
        assertEquals(listOf("forms.links.change", "forms.links.remove"), card.actions!!.map { it.text })
    }

    @Test
    fun `afterStepIsReached clears the freeholder being edited`() {
        // Arrange
        val stepConfig = FreeholdersListStepConfig(urlParameterService, FreeholderSummaryCardHelper())

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).editingFreeholderId = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCards(stepConfig: FreeholdersListStepConfig) =
        stepConfig.getStepSpecificContent(mockState)["summaryCards"] as List<SummaryCardViewModel>

    private fun setupStepConfig(
        freeholders: Map<Int, FreeholderDataModel>? = DEFAULT_FREEHOLDERS,
        stubRemoveStep: Boolean = true,
    ): FreeholdersListStepConfig {
        val stepConfig = FreeholdersListStepConfig(urlParameterService, FreeholderSummaryCardHelper())
        stepConfig.urlPath = FreeholdersListStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockState.freeholdersMap).thenReturn(freeholders)
        whenever(mockState.freeholderProvideDetailsStep).thenReturn(freeholderProvideDetailsStep)
        whenever(freeholderProvideDetailsStep.urlPath).thenReturn(ProvideFreeholderDetailsStep.ROUTE_SEGMENT)
        whenever(freeholderProvideDetailsStep.currentJourneyId).thenReturn("journey-123")
        whenever(freeholderProvideDetailsStep.isStepReachable).thenReturn(true)
        whenever(mockState.setStateForFreeholderEditStep).thenReturn(setStateForFreeholderEditStep)
        whenever(setStateForFreeholderEditStep.urlPath).thenReturn(SetStateForFreeholderEditStep.ROUTE_SEGMENT)
        whenever(setStateForFreeholderEditStep.currentJourneyId).thenReturn("journey-123")
        whenever(setStateForFreeholderEditStep.isStepReachable).thenReturn(true)
        whenever(urlParameterService.createParameterPair(any())).thenAnswer {
            "freeholderId" to it.getArgument<Int>(0).toString()
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
