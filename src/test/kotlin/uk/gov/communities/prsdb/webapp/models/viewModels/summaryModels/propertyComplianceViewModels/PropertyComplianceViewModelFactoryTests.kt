package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyComplianceViewModelFactoryTests {
    private val mockMessageSource: MessageSource = mock()

    init {
        whenever(mockMessageSource.getMessage(any(), any(), any())).thenReturn("")
    }

    private val gasSafetyViewModelFactory = GasSafetyViewModelFactory(mock(), mockMessageSource, mock())
    private val electricalSafetyViewModelFactory = ElectricalSafetyViewModelFactory(mock(), mockMessageSource, mock())
    private val propertyComplianceViewModelFactory =
        PropertyComplianceViewModelFactory(
            gasSafetyViewModelFactory,
            electricalSafetyViewModelFactory,
            EpcViewModelFactory(mockMessageSource, mock()),
        )

    private val propertyOwnershipId = 1L

    @Test
    fun `isAllValid is true when the property is compliant`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val result = propertyComplianceViewModelFactory.create(propertyCompliance, propertyOwnershipId = propertyOwnershipId)

        assertTrue(result.isAllValid)
    }

    @Test
    fun `isAllValid is false when certificates are missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

        val result = propertyComplianceViewModelFactory.create(propertyCompliance, propertyOwnershipId = propertyOwnershipId)

        assertFalse(result.isAllValid)
    }

    @Nested
    inner class CardActions {
        @Test
        fun `cards have change actions when landlordView is true`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()
            val propertyOwnershipId = propertyCompliance.propertyOwnership.id

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            val expectedGasSafetyActions =
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateGasSafetyController.getUpdateGasSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )

            val expectedElectricalSafetyActions =
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateElectricalSafetyController.getUpdateElectricalSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )

            val expectedEpcActions =
                listOf(
                    SummaryCardActionViewModel(
                        "propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc",
                        "${PropertyComplianceBuilder.TEST_EPC_BASE_URL}/0000-0000-0000-0000-0000",
                        opensInNewTab = true,
                    ),
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateEpcController.getUpdateEpcRouteFirstStep(propertyOwnershipId),
                    ),
                )
            assertEquals(expectedGasSafetyActions, result.gasSafetySummaryCard.actions)
            assertEquals(expectedElectricalSafetyActions, result.electricalSafetySummaryCard.actions)
            assertEquals(expectedEpcActions, result.epcSummaryCard.actions)
        }

        @Test
        fun `cards have no change actions when landlordView is false`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertNull(result.gasSafetySummaryCard.actions)
            assertNull(result.electricalSafetySummaryCard.actions)
            assertEquals(
                listOf(
                    SummaryCardActionViewModel(
                        "propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc",
                        "${PropertyComplianceBuilder.TEST_EPC_BASE_URL}/0000-0000-0000-0000-0000",
                        opensInNewTab = true,
                    ),
                ),
                result.epcSummaryCard.actions,
            )
        }
    }
}
