package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class GasSafetyTask : Task<GasSafetyState>() {
    // TODO PDJB-467 - check submit button text for steps that finish at the exit step
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            step(journey.gasSafetyStep) {
                routeSegment(GasSafetyStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        GasSafetyMode.HAS_CERTIFICATE -> journey.gasSafetyIssueDateStep
                        GasSafetyMode.NO_CERTIFICATE -> journey.gasSafetyExemptionStep
                    }
                }
                savable()
            }
            step(journey.gasSafetyIssueDateStep) {
                routeSegment(GasSafetyIssueDateStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyStep.hasOutcome(GasSafetyMode.HAS_CERTIFICATE) }
                nextStep { mode ->
                    when (mode) {
                        GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE -> journey.gasSafetyEngineerNumberStep
                        GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED -> journey.gasSafetyOutdatedStep
                    }
                }
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
            step(journey.gasSafetyOutdatedStep) {
                routeSegment(GasSafetyOutdatedStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyIssueDateStep.hasOutcome(GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED) }
                nextStep { exitStep }
                savable()
            }
            step(journey.gasSafetyExemptionStep) {
                routeSegment(GasSafetyExemptionStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyStep.hasOutcome(GasSafetyMode.NO_CERTIFICATE) }
                nextStep { mode ->
                    when (mode) {
                        GasSafetyExemptionMode.HAS_EXEMPTION -> journey.gasSafetyExemptionReasonStep
                        GasSafetyExemptionMode.NO_EXEMPTION -> journey.gasSafetyExemptionMissingStep
                    }
                }
                savable()
            }
            step(journey.gasSafetyExemptionReasonStep) {
                routeSegment(GasSafetyExemptionReasonStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyExemptionStep.hasOutcome(GasSafetyExemptionMode.HAS_EXEMPTION) }
                nextStep { mode ->
                    when (mode) {
                        GasSafetyExemptionReasonMode.LISTED_REASON_SELECTED -> journey.gasSafetyExemptionConfirmationStep
                        GasSafetyExemptionReasonMode.OTHER_REASON_SELECTED -> journey.gasSafetyExemptionOtherReasonStep
                    }
                }
                savable()
            }
            step(journey.gasSafetyExemptionOtherReasonStep) {
                routeSegment(GasSafetyExemptionOtherReasonStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyExemptionReasonStep.hasOutcome(GasSafetyExemptionReasonMode.OTHER_REASON_SELECTED) }
                nextStep { journey.gasSafetyExemptionConfirmationStep }
                savable()
            }
            step(journey.gasSafetyExemptionConfirmationStep) {
                routeSegment(GasSafetyExemptionConfirmationStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.gasSafetyExemptionReasonStep.hasOutcome(GasSafetyExemptionReasonMode.LISTED_REASON_SELECTED),
                        journey.gasSafetyExemptionOtherReasonStep.hasOutcome(Complete.COMPLETE),
                    )
                }
                nextStep { exitStep }
                savable()
            }
            step(journey.gasSafetyExemptionMissingStep) {
                routeSegment(GasSafetyExemptionMissingStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyExemptionStep.hasOutcome(GasSafetyExemptionMode.NO_EXEMPTION) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.gasSafetyUploadConfirmationStep.hasOutcome(Complete.COMPLETE),
                        journey.gasSafetyOutdatedStep.hasOutcome(Complete.COMPLETE),
                        journey.gasSafetyExemptionMissingStep.hasOutcome(Complete.COMPLETE),
                        journey.gasSafetyExemptionConfirmationStep.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
