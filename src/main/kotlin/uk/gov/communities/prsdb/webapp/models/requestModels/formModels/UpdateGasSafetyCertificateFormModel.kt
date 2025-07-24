package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class UpdateGasSafetyCertificateFormModel : FormModel {
    // TODO PRSD-1245: Add ths validation back in when originalJourneyData is being loaded correctly
//    @NotNull(message = "forms.update.gasSafetyType.error.missing")
    var hasNewCertificate: Boolean? = null
}
