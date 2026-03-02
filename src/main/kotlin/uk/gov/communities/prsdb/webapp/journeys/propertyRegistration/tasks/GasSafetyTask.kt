package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasSafetyLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasSafetyUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasSafetyStep

@JourneyFrameworkComponent("propertyRegistrationGasSafetyTask")
class GasSafetyTask : Task<GasSafetyState>() {
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            // TODO PDJB-628: Implement Has Gas Supply step logic
            step(journey.hasGasSupplyStep) {
                routeSegment(HasGasSupplyStep.ROUTE_SEGMENT)
                nextStep { journey.hasGasSafetyStep }
                savable()
            }
            // TODO PDJB-629: Implement Has Gas Safety step logic
            step(journey.hasGasSafetyStep) {
                routeSegment(HasGasSafetyStep.ROUTE_SEGMENT)
                parents { journey.hasGasSupplyStep.isComplete() }
                nextStep { journey.gasSafetyIssueDateStep }
                savable()
            }
            // TODO PDJB-631: Implement Gas Safety Issue Date step logic
            step(journey.gasSafetyIssueDateStep) {
                routeSegment(GasSafetyIssueDateStep.ROUTE_SEGMENT)
                parents { journey.hasGasSafetyStep.isComplete() }
                nextStep { journey.uploadGasSafetyStep }
                savable()
            }
            // TODO PDJB-634: Implement Upload Gas Safety step logic
            step(journey.uploadGasSafetyStep) {
                routeSegment(UploadGasSafetyStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyIssueDateStep.isComplete() }
                nextStep { journey.checkGasSafetyUploadsStep }
                savable()
            }
            // TODO PDJB-635: Implement Check Gas Safety Uploads step logic
            step(journey.checkGasSafetyUploadsStep) {
                routeSegment(CheckGasSafetyUploadsStep.ROUTE_SEGMENT)
                parents { journey.uploadGasSafetyStep.isComplete() }
                nextStep { journey.removeGasSafetyUploadStep }
                savable()
            }
            // TODO PDJB-636: Implement Remove Gas Safety Upload step logic
            step(journey.removeGasSafetyUploadStep) {
                routeSegment(RemoveGasSafetyUploadStep.ROUTE_SEGMENT)
                parents { journey.checkGasSafetyUploadsStep.isComplete() }
                nextStep { journey.gasSafetyExpiredStep }
                savable()
            }
            // TODO PDJB-632: Implement Gas Safety Expired step logic
            step(journey.gasSafetyExpiredStep) {
                routeSegment(GasSafetyExpiredStep.ROUTE_SEGMENT)
                parents { journey.removeGasSafetyUploadStep.isComplete() }
                nextStep { journey.gasSafetyMissingStep }
                savable()
            }
            // TODO PDJB-630: Implement Gas Safety Missing step logic
            step(journey.gasSafetyMissingStep) {
                routeSegment(GasSafetyMissingStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyExpiredStep.isComplete() }
                nextStep { journey.provideGasSafetyLaterStep }
                savable()
            }
            // TODO PDJB-633: Implement Provide Gas Safety Later step logic
            step(journey.provideGasSafetyLaterStep) {
                routeSegment(ProvideGasSafetyLaterStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyMissingStep.isComplete() }
                nextStep { journey.checkGasSafetyAnswersStep }
                savable()
            }
            // TODO PDJB-637: Implement Check Gas Safety Answers step logic
            step(journey.checkGasSafetyAnswersStep) {
                routeSegment(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.provideGasSafetyLaterStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkGasSafetyAnswersStep.isComplete() }
            }
        }
}
