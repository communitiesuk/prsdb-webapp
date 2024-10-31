package uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import uk.gov.communities.prsdb.webapp.multipageforms.FormField
import uk.gov.communities.prsdb.webapp.multipageforms.FormModel

data class BestFriendEmailForm(
    @FormField(
        fragmentName = "email",
        labelKey = "registerAsALandlord.bestfriendemail.label",
    )
    @field:NotBlank(message = "registerAsALandlord.bestfriendemail.error.missing")
    @field:Email(message = "registerAsALandlord.bestfriendemail.error.invalidFormat")
    var email: String? = null,
) : FormModel<BestFriendEmailForm>
