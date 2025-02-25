package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class UpdateLandlordDetailsJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getEmailUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateDetailsStepId.UpdateEmail.urlPathSegment,
                "emailAddress",
            )

        fun getNameUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateDetailsStepId.UpdateName.urlPathSegment,
                "name",
            )

        fun getAddressIfPresent(
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            val selectedAddress =
                getFieldStringValue(
                    journeyData,
                    UpdateDetailsStepId.SelectEnglandAndWalesAddress.urlPathSegment,
                    "address",
                )

            return if (selectedAddress == MANUAL_ADDRESS_CHOSEN) {
                getManualAddress(journeyData, UpdateDetailsStepId.ManualEnglandAndWalesAddress.urlPathSegment)
            } else if (selectedAddress != null) {
                addressDataService.getAddressData(selectedAddress)
            } else {
                null
            }
        }
    }
}
