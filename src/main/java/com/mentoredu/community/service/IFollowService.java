package com.mentoredu.community.service;

import java.util.UUID;

public interface IFollowService {
    /**
     * Alterna el seguimiento de un usuario. Si ya lo sigue, lo deja de seguir.
     * @param targetUserId ID del usuario a seguir/dejar de seguir.
     * @param followerEmail Email del usuario que realiza la acción (extraído del JWT).
     * @return true si se empezó a seguir, false si se dejó de seguir.
     */
    boolean toggleFollow(UUID targetUserId, String followerEmail);
}
