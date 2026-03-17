package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class AnyDateFormModel : DateFormModel() {
    @AnyDateDayValidation
    override var day: String = ""

    @AnyDateMonthValidation
    override var month: String = ""

    @AnyDateYearValidation
    override var year: String = ""
}
