package uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.multipageforms.FormField
import uk.gov.communities.prsdb.webapp.multipageforms.FormModel

data class EmailForm(
    @FormField(
        fragmentName = "email",
        labelKey = "registerAsALandlord.email.label",
    )
    @field:NotNull(message = "registerAsALandlord.email.error.missing")
    @field:Email(message = "registerAsALandlord.email.error.invalidFormat")
    var email: String? = null,
) : FormModel
