package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Test

class PropertyComplianceCyaStepConfigTests {
    // To test "including only data from relevant steps", populate the state as though the user has added answers for one branch of steps
    // then gone back and changed an answer to take the other branch.
    // E.g. added a gas safety certificate then gone back and added an exemption - the cya should only show the exemption,
    // and the created property compliance record should only include the exemption data, not the certificate data.
    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance record with valid certificates including only data from relevant steps`() {
        // Gas safety: has valid certificate
        // Eicr: has valid certificate
        // Epc: has valid certificate, in date, good energy rating
    }

    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance record with exemptions including only data from relevant steps`() {
        // Gas Safety: has exemption (with "other" reason)
        // Eicr: has exemption (with "other" reason)
        // Epc: has epc exemption
    }

    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance with mees exemption and epc expiry data`() {
        // Epc: expired with low energy rating, tenancyStartedBeforeExpiry true, has a mees exemption
    }

    @Test
    fun `afterStepDataIsAdded sends a FullPropertyComplianceConfirmationEmail for a fully compliant property`() {
        // nonCompliantMsgKeys will return empty
    }

    @Test
    fun `afterStepDataIsAdded sends a FullPropertyComplianceConfirmationEmail for a property which is not fully compliant`() {
        // nonCompliantMsgKeys will return not empty
    }

    @Test
    fun `afterStepDataIsAdded adds the propertyId to the session`() {
    }

    @Test
    fun `afterStepDataIsAdded deletes the incomplete property compliance from the database`() {
    }
}
