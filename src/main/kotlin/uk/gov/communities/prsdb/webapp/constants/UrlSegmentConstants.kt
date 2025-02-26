package uk.gov.communities.prsdb.webapp.constants

import org.springframework.web.servlet.support.ServletUriComponentsBuilder

const val UPDATE_PATH_SEGMENT = "update"
const val DETAILS_PATH_SEGMENT = "details"
const val LANDLORD_DETAILS_PATH_SEGMENT = "landlord-details"
const val LOCAL_AUTHORITY_PATH_SEGMENT = "local-authority"
const val LANDLORD_PATH_SEGMENT = "landlord"
const val PROPERTY_PATH_SEGMENT = "property"
const val DASHBOARD_PATH_SEGMENT = "dashboard"
const val SEARCH_PATH_SEGMENT = "search"

// TODO PRSD-670: Set to landlord dashboard
val LANDLORD_DASHBOARD_URL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
