package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised

@IsValidPrioritised
class UpdateEicrFormModel : FormModel {
    // TODO PRSD-1245 or PRSD-1246: Add ths validation back in when originalJourneyData is being loaded correctly
//    @NotNull(message = "forms.update.eicr.error.missing")
    var hasNewCertificate: Boolean? = null
}
