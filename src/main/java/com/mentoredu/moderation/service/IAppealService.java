package com.mentoredu.moderation.service;

import com.mentoredu.moderation.dto.AppealRequest;
import com.mentoredu.moderation.dto.AppealResponse;

public interface IAppealService {
    AppealResponse submit(AppealRequest request, String userEmail);
}
