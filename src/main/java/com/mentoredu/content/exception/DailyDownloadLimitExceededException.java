package com.mentoredu.content.exception;

public class DailyDownloadLimitExceededException extends RuntimeException {
    public DailyDownloadLimitExceededException() {
        super("Has alcanzado tu límite diario. Puedes esperar a mañana o canjear monedas virtuales.");
    }
}
