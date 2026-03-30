package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.SubjectIdentifierController.Companion.LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL
import uk.gov.communities.prsdb.webapp.controllers.SubjectIdentifierController.Companion.SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.test.Test

@WebMvcTest(SubjectIdentifierController::class)
class SubjectIdentifierControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `LC subject identifier page redirects unauthenticated users`() {
        mvc.get(LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `LC subject identifier page returns 403 for unauthorized users`() {
        mvc.get(LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(username = "lc-test-subject-id", roles = ["LOCAL_COUNCIL_USER"])
    fun `LC subject identifier page returns 200 with correct model for LC users`() {
        mvc.get(LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL).andExpect {
            status { isOk() }
            model {
                attribute(
                    "listRows",
                    listOf(
                        SummaryListRowViewModel(
                            fieldHeading = "subjectIdentifier.summaryList.subjectIdentifier",
                            fieldValue = "lc-test-subject-id",
                        ),
                        SummaryListRowViewModel(
                            fieldHeading = "subjectIdentifier.summaryList.authenticationProvider",
                            fieldValue = "subjectIdentifier.authProvider.internalAccess",
                        ),
                    ),
                )
            }
        }
    }

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `LC subject identifier page returns 200 for LC admins`() {
        mvc.get(LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL).andExpect {
            status { isOk() }
            model { attributeExists("listRows") }
        }
    }

    @Test
    fun `system operator subject identifier page redirects unauthenticated users`() {
        mvc.get(SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `system operator subject identifier page returns 403 for unauthorized users`() {
        mvc.get(SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(username = "so-test-subject-id", roles = ["SYSTEM_OPERATOR"])
    fun `system operator subject identifier page returns 200 with correct model for system operators`() {
        mvc.get(SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL).andExpect {
            status { isOk() }
            model {
                attribute(
                    "listRows",
                    listOf(
                        SummaryListRowViewModel(
                            fieldHeading = "subjectIdentifier.summaryList.subjectIdentifier",
                            fieldValue = "so-test-subject-id",
                        ),
                        SummaryListRowViewModel(
                            fieldHeading = "subjectIdentifier.summaryList.authenticationProvider",
                            fieldValue = "subjectIdentifier.authProvider.oneLogin",
                        ),
                    ),
                )
            }
        }
    }
}
