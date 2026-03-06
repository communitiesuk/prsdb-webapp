package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotAutomatchedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.SearchForEpcStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class EpcStateTests {
    @Test
    fun `getNotNullAcceptedEpc throws PrsdbWebException when acceptedEpc is null`() {
        // Arrange
        val state = buildTestEpcState(acceptedEpc = null)

        // Act & Assert
        assertThrows<PrsdbWebException> { state.getNotNullAcceptedEpc() }
    }

    @Test
    fun `getNotNullAcceptedEpc returns acceptedEpc when it is not null`() {
        // Arrange
        val epcData = MockEpcData.createEpcDataModel()
        val state = buildTestEpcState(acceptedEpc = epcData)

        // Act
        val result = state.getNotNullAcceptedEpc()

        // Assert
        assertEquals(epcData, result)
    }

    private fun buildTestEpcState(
        automatchedEpc: EpcDataModel? = null,
        searchedEpc: EpcDataModel? = null,
        acceptedEpc: EpcDataModel? = null,
    ): EpcState =
        object : AbstractJourneyState(journeyStateService = mock()), EpcState {
            override var automatchedEpc: EpcDataModel? = automatchedEpc
            override var searchedEpc: EpcDataModel? = searchedEpc
            override var acceptedEpc: EpcDataModel? = acceptedEpc
            override val propertyId: Long = 123L
            override val epcQuestionStep = mock<EpcQuestionStep>()
            override val checkAutomatchedEpcStep = mock<CheckMatchedEpcStep>()
            override val epcNotAutomatchedStep = mock<EpcNotAutomatchedStep>()
            override val searchForEpcStep = mock<SearchForEpcStep>()
            override val epcSupersededStep = mock<EpcSupersededStep>()
            override val checkMatchedEpcStep = mock<CheckMatchedEpcStep>()
            override val epcNotFoundStep = mock<EpcNotFoundStep>()
            override val epcMissingStep = mock<EpcMissingStep>()
            override val epcExemptionReasonStep = mock<EpcExemptionReasonStep>()
            override val epcExemptionConfirmationStep = mock<EpcExemptionConfirmationStep>()
            override val meesExemptionCheckStep = mock<MeesExemptionCheckStep>()
            override val meesExemptionReasonStep = mock<MeesExemptionReasonStep>()
            override val meesExemptionConfirmationStep = mock<MeesExemptionConfirmationStep>()
            override val lowEnergyRatingStep = mock<LowEnergyRatingStep>()
            override val epcExpiryCheckStep = mock<EpcExpiryCheckStep>()
            override val epcExpiredStep = mock<EpcExpiredStep>()
            override val cyaStep: AbstractCheckYourAnswersStep<*> = mock()
            override var cyaChildJourneyIdIfInitialized: String? = "childJourneyId"
        }
}
