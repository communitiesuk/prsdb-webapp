package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
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
                FreeholderDataModel(
                    detailsKnown = true,
                    name =
                        state.freeholderNameStep.formModelOrNull?.name
                            ?: throw PrsdbWebException("Freeholder name step data is missing"),
                    // TODO PDJB-1966: save the email address the user entered on the freeholder email step
                    email = if (contactType == FreeholderContactType.EMAIL) PLACEHOLDER_EMAIL else null,
                    // TODO PDJB-1967: save the address the user entered in the freeholder address task, along with the
                    //  address lookup, select and manual entry answers needed to prefill those steps on change
                    address = if (contactType == FreeholderContactType.POSTAL_ADDRESS) PLACEHOLDER_ADDRESS else null,
                )
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
        // TODO PDJB-1965: check this still clears the real contact type page correctly
        state.freeholderContactTypeStep.clearFormData()
        // TODO PDJB-1966: check this still clears the real email page correctly
        state.freeholderEmailStep.clearFormData()
        // TODO PDJB-1967: check this still clears all the real address steps correctly
        state.freeholderAddressTask.clearFormData()
    }

    companion object {
        // TODO PDJB-1966: remove once the email the user entered is saved instead
        const val PLACEHOLDER_EMAIL = "placeholder.freeholder@example.com"

        // TODO PDJB-1967: remove once the address the user entered is saved instead
        val PLACEHOLDER_ADDRESS =
            AddressDataModel.fromManualAddressData(
                addressLineOne = "1 Placeholder Street",
                townOrCity = "Placeholder Town",
                postcode = "AB1 2CD",
            )
    }
}

@JourneyFrameworkComponent
final class SaveFreeholderStep(
    stepConfig: SaveFreeholderStepConfig,
) : JourneyStep.InternalStep<Complete, FreeholdersDetailsState>(stepConfig)
