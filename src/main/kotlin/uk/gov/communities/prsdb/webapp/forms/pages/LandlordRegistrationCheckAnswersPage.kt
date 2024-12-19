package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.LandlordJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.FormSummaryViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

class LandlordRegistrationCheckAnswersPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
    private val addressDataService: AddressDataService,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        journeyData!!

        val formData =
            getIdentityFormData(journeyData) +
                getEmailAndPhoneFormData(journeyData) +
                getAddressFormData(journeyData)

        model.addAttribute("formData", formData)
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }

    private fun getIdentityFormData(journeyData: JourneyData): List<FormSummaryViewModel> {
        val isIdentityVerified = LandlordJourneyDataHelper.isIdentityVerified(journeyDataService, journeyData)

        return listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                LandlordJourneyDataHelper.getName(journeyDataService, journeyData)!!,
                if (isIdentityVerified) null else "/${BASE_CHANGE_URL}/${LandlordRegistrationStepId.Name.urlPathSegment}",
            ),
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                LandlordJourneyDataHelper.getDOB(journeyDataService, journeyData)!!,
                if (isIdentityVerified) null else "/${BASE_CHANGE_URL}/${LandlordRegistrationStepId.DateOfBirth.urlPathSegment}",
            ),
        )
    }

    private fun getEmailAndPhoneFormData(journeyData: JourneyData): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                LandlordJourneyDataHelper.getEmail(journeyDataService, journeyData)!!,
                "/${BASE_CHANGE_URL}/${LandlordRegistrationStepId.Email.urlPathSegment}",
            ),
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                LandlordJourneyDataHelper.getPhoneNumber(journeyDataService, journeyData)!!,
                "/${BASE_CHANGE_URL}/${LandlordRegistrationStepId.PhoneNumber.urlPathSegment}",
            ),
        )

    private fun getAddressFormData(journeyData: JourneyData): List<FormSummaryViewModel> {
        val livesInUK = LandlordJourneyDataHelper.getLivesInUK(journeyDataService, journeyData)!!

        return getLivesInUKFormData(livesInUK) +
            (if (!livesInUK) getInternationalAddressFormData(journeyData) else emptyList()) +
            getContactAddressFormData(journeyData, addressDataService, livesInUK)
    }

    private fun getLivesInUKFormData(livesInUK: Boolean): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.ukResident",
                if (livesInUK) "commonText.yes" else "commonText.no",
                "/${BASE_CHANGE_URL}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
            ),
        )

    private fun getInternationalAddressFormData(journeyData: JourneyData): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                LandlordJourneyDataHelper.getNonUKCountryOfResidence(journeyDataService, journeyData)!!,
                "/${BASE_CHANGE_URL}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
            ),
            FormSummaryViewModel(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddressOutsideUK",
                LandlordJourneyDataHelper.getInternationalAddress(journeyDataService, journeyData)!!,
                "/${BASE_CHANGE_URL}/${LandlordRegistrationStepId.InternationalAddress.urlPathSegment}",
            ),
        )

    private fun getContactAddressFormData(
        journeyData: JourneyData,
        addressDataService: AddressDataService,
        livesInUK: Boolean,
    ): List<FormSummaryViewModel> {
        val address = LandlordJourneyDataHelper.getAddress(journeyDataService, journeyData, addressDataService)!!
        val localAuthority = LOCAL_AUTHORITIES.find { it.custodianCode == address.custodianCode }?.displayName

        return listOfNotNull(
            FormSummaryViewModel(
                if (livesInUK) {
                    "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
                } else {
                    "registerAsALandlord.checkAnswers.rowHeading.ukContactAddress"
                },
                address.singleLineAddress,
                getContactAddressChangeURLPathSegment(journeyData, livesInUK),
            ),
            if (localAuthority == null) {
                null
            } else {
                FormSummaryViewModel(
                    "registerAsALandlord.checkAnswers.rowHeading.localAuthority",
                    localAuthority,
                    null,
                )
            },
        )
    }

    private fun getContactAddressChangeURLPathSegment(
        journeyData: JourneyData,
        livesInUK: Boolean,
    ): String =
        if (livesInUK) {
            if (LandlordJourneyDataHelper.isManualAddressChosen(journeyDataService, journeyData)) {
                LandlordRegistrationStepId.ManualAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupAddress.urlPathSegment
            }
        } else {
            val isContactAddress = true
            if (LandlordJourneyDataHelper.isManualAddressChosen(journeyDataService, journeyData, isContactAddress)
            ) {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment
            }
        }

    companion object {
        private val BASE_CHANGE_URL = JourneyType.LANDLORD_REGISTRATION.urlPathSegment
    }
}
