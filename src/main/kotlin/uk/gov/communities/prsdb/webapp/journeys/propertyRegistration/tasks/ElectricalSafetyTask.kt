package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers

@JourneyFrameworkComponent
class ElectricalSafetyTask : Task<ElectricalSafetyState>() {
    override fun makeSubJourney(state: ElectricalSafetyState) =
        subJourney(state) {
            step(journey.hasElectricalCertStep) {
                routeSegment(HasElectricalCertStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        HasElectricalCertMode.HAS_EIC -> journey.electricalCertExpiryDateStep
                        HasElectricalCertMode.HAS_EICR -> journey.electricalCertExpiryDateStep
                        HasElectricalCertMode.NO_CERTIFICATE -> journey.electricalCertMissingStep
                        HasElectricalCertMode.PROVIDE_THIS_LATER -> journey.provideElectricalCertLaterStep
                    }
                }
            }
            step(journey.electricalCertExpiryDateStep) {
                routeSegment(ElectricalCertExpiryDateStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.HAS_EIC),
                        journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.HAS_EICR),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED -> journey.electricalCertExpiredStep
                        ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE -> journey.hasUploadedElectricalCert
                    }
                }
                savable()
            }
            step<AnyMembers, HasAnyInCollectionStepConfig>(journey.hasUploadedElectricalCert) {
                parents {
                    journey.electricalCertExpiryDateStep.hasOutcome(
                        ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE,
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.uploadElectricalCertStep
                        AnyMembers.SOME_MEMBERS -> journey.checkElectricalCertUploadsStep
                    }
                }
                stepSpecificInitialisation { collectionMap = journey.electricalUploadMap }
            }
            step(journey.uploadElectricalCertStep) {
                routeSegment(UploadElectricalCertStep.ROUTE_SEGMENT)
                parents {
                    journey.electricalCertExpiryDateStep.hasOutcome(ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE)
                }
                nextStep { journey.checkElectricalCertUploadsStep }
                savable()
            }
            step(journey.checkElectricalCertUploadsStep) {
                routeSegment(CheckElectricalCertUploadsStep.ROUTE_SEGMENT)
                parents { journey.uploadElectricalCertStep.isComplete() }
                nextStep { journey.checkElectricalSafetyAnswersStep }
                backStep { journey.electricalCertExpiryDateStep }
                savable()
            }
            step(journey.removeElectricalCertUploadStep) {
                routeSegment(RemoveElectricalCertUploadStep.ROUTE_SEGMENT)
                parents {
                    journey.hasUploadedElectricalCert.hasOutcome(AnyMembers.SOME_MEMBERS)
                }
                backStep { journey.checkElectricalCertUploadsStep }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> journey.checkElectricalCertUploadsStep
                        AnyMembers.NO_MEMBERS -> journey.uploadElectricalCertStep
                    }
                }
                savable()
            }
            step(journey.electricalCertExpiredStep) {
                routeSegment(ElectricalCertExpiredStep.ROUTE_SEGMENT)
                parents {
                    journey.electricalCertExpiryDateStep.hasOutcome(ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED)
                }
                nextStep { journey.checkElectricalSafetyAnswersStep }
                savable()
            }
            step(journey.electricalCertMissingStep) {
                routeSegment(ElectricalCertMissingStep.ROUTE_SEGMENT)
                parents { journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.NO_CERTIFICATE) }
                nextStep { journey.checkElectricalSafetyAnswersStep }
                savable()
            }
            step(journey.provideElectricalCertLaterStep) {
                routeSegment(ProvideElectricalCertLaterStep.ROUTE_SEGMENT)
                parents { journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.PROVIDE_THIS_LATER) }
                nextStep { journey.checkElectricalSafetyAnswersStep }
                savable()
            }
            step(journey.checkElectricalSafetyAnswersStep) {
                routeSegment(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.provideElectricalCertLaterStep.isComplete(),
                        journey.electricalCertMissingStep.isComplete(),
                        journey.electricalCertExpiredStep.isComplete(),
                        journey.checkElectricalCertUploadsStep.isComplete(),
                    )
                }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkElectricalSafetyAnswersStep.isComplete() }
            }
        }
}
