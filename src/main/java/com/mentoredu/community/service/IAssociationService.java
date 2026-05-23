package com.mentoredu.community.service;

import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;

import java.util.List;
import java.util.UUID;

public interface IAssociationService {
    AssociationResponse requestAssociation(CreateAssociationRequest request, String teacherEmail);
    List<AssociationResponse> getMyAssociations(String teacherEmail);
    List<AssociationResponse> getAcademyRequests(String academyEmail);
    AssociationResponse acceptAssociation(UUID linkId, String academyEmail);
    AssociationResponse rejectAssociation(UUID linkId, String academyEmail);
}
