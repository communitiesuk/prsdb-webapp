package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckAutomatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep

interface EpcState : JourneyState {
    val hasEpcStep: HasEpcStep
    val checkAutomatchedEpcStep: CheckAutomatchedEpcStep
    val epcSearchStep: EpcSearchStep
    val checkMatchedEpcStep: CheckMatchedEpcStep
    val epcSuperseededStep: EpcSuperseededStep
    val epcNotFoundStep: EpcNotFoundStep
    val epcExpiryCheckStep: EpcExpiryCheckStep
    val hasMeesExemptionStep: HasMeesExemptionStep
    val meesExemptionStep: MeesExemptionStep
    val lowEnergyRatingStep: LowEnergyRatingStep
    val epcExpiredStep: EpcExpiredStep
    val hasEpcExemptionStep: HasEpcExemptionStep
    val epcExemptionStep: EpcExemptionStep
    val epcMissingStep: EpcMissingStep
    val provideEpcLaterStep: ProvideEpcLaterStep
    val checkEpcAnswersStep: CheckEpcAnswersStep
}
