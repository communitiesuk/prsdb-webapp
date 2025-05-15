package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class DeleteIncompletePropertyRegistrationAreYouSureFormModel : FormModel {
    @NotNull(message = "registerProperty.deleteIncompleteProperties.areYouSure.radios.error.missing")
    var wantsToProceed: Boolean? = null
}
