package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep

@JourneyFrameworkComponent("propertyRegistrationElectricalSafetyTask")
class ElectricalSafetyTask(
    journeyStateService: JourneyStateService,
    override val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask,
    override val hasElectricalCertStep: HasElectricalCertStep,
    override val electricalCertExpiryDateStep: ElectricalCertExpiryDateStep,
    override val uploadElectricalCertStep: UploadElectricalCertStep,
    override val hasUploadedElectricalCert: HasAnyInCollectionStep,
    override val checkElectricalCertUploadsStep: CheckElectricalCertUploadsStep,
    override val removeElectricalCertUploadStep: RemoveElectricalCertUploadStep,
    override val electricalCertExpiredStep: ElectricalCertExpiredStep,
    override val electricalCertMissingStep: ElectricalCertMissingStep,
    override val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep,
    override val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep,
) : DuplicableTaskWithDependencies<ElectricalSafetyState, ElectricalSafetyDependencies>(journeyStateService),
    ElectricalSafetyState {
    override val isOccupied: Boolean
        get() = dependencies.isOccupied
    override val allowProvideCertificateLaterRoute: Boolean
        get() = dependencies.allowProvideCertificateLaterRoute

    override var electricalUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("electricalUploadMap", mapOf())
    override var highestAssignedElectricalMemberId: Int? by delegateProvider.nullableDelegate("highestAssignedElectricalMemberId")

    override fun makeSubJourney(state: ElectricalSafetyState) =
        subJourney(state) {
            task(journey.electricalSafetyDetailsTask) {
                nextStep { journey.checkElectricalSafetyAnswersStep }
                savable()
            }
            step(journey.checkElectricalSafetyAnswersStep) {
                routeSegment(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.electricalSafetyDetailsTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkElectricalSafetyAnswersStep.isComplete() }
            }
        }

    override val taskState: ElectricalSafetyState
        get() = this
}

interface ElectricalSafetyDependencies {
    val isOccupied: Boolean
    val allowProvideCertificateLaterRoute: Boolean
}
