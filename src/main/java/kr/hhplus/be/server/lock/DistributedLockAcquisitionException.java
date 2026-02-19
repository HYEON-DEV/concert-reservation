package kr.hhplus.be.server.lock;

public class DistributedLockAcquisitionException extends RuntimeException {

    public DistributedLockAcquisitionException(String message) {
        super(message);
    }
}
