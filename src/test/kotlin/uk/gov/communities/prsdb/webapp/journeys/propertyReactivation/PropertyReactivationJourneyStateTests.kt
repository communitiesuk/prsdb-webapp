package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.ELECTRICAL_SAFETY
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.EPC
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.GAS_SAFETY
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.LICENSING
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.TENANCY_DETAILS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask

class PropertyReactivationJourneyStateTests {
    private val state: PropertyReactivationJourneyState = mock(defaultAnswer = Answers.CALLS_REAL_METHODS)

    @Test
    fun `compliance certificates are not missing when their tasks are not required`() {
        doReturn(listOf(LICENSING, TENANCY_DETAILS)).whenever(state).requiredTaskTypes

        assertFalse(state.isGasCertMissing)
        assertFalse(state.isElectricalCertMissing)
        assertFalse(state.isEpcMissing)
    }

    @Test
    fun `isGasCertMissing is true when gas safety is required and the certificate is missing`() {
        doReturn(listOf(GAS_SAFETY)).whenever(state).requiredTaskTypes
        val hasGasSupplyStep = mock<HasGasSupplyStep>()
        whenever(hasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.HAS_SUPPLY)
        val detailsTask = mock<GasSafetyDetailsTask>()
        whenever(detailsTask.hasGasSupplyStep).thenReturn(hasGasSupplyStep)
        whenever(detailsTask.getGasSafetyCertificateIsOutdated()).thenReturn(null)
        val gasSafetyTask = mock<GasSafetyTask>()
        whenever(gasSafetyTask.gasSafetyDetailsTask).thenReturn(detailsTask)
        doReturn(gasSafetyTask).whenever(state).gasSafetyTask

        assertTrue(state.isGasCertMissing)
    }

    @Test
    fun `isElectricalCertMissing is true when electrical safety is required and the certificate is missing`() {
        doReturn(listOf(ELECTRICAL_SAFETY)).whenever(state).requiredTaskTypes
        val detailsTask = mock<ElectricalSafetyDetailsTask>()
        whenever(detailsTask.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
        whenever(detailsTask.getElectricalCertificateIsOutdated()).thenReturn(null)
        val electricalSafetyTask = mock<ElectricalSafetyTask>()
        whenever(electricalSafetyTask.electricalSafetyDetailsTask).thenReturn(detailsTask)
        doReturn(electricalSafetyTask).whenever(state).electricalSafetyTask

        assertTrue(state.isElectricalCertMissing)
    }

    @Test
    fun `isEpcMissing is true when EPC is required and the EPC is missing`() {
        doReturn(listOf(EPC)).whenever(state).requiredTaskTypes
        val epcExemptionStep = mock<EpcExemptionStep>()
        whenever(epcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        val detailsTask = mock<EpcDetailsTask>()
        whenever(detailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
        whenever(detailsTask.acceptedEpcIfStillAccepted).thenReturn(null)
        whenever(detailsTask.epcExemptionStep).thenReturn(epcExemptionStep)
        val epcTask = mock<EpcTask>()
        whenever(epcTask.epcDetailsTask).thenReturn(detailsTask)
        doReturn(epcTask).whenever(state).epcTask

        assertTrue(state.isEpcMissing)
    }
}
