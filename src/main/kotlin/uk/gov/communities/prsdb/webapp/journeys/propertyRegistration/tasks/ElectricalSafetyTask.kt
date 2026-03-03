package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep

// TODO PDJB-80: Implement Electrical Safety task logic
@JourneyFrameworkComponent
class ElectricalSafetyTask : Task<ElectricalSafetyState>() {
    override fun makeSubJourney(state: ElectricalSafetyState) =
        subJourney(state) {
            step(journey.hasElectricalCertStep) {
                routeSegment(HasElectricalCertStep.ROUTE_SEGMENT)
                nextStep { journey.electricalCertIssueDateStep }
            }
            step(journey.electricalCertIssueDateStep) {
                routeSegment(ElectricalCertIssueDateStep.ROUTE_SEGMENT)
                parents { journey.hasElectricalCertStep.isComplete() }
                nextStep { journey.uploadElectricalCertStep }
            }
            step(journey.uploadElectricalCertStep) {
                routeSegment(UploadElectricalCertStep.ROUTE_SEGMENT)
                parents { journey.electricalCertIssueDateStep.isComplete() }
                nextStep { journey.checkElectricalCertUploadsStep }
            }
            step(journey.checkElectricalCertUploadsStep) {
                routeSegment(CheckElectricalCertUploadsStep.ROUTE_SEGMENT)
                parents { journey.uploadElectricalCertStep.isComplete() }
                nextStep { journey.removeElectricalCertUploadStep }
            }
            step(journey.removeElectricalCertUploadStep) {
                routeSegment(RemoveElectricalCertUploadStep.ROUTE_SEGMENT)
                parents { journey.checkElectricalCertUploadsStep.isComplete() }
                nextStep { journey.electricalCertExpiredStep }
            }
            step(journey.electricalCertExpiredStep) {
                routeSegment(ElectricalCertExpiredStep.ROUTE_SEGMENT)
                parents { journey.removeElectricalCertUploadStep.isComplete() }
                nextStep { journey.electricalCertMissingStep }
            }
            step(journey.electricalCertMissingStep) {
                routeSegment(ElectricalCertMissingStep.ROUTE_SEGMENT)
                parents { journey.electricalCertExpiredStep.isComplete() }
                nextStep { journey.provideElectricalCertLaterStep }
            }
            step(journey.provideElectricalCertLaterStep) {
                routeSegment(ProvideElectricalCertLaterStep.ROUTE_SEGMENT)
                parents { journey.electricalCertMissingStep.isComplete() }
                nextStep { journey.checkElectricalSafetyAnswersStep }
            }
            step(journey.checkElectricalSafetyAnswersStep) {
                routeSegment(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.provideElectricalCertLaterStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.checkElectricalSafetyAnswersStep.isComplete() }
            }
        }
}
