package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationMainContact

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class CompleteOrganisationMainContactUpdateStepConfigTests {
    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockState: UpdateOrganisationMainContactJourneyState

    @Mock
    lateinit var mockOrgMainContactStep: OrgMainContactStep

    @Test
    fun `afterStepIsReached updates the organisation main contact`() {
        val formModel =
            OrgMainContactFormModel().apply {
                name = "New Name"
                emailAddress = "new@example.com"
                phoneNumber = "07222222222"
            }
        whenever(mockState.orgMainContactStep).thenReturn(mockOrgMainContactStep)
        whenever(mockOrgMainContactStep.formModel).thenReturn(formModel)

        val stepConfig = CompleteOrganisationMainContactUpdateStepConfig(mockLandlordService)
        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordService).updateOrganisationLandlordMainContact(
            name = "New Name",
            email = "new@example.com",
            phone = "07222222222",
        )
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val stepConfig = CompleteOrganisationMainContactUpdateStepConfig(mockLandlordService)
        val defaultDestination = Destination.ExternalUrl("/test")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }
}
