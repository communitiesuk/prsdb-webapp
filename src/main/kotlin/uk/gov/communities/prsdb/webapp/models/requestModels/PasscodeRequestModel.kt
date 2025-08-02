package uk.gov.communities.prsdb.webapp.models.requestModels

import jakarta.validation.constraints.NotBlank

data class PasscodeRequestModel(
    @NotBlank(
        message = "passcodeEntry.error.missingPasscode",
    )
    var passcode: String = "",
)
