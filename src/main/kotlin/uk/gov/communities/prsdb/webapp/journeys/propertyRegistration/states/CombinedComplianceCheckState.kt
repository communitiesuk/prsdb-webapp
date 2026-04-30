package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

interface CombinedComplianceCheckState :
    GasSafetyState,
    ElectricalSafetyState,
    EpcState
