package com.github.fjbaldon.attendex.platform.capture;

import com.github.fjbaldon.attendex.platform.event.SessionDeletedEvent;
import com.github.fjbaldon.attendex.platform.event.SessionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
class CaptureSessionEventListener {

    private final CaptureFacade captureFacade;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionUpdated(SessionUpdatedEvent event) {
        captureFacade.recalculateSessionPunctuality(
                event.sessionId(),
                event.targetTime(),
                event.graceMinutesBefore(),
                event.graceMinutesAfter()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionDeleted(SessionDeletedEvent event) {
        captureFacade.reassignEntriesForDeletedSession(
                event.deletedSessionId(),
                event.remainingSessions()
        );
    }
}
