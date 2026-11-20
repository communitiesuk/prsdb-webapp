package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipStructureTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipTypeTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@ExtendWith(MockitoExtension::class)
class OwnershipStructureDetailsHelperTests {
    private val helper = OwnershipStructureDetailsHelper(FreeholderSummaryCardHelper(), SuperiorLandlordSummaryCardHelper())

    @Mock
    private lateinit var mockState: CheckYourAnswersJourneyState

    @Mock
    private lateinit var mockOwnershipStructureTask: OwnershipStructureTask

    @Mock
    private lateinit var mockOwnershipTypeTask: OwnershipTypeTask

    @Mock
    private lateinit var mockOwnsPropertyStep: OwnsPropertyStep

    @Mock
    private lateinit var mockOwnershipKindStep: OwnershipKindStep

    @Mock
    private lateinit var mockFreeholdTypeStep: FreeholdTypeStep

    @Mock
    private lateinit var mockFreeholdersTask: FreeholdersTask

    @Mock
    private lateinit var mockFreeholdersDetailsTask: FreeholdersDetailsTask

    @Mock
    private lateinit var mockFreeholdersListStep: FreeholdersListStep

    @Mock
    private lateinit var mockSuperiorLandlordsTask: SuperiorLandlordsTask

    @Mock
    private lateinit var mockAnyoneInOwnershipChainStep: AnyoneInOwnershipChainStep

    @Mock
    private lateinit var mockSuperiorLandlordsDetailsTask: SuperiorLandlordsDetailsTask

    @Mock
    private lateinit var mockSuperiorLandlordsListStep: SuperiorLandlordsListStep

    @BeforeEach
    fun setUp() {
        lenient().`when`(mockState.journeyId).thenReturn("journey-id")

        lenient().`when`(mockOwnershipStructureTask.ownershipTypeTask).thenReturn(mockOwnershipTypeTask)
        lenient().`when`(mockOwnershipTypeTask.ownsPropertyStep).thenReturn(mockOwnsPropertyStep)
        lenient().`when`(mockOwnershipTypeTask.ownershipKindStep).thenReturn(mockOwnershipKindStep)
        lenient().`when`(mockOwnershipTypeTask.freeholdTypeStep).thenReturn(mockFreeholdTypeStep)
        lenient().`when`(mockOwnsPropertyStep.isStepReachable).thenReturn(true)
        lenient().`when`(mockOwnsPropertyStep.urlPath).thenReturn(OwnsPropertyStep.ROUTE_SEGMENT)
        lenient().`when`(mockState.getCyaJourneyId(mockOwnsPropertyStep)).thenReturn("owns-change")
        lenient().`when`(mockOwnershipKindStep.isStepReachable).thenReturn(true)
        lenient().`when`(mockOwnershipKindStep.urlPath).thenReturn(OwnershipKindStep.ROUTE_SEGMENT)
        lenient().`when`(mockState.getCyaJourneyId(mockOwnershipKindStep)).thenReturn("kind-change")
        lenient().`when`(mockFreeholdTypeStep.isStepReachable).thenReturn(true)
        lenient().`when`(mockFreeholdTypeStep.urlPath).thenReturn(FreeholdTypeStep.ROUTE_SEGMENT)
        lenient().`when`(mockState.getCyaJourneyId(mockFreeholdTypeStep)).thenReturn("freehold-type-change")

        lenient().`when`(mockOwnershipStructureTask.freeholdersTask).thenReturn(mockFreeholdersTask)
        lenient().`when`(mockFreeholdersTask.freeholdersMap).thenReturn(mapOf(1 to FreeholderDataModel(detailsKnown = false)))
        lenient().`when`(mockFreeholdersTask.freeholdersDetailsTask).thenReturn(mockFreeholdersDetailsTask)
        lenient().`when`(mockFreeholdersDetailsTask.freeholdersListStep).thenReturn(mockFreeholdersListStep)
        lenient().`when`(mockFreeholdersListStep.isStepReachable).thenReturn(true)
        lenient().`when`(mockFreeholdersListStep.urlPath).thenReturn(FreeholdersListStep.ROUTE_SEGMENT)
        lenient().`when`(mockState.getCyaJourneyId(mockFreeholdersListStep)).thenReturn("freeholders-change")

        lenient().`when`(mockOwnershipStructureTask.superiorLandlordsTask).thenReturn(mockSuperiorLandlordsTask)
        lenient().`when`(mockSuperiorLandlordsTask.anyoneInOwnershipChainStep).thenReturn(mockAnyoneInOwnershipChainStep)
        lenient().`when`(mockSuperiorLandlordsTask.superiorLandlordsDetailsTask).thenReturn(mockSuperiorLandlordsDetailsTask)
        lenient().`when`(mockSuperiorLandlordsDetailsTask.superiorLandlordsListStep).thenReturn(mockSuperiorLandlordsListStep)
        lenient().`when`(mockAnyoneInOwnershipChainStep.isStepReachable).thenReturn(true)
        lenient().`when`(mockAnyoneInOwnershipChainStep.urlPath).thenReturn(AnyoneInOwnershipChainStep.ROUTE_SEGMENT)
        lenient().`when`(mockState.getCyaJourneyId(mockAnyoneInOwnershipChainStep)).thenReturn("chain-change")
        lenient().`when`(mockSuperiorLandlordsListStep.isStepReachable).thenReturn(true)
        lenient().`when`(mockSuperiorLandlordsListStep.urlPath).thenReturn(SuperiorLandlordsListStep.ROUTE_SEGMENT)
        lenient().`when`(mockState.getCyaJourneyId(mockSuperiorLandlordsListStep)).thenReturn("list-change")
        lenient().`when`(mockAnyoneInOwnershipChainStep.formModelIfReachableOrNull)
            .thenReturn(AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = false))
    }

    private fun getContent() = helper.getOwnershipStructureCyaContent(mockState, mockOwnershipStructureTask)

    @Test
    fun `missing ownership type throws an unrecoverable journey state exception`() {
        // Arrange
        whenever(mockOwnershipTypeTask.ownershipType).thenReturn(null)

        // Act & Assert
        assertThrows<UnrecoverableJourneyStateException> { getContent() }
    }

    @Nested
    inner class OwnershipContent {
        @Test
        fun `not owning the property shows only the owns property row`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.NONE)

            // Act
            val rows = ownershipRows()

            // Assert
            assertEquals(listOf("propertyDetails.peopleResponsible.ownership.ownsProperty"), rows.map { it.fieldHeading })
            assertEquals("commonText.no", rows.single().getConvertedFieldValue())
            assertEquals(
                JourneyStateService.urlWithJourneyState(OwnsPropertyStep.ROUTE_SEGMENT, "owns-change"),
                rows.single().actions.single().url,
            )
        }

        @Test
        fun `leasehold only shows the owns property and ownership type rows`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)

            // Act
            val rows = ownershipRows()

            // Assert
            assertEquals(
                listOf(
                    "propertyDetails.peopleResponsible.ownership.ownsProperty",
                    "propertyDetails.peopleResponsible.ownership.ownershipType",
                ),
                rows.map { it.fieldHeading },
            )
            assertEquals(
                listOf("commonText.yes", "registerProperty.ownershipKind.checkbox.leasehold.label"),
                rows.map { it.getConvertedFieldValue() },
            )
            assertEquals(
                JourneyStateService.urlWithJourneyState(OwnershipKindStep.ROUTE_SEGMENT, "kind-change"),
                rows[1].actions.single().url,
            )
        }

        @Test
        fun `freehold only shows all three rows with change links to each question`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SOLE_FREEHOLD)

            // Act
            val rows = ownershipRows()

            // Assert
            assertEquals(
                listOf(
                    "propertyDetails.peopleResponsible.ownership.ownsProperty",
                    "propertyDetails.peopleResponsible.ownership.ownershipType",
                    "propertyDetails.peopleResponsible.ownership.freeholdType",
                ),
                rows.map { it.fieldHeading },
            )
            assertEquals(
                listOf(
                    "commonText.yes",
                    "registerProperty.ownershipKind.checkbox.freehold.label",
                    "registerProperty.freeholdType.radios.option.soleOwnership.label",
                ),
                rows.map { it.getConvertedFieldValue() },
            )
            assertEquals(
                listOf(
                    JourneyStateService.urlWithJourneyState(OwnsPropertyStep.ROUTE_SEGMENT, "owns-change"),
                    JourneyStateService.urlWithJourneyState(OwnershipKindStep.ROUTE_SEGMENT, "kind-change"),
                    JourneyStateService.urlWithJourneyState(FreeholdTypeStep.ROUTE_SEGMENT, "freehold-type-change"),
                ),
                rows.map { it.actions.single().url },
            )
        }

        @Test
        fun `freehold and leasehold shows the combined ownership type value`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SHARE_OF_TENANTS_IN_COMMON)

            // Act
            val rows = ownershipRows()

            // Assert
            assertEquals(
                listOf(
                    "commonText.yes",
                    "registerProperty.ownershipKind.value.freeholdAndLeasehold",
                    "registerProperty.freeholdType.radios.option.tenantsInCommon.label",
                ),
                rows.map { it.getConvertedFieldValue() },
            )
        }

        private fun ownershipRows() = getContent()["ownershipRows"] as List<SummaryListRowViewModel>
    }

    @Nested
    inner class FreeholdersContent {
        @BeforeEach
        fun setUpFreeholders() {
            // LEASEHOLD_ONLY requires non-joint freeholder records
            lenient().`when`(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)
        }

        @Test
        fun `ownership type that does not require freeholder records has no freeholders content`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SOLE_FREEHOLD)

            // Act
            val content = getContent()

            // Assert
            assertNull(content["freeholdersHeadingKey"])
            assertNull(content["freeholdersChangeLinkKey"])
            assertNull(content["freeholdersChangeUrl"])
            assertNull(content["freeholdersCards"])
        }

        @Test
        fun `missing freeholders throws an unrecoverable journey state exception`() {
            // Arrange
            whenever(mockFreeholdersTask.freeholdersMap).thenReturn(null)

            // Act & Assert
            assertThrows<UnrecoverableJourneyStateException> { getContent() }
        }

        @Test
        fun `empty freeholders throws an unrecoverable journey state exception`() {
            // Arrange
            whenever(mockFreeholdersTask.freeholdersMap).thenReturn(emptyMap())

            // Act & Assert
            assertThrows<UnrecoverableJourneyStateException> { getContent() }
        }

        @Test
        fun `non-joint ownership type uses the non-joint heading and change link keys`() {
            // Act
            val content = getContent()

            // Assert
            assertEquals("registerProperty.freeholders.checkAnswers.heading.nonJoint", content["freeholdersHeadingKey"])
            assertEquals("registerProperty.freeholders.checkAnswers.changeLink.nonJoint", content["freeholdersChangeLinkKey"])
        }

        @Test
        fun `joint ownership type uses the joint heading and change link keys`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.JOINT_TENANTS)

            // Act
            val content = getContent()

            // Assert
            assertEquals("registerProperty.freeholders.checkAnswers.heading.joint", content["freeholdersHeadingKey"])
            assertEquals("registerProperty.freeholders.checkAnswers.changeLink.joint", content["freeholdersChangeLinkKey"])
        }

        @Test
        fun `freeholders content has a link to the freeholders list and cards without actions`() {
            // Arrange
            whenever(mockFreeholdersTask.freeholdersMap).thenReturn(
                mapOf(
                    2 to FreeholderDataModel(detailsKnown = false),
                    1 to FreeholderDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com"),
                ),
            )

            // Act
            val content = getContent()

            // Assert
            assertEquals(
                JourneyStateService.urlWithJourneyState(FreeholdersListStep.ROUTE_SEGMENT, "freeholders-change"),
                content["freeholdersChangeUrl"],
            )
            val cards = content["freeholdersCards"] as List<SummaryCardViewModel>
            assertEquals(
                listOf("Billie Smith", "registerProperty.freeholders.list.unknownFreeholder.title"),
                cards.map { it.title },
            )
            cards.forEach { assertEquals(emptyList<SummaryCardActionViewModel>(), it.actions) }
        }
    }

    @Nested
    inner class SuperiorLandlordsContent {
        @BeforeEach
        fun setUpSuperiorLandlords() {
            // LEASEHOLD_ONLY requires superior landlord info
            lenient().`when`(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)
        }

        @Test
        fun `ownership type that does not require superior landlord info has no superior landlords content`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SOLE_FREEHOLD)

            // Act
            val content = getContent()

            // Assert
            assertNull(content["superiorLandlordsRows"])
            assertNull(content["superiorLandlordsCards"])
            assertNull(content["superiorLandlordsChangeUrl"])
        }

        @Test
        fun `missing anyone in ownership chain answer throws an unrecoverable journey state exception`() {
            // Arrange
            whenever(mockAnyoneInOwnershipChainStep.formModelIfReachableOrNull).thenReturn(null)

            // Act & Assert
            assertThrows<UnrecoverableJourneyStateException> { getContent() }
        }

        @Test
        fun `answering yes with no superior landlords throws an unrecoverable journey state exception`() {
            // Arrange
            whenever(mockAnyoneInOwnershipChainStep.formModelIfReachableOrNull)
                .thenReturn(AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = true))
            whenever(mockSuperiorLandlordsTask.superiorLandlordsMap).thenReturn(null)

            // Act & Assert
            assertThrows<UnrecoverableJourneyStateException> { getContent() }
        }

        @Test
        fun `answering no shows a single row with a change link to the ownership chain question`() {
            // Act
            val content = getContent()

            // Assert
            val row = (content["superiorLandlordsRows"] as List<SummaryListRowViewModel>).single()
            assertEquals("registerProperty.superiorLandlords.checkAnswers.anyoneInOwnershipChain", row.fieldHeading)
            assertEquals("commonText.no", row.fieldValue)
            assertEquals(
                JourneyStateService.urlWithJourneyState(AnyoneInOwnershipChainStep.ROUTE_SEGMENT, "chain-change"),
                row.actions.single().url,
            )
            assertNull(content["superiorLandlordsCards"])
            assertNull(content["superiorLandlordsChangeUrl"])
        }

        @Test
        fun `answering yes shows a link to the superior landlords list and cards without actions`() {
            // Arrange
            whenever(mockAnyoneInOwnershipChainStep.formModelIfReachableOrNull)
                .thenReturn(AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = true))
            whenever(mockSuperiorLandlordsTask.superiorLandlordsMap).thenReturn(
                mapOf(
                    2 to SuperiorLandlordDataModel(detailsKnown = false),
                    1 to SuperiorLandlordDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com"),
                ),
            )

            // Act
            val content = getContent()

            // Assert
            assertEquals(
                JourneyStateService.urlWithJourneyState(SuperiorLandlordsListStep.ROUTE_SEGMENT, "list-change"),
                content["superiorLandlordsChangeUrl"],
            )
            val cards = content["superiorLandlordsCards"] as List<SummaryCardViewModel>
            assertEquals(
                listOf("Billie Smith", "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title"),
                cards.map { it.title },
            )
            cards.forEach { assertEquals(emptyList<SummaryCardActionViewModel>(), it.actions) }
            assertNull(content["superiorLandlordsRows"])
        }
    }
}
