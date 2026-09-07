package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.mock.web.MockHttpServletRequest
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipAndLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.WhoProvidesDetailsTask
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRegistrationTaskListStepConfigTests {
    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    lateinit var mockBackUrlStorageService: BackUrlStorageService

    @Mock
    lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    lateinit var mockCyaStep: PropertyRegistrationCyaStep

    private lateinit var request: MockHttpServletRequest
    private lateinit var stepConfig: PropertyRegistrationTaskListStepConfig

    @BeforeEach
    fun setUp() {
        request = MockHttpServletRequest()
        stepConfig = PropertyRegistrationTaskListStepConfig(mockFeatureFlagManager, request, mockBackUrlStorageService)
    }

    @Test
    fun `chooseTemplate returns taskList`() {
        assertEquals("taskList", stepConfig.chooseTemplate(mockState))
    }

    @Nested
    inner class WhoProvidesDetailsTaskListItemTests {
        @BeforeEach
        fun enableRestructureAndStubState() {
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
            stubRestructuredState()
        }

        @Test
        fun `getTaskListViewModel excludes the who provides details task when DELEGATE_TO_LETTING_AGENT is disabled`() {
            // Arrange
            whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)
            whenever(mockState.cachedOccupied).thenReturn(true)

            // Act
            val taskListViewModel = stepConfig.getTaskListViewModel(mockState)

            // Assert
            val rentedOutNames = taskListViewModel.taskSections[1].tasks.map { it.nameKey }
            assert("registerProperty.taskList.rentedOut.whoProvidesDetails" !in rentedOutNames)
        }

        @Test
        fun `getTaskListViewModel shows who provides details as NOT_NEEDED_YET for an unoccupied property`() {
            // Arrange
            whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
            whenever(mockState.cachedOccupied).thenReturn(false)

            // Act
            val taskListViewModel = stepConfig.getTaskListViewModel(mockState)

            // Assert
            val whoProvidesItem =
                taskListViewModel.taskSections[1].tasks.find {
                    it.nameKey == "registerProperty.taskList.rentedOut.whoProvidesDetails"
                }
            assertEquals("taskList.status.notNeededYet", whoProvidesItem?.status?.textKey)
            assertEquals("registerProperty.taskList.rentedOut.whoProvidesDetailsNotRequiredHint", whoProvidesItem?.hintKey)
            assertNull(whoProvidesItem?.url)
        }

        @Test
        fun `getTaskListViewModel shows who provides details as a task for an occupied property`() {
            // Arrange
            whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
            whenever(mockState.cachedOccupied).thenReturn(true)

            // Act
            val taskListViewModel = stepConfig.getTaskListViewModel(mockState)

            // Assert
            val whoProvidesItem =
                taskListViewModel.taskSections[1].tasks.find {
                    it.nameKey == "registerProperty.taskList.rentedOut.whoProvidesDetails"
                }
            assertEquals("taskList.status.cannotStart", whoProvidesItem?.status?.textKey)
        }
    }

    @Test
    fun `getTaskListViewModel shows tenancy details as NOT_REQUIRED for an unoccupied property when delegation is disabled`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)
        whenever(mockState.cachedOccupied).thenReturn(false)
        stubRestructuredState()

        // Act
        val taskListViewModel = stepConfig.getTaskListViewModel(mockState)

        // Assert
        val tenancyItem =
            taskListViewModel.taskSections[1].tasks.find {
                it.nameKey == "registerProperty.taskList.rentedOut.tenancyDetails"
            }
        assertEquals("taskList.status.notRequired", tenancyItem?.status?.textKey)
        assertEquals("registerProperty.taskList.rentedOut.tenancyDetailsNotRequiredHint", tenancyItem?.hintKey)
        assertNull(tenancyItem?.url)
    }

    @Test
    fun `getTaskListViewModel shows tenancy details as NOT_NEEDED_YET for an unoccupied property when delegation is enabled`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockState.cachedOccupied).thenReturn(false)
        stubRestructuredState()

        // Act
        val taskListViewModel = stepConfig.getTaskListViewModel(mockState)

        // Assert
        val tenancyItem =
            taskListViewModel.taskSections[1].tasks.find {
                it.nameKey == "registerProperty.taskList.rentedOut.tenancyDetails"
            }
        assertEquals("taskList.status.notNeededYet", tenancyItem?.status?.textKey)
        assertEquals("registerProperty.taskList.rentedOut.tenancyDetailsNotNeededYetHint", tenancyItem?.hintKey)
        assertNull(tenancyItem?.url)
    }

    @Test
    fun `getTaskListViewModel shows tenancy details task for an occupied property`() {
        // Arrange
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)
        whenever(mockState.cachedOccupied).thenReturn(true)
        stubRestructuredState()

        // Act
        val taskListViewModel = stepConfig.getTaskListViewModel(mockState)

        // Assert
        val tenancyItem =
            taskListViewModel.taskSections[1].tasks.find {
                it.nameKey == "registerProperty.taskList.rentedOut.tenancyDetails"
            }
        assertEquals("taskList.status.cannotStart", tenancyItem?.status?.textKey)
        assertNull(tenancyItem?.url)
    }

    private fun stubRestructuredState() {
        val mockPropertyDetailsTask = mock<PropertyDetailsTask>()
        val mockOwnershipAndLandlordsTask = mock<OwnershipAndLandlordsTask>()
        val mockLicensingTask = mock<LicensingTask>()
        val mockTenancyDetailsTask = mock<TenancyDetailsTask>()
        val mockWhoProvidesDetailsTask = mock<WhoProvidesDetailsTask>()
        val mockGasSafetyTask = mock<GasSafetyTask>()
        val mockElectricalSafetyTask = mock<ElectricalSafetyTask>()
        val mockEpcTask = mock<EpcTask>()

        whenever(mockPropertyDetailsTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockOwnershipAndLandlordsTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockLicensingTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockTenancyDetailsTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockWhoProvidesDetailsTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockGasSafetyTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockElectricalSafetyTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockEpcTask.taskStatus()).thenReturn(TaskStatus.CANNOT_START)
        whenever(mockOccupiedStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockCyaStep.currentJourneyId).thenReturn("test-journey-id")

        whenever(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        whenever(mockState.ownershipAndLandlordsTask).thenReturn(mockOwnershipAndLandlordsTask)
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockState.licensingTask).thenReturn(mockLicensingTask)
        whenever(mockState.tenancyDetailsTask).thenReturn(mockTenancyDetailsTask)
        whenever(mockState.whoProvidesDetailsTask).thenReturn(mockWhoProvidesDetailsTask)
        whenever(mockState.gasSafetyTask).thenReturn(mockGasSafetyTask)
        whenever(mockState.electricalSafetyTask).thenReturn(mockElectricalSafetyTask)
        whenever(mockState.epcTask).thenReturn(mockEpcTask)
        whenever(mockState.cyaStep).thenReturn(mockCyaStep)
    }
}
