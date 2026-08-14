package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.ORGANISATION_CONTACTS_FRAGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.GovBodyMembersListState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import java.security.Principal

@PrsdbWebService
class UpdateGoverningBodyJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateGoverningBodyJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }

            step(journey.initialiseGovBodyMembersStep) {
                initialStep()
                routeSegment(InitialiseGovBodyMembersForGovBodyUpdateStep.ROUTE_SEGMENT)
                nextStep { journey.orgGovBodyMembersTask.firstStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents { journey.initialiseGovBodyMembersStep.isComplete() }
                nextStep { journey.cyaStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        listState = journey,
                        allowRemovingLastMember = false,
                    )
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateGoverningBodyCyaStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyMembersTask.isComplete() }
                nextStep { journey.completeGoverningBodyUpdateStep }
            }
            step(journey.completeGoverningBodyUpdateStep) {
                parents { journey.cyaStep.isComplete() }
                nextUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateGoverningBodyJourney(
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val initialiseGovBodyMembersStep: InitialiseGovBodyMembersForGovBodyUpdateStep,
    override val cyaStep: UpdateGoverningBodyCyaStep,
    override val completeGoverningBodyUpdateStep: CompleteGoverningBodyUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "governing-body",
) : AbstractJourneyState(journeyStateService),
    UpdateGoverningBodyJourneyState {
    override var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>? by delegateProvider.nullableDelegate(
        "governingBodyMembersMap",
    )
    override var nextGoverningBodyMemberId: Int? by delegateProvider.nullableDelegate("nextGoverningBodyMemberId")
    override var editingGovBodyMemberId: Int? by delegateProvider.nullableDelegate("editingGovBodyMemberId")
    override var governingBodyMembersInitialised: Boolean? by delegateProvider.nullableDelegate("governingBodyMembersInitialised")

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateGoverningBodyJourneyState :
    JourneyState,
    GovBodyMembersListState {
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val initialiseGovBodyMembersStep: InitialiseGovBodyMembersForGovBodyUpdateStep
    val cyaStep: UpdateGoverningBodyCyaStep
    val completeGoverningBodyUpdateStep: CompleteGoverningBodyUpdateStep
    var governingBodyMembersInitialised: Boolean?
}
