package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class GasSafetyTask : Task<GasSafetyState>() {
    // TODO PDJB-467 - configure this task
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            step(journey.gasSafetyStep) {
                routeSegment(GasSafetyStep.ROUTE_SEGMENT)
                nextStep { state.gasSafetyIssueDateStep }
                savable()
            }
            step(journey.gasSafetyIssueDateStep) {
                routeSegment(GasSafetyIssueDateStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyStep.hasOutcome(Complete.COMPLETE) }
                nextStep { mode ->
                    when (mode) {
                        GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE -> journey.gasSafetyEngineerNumberStep
                        GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED -> journey.gasSafetyOutdatedStep
                    }
                }
                savable()
            }
            step(journey.gasSafetyOutdatedStep) {
                routeSegment(GasSafetyOutdatedStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyIssueDateStep.hasOutcome(GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED) }
                nextStep { exitStep }
                savable()
            }
            step(journey.gasSafetyEngineerNumberStep) {
                routeSegment(GasSafetyEngineerNumberStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyIssueDateStep.hasOutcome(GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE) }
                nextStep { state.gasSafetyCertificateUploadStep }
                savable()
            }
            step(journey.gasSafetyCertificateUploadStep) {
                routeSegment(GasSafetyCertificateUploadStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyEngineerNumberStep.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.gasSafetyUploadConfirmationStep }
                savable()
            }
            step(journey.gasSafetyUploadConfirmationStep) {
                routeSegment(GasSafetyUploadConfirmationStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyCertificateUploadStep.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.gasSafetyOutdatedStep.hasOutcome(Complete.COMPLETE),
                        journey.gasSafetyUploadConfirmationStep.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
