package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class UpdateLandlordDetailsJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getEmailUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdateEmail.urlPathSegment,
                "emailAddress",
            )

        fun getNameUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdateName.urlPathSegment,
                "name",
            )

        fun getPhoneNumberIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateLandlordDetailsStepId.UpdatePhoneNumber.urlPathSegment,
                "phoneNumber",
            )

        fun getAddressIfPresent(
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            val selectedAddress =
                getFieldStringValue(
                    journeyData,
                    UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress.urlPathSegment,
                    "address",
                )

            return if (selectedAddress == MANUAL_ADDRESS_CHOSEN) {
                getManualAddress(journeyData, UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress.urlPathSegment)
            } else if (selectedAddress != null) {
                addressDataService.getAddressData(selectedAddress)
            } else {
                null
            }
        }
    }
}
