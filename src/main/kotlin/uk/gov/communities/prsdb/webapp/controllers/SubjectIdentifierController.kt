package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SUBJECT_IDENTIFIER_PAGE
import uk.gov.communities.prsdb.webapp.constants.SUBJECT_IDENTIFIER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import java.security.Principal

@PrsdbController
@RequestMapping
class SubjectIdentifierController {
    // No @PreAuthorize — this page must be accessible before users are assigned roles, as it is used to gather seed data.
    @GetMapping(LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL)
    @AvailableWhenFeatureEnabled(SUBJECT_IDENTIFIER_PAGE)
    fun getLocalCouncilSubjectIdentifier(
        model: Model,
        principal: Principal,
    ): String = populateModelAndReturnView(model, principal, INTERNAL_ACCESS_AUTH_PROVIDER_KEY)

    @GetMapping(SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL)
    @AvailableWhenFeatureEnabled(SUBJECT_IDENTIFIER_PAGE)
    fun getSystemOperatorSubjectIdentifier(
        model: Model,
        principal: Principal,
    ): String = populateModelAndReturnView(model, principal, ONE_LOGIN_AUTH_PROVIDER_KEY)

    private fun populateModelAndReturnView(
        model: Model,
        principal: Principal,
        authProvider: String,
    ): String {
        model.addAttribute(
            "listRows",
            listOf(
                SummaryListRowViewModel(
                    fieldHeading = "subjectIdentifier.summaryList.subjectIdentifier",
                    fieldValue = principal.name,
                ),
                SummaryListRowViewModel(
                    fieldHeading = "subjectIdentifier.summaryList.authenticationProvider",
                    fieldValue = authProvider,
                ),
            ),
        )
        return "subjectIdentifier"
    }

    companion object {
        private const val INTERNAL_ACCESS_AUTH_PROVIDER_KEY = "subjectIdentifier.authProvider.internalAccess"
        private const val ONE_LOGIN_AUTH_PROVIDER_KEY = "subjectIdentifier.authProvider.oneLogin"
        const val LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL =
            "/$LOCAL_COUNCIL_PATH_SEGMENT/$SUBJECT_IDENTIFIER_PATH_SEGMENT"
        const val SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL =
            "/$SYSTEM_OPERATOR_PATH_SEGMENT/$SUBJECT_IDENTIFIER_PATH_SEGMENT"
    }
}
