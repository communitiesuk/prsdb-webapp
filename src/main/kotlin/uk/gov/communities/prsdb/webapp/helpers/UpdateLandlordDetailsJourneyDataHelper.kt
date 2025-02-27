package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateLandlordDetailsJourney
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import java.time.LocalDate

class UpdateLandlordDetailsJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getIsIdentityVerified(journeyData: JourneyData): Boolean =
            getValueByKey(journeyData, UpdateLandlordDetailsJourney.IS_IDENTITY_VERIFIED_KEY).toBoolean()

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

        fun getPhoneNumberIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateDetailsStepId.UpdatePhoneNumber.urlPathSegment,
                "phoneNumber",
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

        fun getDateOfBirthIfPresent(journeyData: JourneyData): LocalDate? {
            val day =
                getFieldIntegerValue(
                    journeyData,
                    UpdateDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    "day",
                ) ?: return null

            val month =
                getFieldIntegerValue(
                    journeyData,
                    UpdateDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    "month",
                ) ?: return null

            val year =
                getFieldIntegerValue(
                    journeyData,
                    UpdateDetailsStepId.UpdateDateOfBirth.urlPathSegment,
                    "year",
                ) ?: return null

            return LocalDate.of(year, month, day)
        }
    }
}
