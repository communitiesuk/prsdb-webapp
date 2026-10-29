package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

// Blank-name validation is done in PersonNameStepConfig.afterPrimaryValidation so the error can depend on journey state
@IsValidPrioritised
class FreeholderNameFormModel : FormModel {
    var name: String? = null
}
