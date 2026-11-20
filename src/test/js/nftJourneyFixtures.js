export const uuidFor = i => `00000000-0000-4000-8000-${String(i).padStart(12, '0')}`;

export const journeyFixtures = (family, count = family === 'paid-registration' ? 456 :
  family === 'council-read' || family.startsWith('organisation-team') ? 17 : 28) => Array.from({length: count}, (_, i) => {
  const row = {ownerSubject: `owner-${i}`, propertyOwnershipId: i + 1, address: `${i} Fixture Road`, files: []};
  if (family === 'joint' || family === 'joint-accept') Object.assign(row, {inviteeSubject: `invitee-${i}`, invitedEmail: `joint-${i}@example.test`});
  if (family === 'joint-accept') Object.assign(row, {token: uuidFor(i), invitationId: i + 1});
  if (family === 'agent-access') Object.assign(row, {token: uuidFor(i), invitedEmail: `agent-${i}@example.test`, hasPassword: false});
  if (family === 'offboarding') Object.assign(row, {subject: `owner-${i}`, action: 'deregister-property'});
  if (family === 'organisation-team-ramp-preparation' || family === 'organisation-team-preparation') {
    return {subject: `admin-${i}`, name: `Organisation ${i}`, organisationId: i + 1, registrationNumber: i + 100};
  }
  if (family.startsWith('organisation-team')) {
    return {
      subject: `admin-${i}`, name: `Organisation ${i}`, organisationId: i + 1, registrationNumber: i + 100,
      invitationToken: uuidFor(i), invitedEmail: `team-${i}@example.test`,
    };
  }
  if (family === 'operator') {
    return {
      subject: `operator-${i}`, councilName: 'Council', councilId: 1, targetId: i + 1, targetName: `Target ${i}`,
      targetEmail: `target-${i}@example.test`, targetSubject: `target-${i}`, targetManager: false,
    };
  }
  if (family === 'paid-registration') {
    return {
      subject: `paid-${i}`, address: `${i + 1} Registration Road, FA1 1AA`, postcode: 'FA1 1AA',
      building: String(i + 1), localCouncilId: 1, uprn: 9200000001 + i,
    };
  }
  if (family === 'council-read') {
    return {
      subject: `council-${i}`, actorName: `Council user ${i}`, councilId: i + 1, councilName: `Council ${i}`,
      targetId: i + 100, targetName: `Target ${i}`, targetEmail: `target-${i}@example.test`,
      targetSubject: `target-${i}`, targetManager: true, propertyId: i + 100027,
      address: `${i} Council Road, FA1 1AA`, uprn: String(i + 2000), propertyRegistrationNumber: i + 3000,
      landlordId: i + 4000, landlordName: `Landlord ${i}`, landlordRegistrationNumber: i + 5000, licenseType: null,
    };
  }
  return row;
});
