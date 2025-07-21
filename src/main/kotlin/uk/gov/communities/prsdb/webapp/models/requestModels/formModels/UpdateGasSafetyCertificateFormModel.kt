package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class UpdateGasSafetyCertificateFormModel : FormModel {
    // TODO PRSD-1312: Make this reachable, maybe with help from Alex
//    @NotNull(message = "forms.update.gasSafetyType.error.missing")
    var hasNewCertificate: Boolean? = null
}
