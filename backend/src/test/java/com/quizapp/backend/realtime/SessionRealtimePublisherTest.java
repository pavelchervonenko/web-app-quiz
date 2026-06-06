package com.quizapp.backend.realtime;

import com.quizapp.backend.dto.session.LeaderboardDTO;
import com.quizapp.backend.dto.session.SessionStateDTO;
import com.quizapp.backend.model.enums.QuizSessionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class SessionRealtimePublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @AfterEach
    public void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void testPublishStateWithoutTransactionSendsImmediately() {
        var publisher = new SessionRealtimePublisher(messagingTemplate);
        var state = state();

        publisher.publishState("AB12CD", state);

        verify(messagingTemplate).convertAndSend("/topic/sessions/AB12CD/state", state);
    }

    @Test
    public void testPublishStateWithTransactionSendsAfterCommit() {
        var publisher = new SessionRealtimePublisher(messagingTemplate);
        var state = state();

        TransactionSynchronizationManager.initSynchronization();

        publisher.publishState("AB12CD", state);

        verifyNoInteractions(messagingTemplate);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(messagingTemplate).convertAndSend("/topic/sessions/AB12CD/state", state);
    }

    private SessionStateDTO state() {
        UUID sessionId = UUID.randomUUID();

        return new SessionStateDTO(
            sessionId,
            "AB12CD",
            QuizSessionStatus.WAITING,
            null,
            new LeaderboardDTO(sessionId, QuizSessionStatus.WAITING, List.of())
        );
    }
}
