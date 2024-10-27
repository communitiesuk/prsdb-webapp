package uk.gov.communities.prsdb.webapp.multipageforms

import jakarta.validation.constraints.NotNull

class PhoneNumberFormModel {
    @field:NotNull
    var phoneNumber: String = ""
}
