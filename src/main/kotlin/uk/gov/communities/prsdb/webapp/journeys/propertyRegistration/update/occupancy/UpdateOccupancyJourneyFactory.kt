package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateOccupancyJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOccupancyJourney>,
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

        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { "/" }
            task(journey.occupationTask) {
                initialStep()
                nextStep { journey.cyaStep }
                checkable()
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(RegisterPropertyStepId.CheckAnswers.urlPathSegment)
                parents { journey.occupationTask.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
            configureStep(journey.occupied) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.occupancy.occupied.fieldSetHeading"
                }
            }
            configureStep(journey.households) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.numberOfHouseholds.fieldSetHeading"
                }
            }
            configureStep(journey.tenants) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.numberOfPeople.fieldSetHeading"
                }
            }
            configureStep(journey.bedrooms) {
                withAdditionalContentProperty {
                    "heading" to "forms.update.numberOfBedrooms.heading"
                }
            }
            configureStep(journey.rentIncludesBills) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.rentIncludesBills.fieldSetHeading"
                }
            }
            configureStep(journey.billsIncluded) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.billsIncluded.fieldSetHeading"
                }
            }
            configureStep(journey.furnishedStatus) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.furnishedStatus.fieldSetHeading"
                }
            }
            configureStep(journey.rentFrequency) {
                withAdditionalContentProperty {
                    "heading" to "forms.update.rentFrequency.heading"
                }
            }
            configureStep(journey.rentAmount) {
                withAdditionalContentProperty {
                    "heading" to getRentAmountHeading(state.rentFrequency.formModel.notNullValue(RentFrequencyFormModel::rentFrequency))
                }
            }
            checkYourAnswersJourney()
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))

    private fun getRentAmountHeading(rentFrequency: RentFrequency): String =
        when (rentFrequency) {
            RentFrequency.WEEKLY -> "forms.update.rentAmount.weekly.fieldSetHeading"
            RentFrequency.FOUR_WEEKLY -> "forms.update.rentAmount.fourWeekly.fieldSetHeading"
            else -> "forms.update.rentAmount.monthly.fieldSetHeading"
        }
}

@JourneyFrameworkComponent
class UpdateOccupancyJourney(
    // Occupancy task
    override val occupationTask: OccupationTask,
    override val occupied: OccupiedStep,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    override val bedrooms: BedroomsStep,
    override val rentIncludesBills: RentIncludesBillsStep,
    override val billsIncluded: BillsIncludedStep,
    override val furnishedStatus: FurnishedStatusStep,
    override val rentFrequency: RentFrequencyStep,
    override val rentAmount: RentAmountStep,
    // Check your answers step
    override val cyaStep: UpdateOccupancyCyaStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
    journeyName: String = "occupancy",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, delegateProvider, journeyName),
    UpdateOccupancyJourneyState {
    override var cyaChildJourneyIdIfInitialized: String? by delegateProvider.nullableDelegate("checkYourAnswersChildJourneyId")
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
}

interface UpdateOccupancyJourneyState :
    OccupationState,
    CheckYourAnswersJourneyState {
    val occupationTask: OccupationTask
    override val cyaStep: UpdateOccupancyCyaStep
    val propertyId: Long
    val lastModifiedDate: String
}
