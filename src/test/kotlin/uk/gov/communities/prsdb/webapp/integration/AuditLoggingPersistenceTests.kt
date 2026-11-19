package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.config.interceptors.LettingAgentAccessInterceptor
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.util.UUID

class AuditLoggingPersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var organisationLandlordRepository: OrganisationLandlordRepository

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Autowired
    private lateinit var propertyComplianceRepository: PropertyComplianceRepository

    @Autowired
    private lateinit var fileUploadRepository: FileUploadRepository

    @Autowired
    private lateinit var jointLandlordInvitationRepository: JointLandlordInvitationRepository

    @Autowired
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    private val transactionTemplate by lazy { TransactionTemplate(transactionManager) }

    @AfterEach
    fun clearContexts() {
        SecurityContextHolder.clearContext()
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `updating a landlord records a modification with the new values and the acting user`() {
        SecurityContextHolder.getContext().authentication =
            TestingAuthenticationToken("urn:fdc:gov.uk:2022:UVWXY", null, "ROLE_LANDLORD")

        transactionTemplate.executeWithoutResult {
            val landlord = landlordRepository.findById(1L).get() as IndividualLandlord
            landlord.phoneNumber = "07700900999"
        }

        val row =
            jdbcTemplate.queryForMap(
                """
                SELECT a.revtype, a.individual_phone_number, r.user_subject_identifier, r.letting_agent_access_id
                FROM landlord_aud a JOIN audit_revision r ON r.id = a.rev
                WHERE a.id = 1 ORDER BY a.rev DESC LIMIT 1
                """.trimIndent(),
            )
        assertEquals(1, (row["revtype"] as Number).toInt())
        assertEquals("07700900999", row["individual_phone_number"])
        assertEquals("urn:fdc:gov.uk:2022:UVWXY", row["user_subject_identifier"])
        assertNull(row["letting_agent_access_id"])
    }

    @Test
    fun `a change made by a letting agent records the letting agent access`() {
        val request = MockHttpServletRequest()
        request.setAttribute(LettingAgentAccessInterceptor.LETTING_AGENT_ACCESS_ID_ATTRIBUTE, 1L)
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        transactionTemplate.executeWithoutResult {
            propertyOwnershipRepository.findById(1L).get().numBedrooms = 5
        }

        val row =
            jdbcTemplate.queryForMap(
                """
                SELECT a.num_bedrooms, r.user_subject_identifier, r.letting_agent_access_id
                FROM property_ownership_aud a JOIN audit_revision r ON r.id = a.rev
                WHERE a.id = 1 ORDER BY a.rev DESC LIMIT 1
                """.trimIndent(),
            )
        assertEquals(5, (row["num_bedrooms"] as Number).toInt())
        assertNull(row["user_subject_identifier"])
        assertEquals(1L, (row["letting_agent_access_id"] as Number).toLong())
    }

    @Test
    fun `deregistering a property records its final values and the removal of its letting agent`() {
        propertyOwnershipService.deletePropertyOwnership(1L)

        val property =
            jdbcTemplate.queryForMap("SELECT correspondence_email FROM property_ownership_aud WHERE id = 1 AND revtype = 2")
        assertEquals("email@example.com", property["correspondence_email"])
        val lettingAgent =
            jdbcTemplate.queryForMap(
                "SELECT invited_email FROM letting_agent_access_aud WHERE property_ownership_id = 1 AND revtype = 2",
            )
        assertEquals("letting.agent.one@example.com", lettingAgent["invited_email"])
    }

    @Test
    fun `attaching a gas safety certificate records the upload and the attachment`() {
        val uploadId =
            transactionTemplate.execute {
                val upload =
                    fileUploadRepository.save(
                        FileUpload(FileUploadStatus.QUARANTINED, "audit-test-key", "pdf", "audit-test-etag", null, "audit-test.pdf"),
                    )
                propertyComplianceRepository.findById(1L).get().gasSafetyFileUploads.add(upload)
                upload.id
            }!!

        assertEquals(
            "audit-test.pdf",
            jdbcTemplate.queryForObject("SELECT file_name FROM file_upload_aud WHERE id = ? AND revtype = 0", String::class.java, uploadId),
        )
        assertEquals(
            1,
            jdbcTemplate.queryForObject(
                """
                SELECT count(*) FROM gas_safety_uploads_aud
                WHERE property_compliance_id = 1 AND gas_safety_file_uploads_id = ? AND revtype = 0
                """.trimIndent(),
                Int::class.java,
                uploadId,
            ),
        )
    }

    @Test
    fun `inviting a joint landlord records the invited email`() {
        val invitationId =
            transactionTemplate.execute {
                val property = propertyOwnershipRepository.findById(8L).get()
                jointLandlordInvitationRepository
                    .save(JointLandlordInvitation(UUID.randomUUID(), "audit.jl@example.com", property, "Inviting landlord"))
                    .id
            }!!

        assertEquals(
            "audit.jl@example.com",
            jdbcTemplate.queryForObject(
                "SELECT invited_email FROM joint_landlord_invitation_aud WHERE id = ? AND revtype = 0",
                String::class.java,
                invitationId,
            ),
        )
    }

    @Test
    fun `deregistering an organisational landlord records the removal of its users and invitations`() {
        transactionTemplate.executeWithoutResult {
            landlordDeregistrationService.deregisterOrganisationalLandlord(organisationLandlordRepository.findById(36L).get())
        }

        assertEquals(
            1,
            jdbcTemplate.queryForObject("SELECT count(*) FROM landlord_aud WHERE id = 36 AND revtype = 2", Int::class.java),
        )
        assertEquals(
            3,
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM organisational_landlord_user_aud WHERE organisation_landlord_id = 36 AND revtype = 2",
                Int::class.java,
            ),
        )
        val invitationRows =
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM organisational_landlord_invitation_aud WHERE organisation_landlord_id = 36",
                Int::class.java,
            )!!
        val invitationDeletes =
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM organisational_landlord_invitation_aud WHERE organisation_landlord_id = 36 AND revtype = 2",
                Int::class.java,
            )
        assertTrue(invitationRows > 0)
        assertEquals(invitationRows, invitationDeletes)
    }
}
