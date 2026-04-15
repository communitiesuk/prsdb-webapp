package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull

class EpcInDateAtStartOfTenancyCheckFormModel : FormModel {
    @NotNull(message = "propertyCompliance.epcTask.epcInDateAtStartOfTenancy.missing")
    var tenancyStartedBeforeExpiry: Boolean? = null
}
