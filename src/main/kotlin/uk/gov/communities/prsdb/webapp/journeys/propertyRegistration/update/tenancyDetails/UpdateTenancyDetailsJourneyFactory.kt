package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.BedroomsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseHoldsAndTenantsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.duplicableCheckAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateTenancyDetailsJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateTenancyDetailsJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnershipService.getPropertyOwnership(propertyId).getMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state, propertyId)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor, propertyId)
        }
    }

    private fun mainJourneyMap(
        state: UpdateTenancyDetailsJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            configure {
                withAdditionalContentProperty { "title" to "propertyDetails.update.title" }
            }
            duplicableTask(journey.householdsAndTenantsTask) {
                initialStep()
                backUrl { propertyDetailsRoute }
                withDependencies { HouseHoldsAndTenantsDependencies(false) }
                nextStep { journey.bedrooms }
            }
            step(journey.bedrooms) {
                routeSegment(BedroomsStep.ROUTE_SEGMENT)
                parents { journey.householdsAndTenantsTask.isComplete() }
                nextStep { journey.rentIncludesBillsTask.firstStep }
            }
            duplicableTask(journey.rentIncludesBillsTask) {
                parents { journey.bedrooms.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.furnishedStatus }
            }
            step(journey.furnishedStatus) {
                routeSegment(FurnishedStatusStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBillsTask.isComplete() }
                nextStep { journey.rentFrequencyAndAmountTask.firstStep }
            }
            duplicableTask(journey.rentFrequencyAndAmountTask) {
                parents { journey.furnishedStatus.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateTenancyDetailsCyaStep.ROUTE_SEGMENT)
                parents { journey.rentFrequencyAndAmountTask.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
            replaceHeadings(state)
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: UpdateTenancyDetailsJourney,
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
                HouseholdStep.ROUTE_SEGMENT, TenantsStep.ROUTE_SEGMENT -> {
                    duplicableCheckAnswerTask(journey.householdsAndTenantsTask)
                }

                BedroomsStep.ROUTE_SEGMENT -> {
                    checkAnswerStep(journey.bedrooms, BedroomsStep.ROUTE_SEGMENT)
                }

                RentIncludesBillsStep.ROUTE_SEGMENT -> {
                    duplicableCheckAnswerTask(journey.rentIncludesBillsTask)
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
                    duplicableCheckAnswerTask(journey.rentFrequencyAndAmountTask)
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

    private fun JourneyBuilder<UpdateTenancyDetailsJourney>.replaceHeadings(state: UpdateTenancyDetailsJourney) {
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
class UpdateTenancyDetailsJourney(
    // Nested households and tenants task
    override val householdsAndTenantsTask: HouseholdsAndTenantsTask,
    override val bedrooms: BedroomsStep,
    // Nested rent includes bills task
    override val rentIncludesBillsTask: RentIncludesBillsTask,
    override val furnishedStatus: FurnishedStatusStep,
    // Nested rent frequency and amount task
    override val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask,
    // Check your answers step
    override val cyaStep: UpdateTenancyDetailsCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "tenancy details",
    override val stateFactory: ObjectFactory<UpdateTenancyDetailsJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateTenancyDetailsJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()

    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
}

interface UpdateTenancyDetailsJourneyState :
    TenancyDetailsState,
    BedroomsState,
    CheckYourAnswersJourneyState {
    override val cyaStep: UpdateTenancyDetailsCyaStep
    val propertyId: Long
    val lastModifiedDate: String
}
