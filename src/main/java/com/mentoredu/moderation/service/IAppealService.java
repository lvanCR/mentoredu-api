package com.mentoredu.moderation.service;

import com.mentoredu.moderation.dto.AppealRequest;
import com.mentoredu.moderation.dto.AppealResponse;

import java.util.UUID;

public interface IAppealService {
    AppealResponse submit(AppealRequest request, String userEmail);
    AppealResponse resolveAppeal(UUID appealId, String newStatus, String moderatorEmail);
}
