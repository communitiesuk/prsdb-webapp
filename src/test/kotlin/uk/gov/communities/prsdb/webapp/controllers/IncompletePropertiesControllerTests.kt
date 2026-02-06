package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyForLandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationConfirmationService

@WebMvcTest(IncompletePropertiesController::class)
class IncompletePropertiesControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var confirmationService: PropertyRegistrationConfirmationService

    @MockitoBean
    private lateinit var incompletePropertyForLandlordService: IncompletePropertyForLandlordService

    @Test
    fun `landlordIncompleteProperties returns a redirect for unauthenticated user`() {
        mvc
            .get(LandlordController.Companion.INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `landlordIncompleteProperties returns 403 for unauthorized user`() {
        mvc
            .get(LandlordController.Companion.INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `landlordIncompleteProperties returns 200 for authorised landlord user`() {
        whenever(
            incompletePropertyForLandlordService.getIncompletePropertiesForLandlord(
                "user",
            ),
        ).thenReturn(emptyList())
        mvc
            .get(LandlordController.Companion.INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { isOk() }
            }
    }

    @Nested
    inner class DeleteIncompleteProperty {
        private val defaultContextId = "1"

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `Posting Yes to AreYouSure deletes the form context, adds the id to the session and redirects to the confirmation page`() {
            mvc
                .post(IncompletePropertiesController.getDeleteIncompletePropertyPath(defaultContextId.toString())) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "wantsToProceed=true"
                    with(SecurityMockMvcRequestPostProcessors.csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(IncompletePropertiesController.getDeleteIncompletePropertyConfirmationPath((defaultContextId)))
                }

            verify(incompletePropertyForLandlordService).deleteIncompleteProperty(anyString(), anyString())
            verify(confirmationService).addIncompletePropertyFormContextsDeletedThisSession(defaultContextId)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `Posting No to deleteIncompletePropertyAreYouSure redirects to Incomplete Properties page without deleting`() {
            mvc
                .post(IncompletePropertiesController.getDeleteIncompletePropertyPath(defaultContextId)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "wantsToProceed=false"
                    with(SecurityMockMvcRequestPostProcessors.csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(LandlordController.INCOMPLETE_PROPERTIES_URL)
                }

            verify(incompletePropertyForLandlordService, never())
                .deleteIncompleteProperty(anyString(), anyString())
            verify(confirmationService, never()).addIncompletePropertyFormContextsDeletedThisSession(any())
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `deleteIncompletePropertyConfirmation returns 404 if the requested form context id is not in the session`() {
            whenever(confirmationService.wasIncompletePropertyDeletedThisSession(defaultContextId))
                .thenReturn(false)

            mvc
                .get(
                    IncompletePropertiesController.Companion.getDeleteIncompletePropertyConfirmationPath(
                        defaultContextId,
                    ),
                ).andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `deleteIncompletePropertyConfirmation returns 500 if the requested form context is still in the database`() {
            whenever(confirmationService.wasIncompletePropertyDeletedThisSession(defaultContextId))
                .thenReturn(true)

            whenever(incompletePropertyForLandlordService.isIncompletePropertyAvailable(defaultContextId, "user")).thenReturn(true)

            mvc
                .get(
                    IncompletePropertiesController.Companion.getDeleteIncompletePropertyConfirmationPath(
                        defaultContextId,
                    ),
                ).andExpect {
                    status { isInternalServerError() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `deleteIncompletePropertyConfirmation returns 200 if the requested form context was deleted in this session`() {
            whenever(confirmationService.wasIncompletePropertyDeletedThisSession(defaultContextId))
                .thenReturn(true)

            mvc
                .get(
                    IncompletePropertiesController.Companion.getDeleteIncompletePropertyConfirmationPath(
                        defaultContextId,
                    ),
                ).andExpect {
                    status { isOk() }
                }
        }
    }
}
