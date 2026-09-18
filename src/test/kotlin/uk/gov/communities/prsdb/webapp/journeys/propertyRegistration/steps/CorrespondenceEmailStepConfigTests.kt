package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class CorrespondenceEmailStepConfigTests {
    @Mock
    private lateinit var userToLandlordService: UserToLandlordService

    @Test
    fun `getStepSpecificContent uses the organisational landlord user email for the account email option`() {
        val organisationalLandlord =
            MockLandlordData.createOrgLandlord(
                email = "organisation@example.com",
                registrantEmail = "current.user@example.com",
            )
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(organisationalLandlord)
        val stepConfig = CorrespondenceEmailStepConfig(userToLandlordService)

        val content = stepConfig.getStepSpecificContent(mock<JourneyState>())

        @Suppress("UNCHECKED_CAST")
        val radioOptions = content["radioOptions"] as List<RadiosButtonViewModel<CorrespondenceEmailOption>>

        assertEquals(
            "current.user@example.com",
            radioOptions.single { it.value == CorrespondenceEmailOption.ACCOUNT_EMAIL }.hintValue,
        )
    }
}
