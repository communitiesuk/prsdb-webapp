package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

@JourneyFrameworkComponent
class SaveGovBodyMemberStepConfig : AbstractInternalStepConfig<Complete, OrgGovBodyMembersState>() {
    override fun mode(state: OrgGovBodyMembersState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: OrgGovBodyMembersState) {
        val name =
            state.orgGovBodyMemberNameStep.formModelOrNull?.name
                ?: throw PrsdbWebException("Governing body member name step data is missing")

        val type =
            state.orgGovBodyWhoToProvideStep.formModelOrNull?.whoToProvide
                ?: throw PrsdbWebException("Governing body member type step data is missing")

        val dobFormModel =
            state.orgGovBodyMemberDobStep.formModelOrNull
                ?: throw PrsdbWebException("Governing body member date of birth step data is missing")
        val dateOfBirth =
            DateTimeHelper.parseDateOrNull(dobFormModel.day, dobFormModel.month, dobFormModel.year)
                ?: throw PrsdbWebException("Governing body member date of birth is invalid")

        val address = state.govBodyMemberAddressTask.getAddress()
        val lookupFormModel = state.govBodyMemberAddressTask.lookupAddressStep.formModelOrNull
        val selectFormModel = state.govBodyMemberAddressTask.selectAddressStep.formModelOrNull
        val wentManual = selectFormModel?.address == MANUAL_ADDRESS_CHOSEN
        val manualFormModel =
            state.govBodyMemberAddressTask.manualAddressStep.formModelOrNull
                ?.takeIf { wentManual }

        val currentMap = state.governingBodyMembersMap?.toMutableMap() ?: mutableMapOf()

        val targetKey =
            state.editingGovBodyMemberId ?: run {
                val nextKey = state.nextGoverningBodyMemberId ?: ((currentMap.keys.maxOrNull() ?: 0) + 1)
                state.nextGoverningBodyMemberId = nextKey + 1
                nextKey
            }

        val member =
            GoverningBodyMemberDataModel(
                name = name,
                type = type,
                dateOfBirth = dateOfBirth,
                address = address,
                databaseId = state.editingGovBodyMember?.databaseId,
                lastUpdatedAt = state.editingGovBodyMember?.lastUpdatedAt,
                addressSearchPostcode = lookupFormModel?.postcode,
                addressSearchHouseNameOrNumber = lookupFormModel?.houseNameOrNumber,
                selectedAddress = selectFormModel?.address,
                manualAddressLineOne = manualFormModel?.addressLineOne,
                manualAddressLineTwo = manualFormModel?.addressLineTwo,
                manualTownOrCity = manualFormModel?.townOrCity,
                manualCounty = manualFormModel?.county,
                manualPostcode = manualFormModel?.postcode,
            )

        currentMap[targetKey] = member
        state.governingBodyMembersMap = currentMap
        state.editingGovBodyMemberId = null

        // Clear the individual step form data so the next member starts fresh
        state.orgGovBodyWhoToProvideStep.clearFormData()
        state.orgGovBodyMemberNameStep.clearFormData()
        state.orgGovBodyMemberDobStep.clearFormData()
        state.govBodyMemberAddressTask.clearFormData()
    }
}

@JourneyFrameworkComponent
final class SaveGovBodyMemberStep(
    stepConfig: SaveGovBodyMemberStepConfig,
) : JourneyStep.InternalStep<Complete, OrgGovBodyMembersState>(stepConfig)
