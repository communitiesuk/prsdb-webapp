package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@JourneyFrameworkComponent
class SaveSuperiorLandlordStepConfig : AbstractInternalStepConfig<Complete, SuperiorLandlordsDetailsState>() {
    override fun mode(state: SuperiorLandlordsDetailsState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: SuperiorLandlordsDetailsState) {
        val provideDetails =
            state.superiorLandlordProvideDetailsStep.formModelOrNull?.provideDetails
                ?: throw UnrecoverableJourneyStateException(
                    state.journeyId,
                    "Superior landlord provide details step data is missing for journey ${state.journeyId}",
                )

        val superiorLandlord =
            if (provideDetails) {
                val contactType =
                    state.superiorLandlordContactTypeStep.formModelOrNull?.contactType
                        ?: throw UnrecoverableJourneyStateException(
                            state.journeyId,
                            "Superior landlord contact type step data is missing for journey ${state.journeyId}",
                        )
                val name =
                    state.superiorLandlordNameStep.formModelOrNull?.name
                        ?: throw UnrecoverableJourneyStateException(
                            state.journeyId,
                            "Superior landlord name step data is missing for journey ${state.journeyId}",
                        )
                when (contactType) {
                    SuperiorLandlordContactType.EMAIL -> buildSuperiorLandlordWithEmail(name)
                    SuperiorLandlordContactType.POSTAL_ADDRESS -> buildSuperiorLandlordWithAddress(state, name)
                }
            } else {
                SuperiorLandlordDataModel(detailsKnown = false)
            }

        val currentMap = state.superiorLandlordsMap?.toMutableMap() ?: mutableMapOf()

        val targetKey =
            state.editingSuperiorLandlordId ?: run {
                val nextKey = state.nextSuperiorLandlordId ?: ((currentMap.keys.maxOrNull() ?: 0) + 1)
                state.nextSuperiorLandlordId = nextKey + 1
                nextKey
            }

        currentMap[targetKey] = superiorLandlord
        state.superiorLandlordsMap = currentMap
        state.editingSuperiorLandlordId = null

        // Clear the individual step form data so the next superior landlord starts fresh
        state.superiorLandlordProvideDetailsStep.clearFormData()
        state.superiorLandlordNameStep.clearFormData()
        state.superiorLandlordContactTypeStep.clearFormData()
        // TODO PDJB-1975: check this still clears the real email page correctly
        state.superiorLandlordEmailStep.clearFormData()
        state.superiorLandlordAddressTask.clearFormData()
    }

    private fun buildSuperiorLandlordWithEmail(name: String): SuperiorLandlordDataModel =
        SuperiorLandlordDataModel(
            detailsKnown = true,
            name = name,
            // TODO PDJB-1975: save the email address the user entered on the superior landlord email step, throwing if it is missing
            email = PLACEHOLDER_EMAIL,
        )

    private fun buildSuperiorLandlordWithAddress(
        state: SuperiorLandlordsDetailsState,
        name: String,
    ): SuperiorLandlordDataModel {
        val addressTask = state.superiorLandlordAddressTask
        val address = addressTask.getAddress()
        val lookupFormModel = addressTask.lookupAddressStep.formModelOrNull
        // Set by both the select address and no address found steps, so it reflects the user's latest choice
        val selectedAddress = addressTask.cachedSelectedAddress
        val manualFormModel = addressTask.manualAddressStep.formModelOrNull?.takeIf { selectedAddress == MANUAL_ADDRESS_CHOSEN }

        return SuperiorLandlordDataModel(
            detailsKnown = true,
            name = name,
            address = address,
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

    companion object {
        // TODO PDJB-1975: remove once the email the user entered is saved instead
        const val PLACEHOLDER_EMAIL = "placeholder.superior.landlord@example.com"
    }
}

@JourneyFrameworkComponent
final class SaveSuperiorLandlordStep(
    stepConfig: SaveSuperiorLandlordStepConfig,
) : JourneyStep.InternalStep<Complete, SuperiorLandlordsDetailsState>(stepConfig)
