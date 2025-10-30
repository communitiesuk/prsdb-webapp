package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_PROPERTIES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.getDeleteIncompletePropertyConfirmationPath
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.getDeleteIncompletePropertyPath
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.ComplianceActionViewModelBuilder
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord

@WebMvcTest(LandlordController::class)
class LandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @MockitoBean
    private lateinit var journeyDataServiceFactory: JourneyDataServiceFactory

    @MockitoBean
    private lateinit var localAuthorityService: LocalAuthorityService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc
            .get("/$LANDLORD_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @WithMockUser
    @Test
    fun `index returns 403 for unauthorized user`() {
        mvc
            .get("/$LANDLORD_PATH_SEGMENT")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `index returns a redirect for authorised user`() {
        mvc
            .get("/$LANDLORD_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    fun `landlordDashboard returns a redirect for unauthenticated user`() {
        mvc
            .get(LANDLORD_DASHBOARD_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `landlordDashboard returns 403 for unauthorized user`() {
        mvc
            .get(LANDLORD_DASHBOARD_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `landlordDashboard returns 200 for authorised landlord user`() {
        val landlord = createLandlord()
        whenever(landlordService.retrieveLandlordByBaseUserId(anyString())).thenReturn(landlord)
        mvc
            .get(LANDLORD_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `landlordIncompleteProperties returns a redirect for unauthenticated user`() {
        mvc
            .get(INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `landlordIncompleteProperties returns 403 for unauthorized user`() {
        mvc
            .get(INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `landlordIncompleteProperties returns 200 for authorised landlord user`() {
        whenever(
            propertyRegistrationService.getIncompletePropertiesForLandlord(
                "user",
            ),
        ).thenReturn(emptyList())
        mvc
            .get(INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `getComplianceActions returns a redirect for unauthenticated user`() {
        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getComplianceActions returns 403 for unauthorized user`() {
        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `getComplianceActions returns 200 for authorised landlord user`() {
        // Arrange
        val incompleteComplianceDataModel =
            ComplianceStatusDataModel(
                1,
                "123 Example Street, EX",
                "P-XXXX-XXXX",
                ComplianceCertStatus.ADDED,
                ComplianceCertStatus.NOT_STARTED,
                ComplianceCertStatus.NOT_STARTED,
                false,
            )
        whenever(propertyOwnershipService.getIncompleteCompliancesForLandlord("user")).thenReturn(listOf(incompleteComplianceDataModel))

        val nonCompliantDataModel =
            ComplianceStatusDataModel(
                2,
                "456 Example Avenue, EX",
                "P-YYYY-YYYY",
                ComplianceCertStatus.EXPIRED,
                ComplianceCertStatus.ADDED,
                ComplianceCertStatus.NOT_ADDED,
                false,
            )
        whenever(propertyComplianceService.getNonCompliantPropertiesForLandlord("user")).thenReturn(listOf(nonCompliantDataModel))

        // Act and Assert
        val expectedComplianceActions =
            listOf(
                ComplianceActionViewModelBuilder.fromDataModel(incompleteComplianceDataModel, 0),
                ComplianceActionViewModelBuilder.fromDataModel(nonCompliantDataModel, 0),
            )

        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { isOk() }
                model {
                    attribute("complianceActions", expectedComplianceActions)
                    attribute(
                        "viewRegisteredPropertiesUrl",
                        "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#$REGISTERED_PROPERTIES_FRAGMENT",
                    )
                    attribute("backUrl", LANDLORD_DASHBOARD_URL)
                }
            }
    }

    @Nested
    inner class DeleteIncompleteProperty {
        private val defaultContextId = 1L

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `Posting Yes to AreYouSure deletes the form context, adds the id to the session and redirects to the confirmation page`() {
            mvc
                .post(getDeleteIncompletePropertyPath(defaultContextId)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "wantsToProceed=true"
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(getDeleteIncompletePropertyConfirmationPath((defaultContextId)))
                }

            verify(propertyRegistrationService).deleteIncompleteProperty(eq(defaultContextId), anyString())
            verify(propertyRegistrationService).addIncompletePropertyFormContextsDeletedThisSession(defaultContextId)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `Posting No to deleteIncompletePropertyAreYouSure redirects to Incomplete Properties page without deleting`() {
            mvc
                .post(getDeleteIncompletePropertyPath(defaultContextId)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "wantsToProceed=false"
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(INCOMPLETE_PROPERTIES_URL)
                }

            verify(propertyRegistrationService, never()).deleteIncompleteProperty(any(), anyString())
            verify(propertyRegistrationService, never()).addIncompletePropertyFormContextsDeletedThisSession(any())
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `deleteIncompletePropertyConfirmation returns 404 if the requested form context id is not in the session`() {
            whenever(propertyRegistrationService.getIncompletePropertyWasDeletedThisSession(defaultContextId))
                .thenReturn(false)

            mvc
                .get(getDeleteIncompletePropertyConfirmationPath(defaultContextId))
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `deleteIncompletePropertyConfirmation returns 500 if the requested form context is still in the database`() {
            whenever(propertyRegistrationService.getIncompletePropertyWasDeletedThisSession(defaultContextId))
                .thenReturn(true)

            whenever(propertyRegistrationService.getFormContextByIdOrNull(defaultContextId)).thenReturn(FormContext())

            mvc
                .get(getDeleteIncompletePropertyConfirmationPath(defaultContextId))
                .andExpect {
                    status { isInternalServerError() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `deleteIncompletePropertyConfirmation returns 200 if the requested form context was deleted in this session`() {
            whenever(propertyRegistrationService.getIncompletePropertyWasDeletedThisSession(defaultContextId))
                .thenReturn(true)

            mvc
                .get(getDeleteIncompletePropertyConfirmationPath(defaultContextId))
                .andExpect {
                    status { isOk() }
                }
        }
    }
}
