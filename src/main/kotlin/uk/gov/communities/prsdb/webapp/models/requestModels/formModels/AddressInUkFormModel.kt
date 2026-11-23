package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

// The missing-answer error is checked by AddressInUkStepConfig
@IsValidPrioritised
class AddressInUkFormModel : FormModel {
    var addressInUk: Boolean? = null
}
