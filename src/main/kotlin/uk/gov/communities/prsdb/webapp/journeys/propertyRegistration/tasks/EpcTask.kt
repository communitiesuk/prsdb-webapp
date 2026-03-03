package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
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

// TODO PDJB-80: Implement EPC task logic
@JourneyFrameworkComponent
class EpcTask : Task<EpcState>() {
    override fun makeSubJourney(state: EpcState) =
        subJourney(state) {
            step(journey.hasEpcStep) {
                routeSegment(HasEpcStep.ROUTE_SEGMENT)
                nextStep { journey.checkAutomatchedEpcStep }
            }
            step(journey.checkAutomatchedEpcStep) {
                routeSegment(CheckAutomatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.hasEpcStep.isComplete() }
                nextStep { journey.epcSearchStep }
            }
            step(journey.epcSearchStep) {
                routeSegment(EpcSearchStep.ROUTE_SEGMENT)
                parents { journey.checkAutomatchedEpcStep.isComplete() }
                nextStep { journey.checkMatchedEpcStep }
            }
            step(journey.checkMatchedEpcStep) {
                routeSegment(CheckMatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.isComplete() }
                nextStep { journey.epcSuperseededStep }
            }
            step(journey.epcSuperseededStep) {
                routeSegment(EpcSuperseededStep.ROUTE_SEGMENT)
                parents { journey.checkMatchedEpcStep.isComplete() }
                nextStep { journey.epcNotFoundStep }
            }
            step(journey.epcNotFoundStep) {
                routeSegment(EpcNotFoundStep.ROUTE_SEGMENT)
                parents { journey.epcSuperseededStep.isComplete() }
                nextStep { journey.epcExpiryCheckStep }
            }
            step(journey.epcExpiryCheckStep) {
                routeSegment(EpcExpiryCheckStep.ROUTE_SEGMENT)
                parents { journey.epcNotFoundStep.isComplete() }
                nextStep { journey.hasMeesExemptionStep }
            }
            step(journey.hasMeesExemptionStep) {
                routeSegment(HasMeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.epcExpiryCheckStep.isComplete() }
                nextStep { journey.meesExemptionStep }
            }
            step(journey.meesExemptionStep) {
                routeSegment(MeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.isComplete() }
                nextStep { journey.lowEnergyRatingStep }
            }
            step(journey.lowEnergyRatingStep) {
                routeSegment(LowEnergyRatingStep.ROUTE_SEGMENT)
                parents { journey.meesExemptionStep.isComplete() }
                nextStep { journey.epcExpiredStep }
            }
            step(journey.epcExpiredStep) {
                routeSegment(EpcExpiredStep.ROUTE_SEGMENT)
                parents { journey.lowEnergyRatingStep.isComplete() }
                nextStep { journey.hasEpcExemptionStep }
            }
            step(journey.hasEpcExemptionStep) {
                routeSegment(HasEpcExemptionStep.ROUTE_SEGMENT)
                parents { journey.epcExpiredStep.isComplete() }
                nextStep { journey.epcExemptionStep }
            }
            step(journey.epcExemptionStep) {
                routeSegment(EpcExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasEpcExemptionStep.isComplete() }
                nextStep { journey.epcMissingStep }
            }
            step(journey.epcMissingStep) {
                routeSegment(EpcMissingStep.ROUTE_SEGMENT)
                parents { journey.epcExemptionStep.isComplete() }
                nextStep { journey.provideEpcLaterStep }
            }
            step(journey.provideEpcLaterStep) {
                routeSegment(ProvideEpcLaterStep.ROUTE_SEGMENT)
                parents { journey.epcMissingStep.isComplete() }
                nextStep { journey.checkEpcAnswersStep }
            }
            step(journey.checkEpcAnswersStep) {
                routeSegment(CheckEpcAnswersStep.ROUTE_SEGMENT)
                parents { journey.provideEpcLaterStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.checkEpcAnswersStep.isComplete() }
            }
        }
}
