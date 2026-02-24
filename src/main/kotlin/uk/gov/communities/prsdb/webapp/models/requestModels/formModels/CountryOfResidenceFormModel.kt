package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class CountryOfResidenceFormModel : FormModel {
    @NotNull(message = "forms.countryOfResidence.radios.error.missing")
    var livesInEnglandOrWales: Boolean? = null
}
