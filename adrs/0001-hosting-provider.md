# ADR-0001: Hosting Provider

## Status

Accepted

Date of decision: 2024-08-21

## Context and Problem Statement

The Private Rented Sector Database service(s) will need to be hosted somewhere accessible to all its users (including 
the public, landlords, letting agents, and local authority staff).

Which hosting provider should we select?

## Considered Options

* AWS
* Azure
* GCP
* Private data centre hosting

## Decision Outcome

AWS, because it aligns with the government Technology Code of Practice (TCoP) and is the most widely used cloud service 
provider, both within MHCLG and globally.

## Pros and Cons of the Options

### AWS

Amazon Web Services is estimated to be the global leading cloud service provider (31% market share as of 2024 Q1), 
providing both infrastructure (e.g. EC2) and managed services (e.g. RDS).
* Good, because it aligns with TCoP point 5: Use cloud first.
* Good, because it is used by many other MHCLG projects.
* Good, because it is very commonly used in general.

### Azure

Azure is Microsoft’s cloud service offering and is estimated to be the second largest cloud service provider (25% market
share as of 2024 Q1). Like AWS, it offers both infrastructure (e.g. Azure Virtual Machines) and managed services (e.g.
Azure SQL).
* Good, because it aligns with TCoP point 5: Use cloud first.
* Bad, because it is not widely used within MHCLG.
* Good, because it is reasonably commonly used in general.

### GCP

Google Cloud Platform is estimated to be the third largest cloud service provider, although some way behind AWS and
Azure (11% market share as of 2024 Q1). It also offers infrastructure (e.g. Compute Engine) and managed services (e.g.
App Engine).
* Good, because it aligns with TCoP point 5: Use cloud first.
* Bad, because it is not widely used within MHCLG.
* Bad, because it is significantly less widely used than AWS and Azure.

### Private data centre hosting

Collocated data centre providers offer managed infrastructure. There are many such providers in the UK.
* Bad, because it does not align with TCoP point 5: Use cloud first.
* Bad, because it is not widely used within MHCLG.
* Bad, because it does not offer managed services (meaning an increased operation burden for MHCLG).

## More Information

* Statista information of cloud service provider market share:
  https://www.statista.com/chart/18819/worldwide-market-share-of-leading-cloud-infrastructure-service-providers/
* GDS Technology Code of Practice: https://www.gov.uk/guidance/the-technology-code-of-practice
* Review of the MHCLG technical landscape: [DLUHC Tech Landscape Review.docx](https://mhclg.sharepoint.com/:w:/s/PrivateRentedSector/EZp45cVALmBDl-MmTf5gd9cBajXyR87tPoGDom_OZFiMgg?e=GgSSh6) 