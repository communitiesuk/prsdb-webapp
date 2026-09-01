package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseHoldsAndTenantsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateOccupancyJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOccupancyJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val featureFlagManager: FeatureFlagManager,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnership.getMostRecentlyUpdated().toString()
            state.wasOccupied = propertyOwnership.isOccupied
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        val isRedesigned = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        return if (isRedesigned) {
            redesignedJourneyMap(state, checkingAnswersFor, propertyId)
        } else if (checkingAnswersFor == null) {
            oldMainJourneyMap(state, propertyId)
        } else {
            oldCheckYourAnswersJourneyMap(state, checkingAnswersFor, propertyId)
        }
    }

    private fun redesignedJourneyMap(
        state: UpdateOccupancyJourney,
        checkingAnswersFor: String?,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> =
        if (featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)) {
            if (checkingAnswersFor == null) {
                redesignedWithCheckAnswersJourneyMap(state, propertyId)
            } else {
                redesignedCheckYourAnswersJourneyMap(state, checkingAnswersFor, propertyId)
            }
        } else {
            redesignedSinglePageJourneyMap(state, propertyId)
        }

    private fun redesignedWithCheckAnswersJourneyMap(
        state: UpdateOccupancyJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            step(journey.occupied) {
                routeSegment(OccupiedStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { propertyDetailsRoute }
                nextStep { journey.interruptionStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "fieldSetHeading" to "forms.update.occupancy.occupied.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.saveAndContinue",
                    )
                }
            }
            // TODO(PDJB-1417): show the interruption only when the change removes an existing letting agent
            step(journey.interruptionStep) {
                routeSegment(UpdateOccupancyInterruptionStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.occupied.hasOutcome(YesOrNo.YES),
                        journey.occupied.hasOutcome(YesOrNo.NO),
                    )
                }
                nextStep { journey.checkYourAnswersStep }
            }
            step(journey.checkYourAnswersStep) {
                routeSegment(UpdateOccupancyCheckYourAnswersStep.ROUTE_SEGMENT)
                parents { journey.interruptionStep.isComplete() }
                nextStep { journey.completeOccupancyUpdateStep }
            }
            step(journey.completeOccupancyUpdateStep) {
                parents { journey.checkYourAnswersStep.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
        }
    }

    private fun redesignedCheckYourAnswersJourneyMap(
        state: UpdateOccupancyJourney,
        checkingAnswersFor: String,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            when (checkingAnswersFor) {
                OccupiedStep.ROUTE_SEGMENT -> {
                    step(journey.occupied) {
                        initialStep()
                        routeSegment(OccupiedStep.ROUTE_SEGMENT)
                        nextStep { journey.interruptionStep }
                        withAdditionalContentProperties {
                            mapOf(
                                "title" to "propertyDetails.update.title",
                                "fieldSetHeading" to "forms.update.occupancy.occupied.fieldSetHeading",
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            )
                        }
                    }
                    // TODO(PDJB-1417): show the interruption only when the change removes an existing letting agent
                    step(journey.interruptionStep) {
                        routeSegment(UpdateOccupancyInterruptionStep.ROUTE_SEGMENT)
                        parents { journey.occupied.isComplete() }
                        nextStep { journey.finishCyaStep }
                    }
                }

                else -> {
                    throw IllegalStateException("Unknown step being checked: $checkingAnswersFor")
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }
    }

    private fun redesignedSinglePageJourneyMap(
        state: UpdateOccupancyJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            step(journey.occupied) {
                routeSegment(OccupiedStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { propertyDetailsRoute }
                nextStep { journey.completeOccupancyUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "fieldSetHeading" to "forms.update.occupancy.occupied.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "submitButton" to "transactionSubmitButton",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completeOccupancyUpdateStep) {
                parents {
                    OrParents(
                        journey.occupied.hasOutcome(YesOrNo.YES),
                        journey.occupied.hasOutcome(YesOrNo.NO),
                    )
                }
                nextUrl { propertyDetailsRoute }
            }
        }
    }

    // TODO(PDJB-1340): delete this old (flag-off) journey when PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed.
    private fun oldMainJourneyMap(
        state: UpdateOccupancyJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            task(journey.occupationTask.inJourney(journey)) {
                initialStep()
                backUrl { propertyDetailsRoute }
                nextStep { journey.legacyCyaStep }
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.legacyCyaStep) {
                routeSegment(UpdateOccupancyCyaStep.ROUTE_SEGMENT)
                parents { journey.occupationTask.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
            replaceHeadings(state)
        }
    }

    // TODO(PDJB-1340): delete this old (flag-off) journey when PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed.
    private fun oldCheckYourAnswersJourneyMap(
        state: UpdateOccupancyJourney,
        checkingAnswersFor: String,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            configure {
                withAdditionalContentProperty { "title" to "propertyDetails.update.title" }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            when (checkingAnswersFor) {
                OccupiedStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.occupationTask.inJourney(journey))
                }

                HouseholdStep.ROUTE_SEGMENT, TenantsStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.householdsAndTenantsTask)
                }

                BedroomsStep.ROUTE_SEGMENT -> {
                    checkAnswerStep(journey.bedrooms, BedroomsStep.ROUTE_SEGMENT)
                }

                RentIncludesBillsStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.rentIncludesBillsTask)
                }

                BillsIncludedStep.ROUTE_SEGMENT -> {
                    fromTask(journey.rentIncludesBillsTask) {
                        checkAnswerStep(task.billsIncluded, BillsIncludedStep.ROUTE_SEGMENT)
                    }
                }

                FurnishedStatusStep.ROUTE_SEGMENT -> {
                    checkAnswerStep(journey.furnishedStatus, FurnishedStatusStep.ROUTE_SEGMENT)
                }

                RentFrequencyStep.ROUTE_SEGMENT, RentAmountStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.rentFrequencyAndAmountTask)
                }

                else -> {
                    throw IllegalStateException("Unknown step being checked: $checkingAnswersFor")
                }
            }
            replaceHeadings(state)
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))

    // TODO(PDJB-1340): delete this helper (only used by the old flag-off journeys above) when
    // PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed.
    private fun JourneyBuilder<UpdateOccupancyJourney>.replaceHeadings(state: UpdateOccupancyJourney) {
        configureStep(journey.occupied) {
            withAdditionalContentProperty {
                "fieldSetHeading" to "forms.update.occupancy.occupied.fieldSetHeading"
            }
        }
        configureStep(journey.householdsAndTenantsTask.households) {
            withAdditionalContentProperty {
                "fieldSetHeading" to "forms.update.numberOfHouseholds.fieldSetHeading"
            }
        }
        configureStep(journey.householdsAndTenantsTask.tenants) {
            withAdditionalContentProperty {
                "fieldSetHeading" to "forms.update.numberOfPeople.fieldSetHeading"
            }
        }
        configureStep(journey.bedrooms) {
            withAdditionalContentProperty {
                "heading" to "forms.update.numberOfBedrooms.heading"
            }
        }
        configureStep(journey.rentIncludesBillsTask.rentIncludesBills) {
            withAdditionalContentProperty {
                "fieldSetHeading" to "forms.update.rentIncludesBills.fieldSetHeading"
            }
        }
        configureStep(journey.rentIncludesBillsTask.billsIncluded) {
            withAdditionalContentProperty {
                "fieldSetHeading" to "forms.update.billsIncluded.fieldSetHeading"
            }
        }
        configureStep(journey.furnishedStatus) {
            withAdditionalContentProperty {
                "fieldSetHeading" to "forms.update.furnishedStatus.fieldSetHeading"
            }
        }
        configureStep(journey.rentFrequencyAndAmountTask.rentFrequency) {
            withAdditionalContentProperty {
                "heading" to "forms.update.rentFrequency.heading"
            }
        }
        configureStep(journey.rentFrequencyAndAmountTask.rentAmount) {
            withAdditionalContentProperty {
                "heading" to state.rentFrequencyAndAmountTask.getUpdateRentAmountHeading()
            }
        }
    }
}

@JourneyFrameworkComponent
class UpdateOccupancyJourney(
    // Occupancy task
    override val occupationTask: OccupationTask,
    override val occupied: OccupiedStep,
    // Nested households and tenants task
    override val householdsAndTenantsTask: HouseholdsAndTenantsTask,
    override val bedrooms: BedroomsStep,
    // Nested rent includes bills task
    override val rentIncludesBillsTask: RentIncludesBillsTask,
    override val furnishedStatus: FurnishedStatusStep,
    // Nested rent frequency and amount task
    override val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask,
    // TODO(PDJB-1340): delete these old (flag-off) check-your-answers steps when
    // PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed (the redesigned update is a single page).
    override val legacyCyaStep: UpdateOccupancyCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    // Completion step for the redesigned single-page update
    override val completeOccupancyUpdateStep: CompleteOccupancyUpdateStep,
    // Check-your-answers step for the redesigned update (included when DELEGATE_TO_LETTING_AGENT is enabled -
    // see redesignedJourneyMap)
    override val checkYourAnswersStep: UpdateOccupancyCheckYourAnswersStep,
    // Skeleton interruption / "are you sure" step for the redesigned update (included when DELEGATE_TO_LETTING_AGENT
    // is enabled - see redesignedJourneyMap)
    override val interruptionStep: UpdateOccupancyInterruptionStep,
    private val featureFlagManager: FeatureFlagManager,
    journeyStateService: JourneyStateService,
    journeyName: String = "occupancy",
    override val stateFactory: ObjectFactory<UpdateOccupancyJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateOccupancyJourneyState {
    // The redesigned update with letting agent delegation uses the new check-your-answers step; every other
    // variant (the redesigned single-page update with delegation off, and the old flag-off journey) uses the
    // legacy check-your-answers step. All variants share a single state object, so switch on the flags here.
    override val cyaStep: JourneyStep.RequestableStep<*, *, *>
        get() =
            if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING) &&
                featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)
            ) {
                checkYourAnswersStep
            } else {
                legacyCyaStep
            }

    override var propertyId: Long by delegateProvider.requiredDelegate("propertyId")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()

    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")

    override var wasOccupied: Boolean by delegateProvider.requiredImmutableDelegate("wasOccupied")

    override var cachedOccupied: Boolean? by delegateProvider.nullableDelegate("cachedOccupied")

    override val householdsAndTenantsDependencies = HouseHoldsAndTenantsDependencies(false)
}

interface UpdateOccupancyJourneyState :
    OccupationState,
    CheckYourAnswersJourneyState {
    val occupationTask: OccupationTask
    val legacyCyaStep: UpdateOccupancyCyaStep
    val completeOccupancyUpdateStep: CompleteOccupancyUpdateStep
    val checkYourAnswersStep: UpdateOccupancyCheckYourAnswersStep
    val interruptionStep: UpdateOccupancyInterruptionStep
    val propertyId: Long
    val lastModifiedDate: String
    val wasOccupied: Boolean
}
