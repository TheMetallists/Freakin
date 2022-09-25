package aq.metallists.freundschaft.overridable;

public class FSUnauthorizedError extends Exception {
    public FSUnauthorizedError(String authorization_error) {
        super(authorization_error);
    }
}
