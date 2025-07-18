package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class PropertyDeregistrationAreYouSureFormModel : FormModel {
    @NotNull(message = "forms.areYouSure.propertyDeregistration.radios.error.missing")
    var wantsToProceed: Boolean? = null
}
