package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent("propertyRegistrationGasSafetyTask")
class GasSafetyTask : Task<GasSafetyState>() {
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            step(journey.hasGasSupplyStep) {
                routeSegment(HasGasSupplyStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.hasGasCertStep
                        YesOrNo.NO -> journey.checkGasSafetyAnswersStep
                    }
                }
                savable()
            }
            step(journey.hasGasCertStep) {
                routeSegment(HasGasCertStep.ROUTE_SEGMENT)
                parents { journey.hasGasSupplyStep.hasOutcome(YesOrNo.YES) }
                nextStep { mode ->
                    when (mode) {
                        HasGasCertMode.HAS_CERTIFICATE -> journey.gasCertIssueDateStep
                        HasGasCertMode.NO_CERTIFICATE -> journey.gasCertMissingStep
                        HasGasCertMode.PROVIDE_THIS_LATER -> journey.provideGasCertLaterStep
                    }
                }
                savable()
            }
            // TODO PDJB-631: Implement Gas Safety Issue Date step logic
            step(journey.gasCertIssueDateStep) {
                routeSegment(GasCertIssueDateStep.ROUTE_SEGMENT)
                parents { journey.hasGasCertStep.hasOutcome(HasGasCertMode.HAS_CERTIFICATE) }
                nextStep { journey.uploadGasCertStep }
                savable()
            }
            // TODO PDJB-634: Implement Upload Gas Safety step logic
            step(journey.uploadGasCertStep) {
                routeSegment(UploadGasCertStep.ROUTE_SEGMENT)
                parents { journey.gasCertIssueDateStep.isComplete() }
                nextStep { journey.checkGasCertUploadsStep }
                savable()
            }
            // TODO PDJB-635: Implement Check Gas Safety Uploads step logic
            step(journey.checkGasCertUploadsStep) {
                routeSegment(CheckGasCertUploadsStep.ROUTE_SEGMENT)
                parents { journey.uploadGasCertStep.isComplete() }
                nextStep { journey.removeGasCertUploadStep }
                savable()
            }
            // TODO PDJB-636: Implement Remove Gas Safety Upload step logic
            step(journey.removeGasCertUploadStep) {
                routeSegment(RemoveGasCertUploadStep.ROUTE_SEGMENT)
                parents { journey.checkGasCertUploadsStep.isComplete() }
                nextStep { journey.gasCertExpiredStep }
                savable()
            }
            // TODO PDJB-632: Implement Gas Safety Expired step logic
            step(journey.gasCertExpiredStep) {
                routeSegment(GasCertExpiredStep.ROUTE_SEGMENT)
                parents { journey.removeGasCertUploadStep.isComplete() }
                nextStep { journey.checkGasSafetyAnswersStep }
                savable()
            }
            // TODO PDJB-630: Implement Gas Safety Missing step logic
            step(journey.gasCertMissingStep) {
                routeSegment(GasCertMissingStep.ROUTE_SEGMENT)
                parents { journey.hasGasCertStep.hasOutcome(HasGasCertMode.NO_CERTIFICATE) }
                nextStep { journey.checkGasSafetyAnswersStep }
                savable()
            }
            // TODO PDJB-633: Implement Provide Gas Safety Later step logic
            step(journey.provideGasCertLaterStep) {
                routeSegment(ProvideGasCertLaterStep.ROUTE_SEGMENT)
                parents { journey.hasGasCertStep.hasOutcome(HasGasCertMode.PROVIDE_THIS_LATER) }
                nextStep { journey.checkGasSafetyAnswersStep }
                savable()
            }
            // TODO PDJB-637: Implement Check Gas Safety Answers step logic
            step(journey.checkGasSafetyAnswersStep) {
                routeSegment(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasGasSupplyStep.hasOutcome(YesOrNo.NO),
                        journey.provideGasCertLaterStep.isComplete(),
                        journey.gasCertMissingStep.isComplete(),
                        journey.gasCertExpiredStep.isComplete(),
                    )
                }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkGasSafetyAnswersStep.isComplete() }
            }
        }
}
