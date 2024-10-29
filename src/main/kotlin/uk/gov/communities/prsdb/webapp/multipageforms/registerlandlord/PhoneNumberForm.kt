package uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import uk.gov.communities.prsdb.webapp.multipageforms.FormField
import uk.gov.communities.prsdb.webapp.multipageforms.FormModel

data class PhoneNumberForm(
    @FormField(
        fragmentName = "phoneNumber",
        labelKey = "registerAsALandlord.phoneNumber.label",
        hintKey = "registerAsALandlord.phoneNumber.hint",
    )
    @field:NotNull(message = "registerAsALandlord.phoneNumber.error.missing")
    @field:NotBlank(message = "registerAsALandlord.phoneNumber.error.missing")
    @field:Pattern(regexp = """(\d+ ?)+""", message = "registerAsALandlord.phoneNumber.error.invalidFormat")
    var phoneNumber: String? = null,
) : FormModel<PhoneNumberForm>
