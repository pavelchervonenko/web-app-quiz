package com.quizapp.backend.dto.session;

import java.util.UUID;

public record LeaderboardEntryDTO(
    Integer rank,
    UUID participantSessionId,
    String displayName,
    Integer score
) {
}
