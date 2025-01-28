package uk.gov.communities.prsdb.webapp.constants

import org.springframework.web.servlet.support.ServletUriComponentsBuilder

// TODO PRSD-670: Set to landlord dashboard
val LANDLORD_DASHBOARD_URL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
