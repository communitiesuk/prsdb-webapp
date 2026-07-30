package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
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

/*
 * This is a legacy task - it's "taskState" does not return itself but the journey it belongs to. This is because it's used
 * for a shared implementation of the legacy journey.
 * TODO PDJB-1340 - Remove this class
 */
@JourneyFrameworkComponent
class OccupationTask(
    private val featureFlagManager: FeatureFlagManager,
    journeyStateService: JourneyStateService,
) : Task<OccupationState, OccupationState>(journeyStateService) {
    override val taskState get() = dependencies

    fun inJourney(state: OccupationState): OccupationTask {
        bindDependencies(state)
        return this
    }

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
            task(journey.householdsAndTenantsTask) {
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                withDependencies { dependencies.householdsAndTenantsDependencies }
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
            task(journey.rentIncludesBillsTask) {
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
            task(journey.rentFrequencyAndAmountTask) {
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
