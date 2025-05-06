package aq.metallists.freundschaft.service.events;

public class PFailureMessage {
    public boolean bIsExtraError;
    public boolean bIsAuthorizationError;

    public PFailureMessage(boolean extra, boolean auth) {
        bIsExtraError = extra;
        bIsAuthorizationError = auth;
    }

}
