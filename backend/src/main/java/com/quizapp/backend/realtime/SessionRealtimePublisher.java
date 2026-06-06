package com.quizapp.backend.realtime;

import com.quizapp.backend.dto.session.SessionStateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class SessionRealtimePublisher {

    private static final String SESSION_STATE_DESTINATION_PREFIX = "/topic/sessions/";
    private static final String SESSION_STATE_DESTINATION_SUFFIX = "/state";

    private final SimpMessagingTemplate messagingTemplate;

    public void publishState(String roomCode, SessionStateDTO state) {
        Runnable publishTask = () -> messagingTemplate.convertAndSend(
            buildStateDestination(roomCode),
            state
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    publishTask.run();
                }
            });
            return;
        }

        publishTask.run();
    }

    private String buildStateDestination(String roomCode) {
        return SESSION_STATE_DESTINATION_PREFIX + roomCode + SESSION_STATE_DESTINATION_SUFFIX;
    }
}
