package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailsTaskDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers

// An electrical-safety-details task that owns its own steps and IS its own ElectricalSafetyState, so it can be
// composed into a journey via duplicableTask. Structure only: makeSubJourney defines the step flow. Instance-specific
// state (isOccupied, allowProvideCertificateLaterRoute) is supplied by the mount site via an
// ElectricalSafetyDetailsTaskDependencies contract.
@JourneyFrameworkComponent
class ElectricalSafetyDetailsTask(
    journeyStateService: JourneyStateService,
    override val hasElectricalCertStep: HasElectricalCertStep,
    override val electricalCertExpiryDateStep: ElectricalCertExpiryDateStep,
    override val uploadElectricalCertStep: UploadElectricalCertStep,
    override val hasUploadedElectricalCert: HasAnyInCollectionStep,
    override val checkElectricalCertUploadsStep: CheckElectricalCertUploadsStep,
    override val removeElectricalCertUploadStep: RemoveElectricalCertUploadStep,
    override val electricalCertExpiredStep: ElectricalCertExpiredStep,
    override val electricalCertMissingStep: ElectricalCertMissingStep,
    override val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep,
) : DuplicableTaskWithDependencies<ElectricalSafetyState, ElectricalSafetyDetailsTaskDependencies>(journeyStateService),
    ElectricalSafetyState {
    override var electricalUploadMap: Map<Int, CertificateUpload>
        by delegateProvider.requiredDelegate("electricalUploadMap", mapOf())
    override var highestAssignedElectricalMemberId: Int?
        by delegateProvider.nullableDelegate("highestAssignedElectricalMemberId")

    override val isOccupied: Boolean get() = dependencies.isOccupied

    override val allowProvideCertificateLaterRoute: Boolean get() = dependencies.allowProvideCertificateLaterRoute

    override val taskState get() = this

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
                nextStep { exitStep }
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
                nextStep { exitStep }
                savable()
            }
            step(journey.electricalCertMissingStep) {
                routeSegment(ElectricalCertMissingStep.ROUTE_SEGMENT)
                parents { journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.NO_CERTIFICATE) }
                nextStep { exitStep }
                savable()
            }
            step(journey.provideElectricalCertLaterStep) {
                routeSegment(ProvideElectricalCertLaterStep.ROUTE_SEGMENT)
                parents { journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.PROVIDE_THIS_LATER) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.provideElectricalCertLaterStep.isComplete(),
                        journey.electricalCertMissingStep.isComplete(),
                        journey.electricalCertExpiredStep.isComplete(),
                        journey.checkElectricalCertUploadsStep.isComplete(),
                    )
                }
            }
        }
}
