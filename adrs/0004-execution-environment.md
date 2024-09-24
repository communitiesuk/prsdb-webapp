# ADR-0004: Execution Environment

## Status

Accepted

Date of decision: 2024-08-23

## Context and Problem Statement

ADR-0001 established that AWS will be the hosting platform for the Private Rented Sector Database (PRSDB), but AWS
offers many “compute” options – i.e. how an application is executed.

Which of the AWS compute services will we use?

The traffic patterns of PRSDB are not currently well known, but the current assumption is that they should be relatively
stable (with higher use in the period after launch, as landlords initially register).

As a very rough estimate of volume, we might expect 400,000 monthly active users (MAU), based on there being
approximately 2,300,000 landlords in the UK, and that they (or a letting agent on their behalf) may use the service an
average of twice a year (plus smaller numbers of local authority users using the service more regularly).

## Considered Options

* AWS Lambda
* AWS ECS + Fargate
* AWS EKS + Fargate
* AWS EC2 + Docker

## Decision Outcome

AWS ECS + Fargate, because it balances simplicity with flexibility, and it is commonly used within MHCLG.

## Pros and Cons of the Options

### AWS Lambda

AWS Lambda is a “serverless” compute service, aka a function-as-a-service platform. In this model, code is broken down
into small components (functions), each of which is run in response to an event (e.g. an API request).
* Good, because all server management and scaling concerns are abstracted away.
* Bad, because Lambdas see limited use within MHCLG.
* Bad, because Lambdas suffer from cold start delays, especially for relatively infrequently used services.
* Bad, because Lambdas cannot execute long-running processes (over 15 minutes) - although note it is not expected that
  PRSDB will require such processes.
* Bad, because Lambdas are AWS-specific, making any potential future move to a different cloud provider much more
  difficult – although note that no such move is expected to occur.

### AWS ECS + Fargate

AWS Elastic Container Service (ECS) is “a fully managed container orchestration service”. It is a control plane,
responsible for managing the lifecycle of containers (e.g. Docker containers). AWS Fargate is a complementary service,
“a serverless, pay-as-you-go compute engine” - i.e. where the containers actually run.
* Good, because it is fully managed.
* Good, because this is a commonly used pattern within MHCLG.
* Good, because it is the simplest service AWS offers for container orchestration.
* Bad, because it abstracts away some details and therefore some control.
* Bad, because although autoscaling is possible, it is not as responsive as Lambdas.

### AWS EKS + Fargate

AWS Elastic Kubernetes Service (EKS) is “a managed Kubernetes service to run Kubernetes in the AWS cloud.” Like ECS, it
is a control plane that manages containers, so needs to be paired with a compute engine. As in the above option, Fargate
is proposed.
* Good, because it is managed.
* Bad, because we are not aware of other MHCLG services using this pattern.
* Bad, because Kubernetes is relatively complex to configure and manage.
* Good, because Kubernetes is powerful (although PRSDB is not expected to require this level of configuration / power).
* Bad, because although autoscaling is possible, it is not as responsive as Lambdas.

### AWS EC2 + Docker

AWS Elastic Compute Cloud (EC2) is an IaaS computer platform, providing access to virtual machines hosted in the AWS
cloud. Docker can be deployed to an EC2 instance, and containerised applications (e.g. PRSDB service(s)) can be executed
there.

* Bad, because it is not fully managed (increasing the operational burden on MHCLG).
* Bad, because this pattern sees limited use with MHCLG. 
* Bad, because although autoscaling is possible, it is not as responsive as Lambdas.

## More Information

* Review of the MHCLG technical landscape: [DLUHC Tech Landscape Review.docx](https://mhclg.sharepoint.com/:w:/s/PrivateRentedSector/EZp45cVALmBDl-MmTf5gd9cBajXyR87tPoGDom_OZFiMgg?e=GgSSh6)
* Estimate of the number of landlords in England: https://www.gov.uk/government/publications/a-fairer-private-rented-sector/a-fairer-private-rented-sector#fn:36  