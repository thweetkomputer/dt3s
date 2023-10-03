// Chen Zhao 1427714
package common;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class MyReentrantLock extends ReentrantLock {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Logger.getLogger(MyReentrantLock.class.getName());

    @Override
    public void lock() {
        super.lock();
        if (!DEBUG) {
            return;
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 2) {
            StackTraceElement element = stackTraceElements[2];
            LOGGER.info("Lock acquired at " + element.toString());
        } else {
            LOGGER.info("Lock acquired but could not determine location");
        }
    }

    @Override
    public void unlock() {
        super.unlock();
        if (!DEBUG) {
            return;
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 2) {
            StackTraceElement element = stackTraceElements[2];
            LOGGER.info("Lock released at " + element.toString());
        } else {
            LOGGER.info("Lock released but could not determine location");
        }
    }
}
