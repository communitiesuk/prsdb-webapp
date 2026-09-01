package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.ServletException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership

@WebMvcTest(LettingAgentPropertyDetailsController::class)
@Import(MessageSourceConfig::class)
class LettingAgentPropertyDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var propertyComplianceViewModelFactory: PropertyComplianceViewModelFactory

    @Test
    fun `getLettingAgentPropertyDetails is accessible without authentication and renders the letting agent view`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        whenever(propertyOwnershipService.getPropertyOwnership(eq(propertyOwnership.id)))
            .thenReturn(propertyOwnership)
        whenever(propertyComplianceService.getComplianceForPropertyOrNull(eq(propertyOwnership.id)))
            .thenReturn(PropertyComplianceBuilder.createWithInDateCerts())

        mvc
            .get(LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(propertyOwnership.id))
            .andExpect {
                status { isOk() }
                view { name("propertyDetailsLettingAgentView") }
                model { attributeExists("propertyDetails") }
                model { attributeExists("backUrl") }
            }
    }

    @Test
    fun `getLettingAgentPropertyDetails throws when the property has no compliance record`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        whenever(propertyOwnershipService.getPropertyOwnership(eq(propertyOwnership.id)))
            .thenReturn(propertyOwnership)
        whenever(propertyComplianceService.getComplianceForPropertyOrNull(any()))
            .thenReturn(null)

        assertThrows<ServletException> {
            mvc.get(LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(propertyOwnership.id))
        }
    }
}
