package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

@JourneyFrameworkComponent
class SaveFreeholderStepConfig : AbstractInternalStepConfig<Complete, FreeholdersDetailsState>() {
    override fun mode(state: FreeholdersDetailsState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: FreeholdersDetailsState) {
        val provideDetails =
            state.freeholderProvideDetailsStep.formModelOrNull?.provideDetails
                ?: throw PrsdbWebException("Freeholder provide details step data is missing")

        val freeholder =
            if (provideDetails == YesOrNo.YES) {
                val contactType =
                    state.freeholderContactTypeStep.formModelOrNull?.contactType
                        ?: throw PrsdbWebException("Freeholder contact type step data is missing")
                val name =
                    state.freeholderNameStep.formModelOrNull?.name
                        ?: throw PrsdbWebException("Freeholder name step data is missing")
                when (contactType) {
                    FreeholderContactType.EMAIL ->
                        FreeholderDataModel(
                            detailsKnown = true,
                            name = name,
                            email =
                                state.freeholderEmailStep.formModelOrNull?.emailAddress
                                    ?: throw PrsdbWebException("Freeholder email step data is missing"),
                        )
                    FreeholderContactType.POSTAL_ADDRESS -> buildFreeholderWithAddress(state, name)
                }
            } else {
                FreeholderDataModel(detailsKnown = false)
            }

        val currentMap = state.freeholdersMap?.toMutableMap() ?: mutableMapOf()

        val targetKey =
            state.editingFreeholderId ?: run {
                val nextKey = state.nextFreeholderId ?: ((currentMap.keys.maxOrNull() ?: 0) + 1)
                state.nextFreeholderId = nextKey + 1
                nextKey
            }

        currentMap[targetKey] = freeholder
        state.freeholdersMap = currentMap
        state.editingFreeholderId = null

        // Clear the individual step form data so the next freeholder starts fresh
        // TODO PDJB-1963: check this still clears the real provide details page correctly
        state.freeholderProvideDetailsStep.clearFormData()
        state.freeholderNameStep.clearFormData()
        state.freeholderContactTypeStep.clearFormData()
        state.freeholderEmailStep.clearFormData()
        state.freeholderAddressTask.clearFormData()
    }

    private fun buildFreeholderWithAddress(
        state: FreeholdersDetailsState,
        name: String,
    ): FreeholderDataModel {
        val addressTask = state.freeholderAddressTask
        val lookupFormModel = addressTask.lookupAddressStep.formModelOrNull
        // Set by both the select address and no address found steps, so it reflects the user's latest choice
        val selectedAddress = addressTask.cachedSelectedAddress
        val manualFormModel = addressTask.manualAddressStep.formModelOrNull?.takeIf { selectedAddress == MANUAL_ADDRESS_CHOSEN }

        return FreeholderDataModel(
            detailsKnown = true,
            name = name,
            address = addressTask.getAddress(),
            addressSearchPostcode = lookupFormModel?.postcode,
            addressSearchHouseNameOrNumber = lookupFormModel?.houseNameOrNumber,
            selectedAddress = selectedAddress,
            manualAddressLineOne = manualFormModel?.addressLineOne,
            manualAddressLineTwo = manualFormModel?.addressLineTwo,
            manualTownOrCity = manualFormModel?.townOrCity,
            manualCounty = manualFormModel?.county,
            manualPostcode = manualFormModel?.postcode,
        )
    }
}

@JourneyFrameworkComponent
final class SaveFreeholderStep(
    stepConfig: SaveFreeholderStepConfig,
) : JourneyStep.InternalStep<Complete, FreeholdersDetailsState>(stepConfig)
