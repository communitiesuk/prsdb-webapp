package uk.gov.communities.prsdb.webapp.multipageforms

import jakarta.validation.constraints.NotNull

class EmailFormModel {
    @field:NotNull
    var emailAddress: String = ""
}
