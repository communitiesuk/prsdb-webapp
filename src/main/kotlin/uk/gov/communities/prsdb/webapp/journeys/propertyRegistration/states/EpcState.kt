package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmedEpcRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

interface EpcState : JourneyState {
    var confirmedEpc: EpcDataModel?
    val isOccupied: Boolean?

    val epcLookupByUprnStep: EpcLookupStep
    val hasEpcStep: HasEpcStep
    val checkUprnMatchedEpcStep: CheckMatchedEpcStep
    val checkSearchedEpcStep: CheckMatchedEpcStep
    val epcSearchStep: EpcSearchStep
    val epcSupersededStep: EpcSupersededStep
    val confirmedEpcRoutingStep: ConfirmedEpcRoutingStep
    val epcNotFoundStep: EpcNotFoundStep
    val epcExpiryCheckStep: EpcExpiryCheckStep
    val hasMeesExemptionStep: HasMeesExemptionStep
    val meesExemptionStep: MeesExemptionStep
    val lowEnergyRatingStep: LowEnergyRatingStep
    val epcExpiredStep: EpcExpiredStep
    val isEpcRequiredStep: IsEpcRequiredStep
    val epcExemptionStep: EpcExemptionStep
    val epcMissingStep: EpcMissingStep
    val provideEpcLaterStep: ProvideEpcLaterStep
    val checkEpcAnswersStep: CheckEpcAnswersStep
}
