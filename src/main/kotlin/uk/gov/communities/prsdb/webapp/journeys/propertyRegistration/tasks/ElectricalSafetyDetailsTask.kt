package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicrStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.ElectricalCertExpiryStatus

@JourneyFrameworkComponent("propertyRegistrationElectricalSafetyDetailsTask")
class ElectricalSafetyDetailsTask(
    override val hasElectricalCertStep: HasElectricalCertStep,
    override val eicExpiryDateStep: EicExpiryDateStep,
    override val hasUploadedEic: HasAnyInCollectionStep,
    override val uploadEicStep: UploadEicStep,
    override val checkEicUploadsStep: CheckEicUploadsStep,
    override val removeEicUploadStep: RemoveEicUploadStep,
    override val eicExpiredStep: EicExpiredStep,
    override val eicrExpiryDateStep: EicrExpiryDateStep,
    override val hasUploadedEicr: HasAnyInCollectionStep,
    override val uploadEicrStep: UploadEicrStep,
    override val checkEicrUploadsStep: CheckEicrUploadsStep,
    override val removeEicrUploadStep: RemoveEicrUploadStep,
    override val eicrExpiredStep: EicrExpiredStep,
    override val electricalCertMissingStep: ElectricalCertMissingStep,
    override val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep,
    journeyStateService: JourneyStateService,
) : Task<ElectricalSafetyDetailState, ElectricalSafetyDependencies>(journeyStateService),
    ElectricalSafetyDetailState {
    override val taskState: ElectricalSafetyDetailState
        get() = this

    override val isOccupied: Boolean
        get() = dependencies.isOccupied
    override val allowProvideCertificateLaterRoute: Boolean
        get() = dependencies.allowProvideCertificateLaterRoute
    override val propertyOwnershipId: Long?
        get() = dependencies.propertyOwnershipId

    override var eicUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("eicUploadMap", mapOf())
    override var highestAssignedEicMemberId: Int? by delegateProvider.nullableDelegate("highestAssignedEicMemberId")
    override var eicrUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("eicrUploadMap", mapOf())
    override var highestAssignedEicrMemberId: Int? by delegateProvider.nullableDelegate("highestAssignedEicrMemberId")

    override fun makeSubJourney(state: ElectricalSafetyDetailState) =
        subJourney(state) {
            step(journey.hasElectricalCertStep) {
                routeSegment(HasElectricalCertStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        HasElectricalCertMode.HAS_EIC -> journey.eicExpiryDateStep
                        HasElectricalCertMode.HAS_EICR -> journey.eicrExpiryDateStep
                        HasElectricalCertMode.NO_CERTIFICATE -> journey.electricalCertMissingStep
                        HasElectricalCertMode.PROVIDE_THIS_LATER -> journey.provideElectricalCertLaterStep
                    }
                }
            }
            step(journey.eicExpiryDateStep) {
                routeSegment(EicExpiryDateStep.ROUTE_SEGMENT)
                parents { journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.HAS_EIC) }
                nextStep { mode ->
                    when (mode) {
                        ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED -> journey.eicExpiredStep
                        ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE -> journey.hasUploadedEic
                    }
                }
                savable()
            }
            step<AnyMembers, HasAnyInCollectionStepConfig>(journey.hasUploadedEic) {
                parents {
                    journey.eicExpiryDateStep.hasOutcome(
                        ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE,
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.uploadEicStep
                        AnyMembers.SOME_MEMBERS -> journey.checkEicUploadsStep
                    }
                }
                stepSpecificInitialisation { collectionMap = journey.eicUploadMap }
            }
            step(journey.uploadEicStep) {
                routeSegment(UploadEicStep.ROUTE_SEGMENT)
                parents {
                    journey.eicExpiryDateStep.hasOutcome(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE)
                }
                nextStep { journey.checkEicUploadsStep }
                savable()
            }
            step(journey.checkEicUploadsStep) {
                routeSegment(CheckEicUploadsStep.ROUTE_SEGMENT)
                parents { journey.uploadEicStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            step(journey.removeEicUploadStep) {
                routeSegment(RemoveEicUploadStep.ROUTE_SEGMENT)
                parents {
                    journey.hasUploadedEic.hasOutcome(AnyMembers.SOME_MEMBERS)
                }
                backStep { journey.checkEicUploadsStep }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> journey.checkEicUploadsStep
                        AnyMembers.NO_MEMBERS -> journey.uploadEicStep
                    }
                }
                savable()
            }
            step(journey.eicExpiredStep) {
                routeSegment(EicExpiredStep.ROUTE_SEGMENT)
                parents {
                    journey.eicExpiryDateStep.hasOutcome(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED)
                }
                nextStep { exitStep }
                savable()
            }
            step(journey.eicrExpiryDateStep) {
                routeSegment(EicrExpiryDateStep.ROUTE_SEGMENT)
                parents { journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.HAS_EICR) }
                nextStep { mode ->
                    when (mode) {
                        ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED -> journey.eicrExpiredStep
                        ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE -> journey.hasUploadedEicr
                    }
                }
                savable()
            }
            step<AnyMembers, HasAnyInCollectionStepConfig>(journey.hasUploadedEicr) {
                parents {
                    journey.eicrExpiryDateStep.hasOutcome(
                        ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE,
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.uploadEicrStep
                        AnyMembers.SOME_MEMBERS -> journey.checkEicrUploadsStep
                    }
                }
                stepSpecificInitialisation { collectionMap = journey.eicrUploadMap }
            }
            step(journey.uploadEicrStep) {
                routeSegment(UploadEicrStep.ROUTE_SEGMENT)
                parents {
                    journey.eicrExpiryDateStep.hasOutcome(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE)
                }
                nextStep { journey.checkEicrUploadsStep }
                savable()
            }
            step(journey.checkEicrUploadsStep) {
                routeSegment(CheckEicrUploadsStep.ROUTE_SEGMENT)
                parents { journey.uploadEicrStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            step(journey.removeEicrUploadStep) {
                routeSegment(RemoveEicrUploadStep.ROUTE_SEGMENT)
                parents {
                    journey.hasUploadedEicr.hasOutcome(AnyMembers.SOME_MEMBERS)
                }
                backStep { journey.checkEicrUploadsStep }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> journey.checkEicrUploadsStep
                        AnyMembers.NO_MEMBERS -> journey.uploadEicrStep
                    }
                }
                savable()
            }
            step(journey.eicrExpiredStep) {
                routeSegment(EicrExpiredStep.ROUTE_SEGMENT)
                parents {
                    journey.eicrExpiryDateStep.hasOutcome(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED)
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
                        journey.eicExpiredStep.isComplete(),
                        journey.checkEicUploadsStep.isComplete(),
                        journey.eicrExpiredStep.isComplete(),
                        journey.checkEicrUploadsStep.isComplete(),
                    )
                }
            }
        }
}
