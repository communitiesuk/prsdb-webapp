package uk.gov.communities.prsdb.webapp.services

import org.hibernate.SessionFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationGoverningBodyMemberRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.testHelpers.IntegrationTestHelper

// This test seeds a small but non-trivial volume of data, so it exercises the same batching, address-generation and
// organisation-landlord branching logic as a real NFT run, just at a scale that completes quickly on a test database.
//
// Deliberately does NOT activate the "nft-data-seeder" profile: NftDataSeedingTaskApplicationRunner (which is only
// active under that profile) calls exitProcess() once seeding finishes, which would kill the test JVM before JUnit
// could run any @Test methods. Instead, NftDataSeeder is constructed directly with test-scale values, bypassing the
// task-runner profile entirely.
@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "local")
@TestPropertySource(
    properties = [
        "EMAILNOTIFICATIONS_APIKEY=test",
        "OS_API_KEY=test",
    ],
)
class NftDataSeederTests(
    @Autowired private val sessionFactory: SessionFactory,
    @Autowired private val localCouncilRepository: LocalCouncilRepository,
    @Autowired private val addressRepository: AddressRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val landlordRepository: LandlordRepository,
    @Autowired private val individualLandlordRepository: IndividualLandlordRepository,
    @Autowired private val organisationLandlordRepository: OrganisationLandlordRepository,
    @Autowired private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
    @Autowired private val organisationGoverningBodyMemberRepository: OrganisationGoverningBodyMemberRepository,
    @Autowired private val propertyOwnershipRepository: PropertyOwnershipRepository,
    @Autowired private val incompletePropertiesRepository: LandlordIncompletePropertiesRepository,
) {
    private val numOfLandlords = 200
    private val numOfProperties = 300

    private fun newSeeder() =
        NftDataSeeder(
            sessionFactory = sessionFactory,
            localCouncilRepository = localCouncilRepository,
            addressRepository = addressRepository,
            epcCertificateBaseUrl = "http://localhost",
            numOfSystemOperators = 2,
            numOfLcUsers = 2,
            numOfLandlords = numOfLandlords,
            numOfProperties = numOfProperties,
            batchSize = 25,
            randomSeed = 239L,
            referenceDate = "",
            numOfGeneratedAddresses = 2000,
        )

    @BeforeEach
    fun setUp() {
        IntegrationTestHelper.resetDatabase(jdbcTemplate)
    }

    @Test
    fun `seedDatabase seeds the configured number of landlords and properties without error`() {
        newSeeder().seedDatabase()

        assertEquals(numOfLandlords.toLong(), landlordRepository.count())
        assertEquals(numOfProperties.toLong(), propertyOwnershipRepository.count() + incompletePropertiesRepository.count())
    }

    @Test
    fun `seedDatabase seeds both individual and organisation landlords`() {
        newSeeder().seedDatabase()

        val individualCount = individualLandlordRepository.count()
        val organisationCount = organisationLandlordRepository.count()

        assertEquals(numOfLandlords.toLong(), individualCount + organisationCount)
        assertTrue(individualCount > 0, "Expected at least one individual landlord to be seeded")
        assertTrue(organisationCount > 0, "Expected at least one organisation landlord to be seeded")
    }

    @Test
    fun `seedDatabase gives every organisation landlord exactly one organisational landlord user`() {
        newSeeder().seedDatabase()

        val organisationLandlords = organisationLandlordRepository.findAll()
        assertTrue(organisationLandlords.isNotEmpty(), "Expected at least one organisation landlord to be seeded")

        organisationLandlords.forEach { organisationLandlord ->
            val users = organisationalLandlordUserRepository.findByOrganisationalLandlord(organisationLandlord)
            assertEquals(1, users.size) {
                "Expected exactly one organisational landlord user for organisation landlord ${organisationLandlord.id}"
            }
        }
    }

    @Test
    fun `seedDatabase only gives non-company organisations governing body members`() {
        newSeeder().seedDatabase()

        val organisationLandlords = organisationLandlordRepository.findAll()
        assertTrue(organisationLandlords.isNotEmpty(), "Expected at least one organisation landlord to be seeded")
        assertTrue(
            organisationLandlords.any { it.isCompany },
            "Expected at least one company organisation landlord to be seeded",
        )
        assertTrue(
            organisationLandlords.any { !it.isCompany },
            "Expected at least one non-company organisation landlord to be seeded",
        )

        organisationLandlords.forEach { organisationLandlord ->
            val memberCount =
                organisationGoverningBodyMemberRepository
                    .findAll()
                    .count { it.organisationalLandlord.id == organisationLandlord.id }

            if (organisationLandlord.isCompany) {
                assertEquals(0, memberCount) {
                    "Expected company organisation landlord ${organisationLandlord.id} to have no governing body members"
                }
            } else {
                assertTrue(memberCount in 1..3) {
                    "Expected non-company organisation landlord ${organisationLandlord.id} to have 1-3 governing " +
                        "body members, but had $memberCount"
                }
            }
        }
    }

    @Test
    fun `seedDatabase is deterministic for a given random seed`() {
        newSeeder().seedDatabase()
        val firstRunLandlordCount = landlordRepository.count()
        val firstRunOrganisationCount = organisationLandlordRepository.count()
        val firstRunPropertyCount = propertyOwnershipRepository.count() + incompletePropertiesRepository.count()

        IntegrationTestHelper.resetDatabase(jdbcTemplate)

        newSeeder().seedDatabase()
        val secondRunLandlordCount = landlordRepository.count()
        val secondRunOrganisationCount = organisationLandlordRepository.count()
        val secondRunPropertyCount = propertyOwnershipRepository.count() + incompletePropertiesRepository.count()

        assertEquals(firstRunLandlordCount, secondRunLandlordCount)
        assertEquals(firstRunOrganisationCount, secondRunOrganisationCount)
        assertEquals(firstRunPropertyCount, secondRunPropertyCount)
    }
}
