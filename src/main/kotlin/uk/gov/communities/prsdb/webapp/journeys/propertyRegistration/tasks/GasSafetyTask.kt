package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep

@JourneyFrameworkComponent("propertyRegistrationGasSafetyTask")
class GasSafetyTask(
    journeyStateService: JourneyStateService,
    override val gasSafetyDetailsTask: GasSafetyDetailsTask,
    override val hasUploadedCert: HasAnyInCollectionStep,
    override val hasGasSupplyStep: HasGasSupplyStep,
    override val hasGasCertStep: HasGasCertStep,
    override val gasCertIssueDateStep: GasCertIssueDateStep,
    override val uploadGasCertStep: UploadGasCertStep,
    override val checkGasCertUploadsStep: CheckGasCertUploadsStep,
    override val removeGasCertUploadStep: RemoveGasCertUploadStep,
    override val gasCertExpiredStep: GasCertExpiredStep,
    override val gasCertMissingStep: GasCertMissingStep,
    override val provideGasCertLaterStep: ProvideGasCertLaterStep,
    override val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep,
) : DuplicableTaskWithDependencies<GasSafetyState, GasSafetyDependencies>(journeyStateService),
    GasSafetyState {
    override val isOccupied: Boolean
        get() = dependencies.isOccupied
    override val allowProvideCertificateLaterRoute: Boolean
        get() = dependencies.allowProvideCertificateLaterRoute

    override var gasUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("gasUploadMap", mapOf())
    override var highestAssignedGasMemberId: Int? by delegateProvider.nullableDelegate("highestGasUploadMemberId")

    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            task(journey.gasSafetyDetailsTask) {
                nextStep { journey.checkGasSafetyAnswersStep }
                savable()
            }
            step(journey.checkGasSafetyAnswersStep) {
                routeSegment(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyDetailsTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkGasSafetyAnswersStep.isComplete() }
            }
        }

    override val taskState: GasSafetyState
        get() = this
}

interface GasSafetyDependencies {
    val isOccupied: Boolean
    val allowProvideCertificateLaterRoute: Boolean
}
