package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.application.DeleteExpiredOrganisationalLandlordInvitationsTaskLogic
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationDeletionService

@ExtendWith(MockitoExtension::class)
class DeleteExpiredOrganisationalLandlordInvitationsTaskApplicationRunnerTests {
    @Mock
    private lateinit var organisationalLandlordInvitationDeletionService: OrganisationalLandlordInvitationDeletionService

    @InjectMocks
    private lateinit var taskLogic: DeleteExpiredOrganisationalLandlordInvitationsTaskLogic

    @Test
    fun `deleteOrganisationalLandlordInvitationsPastRetentionPeriod calls deletion service`() {
        taskLogic.deleteOrganisationalLandlordInvitationsPastRetentionPeriod()

        verify(organisationalLandlordInvitationDeletionService).deleteInvitationsPastRetentionPeriod()
    }
}
