package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.ExemptionMode

@JourneyFrameworkComponent
class EicrTask : Task<EicrState>() {
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
                routeSegment(EicrIssueDateStep.ROUTE_SEGMENT)
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
                savable()
            }
            step(journey.eicrUploadConfirmationStep) {
                routeSegment(EicrUploadConfirmationStep.ROUTE_SEGMENT)
                parents { journey.eicrUploadStep.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
                savable()
            }
            step(journey.eicrOutdatedStep) {
                routeSegment(EicrOutdatedStep.ROUTE_SEGMENT)
                parents { journey.eicrIssueDateStep.hasOutcome(EicrIssueDateMode.EICR_CERTIFICATE_OUTDATED) }
                nextStep { exitStep }
                savable()
            }
            step(journey.eicrExemptionStep) {
                routeSegment(EicrExemptionStep.ROUTE_SEGMENT)
                parents { journey.eicrStep.hasOutcome(EicrMode.NO_CERTIFICATE) }
                nextStep { mode ->
                    when (mode) {
                        ExemptionMode.HAS_EXEMPTION -> journey.eicrExemptionReasonStep
                        ExemptionMode.NO_EXEMPTION -> journey.eicrExemptionMissingStep
                    }
                }
            }
            step(journey.eicrExemptionReasonStep) {
                routeSegment(EicrExemptionReasonStep.ROUTE_SEGMENT)
                parents { journey.eicrExemptionStep.hasOutcome(ExemptionMode.HAS_EXEMPTION) }
                nextStep { mode ->
                    when (mode) {
                        EicrExemptionReasonMode.LISTED_REASON_SELECTED -> journey.eicrExemptionConfirmationStep
                        EicrExemptionReasonMode.OTHER_REASON_SELECTED -> journey.eicrExemptionOtherReasonStep
                    }
                }
            }
            step(journey.eicrExemptionOtherReasonStep) {
                routeSegment(EicrExemptionOtherReasonStep.ROUTE_SEGMENT)
                parents { journey.eicrExemptionReasonStep.hasOutcome(EicrExemptionReasonMode.OTHER_REASON_SELECTED) }
                nextStep { journey.eicrExemptionConfirmationStep }
            }
            step(journey.eicrExemptionConfirmationStep) {
                routeSegment(EicrExemptionConfirmationStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.eicrExemptionReasonStep.hasOutcome(EicrExemptionReasonMode.LISTED_REASON_SELECTED),
                        journey.eicrExemptionOtherReasonStep.hasOutcome(Complete.COMPLETE),
                    )
                }
                nextStep { exitStep }
            }
            step(journey.eicrExemptionMissingStep) {
                routeSegment(EicrExemptionMissingStep.ROUTE_SEGMENT)
                parents { journey.eicrExemptionStep.hasOutcome(ExemptionMode.NO_EXEMPTION) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.eicrUploadConfirmationStep.hasOutcome(Complete.COMPLETE),
                        journey.eicrOutdatedStep.hasOutcome(Complete.COMPLETE),
                        journey.eicrExemptionMissingStep.hasOutcome(Complete.COMPLETE),
                        journey.eicrExemptionConfirmationStep.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
