package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class OccupationTaskWithProvideLaterAllowed(
    featureFlagManager: FeatureFlagManager,
) : OccupationTask(featureFlagManager) {
    override val householdsAndTenantsDependencies = HouseHoldsAndTenantsDependencies(true)
}

@JourneyFrameworkComponent
class OccupationTaskWithOccupationRequired(
    featureFlagManager: FeatureFlagManager,
) : OccupationTask(featureFlagManager) {
    override val householdsAndTenantsDependencies = HouseHoldsAndTenantsDependencies(false)
}

abstract class OccupationTask(
    private val featureFlagManager: FeatureFlagManager,
) : Task<OccupationState>() {
    // TODO PDJB-896: Remerge the three versions of occupation task when this class uses DuplicableTaskWithDependencies
    abstract val householdsAndTenantsDependencies: HouseHoldsAndTenantsDependencies

    override fun makeSubJourney(state: OccupationState) =
        subJourney(state) {
            val isRestructureAndSkippingEnabled =
                featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)

            step(journey.occupied) {
                routeSegment(OccupiedStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.householdsAndTenantsTask.firstStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            duplicableTask(journey.householdsAndTenantsTask) {
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                withDependencies { householdsAndTenantsDependencies }
                nextStep {
                    if (isRestructureAndSkippingEnabled) {
                        journey.rentIncludesBillsTask.firstStep
                    } else {
                        journey.bedrooms
                    }
                }
                savable()
            }
            if (!isRestructureAndSkippingEnabled) {
                step(journey.bedrooms) {
                    routeSegment(BedroomsStep.ROUTE_SEGMENT)
                    parents { journey.householdsAndTenantsTask.isComplete() }
                    nextStep { journey.rentIncludesBillsTask.firstStep }
                    savable()
                }
            }
            duplicableTask(journey.rentIncludesBillsTask) {
                parents {
                    if (isRestructureAndSkippingEnabled) {
                        journey.householdsAndTenantsTask.isComplete()
                    } else {
                        journey.bedrooms.hasOutcome(Complete.COMPLETE)
                    }
                }
                nextStep { journey.furnishedStatus }
            }
            step(journey.furnishedStatus) {
                routeSegment(FurnishedStatusStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBillsTask.isComplete() }
                nextStep { journey.rentFrequencyAndAmountTask.firstStep }
                savable()
            }
            duplicableTask(journey.rentFrequencyAndAmountTask) {
                parents {
                    journey.furnishedStatus.hasOutcome(Complete.COMPLETE)
                }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                savable()
                parents {
                    OrParents(
                        journey.rentFrequencyAndAmountTask.isComplete(),
                        journey.occupied.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
