package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.ElectricalSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GasSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyReactivationConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PropertyReactivationServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockActivationService: ActivationService

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockLandlordActiveStatusService: LandlordActiveStatusService

    @Mock
    private lateinit var mockSession: HttpSession

    @Mock
    private lateinit var mockCurrentUserService: CurrentUserService

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyReactivationConfirmationEmail>

    @InjectMocks
    private lateinit var propertyReactivationService: PropertyReactivationService

    private val propertyOwnershipId = 1L
    private val uprn = 2L
    private val currentLandlord = createLandlordWithId(1L)
    private val propertyOwnership =
        MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, isActive = false, landlords = mutableSetOf(currentLandlord))

    @Test
    fun `reactivatePropertyIfAddressAvailable saves the details provided and then reactivates the property`() {
        // Arrange
        val licensing = LicensingDetailsDataModel(LicensingType.NO_LICENSING, null)
        val gasSafety = GasSafetyDetailsDataModel(hasGasSupply = false, issueDate = null, uploadIds = emptyList())
        val electricalSafety =
            ElectricalSafetyDetailsDataModel(CertificateType.Eicr, LocalDate.of(2030, 1, 1), null, null, listOf(2L))
        val epc = EpcDetailsDataModel("https://epc.example/cert", LocalDate.of(2031, 1, 1), "C", null, null, null)
        val tenancyDetails =
            TenancyDetailsDataModel(
                1,
                2,
                null,
                null,
                FurnishedStatus.FURNISHED,
                RentFrequency.MONTHLY,
                null,
                BigDecimal("400"),
            )
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail()

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(
            propertyOwnershipId,
            uprn,
            PropertyReactivationDataModel(licensing, gasSafety, electricalSafety, epc, tenancyDetails),
        )

        // Assert
        inOrder(mockPropertyOwnershipService, mockPropertyComplianceService, mockActivationService) {
            verify(mockPropertyOwnershipService).saveReactivationLicensing(propertyOwnership, licensing)
            verify(mockPropertyOwnershipService).saveReactivationTenancyDetails(propertyOwnership, tenancyDetails)
            verify(mockPropertyComplianceService).saveReactivationComplianceData(propertyOwnership, gasSafety, electricalSafety, epc)
            verify(mockActivationService).reactivateProperty(propertyOwnershipId)
        }
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable doesn't save any details when the property had none to provide`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())

        // Assert
        verify(mockPropertyOwnershipService, never()).saveReactivationLicensing(any(), any())
        verify(mockPropertyOwnershipService, never()).saveReactivationTenancyDetails(any(), any())
        verifyNoInteractions(mockPropertyComplianceService)
        verify(mockActivationService).reactivateProperty(propertyOwnershipId)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable stores the property in the session as the last property reactivated`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())

        // Assert
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable throws an EntityExistsException without saving when the address is unavailable`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act & Assert
        assertThrows<EntityExistsException> {
            propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())
        }
        verifyNoInteractions(mockPropertyOwnershipService)
        verifyNoInteractions(mockPropertyComplianceService)
        verifyNoInteractions(mockActivationService)
        verifyNoInteractions(mockSession)
        verifyNoInteractions(mockConfirmationEmailSender)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable reactivates the property without checking its address when it has no UPRN`() {
        // Arrange
        arrangeConfirmationEmail()

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, null, PropertyReactivationDataModel())

        // Assert
        verify(mockActivationService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        verifyNoInteractions(mockAddressAvailabilityService)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable emails the current user a reactivation confirmation`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail()

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            eq(
                PropertyReactivationConfirmationEmail(
                    recipientName = "James",
                    multiLineAddress = propertyOwnership.address.toMultiLineAddress(),
                    registrationNumber =
                        RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                    dashboardUrl = "https://example.com/dashboard",
                    hasJointLandlords = false,
                    landlordReactivated = false,
                ),
            ),
        )
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable includes the joint landlords section in the email when the property has other landlords`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        val propertyOwnershipWithJointLandlord =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                landlords = mutableSetOf(currentLandlord, createLandlordWithId(2L)),
            )
        arrangeConfirmationEmail(propertyOwnership = propertyOwnershipWithJointLandlord)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            argThat<PropertyReactivationConfirmationEmail> { hasJointLandlords },
        )
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable includes the landlord account section in the email when the landlord was inactive`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail(isLandlordInactive = true)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())

        // Assert
        val inOrder = inOrder(mockLandlordActiveStatusService, mockActivationService)
        inOrder.verify(mockLandlordActiveStatusService).isCurrentLandlordInactive()
        inOrder.verify(mockActivationService).reactivateProperty(propertyOwnershipId)
        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            argThat<PropertyReactivationConfirmationEmail> { landlordReactivated },
        )
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable still reactivates the property when the confirmation email fails to send`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        arrangeConfirmationEmail()
        whenever(mockConfirmationEmailSender.sendEmail(any(), any())).thenThrow(RuntimeException("Notify is down"))

        // Act & Assert
        assertDoesNotThrow { propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel()) }
        verify(mockActivationService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }

    private fun arrangeConfirmationEmail(
        isLandlordInactive: Boolean = false,
        propertyOwnership: PropertyOwnership = this.propertyOwnership,
    ) {
        whenever(mockLandlordActiveStatusService.isCurrentLandlordInactive()).thenReturn(isLandlordInactive)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(this.propertyOwnership)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(currentLandlord)
        whenever(mockCurrentUserService.getRegisteredUserDetails())
            .thenReturn(CurrentUserService.RegisteredUserDetails(name = "James", email = "james@example.com"))
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/dashboard"))
    }

    private fun createLandlordWithId(id: Long) =
        MockLandlordData.createIndividualLandlord().also { ReflectionTestUtils.setField(it, "id", id) }
}
