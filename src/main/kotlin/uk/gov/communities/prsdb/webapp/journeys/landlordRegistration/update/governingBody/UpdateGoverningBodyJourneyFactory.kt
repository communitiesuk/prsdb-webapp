package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PrsdbWebService
class UpdateGoverningBodyJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateGoverningBodyJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
            task(journey.orgGovBodyMembersTask) {
                initialStep()
                nextStep { journey.cyaStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        listState = journey,
                        allowRemovingLastMember = false,
                    )
                }
                withAdditionalContentProperty {
                    "title" to "landlordDetails.update.title"
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

    fun initializeJourneyState(user: Principal): String {
        val state = stateFactory.getObject()
        return state.initializeOrRestoreState(user)
    }

    internal fun buildInitialMembersMap(
        members: List<uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember>,
    ): Map<Int, GoverningBodyMemberDataModel> =
        members
            .mapIndexed { index, entity ->
                val key = index + 1
                key to
                    GoverningBodyMemberDataModel(
                        name = entity.name,
                        type = entity.type,
                        dateOfBirth = entity.dateOfBirth.toKotlinLocalDate(),
                        address = AddressDataModel.fromAddress(entity.address),
                        addressSearchPostcode = entity.address.postcode,
                        addressSearchHouseNameOrNumber =
                            entity.address.buildingNumber
                                ?: entity.address.buildingName
                                ?: entity.address.subBuilding
                                ?: entity.address.singleLineAddress.substringBefore(","),
                        databaseId = entity.id,
                        lastUpdatedAt = entity.getMostRecentlyUpdated().toString(),
                    )
            }.toMap()
}

@JourneyFrameworkComponent
class UpdateGoverningBodyJourney(
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val cyaStep: UpdateGoverningBodyCyaStep,
    override val completeGoverningBodyUpdateStep: CompleteGoverningBodyUpdateStep,
    private val journeyStateServiceInstance: JourneyStateService,
    private val userToLandlordService: UserToLandlordService,
    private val journeyFactory: UpdateGoverningBodyJourneyFactory,
    private val journeyName: String = "governing-body",
) : AbstractJourneyState(journeyStateServiceInstance),
    UpdateGoverningBodyJourneyState {
    override var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>? by delegateProvider.nullableDelegate(
        "governingBodyMembersMap",
    )
    override var nextGoverningBodyMemberId: Int? by delegateProvider.nullableDelegate("nextGoverningBodyMemberId")
    override var editingGovBodyMemberId: Int? by delegateProvider.nullableDelegate("editingGovBodyMemberId")

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }

    override fun initializeOrRestoreState(seed: Any?): String {
        val journeyId = generateJourneyId(seed)

        journeyStateServiceInstance.initialiseOrRestoreJourneyWithId(journeyId) {
            val landlord = userToLandlordService.getCurrentOrganisationLandlordForUser()
            val membersMap = journeyFactory.buildInitialMembersMap(landlord.governingBodyMembers)
            setValue(
                "governingBodyMembersMap",
                Json.encodeToString(membersMap),
            )
            setValue(
                "nextGoverningBodyMemberId",
                Json.encodeToString((membersMap.keys.maxOrNull() ?: 0) + 1),
            )
        }
        return journeyId
    }
}

interface UpdateGoverningBodyJourneyState :
    JourneyState,
    GovBodyMembersListState {
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val cyaStep: UpdateGoverningBodyCyaStep
    val completeGoverningBodyUpdateStep: CompleteGoverningBodyUpdateStep
}
