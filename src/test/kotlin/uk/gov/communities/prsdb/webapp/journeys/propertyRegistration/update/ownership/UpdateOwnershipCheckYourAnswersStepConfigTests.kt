package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipStructureTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OwnershipStructureDetailsHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.test.assertEquals
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class UpdateOwnershipCheckYourAnswersStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateOwnershipJourneyState

    @Mock
    private lateinit var mockOwnershipStructureDetailsHelper: OwnershipStructureDetailsHelper

    @Mock
    private lateinit var mockOwnershipStructureTask: OwnershipStructureTask

    private lateinit var stepConfig: UpdateOwnershipCheckYourAnswersStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = UpdateOwnershipCheckYourAnswersStepConfig(mockOwnershipStructureDetailsHelper)
    }

    @Test
    fun `getStepSpecificContent includes the ownership structure content from the helper and the update page content`() {
        // Arrange
        val ownershipRows = listOf(mock<SummaryListRowViewModel>())
        whenever(mockState.ownershipStructureTask).thenReturn(mockOwnershipStructureTask)
        whenever(mockOwnershipStructureDetailsHelper.getOwnershipStructureCyaContent(mockState, mockOwnershipStructureTask))
            .thenReturn(mapOf("ownershipRows" to ownershipRows))

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(
            mapOf(
                "ownershipRows" to ownershipRows,
                "insetTextKey" to "forms.update.checkOwnership.insetText",
                "warningTextKey" to "forms.warning",
                "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            ),
            content,
        )
    }

    @Test
    fun `resolveNextDestination returns the default destination without deleting the journey`() {
        // Arrange
        val defaultDestination = Destination.Nowhere()

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertSame(defaultDestination, result)
        verify(mockState, never()).deleteJourney()
    }
}
