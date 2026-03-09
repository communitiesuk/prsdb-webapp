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

@JourneyFrameworkComponent("propertyRegistrationEpcTask")
class EpcTask : Task<EpcState>() {
    override fun makeSubJourney(state: EpcState) =
        subJourney(state) {
            // TODO PDJB-656: Implement Has EPC step logic
            step(journey.hasEpcStep) {
                routeSegment(HasEpcStep.ROUTE_SEGMENT)
                nextStep { journey.checkAutomatchedEpcStep }
            }
            // TODO PDJB-661: Implement Check Automatched EPC step logic
            step(journey.checkAutomatchedEpcStep) {
                routeSegment(CheckAutomatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.hasEpcStep.isComplete() }
                nextStep { journey.epcSearchStep }
            }
            // TODO PDJB-662: Implement EPC Search step logic
            step(journey.epcSearchStep) {
                routeSegment(EpcSearchStep.ROUTE_SEGMENT)
                parents { journey.checkAutomatchedEpcStep.isComplete() }
                nextStep { journey.checkMatchedEpcStep }
            }
            // TODO PDJB-661: Implement Check Matched EPC step logic
            step(journey.checkMatchedEpcStep) {
                routeSegment(CheckMatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.isComplete() }
                nextStep { journey.epcSuperseededStep }
            }
            // TODO PDJB-664: Implement EPC Superseded step logic
            step(journey.epcSuperseededStep) {
                routeSegment(EpcSuperseededStep.ROUTE_SEGMENT)
                parents { journey.checkMatchedEpcStep.isComplete() }
                nextStep { journey.epcNotFoundStep }
            }
            // TODO PDJB-663: Implement EPC Not Found step logic
            step(journey.epcNotFoundStep) {
                routeSegment(EpcNotFoundStep.ROUTE_SEGMENT)
                parents { journey.epcSuperseededStep.isComplete() }
                nextStep { journey.epcExpiryCheckStep }
            }
            // TODO PDJB-665: Implement EPC Expiry Check step logic
            step(journey.epcExpiryCheckStep) {
                routeSegment(EpcExpiryCheckStep.ROUTE_SEGMENT)
                parents { journey.epcNotFoundStep.isComplete() }
                nextStep { journey.hasMeesExemptionStep }
            }
            // TODO PDJB-667: Implement Has MEES Exemption step logic
            step(journey.hasMeesExemptionStep) {
                routeSegment(HasMeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.epcExpiryCheckStep.isComplete() }
                nextStep { journey.meesExemptionStep }
            }
            // TODO PDJB-668: Implement MEES Exemption step logic
            step(journey.meesExemptionStep) {
                routeSegment(MeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.isComplete() }
                nextStep { journey.lowEnergyRatingStep }
            }
            // TODO PDJB-669: Implement Low Energy Rating step logic
            step(journey.lowEnergyRatingStep) {
                routeSegment(LowEnergyRatingStep.ROUTE_SEGMENT)
                parents { journey.meesExemptionStep.isComplete() }
                nextStep { journey.epcExpiredStep }
            }
            // TODO PDJB-666: Implement EPC Expired step logic
            step(journey.epcExpiredStep) {
                routeSegment(EpcExpiredStep.ROUTE_SEGMENT)
                parents { journey.lowEnergyRatingStep.isComplete() }
                nextStep { journey.hasEpcExemptionStep }
            }
            // TODO PDJB-657: Implement Has EPC Exemption step logic
            step(journey.hasEpcExemptionStep) {
                routeSegment(HasEpcExemptionStep.ROUTE_SEGMENT)
                parents { journey.epcExpiredStep.isComplete() }
                nextStep { journey.epcExemptionStep }
            }
            // TODO PDJB-658: Implement EPC Exemption step logic
            step(journey.epcExemptionStep) {
                routeSegment(EpcExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasEpcExemptionStep.isComplete() }
                nextStep { journey.epcMissingStep }
            }
            // TODO PDJB-659: Implement EPC Missing step logic
            step(journey.epcMissingStep) {
                routeSegment(EpcMissingStep.ROUTE_SEGMENT)
                parents { journey.epcExemptionStep.isComplete() }
                nextStep { journey.provideEpcLaterStep }
            }
            // TODO PDJB-660: Implement Provide EPC Later step logic
            step(journey.provideEpcLaterStep) {
                routeSegment(ProvideEpcLaterStep.ROUTE_SEGMENT)
                parents { journey.epcMissingStep.isComplete() }
                nextStep { journey.checkEpcAnswersStep }
            }
            // TODO PDJB-670: Implement Check EPC Answers step logic
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
