package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class EicrTask : Task<EicrState>() {
    // TODO PDJB-467 - configure this task
    // TODO PDJB-467 - check submit button text for steps that finish at the exit step
    override fun makeSubJourney(state: EicrState) =
        subJourney(state) {
            step(journey.eicrStep) {
                routeSegment(EicrStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        EicrMode.HAS_CERTIFICATE -> journey.eicrIssueDateStep
                        EicrMode.NO_CERTIFICATE -> journey.eicrExemptionStep
                    }
                }
                savable()
            }
            step(journey.eicrIssueDateStep) {
                routeSegment(EicrStep.ROUTE_SEGMENT)
                parents { journey.eicrStep.hasOutcome(EicrMode.HAS_CERTIFICATE) }
                nextStep { mode ->
                    when (mode) {
                        EicrIssueDateMode.EICR_CERTIFICATE_IN_DATE -> journey.eicrUploadStep
                        EicrIssueDateMode.EICR_CERTIFICATE_OUTDATED -> journey.eicrOutdatedStep
                    }
                }
                savable()
            }
            step(journey.eicrUploadStep) {
                routeSegment(EicrUploadStep.ROUTE_SEGMENT)
                parents { journey.eicrIssueDateStep.hasOutcome(EicrIssueDateMode.EICR_CERTIFICATE_IN_DATE) }
                nextStep { journey.eicrUploadConfirmationStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.eicrUploadConfirmationStep.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
