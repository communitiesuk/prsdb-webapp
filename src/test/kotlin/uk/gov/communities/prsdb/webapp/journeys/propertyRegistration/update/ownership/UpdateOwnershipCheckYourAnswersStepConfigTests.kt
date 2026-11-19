package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
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
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.FreeholderSummaryCardHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OwnershipStructureDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.SuperiorLandlordSummaryCardHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class UpdateOwnershipCheckYourAnswersStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateOwnershipJourneyState

    @Mock
    private lateinit var mockFreeholderSummaryCardHelper: FreeholderSummaryCardHelper

    @Mock
    private lateinit var mockSuperiorLandlordSummaryCardHelper: SuperiorLandlordSummaryCardHelper

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

    private lateinit var stepConfig: UpdateOwnershipCheckYourAnswersStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateOwnershipCheckYourAnswersStepConfig(
                OwnershipStructureDetailsHelper(mockFreeholderSummaryCardHelper, mockSuperiorLandlordSummaryCardHelper),
            )
    }

    @Nested
    inner class GetStepSpecificContentTests {
        private val freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = true, name = "Freeholder", email = "f@example.com"))
        private val freeholderCards = listOf(SummaryCardViewModel(title = "Freeholder", summaryList = emptyList()))
        private val superiorLandlords =
            mapOf(1 to SuperiorLandlordDataModel(detailsKnown = true, name = "Superior landlord", email = "s@example.com"))
        private val superiorLandlordCards = listOf(SummaryCardViewModel(title = "Superior landlord", summaryList = emptyList()))

        @BeforeEach
        fun setUp() {
            lenient().`when`(mockState.ownershipStructureTask).thenReturn(mockOwnershipStructureTask)
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
            lenient().`when`(mockFreeholdersTask.freeholdersMap).thenReturn(freeholders)
            lenient().`when`(mockFreeholdersTask.freeholdersDetailsTask).thenReturn(mockFreeholdersDetailsTask)
            lenient().`when`(mockFreeholdersDetailsTask.freeholdersListStep).thenReturn(mockFreeholdersListStep)
            lenient().`when`(mockFreeholdersListStep.isStepReachable).thenReturn(true)
            lenient().`when`(mockFreeholdersListStep.urlPath).thenReturn(FreeholdersListStep.ROUTE_SEGMENT)
            lenient().`when`(mockState.getCyaJourneyId(mockFreeholdersListStep)).thenReturn("freeholders-change")
            lenient().`when`(mockFreeholderSummaryCardHelper.getSummaryCards(any(), any())).thenReturn(freeholderCards)

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
            lenient().`when`(mockSuperiorLandlordsTask.superiorLandlordsMap).thenReturn(superiorLandlords)
            lenient().`when`(mockSuperiorLandlordSummaryCardHelper.getSummaryCards(any(), any())).thenReturn(superiorLandlordCards)
        }

        @Test
        fun `not owning the property shows only the owns property row`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.NONE)
            stubAnyoneInOwnershipChain(false)

            // Act
            val rows = ownershipRows()

            // Assert
            assertEquals(listOf("propertyDetails.peopleResponsible.ownership.ownsProperty"), rows.map { it.fieldHeading })
            assertEquals(
                JourneyStateService.urlWithJourneyState(OwnsPropertyStep.ROUTE_SEGMENT, "owns-change"),
                rows.single().actions.single().url,
            )
        }

        @Test
        fun `leasehold only shows the owns property and ownership type rows`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)
            stubAnyoneInOwnershipChain(false)

            // Act
            val rows = ownershipRows()

            // Assert
            assertEquals(
                listOf(
                    JourneyStateService.urlWithJourneyState(OwnsPropertyStep.ROUTE_SEGMENT, "owns-change"),
                    JourneyStateService.urlWithJourneyState(OwnershipKindStep.ROUTE_SEGMENT, "kind-change"),
                ),
                rows.map { it.actions.single().url },
            )
        }

        @Test
        fun `joint freehold shows all three rows with change links to each question`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.JOINT_TENANTS)

            // Act
            val rows = ownershipRows()

            // Assert
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
        fun `ownership type requiring freeholder records shows freeholder cards and a change link`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.JOINT_TENANTS)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            assertSame(freeholderCards, content["freeholdersCards"])
            verify(mockFreeholderSummaryCardHelper).getSummaryCards(eq(freeholders), any())
            assertEquals(
                JourneyStateService.urlWithJourneyState(FreeholdersListStep.ROUTE_SEGMENT, "freeholders-change"),
                content["freeholdersChangeUrl"],
            )
            assertEquals("registerProperty.freeholders.checkAnswers.heading.joint", content["freeholdersHeadingKey"])
        }

        @Test
        fun `ownership type not requiring freeholder records has no freeholders content`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SHARE_OF_SOLE_FREEHOLD)
            stubAnyoneInOwnershipChain(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            assertNull(content["freeholdersCards"])
            assertNull(content["freeholdersHeadingKey"])
        }

        @Test
        fun `answering yes to anyone in the ownership chain shows superior landlord cards and a change link`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)
            stubAnyoneInOwnershipChain(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            assertSame(superiorLandlordCards, content["superiorLandlordsCards"])
            verify(mockSuperiorLandlordSummaryCardHelper).getSummaryCards(eq(superiorLandlords), any())
            assertEquals(
                JourneyStateService.urlWithJourneyState(SuperiorLandlordsListStep.ROUTE_SEGMENT, "list-change"),
                content["superiorLandlordsChangeUrl"],
            )
            assertNull(content["superiorLandlordsRows"])
        }

        @Test
        fun `answering no to anyone in the ownership chain shows a single no row`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.LEASEHOLD_ONLY)
            stubAnyoneInOwnershipChain(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val row = (content["superiorLandlordsRows"] as List<SummaryListRowViewModel>).single()
            assertEquals("registerProperty.superiorLandlords.checkAnswers.anyoneInOwnershipChain", row.fieldHeading)
            assertEquals("commonText.no", row.fieldValue)
            assertEquals(
                JourneyStateService.urlWithJourneyState(AnyoneInOwnershipChainStep.ROUTE_SEGMENT, "chain-change"),
                row.actions.single().url,
            )
            assertNull(content["superiorLandlordsCards"])
        }

        @Test
        fun `ownership type not requiring superior landlord info has no superior landlords content`() {
            // Arrange
            whenever(mockOwnershipTypeTask.ownershipType).thenReturn(OwnershipType.SOLE_FREEHOLD)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            assertNull(content["superiorLandlordsRows"])
            assertNull(content["superiorLandlordsCards"])
            assertNull(content["superiorLandlordsChangeUrl"])
        }

        private fun ownershipRows() = stepConfig.getStepSpecificContent(mockState)["ownershipRows"] as List<SummaryListRowViewModel>

        private fun stubAnyoneInOwnershipChain(anyoneInOwnershipChain: Boolean) {
            whenever(mockAnyoneInOwnershipChainStep.formModelIfReachableOrNull)
                .thenReturn(AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = anyoneInOwnershipChain))
        }
    }

    @Test
    fun `resolveNextDestination returns the default destination without deleting the journey`() {
        // Arrange
        val defaultDestination = Destination.Nowhere()

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertSame(defaultDestination, result)
        verify(mockState, never()).deleteJourney()
    }
}
