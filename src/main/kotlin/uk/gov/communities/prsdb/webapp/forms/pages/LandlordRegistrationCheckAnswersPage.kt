package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FormSummaryDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.DateFormatterService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

class LandlordRegistrationCheckAnswersPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        val livesInUK = getLivesInUk(journeyData!!)
        val formData = mutableListOf<FormSummaryDataModel>()

        formData.addAll(getIdFormData(journeyData))
        formData.addAll(getEmailAndPhoneFormData(journeyData))
        formData.add(getUKResidentRow(livesInUK))
        formData.addAll(getAddressFormData(journeyData, livesInUK))

        model.addAttribute("formData", formData)
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }

    private fun getIdFormData(journeyData: JourneyData): List<FormSummaryDataModel> =
        if (LandlordRegistrationJourney.doesJourneyDataContainVerifiedIdentity(journeyData)) {
            getVerifiedIdentifyRows(journeyData)
        } else {
            getManuallyEnteredIdentifyRows(journeyData)
        }

    private fun getVerifiedIdentifyRows(journeyData: JourneyData): List<FormSummaryDataModel> =
        listOf(
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.VerifyIdentity.urlPathSegment, "name"),
                null,
            ),
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment])?.get("birthDate"),
                null,
            ),
        )

    private fun getManuallyEnteredIdentifyRows(journeyData: JourneyData): List<FormSummaryDataModel> {
        val formattedDate = getFormattedDateOfBirth(journeyData)
        return listOf(
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.Name.urlPathSegment, "name"),
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.Name.urlPathSegment}",
            ),
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                formattedDate,
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.DateOfBirth.urlPathSegment}",
            ),
        )
    }

    private fun getFormattedDateOfBirth(journeyData: JourneyData): String {
        val day = journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.DateOfBirth.urlPathSegment, "day")!!
        val month = journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.DateOfBirth.urlPathSegment, "month")!!
        val year = journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.DateOfBirth.urlPathSegment, "year")!!
        return DateFormatterService.getFormattedDate(day, month, year)
    }

    private fun getEmailAndPhoneFormData(journeyData: JourneyData): List<FormSummaryDataModel> =
        listOf(
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.Email.urlPathSegment, "emailAddress"),
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.Email.urlPathSegment}",
            ),
            FormSummaryDataModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.PhoneNumber.urlPathSegment, "phoneNumber"),
                "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.PhoneNumber.urlPathSegment}",
            ),
        )

    private fun getUKResidentRow(livesInUK: Boolean): FormSummaryDataModel =
        FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.ukResident",
            if (livesInUK) "commonText.yes" else "commonText.no",
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
        )

    private fun getAddressFormData(
        journeyData: JourneyData,
        livesInUK: Boolean,
    ): List<FormSummaryDataModel> {
        val addressFormData = mutableListOf<FormSummaryDataModel>()
        if (!livesInUK) {
            addressFormData.add(getCountryOfResidenceRow(journeyData))
            addressFormData.add(getContactAddressOutsideUKRow(journeyData))
            addressFormData.add(getUKContactAddressOutsideUK(journeyData))
        } else {
            addressFormData.add(getContactAddressRow(journeyData))
        }

        return addressFormData
    }

    private fun getCountryOfResidenceRow(journeyData: JourneyData): FormSummaryDataModel =
        FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
            journeyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "countryOfResidence",
            ),
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
        )

    private fun getContactAddressOutsideUKRow(journeyData: JourneyData): FormSummaryDataModel =
        FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK",
            journeyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
                "internationalAddress",
            ),
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.InternationalAddress.urlPathSegment}",
        )

    private fun getUKContactAddressOutsideUK(journeyData: JourneyData): FormSummaryDataModel {
        var addressValue =
            journeyDataService.getFieldStringValue(journeyData, LandlordRegistrationStepId.SelectContactAddress.urlPathSegment, "address")
        if (addressValue == MANUAL_ADDRESS_CHOSEN) {
            addressValue = getManualAddressValue(journeyData, LandlordRegistrationStepId.ManualContactAddress.urlPathSegment)
        }

        return FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress",
            addressValue,
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.LookupContactAddress.urlPathSegment}",
        )
    }

    private fun getContactAddressRow(journeyData: JourneyData): FormSummaryDataModel {
        var addressValue =
            journeyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.SelectAddress.urlPathSegment,
                "address",
            )
        if (addressValue == MANUAL_ADDRESS_CHOSEN) {
            addressValue = getManualAddressValue(journeyData, LandlordRegistrationStepId.ManualAddress.urlPathSegment)
        }
        return FormSummaryDataModel(
            "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
            addressValue,
            "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}/${LandlordRegistrationStepId.LookupAddress.urlPathSegment}",
        )
    }

    private fun getManualAddressValue(
        journeyData: JourneyData,
        urlPathSegment: String,
    ): String {
        val addressLineOne = journeyDataService.getFieldStringValue(journeyData, urlPathSegment, "addressLineOne")!!
        val addressLineTwo = journeyDataService.getFieldStringValue(journeyData, urlPathSegment, "addressLineTwo")
        val townOrCity = journeyDataService.getFieldStringValue(journeyData, urlPathSegment, "townOrCity")!!
        val county = journeyDataService.getFieldStringValue(journeyData, urlPathSegment, "county")
        val postcode = journeyDataService.getFieldStringValue(journeyData, urlPathSegment, "postcode")!!
        return AddressDataModel.manualAddressDataToSingleLineAddress(addressLineOne, townOrCity, postcode, addressLineTwo, county)
    }

    private fun getLivesInUk(journeyData: JourneyData): Boolean =
        journeyDataService.getFieldBooleanValue(journeyData, LandlordRegistrationStepId.CountryOfResidence.urlPathSegment, "livesInUK")!!
}
