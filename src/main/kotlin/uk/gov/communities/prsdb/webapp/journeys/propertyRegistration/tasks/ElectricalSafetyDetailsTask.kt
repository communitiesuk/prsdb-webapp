package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicrStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers

@JourneyFrameworkComponent("propertyRegistrationElectricalSafetyDetailsTask")
class ElectricalSafetyDetailsTask(
    override val hasUploadedEicr: HasAnyInCollectionStep,
    override val hasElectricalCertStep: HasElectricalCertStep,
    override val eicrExpiryDateStep: EicrExpiryDateStep,
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

    override var eicrUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("eicrUploadMap", mapOf())
    override var highestAssignedEicrMemberId: Int? by delegateProvider.nullableDelegate("highestAssignedEicrMemberId")

    override fun makeSubJourney(state: ElectricalSafetyDetailState) =
        subJourney(state) {
            step(journey.hasElectricalCertStep) {
                routeSegment(HasElectricalCertStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        HasElectricalCertMode.HAS_EIC -> journey.eicrExpiryDateStep
                        HasElectricalCertMode.HAS_EICR -> journey.eicrExpiryDateStep
                        HasElectricalCertMode.NO_CERTIFICATE -> journey.electricalCertMissingStep
                        HasElectricalCertMode.PROVIDE_THIS_LATER -> journey.provideElectricalCertLaterStep
                    }
                }
            }
            step(journey.eicrExpiryDateStep) {
                routeSegment(EicrExpiryDateStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.HAS_EIC),
                        journey.hasElectricalCertStep.hasOutcome(HasElectricalCertMode.HAS_EICR),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED -> journey.eicrExpiredStep
                        ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE -> journey.hasUploadedEicr
                    }
                }
                savable()
            }
            step<AnyMembers, HasAnyInCollectionStepConfig>(journey.hasUploadedEicr) {
                parents {
                    journey.eicrExpiryDateStep.hasOutcome(
                        ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE,
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
                    journey.eicrExpiryDateStep.hasOutcome(ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE)
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
                    journey.eicrExpiryDateStep.hasOutcome(ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED)
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
                        journey.eicrExpiredStep.isComplete(),
                        journey.checkEicrUploadsStep.isComplete(),
                    )
                }
            }
        }
}
