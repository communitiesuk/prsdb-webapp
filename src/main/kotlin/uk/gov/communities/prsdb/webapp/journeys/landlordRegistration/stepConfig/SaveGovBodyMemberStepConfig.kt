package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

@JourneyFrameworkComponent
class SaveGovBodyMemberStepConfig : AbstractInternalStepConfig<Complete, LandlordRegistrationOrgLandlordState>() {
    override fun mode(state: LandlordRegistrationOrgLandlordState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: LandlordRegistrationOrgLandlordState) {
        val name =
            state.getStepData(OrgGovBodyMemberNameStep.ROUTE_SEGMENT)?.get("name") as? String
                ?: throw PrsdbWebException("Governing body member name step data is missing")

        val type =
            state.getStepData(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT)?.get("whoToProvide") as? GoverningBodyMemberType
                ?: throw PrsdbWebException("Governing body member type step data is missing")

        val dobData =
            state.getStepData(OrgGovBodyMemberDobStep.ROUTE_SEGMENT)
                ?: throw PrsdbWebException("Governing body member date of birth step data is missing")
        val dateOfBirth =
            DateTimeHelper.parseDateOrNull(
                day = dobData["day"] as? String ?: "",
                month = dobData["month"] as? String ?: "",
                year = dobData["year"] as? String ?: "",
            ) ?: throw PrsdbWebException("Governing body member date of birth is invalid")

        val address = state.govBodyMemberAddressTask.getAddress()

        val currentMap = state.governingBodyMembersMap?.toMutableMap() ?: mutableMapOf()
        val nextKey = state.nextGoverningBodyMemberId ?: ((currentMap.keys.maxOrNull() ?: 0) + 1)

        val member =
            GoverningBodyMemberDataModel(
                name = name,
                type = type,
                dateOfBirth = dateOfBirth,
                address = address,
            )

        currentMap[nextKey] = member
        state.governingBodyMembersMap = currentMap
        state.nextGoverningBodyMemberId = nextKey + 1

        // Clear the individual step form data so the next member starts fresh
        state.clearStepData(OrgGovBodyMemberNameStep.ROUTE_SEGMENT)
        state.clearStepData(OrgGovBodyMemberDobStep.ROUTE_SEGMENT)
        state.clearStepData(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT)
    }
}

@JourneyFrameworkComponent
final class SaveGovBodyMemberStep(
    stepConfig: SaveGovBodyMemberStepConfig,
) : JourneyStep.InternalStep<Complete, LandlordRegistrationOrgLandlordState>(stepConfig)
