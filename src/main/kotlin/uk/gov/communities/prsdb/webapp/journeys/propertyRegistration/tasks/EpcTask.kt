package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyOccupiedCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.StartEpcStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

@JourneyFrameworkComponent("propertyRegistrationEpcTask")
class EpcTask(
    journeyStateService: JourneyStateService,
    override val epcDetailsTask: EpcDetailsTask,
    override val startEpcStep: StartEpcStep,
    override val epcLookupByUprnStep: EpcLookupByUprnStep,
    override val hasEpcStep: HasEpcStep,
    override val checkUprnMatchedEpcStep: ConfirmEpcRetrievedByUprnStep,
    override val epcAgeCheckStep: EpcAgeCheckStep,
    override val epcEnergyRatingCheckStep: EpcEnergyRatingCheckStep,
    override val isPropertyOccupiedCheckStep: PropertyOccupiedCheckStep,
    override val confirmEpcDetailsRetrievedByCertificateNumberStep: ConfirmEpcDetailsRetrievedByCertificateNumberStep,
    override val findYourEpcStep: FindYourEpcStep,
    override val checkSupersededEpcStep: EpcSuperseededStep,
    override val epcNotFoundStep: EpcNotFoundStep,
    override val epcInDateAtStartOfTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep,
    override val hasMeesExemptionStep: HasMeesExemptionStep,
    override val meesExemptionStep: MeesExemptionStep,
    override val lowEnergyRatingStep: LowEnergyRatingStep,
    override val epcExpiredStep: EpcExpiredStep,
    override val isEpcRequiredStep: IsEpcRequiredStep,
    override val epcExemptionStep: EpcExemptionStep,
    override val epcMissingStep: EpcMissingStep,
    override val provideEpcLaterStep: ProvideEpcLaterStep,
    override val checkEpcAnswersStep: CheckEpcAnswersStep,
) : DuplicableTaskWithDependencies<EpcState, EpcDependencies>(journeyStateService),
    EpcState {
    override val isOccupied: Boolean?
        get() = dependencies.isOccupied
    override val uprn: Long?
        get() = dependencies.uprn
    override val allowProvideCertificateLaterRoute: Boolean
        get() = dependencies.allowProvideCertificateLaterRoute

    override var epcRetrievedByUprn: EpcDataModel? by delegateProvider.nullableDelegate("epcRetrievedByUprn")
    override var epcRetrievedByUprnUpdatedSinceUserReview: Boolean?
        by delegateProvider.nullableDelegate("epcRetrievedByUprnUpdatedSinceUserReview")
    override var epcRetrievedByCertificateNumber: EpcDataModel? by delegateProvider.nullableDelegate("epcRetrievedByCertificateNumber")
    override var epcRetrievedByCertificateNumberUpdatedSinceUserReview: Boolean?
        by delegateProvider.nullableDelegate("epcRetrievedByCertificateNumberUpdatedSinceUserReview")
    override var updatedEpcRetrievedByCertificateNumber: EpcDataModel? by delegateProvider
        .nullableDelegate("updatedEpcRetrievedByCertificateNumber")
    override var acceptedEpc: EpcDataModel? by delegateProvider.nullableDelegate("acceptedEpc")

    override fun makeSubJourney(state: EpcState) =
        subJourney(state) {
            taskStatus {
                when {
                    exitStep.isStepReachable -> TaskStatus.COMPLETED
                    journey.checkUprnMatchedEpcStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.hasEpcStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.startEpcStep.isStepReachable -> TaskStatus.NOT_STARTED
                    else -> TaskStatus.CANNOT_START
                }
            }
            task(journey.epcDetailsTask) {
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            step(journey.checkEpcAnswersStep) {
                routeSegment(CheckEpcAnswersStep.ROUTE_SEGMENT)
                parents { journey.epcDetailsTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkEpcAnswersStep.isComplete() }
            }
        }

    override val taskState: EpcState
        get() = this
}

interface EpcDependencies {
    val isOccupied: Boolean?
    val uprn: Long?
    val allowProvideCertificateLaterRoute: Boolean
}
