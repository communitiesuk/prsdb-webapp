package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@JourneyFrameworkComponent
class SaveSuperiorLandlordStepConfig : AbstractInternalStepConfig<Complete, SuperiorLandlordsDetailsState>() {
    override fun mode(state: SuperiorLandlordsDetailsState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: SuperiorLandlordsDetailsState) {
        val provideDetails =
            state.superiorLandlordProvideDetailsStep.formModelOrNull?.provideDetails
                ?: throw PrsdbWebException("Superior landlord provide details step data is missing")

        val superiorLandlord =
            if (provideDetails) {
                val contactType =
                    state.superiorLandlordContactTypeStep.formModelOrNull?.contactType
                        ?: throw PrsdbWebException("Superior landlord contact type step data is missing")
                val name =
                    state.superiorLandlordNameStep.formModelOrNull?.name
                        ?: throw PrsdbWebException("Superior landlord name step data is missing")
                SuperiorLandlordDataModel(
                    detailsKnown = true,
                    name = name,
                    // TODO PDJB-1975: save the email address the user entered on the superior landlord email step, throwing if it is missing
                    email = if (contactType == SuperiorLandlordContactType.EMAIL) PLACEHOLDER_EMAIL else null,
                    // TODO PDJB-1976: save the address the user entered in the superior landlord address task, along with
                    //  the address lookup, select and manual entry answers needed to prefill those steps on change, throwing if it is missing
                    address = if (contactType == SuperiorLandlordContactType.POSTAL_ADDRESS) PLACEHOLDER_ADDRESS else null,
                )
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
        // TODO PDJB-1974: check this still clears the real contact type page correctly
        state.superiorLandlordContactTypeStep.clearFormData()
        // TODO PDJB-1975: check this still clears the real email page correctly
        state.superiorLandlordEmailStep.clearFormData()
        // TODO PDJB-1976: check this still clears all the real address steps correctly
        state.superiorLandlordAddressTask.clearFormData()
    }

    companion object {
        // TODO PDJB-1975: remove once the email the user entered is saved instead
        const val PLACEHOLDER_EMAIL = "placeholder.superior.landlord@example.com"

        // TODO PDJB-1976: remove once the address the user entered is saved instead
        val PLACEHOLDER_ADDRESS =
            AddressDataModel.fromManualAddressData(
                addressLineOne = "1 Placeholder Street",
                townOrCity = "Placeholder Town",
                postcode = "AB1 2CD",
            )
    }
}

@JourneyFrameworkComponent
final class SaveSuperiorLandlordStep(
    stepConfig: SaveSuperiorLandlordStepConfig,
) : JourneyStep.InternalStep<Complete, SuperiorLandlordsDetailsState>(stepConfig)
