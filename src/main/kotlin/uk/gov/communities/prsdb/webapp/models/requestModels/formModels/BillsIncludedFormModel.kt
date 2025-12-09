package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.CUSTOM_BILLS_INCLUDED_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class BillsIncludedFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.billsIncluded.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "notAllFalse",
            ),
        ],
    )
    var gas: Boolean = false
    var electricity: Boolean = false
    var water: Boolean = false
    var councilTax: Boolean = false
    var contentsInsurance: Boolean = false
    var broadband: Boolean = false
    var tvLicence: Boolean = false
    var cableSatelliteTv: Boolean = false
    var gardening: Boolean = false
    var communalAreasCleaner: Boolean = false
    var somethingElse: Boolean = false

    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.billsIncluded.error.somethingElse.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCustomBillsIncludedValidNotBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.billsIncluded.error.somethingElse.tooLong",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isCustomBillsIncludedNotTooLong",
            ),
        ],
    )
    var customBillsIncluded: String = ""

    var billsIncluded: String = ""

    fun notAllFalse(): Boolean =
        !(
            gas || electricity || water || councilTax || contentsInsurance ||
                broadband || tvLicence || cableSatelliteTv || gardening || communalAreasCleaner || somethingElse
        )

    fun isCustomBillsIncludedValidNotBlank(): Boolean = !somethingElse || customBillsIncluded.isNotBlank()

    fun isCustomBillsIncludedNotTooLong(): Boolean =
        !somethingElse ||
            LengthConstraintValidator("0", CUSTOM_BILLS_INCLUDED_MAX_LENGTH.toString()).isValid(customBillsIncluded)
}

// TODO: PDJB-108 revist this once we can see it
