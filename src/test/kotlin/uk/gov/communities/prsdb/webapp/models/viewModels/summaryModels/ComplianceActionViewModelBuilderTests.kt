package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ComplianceActionViewModelBuilderTests {
    @Test
    fun `fromDataModel returns a SummaryCardViewModel with the correct title and summary list for an occupied property`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.EXPIRED,
                isComplete = true,
                isOccupied = true,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        assertEquals(dataModel.singleLineAddress, viewModel.title)

        val expectedSummaryList =
            listOf(
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.may26redesign.registrationNumber",
                    dataModel.registrationNumber,
                ),
                SummaryListRowViewModel(
                    fieldHeading = "complianceActions.summaryRow.may26redesign.status",
                    fieldValue = "complianceActions.summaryRow.may26redesign.occupied",
                    tagColour = "pink",
                ),
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.may26redesign.electricalSafety",
                    MessageKeyConverter.convert(dataModel.eicrStatus),
                ),
                SummaryListRowViewModel(
                    "complianceActions.summaryRow.may26redesign.energyPerformance",
                    MessageKeyConverter.convert(dataModel.epcStatus),
                ),
            )
        assertEquals(expectedSummaryList, viewModel.summaryList)
    }

    @Test
    fun `fromDataModel includes status row with pink tag when includeStatusRow is true and property is occupied`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.ADDED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.EXPIRED,
                isComplete = true,
                isOccupied = true,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        val statusRow = viewModel.summaryList[1]
        assertEquals("complianceActions.summaryRow.may26redesign.status", statusRow.fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.occupied", statusRow.fieldValue)
        assertEquals("pink", statusRow.tagColour)
    }

    @Test
    fun `fromDataModel includes status row with grey tag when includeStatusRow is true and property is unoccupied`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = false,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        val statusRow = viewModel.summaryList[1]
        assertEquals("complianceActions.summaryRow.may26redesign.status", statusRow.fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.unoccupied", statusRow.fieldValue)
        assertEquals("grey", statusRow.tagColour)
    }

    @Test
    fun `fromDataModel only shows expired cert rows for vacant properties`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.NOT_ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = false,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        assertEquals(3, viewModel.summaryList.size)
        assertEquals("complianceActions.summaryRow.may26redesign.registrationNumber", viewModel.summaryList[0].fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.status", viewModel.summaryList[1].fieldHeading)
        assertEquals("complianceActions.summaryRow.may26redesign.gasSafety", viewModel.summaryList[2].fieldHeading)
    }

    @Test
    fun `fromDataModel returns a SummaryCardViewModel with goToProperty action`() {
        val dataModel =
            ComplianceStatusDataModel(
                propertyOwnershipId = 1L,
                singleLineAddress = "123 Test Street",
                registrationNumber = "P-XXXX-XXXX",
                gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                eicrStatus = ComplianceCertStatus.ADDED,
                epcStatus = ComplianceCertStatus.ADDED,
                isComplete = true,
                isOccupied = true,
            )

        val viewModel = ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(dataModel)

        assertEquals(1, viewModel.actions?.size)
        assertEquals("complianceActions.action.goToProperty", viewModel.actions?.first()?.text)
        assertEquals(
            PropertyDetailsController.getPropertyCompliancePath(dataModel.propertyOwnershipId),
            viewModel.actions?.first()?.url,
        )
    }

    @Nested
    inner class GasSafetyCertRowTests {
        private val provideLaterDeadline = LocalDate.of(2025, 6, 15)
        private val gasSafetyExpiryDate = LocalDate.of(2025, 3, 1)

        private fun buildDataModel(
            gasSafetyStatus: ComplianceCertStatus,
            isOccupied: Boolean,
            provideLaterDeadline: LocalDate? = null,
            gasSafetyExpiryDate: LocalDate? = null,
        ) = ComplianceStatusDataModel(
            propertyOwnershipId = 1L,
            singleLineAddress = "123 Test Street",
            registrationNumber = "P-XXXX-XXXX",
            gasSafetyStatus = gasSafetyStatus,
            eicrStatus = ComplianceCertStatus.ADDED,
            epcStatus = ComplianceCertStatus.ADDED,
            isComplete = true,
            isOccupied = isOccupied,
            provideLaterDeadline = provideLaterDeadline,
            gasSafetyExpiryDate = gasSafetyExpiryDate,
        )

        private fun getGasSafetyRow(viewModel: SummaryCardViewModel) =
            viewModel.summaryList.find { it.fieldHeading == "complianceActions.summaryRow.may26redesign.gasSafety" }

        @Test
        fun `occupied property with provide later status shows gas row with provide later message`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.PROVIDE_LATER,
                        isOccupied = true,
                        provideLaterDeadline = provideLaterDeadline,
                    ),
                )

            val gasSafetyRow = getGasSafetyRow(viewModel)
            assertNotNull(gasSafetyRow)
            assertEquals("complianceActions.status.provideLater.may26Redesign", gasSafetyRow.fieldValue)
            assertEquals(
                provideLaterDeadline.format(ComplianceActionViewModelBuilderMay26Redesign.DATE_FORMATTER),
                gasSafetyRow.optionalFieldValueParam,
            )
        }

        @Test
        fun `unoccupied property with provide later status does not show gas row`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.PROVIDE_LATER,
                        isOccupied = false,
                        provideLaterDeadline = provideLaterDeadline,
                    ),
                )

            assertNull(getGasSafetyRow(viewModel))
        }

        @Test
        fun `occupied property with expired cert shows gas row with expired message and date`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                        isOccupied = true,
                        gasSafetyExpiryDate = gasSafetyExpiryDate,
                    ),
                )

            val gasSafetyRow = getGasSafetyRow(viewModel)
            assertNotNull(gasSafetyRow)
            assertEquals("complianceActions.status.expired.may26Redesign", gasSafetyRow.fieldValue)
            assertEquals(
                gasSafetyExpiryDate.format(ComplianceActionViewModelBuilderMay26Redesign.DATE_FORMATTER),
                gasSafetyRow.optionalFieldValueParam,
            )
        }

        @Test
        fun `unoccupied property with expired cert shows gas row with expired message and date`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.EXPIRED,
                        isOccupied = false,
                        gasSafetyExpiryDate = gasSafetyExpiryDate,
                    ),
                )

            val gasSafetyRow = getGasSafetyRow(viewModel)
            assertNotNull(gasSafetyRow)
            assertEquals("complianceActions.status.expired.may26Redesign", gasSafetyRow.fieldValue)
            assertEquals(
                gasSafetyExpiryDate.format(ComplianceActionViewModelBuilderMay26Redesign.DATE_FORMATTER),
                gasSafetyRow.optionalFieldValueParam,
            )
        }

        @Test
        fun `occupied property with no certificate shows gas row with not added message`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.NOT_ADDED,
                        isOccupied = true,
                    ),
                )

            val gasSafetyRow = getGasSafetyRow(viewModel)
            assertNotNull(gasSafetyRow)
            assertEquals("complianceActions.status.notAdded.may26Redesign.gasSafety", gasSafetyRow.fieldValue)
            assertNull(gasSafetyRow.optionalFieldValueParam)
        }

        @Test
        fun `unoccupied property with no certificate does not show gas row`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.NOT_ADDED,
                        isOccupied = false,
                    ),
                )

            assertNull(getGasSafetyRow(viewModel))
        }

        @Test
        fun `occupied property with valid cert does not show gas row`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.ADDED,
                        isOccupied = true,
                    ),
                )

            assertNull(getGasSafetyRow(viewModel))
        }

        @Test
        fun `unoccupied property with valid cert does not show gas row`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.ADDED,
                        isOccupied = false,
                    ),
                )

            assertNull(getGasSafetyRow(viewModel))
        }

        @Test
        fun `occupied property with no gas does not show gas row`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.NOT_REQUIRED,
                        isOccupied = true,
                    ),
                )

            assertNull(getGasSafetyRow(viewModel))
        }

        @Test
        fun `unoccupied property with no gas does not show gas row`() {
            val viewModel =
                ComplianceActionViewModelBuilderMay26Redesign.fromDataModel(
                    buildDataModel(
                        gasSafetyStatus = ComplianceCertStatus.NOT_REQUIRED,
                        isOccupied = false,
                    ),
                )

            assertNull(getGasSafetyRow(viewModel))
        }
    }
}
