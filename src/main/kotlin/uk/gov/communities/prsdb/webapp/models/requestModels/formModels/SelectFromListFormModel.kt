package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class SelectFromListFormModel : FormModel {
    var selectedOption: String? = null
}
