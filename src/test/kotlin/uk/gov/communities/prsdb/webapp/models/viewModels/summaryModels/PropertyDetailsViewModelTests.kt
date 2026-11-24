package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateCorrespondenceAddressController
import uk.gov.communities.prsdb.webapp.controllers.UpdateBedroomsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.controllers.UpdatePropertyManagerController
import uk.gov.communities.prsdb.webapp.controllers.UpdateSuperiorLandlordsController
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership.InitialiseOwnershipUpdateStep
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyManager
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createSuperiorLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createUnoccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.time.LocalDate
import java.util.Locale

class PropertyDetailsViewModelTests {
    private val mockMessageSource = MockMessageSource()

    private val yamlMessageSource = YamlMessageSource("classpath:messages")

    // A property "occupied when registered" has a lastOccupiedDate matching its registration (created) date.
    private val occupiedAtRegistrationDate = LocalDate.of(2025, 1, 1)
    private val occupiedAtRegistrationInstant =
        occupiedAtRegistrationDate.atStartOfDay(DateTimeHelper.UK_ZONE).toInstant()

    @Test
    fun `property details section is in the correct order`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                address = createAddress(uprn = 1234.toLong()),
                numberOfBedrooms = 2,
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.propertyDetails.address",
                "propertyDetails.propertyRecord.localCouncil",
                "propertyDetails.propertyRecord.propertyType",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
            )

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(expectedHeaderList, viewModel.propertyDetailsSection.map { it.fieldHeading })
    }

    @Test
    fun `property details section renders the address as multiple lines`() {
        val propertyOwnership = createOccupiedPropertyOwnership(address = createAddress(uprn = 1234.toLong()))

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        val addressValue =
            viewModel.propertyDetailsSection
                .single { it.fieldHeading == "propertyDetails.propertyRecord.propertyDetails.address" }
                .fieldValue

        assertEquals(propertyOwnership.address.toMultiLineAddress().split("\n"), addressValue)
    }

    @Test
    fun `bedrooms row shows the not added message key when no bedroom count was entered`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms.notAdded",
            bedroomsRow(viewModel).fieldValue,
        )
    }

    @Test
    fun `bedrooms row shows the bedroom count when a bedroom count was entered`() {
        val propertyOwnership = createOccupiedPropertyOwnership(numberOfBedrooms = 3)

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(3, bedroomsRow(viewModel).fieldValue)
    }

    @Test
    fun `bedrooms row keeps its update action for a landlord when no bedroom count was entered`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership(id = 123)

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertEquals(
            UpdateBedroomsController.getUpdateBedroomsRoute(propertyOwnership.id) + "/${BedroomsStep.ROUTE_SEGMENT}",
            bedroomsRow(viewModel)
                .actions
                .single()
                .url,
        )
    }

    @Test
    fun `bedrooms row shows the not added message key with no update action for a council`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertEquals(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms.notAdded",
            bedroomsRow(viewModel).fieldValue,
        )
        assertTrue(bedroomsRow(viewModel).actions.isEmpty()) {
            "The local council view must not show a change link for the bedrooms row"
        }
    }

    @Test
    fun `bedrooms not added message key resolves to a message`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        val messageKey = bedroomsRow(viewModel).fieldValue as String

        val resolvedMessage = yamlMessageSource.getMessage(messageKey, null, messageKey, Locale.getDefault())

        assertNotEquals(messageKey, resolvedMessage) {
            "Message key '$messageKey' does not resolve — it would display as the raw key on the page"
        }
    }

    @Test
    fun `registration and occupation sections contain the expected rows`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(
            listOf(
                "propertyDetails.propertyRecord.registrationNumber",
                "propertyDetails.propertyRecord.registrationDate",
            ),
            viewModel.registrationDetails.map { it.fieldHeading },
        )
        assertEquals(
            listOf("propertyDetails.propertyRecord.occupation.isOccupied"),
            viewModel.occupiedSection.map { it.fieldHeading },
        )
    }

    @Test
    fun `the people responsible tab shows the ownership answers`() {
        val propertyOwnership = createOccupiedPropertyOwnership(ownershipType = OwnershipType.SHARE_OF_JOINT_TENANTS)

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showPeopleResponsibleOwnership)
        assertEquals(
            listOf(
                "propertyDetails.peopleResponsible.ownership.ownsProperty",
                "propertyDetails.peopleResponsible.ownership.ownershipType",
                "propertyDetails.peopleResponsible.ownership.freeholdType",
            ),
            viewModel.peopleResponsibleOwnershipSection.map { it.fieldHeading },
        )
    }

    @Test
    fun `ownership answer rows link to the ownership update journey start for landlords`() {
        // Arrange
        val propertyOwnership =
            createOccupiedPropertyOwnership(id = 123, ownershipType = OwnershipType.SHARE_OF_JOINT_TENANTS)

        // Act
        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        // Assert
        val expectedUrl =
            UpdateOwnershipController.getUpdateOwnershipRoute(propertyOwnership.id) +
                "/${InitialiseOwnershipUpdateStep.ROUTE_SEGMENT}"
        assertEquals(3, viewModel.peopleResponsibleOwnershipSection.size)
        assertTrue(viewModel.peopleResponsibleOwnershipSection.all { it.actions.single().url == expectedUrl }) {
            "Every ownership answer row should link to the start of the ownership update journey"
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExpectedOwnershipAnswerRows")
    fun `the ownership answers show the right rows and values for each ownership type`(
        ownershipType: OwnershipType,
        expectedRows: List<Pair<String, String>>,
    ) {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = ownershipType),
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertEquals(
            expectedRows,
            viewModel.peopleResponsibleOwnershipSection.map { it.fieldHeading to it.fieldValue },
        )
    }

    @Test
    fun `the freeholder section uses ownership specific labels and update url`() {
        val ownershipTypes =
            listOf(
                Triple(
                    OwnershipType.SHARE_OF_JOINT_TENANTS,
                    "registerProperty.freeholders.checkAnswers.heading.joint",
                    "registerProperty.freeholders.checkAnswers.changeLink.joint",
                ),
                Triple(
                    OwnershipType.LEASEHOLD_ONLY,
                    "registerProperty.freeholders.checkAnswers.heading.nonJoint",
                    "registerProperty.freeholders.checkAnswers.changeLink.nonJoint",
                ),
            )

        ownershipTypes.forEach { (ownershipType, headingKey, linkTextKey) ->
            val propertyOwnership = createOccupiedPropertyOwnership(ownershipType = ownershipType)
            val viewModel =
                PropertyDetailsViewModel(
                    propertyOwnership,
                    viewType = PropertyDetailsViewType.LANDLORD,
                    messageSource = mockMessageSource,
                )

            assertTrue(viewModel.showFreeholderSection)
            assertTrue(viewModel.showFreeholderSectionUpdateLink)
            assertEquals(headingKey, viewModel.freeholderSectionHeadingKey)
            assertEquals(linkTextKey, viewModel.freeholderSectionLinkTextKey)
            assertEquals(
                UpdateFreeholdersController.getUpdateFreeholdersFirstStepRoute(propertyOwnership.id),
                viewModel.freeholderSectionUpdateUrl,
            )
        }
    }

    @Test
    fun `the local council view shows the ownership answers without change links`() {
        // Arrange & Act
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = OwnershipType.SHARE_OF_JOINT_TENANTS),
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        // Assert
        assertTrue(viewModel.showPeopleResponsibleOwnership)
        assertEquals(3, viewModel.peopleResponsibleOwnershipSection.size)
        assertTrue(viewModel.peopleResponsibleOwnershipSection.all { it.actions.isEmpty() })
    }

    @Test
    fun `the freeholder section is shown in the local council view without an update link`() {
        // Arrange & Act
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY),
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        // Assert
        assertTrue(viewModel.showFreeholderSection)
        assertFalse(viewModel.showFreeholderSectionUpdateLink)
    }

    @Test
    fun `the freeholder section is hidden when the ownership type does not require freeholder records`() {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = OwnershipType.SOLE_FREEHOLD),
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
                associatedPartiesEnabled = true,
            )

        assertFalse(viewModel.showFreeholderSection)
    }

    @ParameterizedTest
    @EnumSource(OwnershipType::class)
    fun `the superior landlord section is shown only for ownership types that require superior landlord info`(
        ownershipType: OwnershipType,
    ) {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = ownershipType),
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
                associatedPartiesEnabled = true,
            )

        assertEquals(ownershipType.requiresSuperiorLandlordInfo(), viewModel.showSuperiorLandlordSection)
    }

    @Test
    fun `the superior landlord section links to the update journey when there are superior landlords`() {
        val propertyOwnership = createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY)
        propertyOwnership.superiorLandlords.add(createSuperiorLandlord(propertyOwnership, name = "Sam Superior"))

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, viewType = PropertyDetailsViewType.LANDLORD, messageSource = mockMessageSource)

        assertTrue(viewModel.hasSuperiorLandlords)
        assertEquals(
            UpdateSuperiorLandlordsController.getUpdateSuperiorLandlordsFirstStepRoute(propertyOwnership.id),
            viewModel.superiorLandlordSectionUpdateUrl,
        )
    }

    @Test
    fun `the superior landlord not used rows show a no answer with a change link in the landlord view`() {
        val propertyOwnership = createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY)
        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        val row = viewModel.superiorLandlordNotUsedRows.single()
        assertEquals("registerProperty.superiorLandlords.checkAnswers.anyoneInOwnershipChain", row.fieldHeading)
        assertEquals("commonText.no", row.fieldValue)
        assertEquals(
            listOf(
                SummaryListRowActionsViewModel(
                    "forms.links.change",
                    UpdateSuperiorLandlordsController.getUpdateSuperiorLandlordsFirstStepRoute(propertyOwnership.id),
                ),
            ),
            row.actions,
        )
    }

    @Test
    fun `the superior landlord section is shown in the local council view without change links`() {
        // Arrange & Act
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY),
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        // Assert
        assertTrue(viewModel.showSuperiorLandlordSection)
        assertFalse(viewModel.showSuperiorLandlordSectionUpdateLink)
        assertTrue(viewModel.superiorLandlordNotUsedRows.single().actions.isEmpty())
    }

    @Test
    fun `every ownership type has an expected set of ownership answer rows`() {
        assertEquals(
            OwnershipType.entries.toSet(),
            provideExpectedOwnershipAnswerRows().map { it.get()[0] }.toSet(),
        ) {
            "Every ownership type must be covered, so a new one cannot be added without checking its rows"
        }
    }

    @Test
    fun `the ownership answer message keys resolve to messages`() {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = OwnershipType.SHARE_OF_JOINT_TENANTS),
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        (
            viewModel.peopleResponsibleOwnershipSection.map { it.fieldHeading } +
                viewModel.peopleResponsibleOwnershipHeadingKey +
                viewModel.peopleResponsibleTabHeadingKey
        ).forEach { messageKey ->
            val resolvedMessage = yamlMessageSource.getMessage(messageKey, null, messageKey, Locale.getDefault())
            assertNotEquals(messageKey, resolvedMessage) {
                "Message key '$messageKey' does not resolve — it would display as the raw key on the page"
            }
        }
    }

    @Test
    fun `the tab heading is renamed for both views`() {
        listOf(PropertyDetailsViewType.LANDLORD, PropertyDetailsViewType.LOCAL_COUNCIL).forEach { viewType ->
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(),
                    viewType = viewType,
                    messageSource = mockMessageSource,
                )

            assertEquals("propertyDetails.peopleResponsible.heading", viewModel.peopleResponsibleTabHeadingKey)
        }
    }

    @Test
    fun `the people responsible tab shows a not-used property manager row when there is no property manager`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showPropertyManagerSection)
        assertFalse(viewModel.hasPropertyManager)
        val row = viewModel.propertyManagerNotUsedRows.single()
        assertEquals("registerProperty.propertyManager.usingPropertyManager.fieldSetHeading", row.fieldHeading)
        assertEquals("registerProperty.propertyManager.usingPropertyManager.radios.no", row.fieldValue)
        assertEquals(UpdatePropertyManagerController.getUpdatePropertyManagerFirstStepRoute(propertyOwnership.id), row.actions.single().url)
    }

    @Test
    fun `there is no not-used property manager row and an update link when there is a property manager`() {
        val propertyOwnership = createOccupiedPropertyOwnership().apply { propertyManager = createPropertyManager() }

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showPropertyManagerSection)
        assertTrue(viewModel.hasPropertyManager)
        assertTrue(viewModel.showPropertyManagerUpdateLink)
        assertTrue(viewModel.propertyManagerNotUsedRows.isEmpty())
        assertEquals(
            UpdatePropertyManagerController.getUpdatePropertyManagerFirstStepRoute(propertyOwnership.id),
            viewModel.propertyManagerUpdateUrl,
        )
    }

    @Test
    fun `the property manager message keys resolve to messages`() {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(),
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        viewModel.propertyManagerNotUsedRows
            .flatMap { listOf(it.fieldHeading, it.fieldValue as String) }
            .forEach { messageKey ->
                val resolvedMessage = yamlMessageSource.getMessage(messageKey, null, messageKey, Locale.getDefault())
                assertNotEquals(messageKey, resolvedMessage) {
                    "Message key '$messageKey' does not resolve — it would display as the raw key on the page"
                }
            }
    }

    @Test
    fun `the property manager section is shown in the local council view without change links`() {
        // Arrange & Act
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(),
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        // Assert
        assertTrue(viewModel.showPropertyManagerSection)
        assertFalse(viewModel.showPropertyManagerUpdateLink)
        assertTrue(viewModel.propertyManagerNotUsedRows.single().actions.isEmpty())
    }

    // TODO PDJB-2016: delete this class when the ASSOCIATED_PARTIES feature flag is removed
    @Nested
    inner class BeforeAssociatedParties {
        @Test
        fun `the property manager section is hidden`() {
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(),
                    viewType = PropertyDetailsViewType.LANDLORD,
                    messageSource = mockMessageSource,
                    associatedPartiesEnabled = false,
                )

            assertFalse(viewModel.showPropertyManagerSection)
            assertTrue(viewModel.propertyManagerNotUsedRows.isEmpty())
        }
    }

    // TODO PDJB-2016: delete this class along with the ASSOCIATED_PARTIES flag. It covers the single
    //  legacy ownership row on the property record tab, which goes with the flag.
    @Nested
    inner class OwnershipBeforePdjb955 {
        @Test
        fun `the freeholder section is hidden when associated parties are disabled`() {
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY),
                    viewType = PropertyDetailsViewType.LANDLORD,
                    messageSource = mockMessageSource,
                    associatedPartiesEnabled = false,
                )

            assertFalse(viewModel.showFreeholderSection)
        }

        @Test
        fun `the superior landlord section is hidden when associated parties are disabled`() {
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY),
                    viewType = PropertyDetailsViewType.LANDLORD,
                    messageSource = mockMessageSource,
                    associatedPartiesEnabled = false,
                )

            assertFalse(viewModel.showSuperiorLandlordSection)
        }

        @Test
        fun `the landlord's legacy ownership section is hidden when associated parties is enabled`() {
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(),
                    viewType = PropertyDetailsViewType.LANDLORD,
                    messageSource = mockMessageSource,
                    associatedPartiesEnabled = true,
                )

            assertFalse(viewModel.showOwnershipSectionOnPropertyDetailsTab)
            assertTrue(viewModel.ownershipSectionBeforePdjb955.isEmpty())
        }

        @Test
        fun `the local council's legacy ownership section is hidden when associated parties is enabled`() {
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(),
                    viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                    messageSource = mockMessageSource,
                    associatedPartiesEnabled = true,
                )

            assertFalse(viewModel.showOwnershipSectionOnPropertyDetailsTab)
            assertTrue(viewModel.ownershipSectionBeforePdjb955.isEmpty())
        }

        @Test
        fun `the legacy ownership section and tab name are kept when associated parties is disabled`() {
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(),
                    viewType = PropertyDetailsViewType.LANDLORD,
                    messageSource = mockMessageSource,
                    associatedPartiesEnabled = false,
                )

            assertTrue(viewModel.showOwnershipSectionOnPropertyDetailsTab)
            assertEquals(
                listOf("propertyDetails.propertyRecord.ownership.ownershipType"),
                viewModel.ownershipSectionBeforePdjb955.map { it.fieldHeading },
            )
            assertFalse(viewModel.showPeopleResponsibleOwnership)
            assertTrue(viewModel.peopleResponsibleOwnershipSection.isEmpty())
            assertEquals("propertyDetails.landlordDetails.heading", viewModel.peopleResponsibleTabHeadingKey)
        }

        @Test
        fun `the local council view keeps the legacy ownership section and hides associated parties when disabled`() {
            val viewModel =
                PropertyDetailsViewModel(
                    createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY),
                    viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                    messageSource = mockMessageSource,
                    associatedPartiesEnabled = false,
                )

            assertTrue(viewModel.showOwnershipSectionOnPropertyDetailsTab)
            assertEquals(
                listOf("propertyDetails.propertyRecord.ownership.ownershipType"),
                viewModel.ownershipSectionBeforePdjb955.map { it.fieldHeading },
            )
            assertFalse(viewModel.showPeopleResponsibleOwnership)
            assertFalse(viewModel.showFreeholderSection)
            assertFalse(viewModel.showSuperiorLandlordSection)
            assertFalse(viewModel.showPropertyManagerSection)
        }
    }

    @Test
    fun `shows a licensing provide-later row for a landlord when licensing is skipped on an occupied property`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                lastOccupiedDate = LocalDate.of(2025, 1, 1),
                licenseProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertEquals(
            listOf("propertyDetails.propertyRecord.licensing.rowName"),
            viewModel.licensingSection.map { it.fieldHeading },
        )
        assertNull(viewModel.licensingProvideLaterParagraph)
    }

    @Test
    fun `shows a licensing deadline paragraph for a council when an occupied-at-registration property skips licensing`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                licenseProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.licensingSection.isEmpty())
        assertEquals(
            "Message for propertyDetails.propertyRecord.licensing.councilOccupied",
            viewModel.licensingProvideLaterParagraph,
        )
    }

    @Test
    fun `shows a not-provided licensing paragraph for a council when a property became occupied after registration`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate.plusDays(30),
                licenseProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.licensingSection.isEmpty())
        assertEquals(
            "Message for propertyDetails.propertyRecord.licensing.councilNotProvided",
            viewModel.licensingProvideLaterParagraph,
        )
    }

    @Test
    fun `shows a no-deadline licensing provide-later row for a landlord when a property became occupied after registration`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate.plusDays(30),
                licenseProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        val licensingRow = viewModel.licensingSection.single()
        assertEquals("propertyDetails.propertyRecord.licensing.provideLaterNoDeadline", licensingRow.fieldValue)
    }

    @Test
    fun `shows a licensing not-provided paragraph for a local council when licensing is skipped on an unoccupied property`() {
        val propertyOwnership = createPropertyOwnership(license = null, licenseProvideLater = true)

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertEquals(
            "Message for propertyDetails.propertyRecord.licensing.councilNotProvided",
            viewModel.licensingProvideLaterParagraph,
        )
    }

    @Test
    fun `shows a no-deadline licensing provide-later row for a landlord when licensing is skipped on an unoccupied property`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership(licenseProvideLater = true)

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        val licensingRow = viewModel.licensingSection.single()
        assertEquals("propertyDetails.propertyRecord.licensing.rowName", licensingRow.fieldHeading)
        assertEquals("propertyDetails.propertyRecord.licensing.provideLaterNoDeadline", licensingRow.fieldValue)
        assertNull(viewModel.licensingProvideLaterParagraph)
    }

    @Test
    fun `shows the licensing type row when a license is present`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
            )

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(
            listOf(
                "propertyDetails.propertyRecord.licensingInformation.licensingType",
                "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
            ),
            viewModel.licensingSection.map { it.fieldHeading },
        )
        assertNull(viewModel.licensingProvideLaterParagraph)
    }

    @Test
    fun `hides the licensing number row and shows a no-licensing type for a no-licensing record or no license`() {
        val declaredNoLicenseViewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(license = License(LicensingType.NO_LICENSING, "")),
                messageSource = mockMessageSource,
            )
        val nullLicenseViewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(license = null),
                messageSource = mockMessageSource,
            )

        listOf(declaredNoLicenseViewModel, nullLicenseViewModel).forEach { viewModel ->
            assertEquals(
                listOf("propertyDetails.propertyRecord.licensingInformation.licensingType"),
                viewModel.licensingSection.map { it.fieldHeading },
            )
            assertEquals(
                "licensing.checkAnswers.noLicensing",
                viewModel.licensingSection.single().fieldValue,
            )
        }
    }

    @Test
    fun `shows a tenancy provide-later row for a landlord when tenancy is skipped on an occupied property`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                currentNumHouseholds = 0,
                lastOccupiedDate = LocalDate.of(2025, 1, 1),
                tenancyProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertEquals("propertyDetails.propertyRecord.tenancy.heading", viewModel.tenancyHeadingKey)
        assertEquals(
            listOf("propertyDetails.propertyRecord.tenancy.rowName"),
            viewModel.tenancySection.map { it.fieldHeading },
        )
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    @Test
    fun `shows a deadline tenancy provide-later row for a landlord for an occupied-at-registration property`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                currentNumHouseholds = 0,
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                tenancyProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertEquals(
            "Message for propertyDetails.propertyRecord.tenancy.provideLaterWithDeadline",
            viewModel.tenancySection.single().fieldValue,
        )
    }

    @Test
    fun `shows a no-deadline tenancy provide-later row for a landlord when a property became occupied after registration`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                currentNumHouseholds = 0,
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate.plusDays(30),
                tenancyProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertEquals(
            "propertyDetails.propertyRecord.tenancy.provideLaterNoDeadline",
            viewModel.tenancySection.single().fieldValue,
        )
    }

    @Test
    fun `shows a tenancy deadline paragraph for a council when an occupied-at-registration property skips tenancy`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                currentNumHouseholds = 0,
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate,
                tenancyProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.tenancySection.isEmpty())
        assertEquals(
            "Message for propertyDetails.propertyRecord.tenancy.councilOccupied",
            viewModel.tenancyProvideLaterParagraph,
        )
    }

    @Test
    fun `shows a not-provided tenancy paragraph for a council when a property became occupied after registration`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                currentNumHouseholds = 0,
                createdDate = occupiedAtRegistrationInstant,
                lastOccupiedDate = occupiedAtRegistrationDate.plusDays(30),
                tenancyProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.tenancySection.isEmpty())
        assertEquals(
            "Message for propertyDetails.propertyRecord.tenancy.councilNotProvided",
            viewModel.tenancyProvideLaterParagraph,
        )
    }

    @Test
    fun `hides the tenancy section for a landlord when the property is unoccupied`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.tenancySection.isEmpty())
    }

    @Test
    fun `hides the tenancy section for a council when the property is unoccupied`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.tenancySection.isEmpty())
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    @Test
    fun `shows the full tenancy rows when tenancy details are provided`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                billsIncludedList = null,
                customBillsIncluded = null,
                rentFrequency = RentFrequency.MONTHLY,
                customRentFrequency = null,
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            )

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(expectedHeaderList, viewModel.tenancySection.map { it.fieldHeading })
        assertEquals("propertyDetails.propertyRecord.tenancy.heading", viewModel.tenancyHeadingKey)
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    @Test
    fun `shows the bills-included row with conditional and custom tenancy values when they are provided`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(
            listOf(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            ),
            viewModel.tenancySection.map { it.fieldHeading },
        )

        assertEquals(
            listOf(true, false, true, false, false, true, false),
            viewModel.tenancySection.map { it.withoutBottomBorder },
        )

        fun rowValue(heading: String) = viewModel.tenancySection.single { it.fieldHeading == heading }.fieldValue

        assertEquals(
            "commonText.yes",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName"),
        )
        assertEquals(
            "Message for tenancyDetails.billsIncluded.checkbox.electricity, " +
                "Message for tenancyDetails.billsIncluded.checkbox.water, Cat sitting",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded"),
        )
        assertEquals(
            "Fortnightly",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName"),
        )
        assertEquals(
            "£200 Message for tenancyDetails.checkAnswers.customFrequencyRentAmountSuffix",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount"),
        )
    }

    private fun bedroomsRow(viewModel: PropertyDetailsViewModel): SummaryListRowViewModel =
        viewModel.propertyDetailsSection.single {
            it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms"
        }

    @Test
    fun `correspondenceSection shows saved contact details`() {
        val propertyOwnership =
            createPropertyOwnership(
                correspondenceEmail = "contact@example.com",
                correspondenceAddress = createAddress("Flat 2, 25 Contact Road, Bristol, BS1 2AB"),
            )
        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.LANDLORD,
                messageSource = mockMessageSource,
            )

        val section = viewModel.correspondenceSection
        assertEquals(2, section.size)

        val emailRow = section[0]
        assertEquals("propertyDetails.propertyRecord.correspondence.emailAddress", emailRow.fieldHeading)
        assertEquals("contact@example.com", emailRow.fieldValue)
        assertTrue(emailRow.hasActions)
        assertEquals(
            "/landlord/property-details/${propertyOwnership.id}/update-correspondence-email/correspondence-email",
            emailRow.actions.single().url,
        )

        val addressRow = section[1]
        assertEquals("propertyDetails.propertyRecord.correspondence.address", addressRow.fieldHeading)
        assertEquals(listOf("Flat 2", "25 Contact Road", "Bristol", "BS1 2AB"), addressRow.fieldValue)
        assertTrue(addressRow.hasActions)
        val action = addressRow.actions.single()
        val expectedUrl =
            LandlordUpdateCorrespondenceAddressController
                .getUpdateCorrespondenceAddressFirstStepRoute(propertyOwnership.id)
        assertEquals(expectedUrl, action.url)
    }

    @Test
    fun `correspondenceSection rows have no change actions in the local council view`() {
        val viewModel =
            PropertyDetailsViewModel(
                createPropertyOwnership(),
                viewType = PropertyDetailsViewType.LOCAL_COUNCIL,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.correspondenceSection.none { it.hasActions })
    }

    @Test
    fun `the deactivated landlord view has no change links in any section`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.DEACTIVATED_LANDLORD,
                messageSource = mockMessageSource,
            )

        val allRows =
            viewModel.registrationDetails +
                viewModel.propertyDetailsSection +
                viewModel.ownershipSectionBeforePdjb955 +
                viewModel.peopleResponsibleOwnershipSection +
                viewModel.correspondenceSection +
                viewModel.occupiedSection +
                viewModel.licensingSection +
                viewModel.tenancySection

        assertTrue(viewModel.peopleResponsibleOwnershipSection.isNotEmpty())
        assertTrue(allRows.none { it.hasActions })
    }

    @Test
    fun `the deactivated landlord view shows the freeholder section without its update link`() {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY),
                viewType = PropertyDetailsViewType.DEACTIVATED_LANDLORD,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showFreeholderSection)
        assertFalse(viewModel.showFreeholderSectionUpdateLink)
    }

    @Test
    fun `the deactivated landlord view shows the superior landlord section without its update link`() {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY),
                viewType = PropertyDetailsViewType.DEACTIVATED_LANDLORD,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showSuperiorLandlordSection)
        assertFalse(viewModel.showSuperiorLandlordSectionUpdateLink)
        assertTrue(viewModel.superiorLandlordNotUsedRows.none { it.hasActions })
    }

    @Test
    fun `the deactivated landlord view shows the property manager section without its update link`() {
        val propertyOwnership = createOccupiedPropertyOwnership().apply { propertyManager = createPropertyManager() }

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.DEACTIVATED_LANDLORD,
                messageSource = mockMessageSource,
            )

        assertTrue(viewModel.showPropertyManagerSection)
        assertFalse(viewModel.showPropertyManagerUpdateLink)
    }

    @Test
    fun `the deactivated landlord view shows the not-used property manager row without a change link`() {
        val viewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(),
                viewType = PropertyDetailsViewType.DEACTIVATED_LANDLORD,
                messageSource = mockMessageSource,
            )

        assertFalse(viewModel.propertyManagerNotUsedRows.single().hasActions)
    }

    @Test
    fun `the deactivated landlord view shows provide later rows without links and no council paragraphs`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                lastOccupiedDate = occupiedAtRegistrationDate,
                createdDate = occupiedAtRegistrationInstant,
                licenseProvideLater = true,
                tenancyProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                viewType = PropertyDetailsViewType.DEACTIVATED_LANDLORD,
                messageSource = mockMessageSource,
            )

        assertEquals(1, viewModel.licensingSection.size)
        assertEquals(1, viewModel.tenancySection.size)
        assertTrue(viewModel.licensingSection.none { it.hasActions })
        assertTrue(viewModel.tenancySection.none { it.hasActions })
        assertNull(viewModel.licensingProvideLaterParagraph)
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    companion object {
        private const val OWNS_PROPERTY = "propertyDetails.peopleResponsible.ownership.ownsProperty"
        private const val OWNERSHIP_TYPE = "propertyDetails.peopleResponsible.ownership.ownershipType"
        private const val FREEHOLD_TYPE = "propertyDetails.peopleResponsible.ownership.freeholdType"

        private const val YES = "commonText.yes"
        private const val NO = "commonText.no"

        private const val FREEHOLD = "registerProperty.ownershipKind.checkbox.freehold.label"
        private const val LEASEHOLD = "registerProperty.ownershipKind.checkbox.leasehold.label"
        private const val FREEHOLD_AND_LEASEHOLD = "registerProperty.ownershipKind.value.freeholdAndLeasehold"

        private const val SOLE_OWNERSHIP = "registerProperty.freeholdType.radios.option.soleOwnership.label"
        private const val JOINT_TENANTS = "registerProperty.freeholdType.radios.option.jointTenants.label"
        private const val TENANTS_IN_COMMON = "registerProperty.freeholdType.radios.option.tenantsInCommon.label"
        private const val COMMONHOLD = "registerProperty.freeholdType.radios.option.commonhold.label"

        // The freehold type row is omitted where there is no freehold to describe, and both the
        // ownership type and freehold type rows where the landlord does not own the property.
        @JvmStatic
        fun provideExpectedOwnershipAnswerRows() =
            listOf(
                Arguments.of(OwnershipType.NONE, listOf(OWNS_PROPERTY to NO)),
                Arguments.of(
                    OwnershipType.LEASEHOLD_ONLY,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to LEASEHOLD),
                ),
                Arguments.of(
                    OwnershipType.SOLE_FREEHOLD,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD, FREEHOLD_TYPE to SOLE_OWNERSHIP),
                ),
                Arguments.of(
                    OwnershipType.JOINT_TENANTS,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD, FREEHOLD_TYPE to JOINT_TENANTS),
                ),
                Arguments.of(
                    OwnershipType.TENANTS_IN_COMMON,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD, FREEHOLD_TYPE to TENANTS_IN_COMMON),
                ),
                Arguments.of(
                    OwnershipType.COMMONHOLD,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD, FREEHOLD_TYPE to COMMONHOLD),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_SOLE_FREEHOLD,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD_AND_LEASEHOLD, FREEHOLD_TYPE to SOLE_OWNERSHIP),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_JOINT_TENANTS,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD_AND_LEASEHOLD, FREEHOLD_TYPE to JOINT_TENANTS),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_TENANTS_IN_COMMON,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD_AND_LEASEHOLD, FREEHOLD_TYPE to TENANTS_IN_COMMON),
                ),
                Arguments.of(
                    OwnershipType.SHARE_OF_COMMONHOLD,
                    listOf(OWNS_PROPERTY to YES, OWNERSHIP_TYPE to FREEHOLD_AND_LEASEHOLD, FREEHOLD_TYPE to COMMONHOLD),
                ),
            )
    }
}
