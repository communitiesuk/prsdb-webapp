package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class CharityRegisteredWithFormModel : FormModel {
    @NotNull(message = "forms.orgCharityRegisteredWith.radios.error.missing")
    var charityRegisteredWith: CharityRegulator? = null
}
