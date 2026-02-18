package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
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
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

interface EpcState :
    JourneyState,
    CheckYourAnswersJourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    var acceptedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestionStep: EpcQuestionStep
    val checkAutomatchedEpcStep: CheckMatchedEpcStep
    val epcNotAutomatchedStep: EpcNotAutomatchedStep
    val searchForEpcStep: SearchForEpcStep
    val epcSupersededStep: EpcSupersededStep
    val checkMatchedEpcStep: CheckMatchedEpcStep
    val epcNotFoundStep: EpcNotFoundStep
    val epcMissingStep: EpcMissingStep
    val epcExemptionReasonStep: EpcExemptionReasonStep
    val epcExemptionConfirmationStep: EpcExemptionConfirmationStep
    val meesExemptionCheckStep: MeesExemptionCheckStep
    val meesExemptionReasonStep: MeesExemptionReasonStep
    val meesExemptionConfirmationStep: MeesExemptionConfirmationStep
    val lowEnergyRatingStep: LowEnergyRatingStep
    val epcExpiryCheckStep: EpcExpiryCheckStep
    val epcExpiredStep: EpcExpiredStep

    fun getNotNullAcceptedEpc() = acceptedEpc ?: throw PrsdbWebException("Attempting to access accepted EPC when it is null in state")
}
