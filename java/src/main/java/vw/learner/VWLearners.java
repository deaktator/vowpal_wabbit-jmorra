package vw.learner;

import vw.jni.NativeUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is the only entrance point to create a VWLearner.  It is the responsibility of the user to supply the type they want
 * given the VW command.  If that type is incorrect a {@link java.lang.ClassCastException} is thrown.
 * @author jmorra
 */
final public class VWLearners {
    private volatile static boolean loadedNativeLibrary = false;
    private static final Lock STATIC_LOCK = new ReentrantLock();

//    private static final Set<Long> openPointers = Collections.synchronizedSet(new LinkedHashSet<Long>());
    private static final ConcurrentHashMap<Long, String> openPointers = new ConcurrentHashMap<Long, String>();

    static {
        Runtime.getRuntime().addShutdownHook(new Closer());
    }

    enum VWReturnType {
        Unknown, VWFloatType, VWIntType, VWIntArrayType, VWFloatArrayType
    }

    private VWLearners() {}

    public static Map<Long, String> getOpenPointers() {
        return new HashMap<Long, String>(openPointers);
    }

//    public static Set<Long> getOpenPointers() {
//        return new LinkedHashSet<Long>(openPointers);
//    }

    /**
     * This is the only way to construct a VW Predictor.  The goal here is to provide a typesafe way of getting an predictor
     * which will return the correct output type given the command specified.
     * <pre>
     * {@code
     *     VWIntLearner vw = VWLearners.create("--cb 4");
     * }
     * </pre>
     * @param command The VW initialization command.
     * @param <T> The type of learner expected.  Note that this type implicitly specifies the output type of the learner.
     * @throws ClassCastException If the specified type T is not a super type of the returned learner given the command.
     * @return A VW Learner
     */
    @SuppressWarnings("unchecked")
    public static <T extends VWLearner> T create(final String command) {
        long nativePointer = initializeVWJni(command);
        VWReturnType returnType = getReturnType(nativePointer);

        switch (returnType) {
            case VWFloatType:      return (T) new VWFloatLearner(nativePointer);
            case VWIntType:        return (T) new VWIntLearner(nativePointer);
            case VWFloatArrayType: return (T) new VWFloatArrayLearner(nativePointer);
            case VWIntArrayType:   return (T) new VWIntArrayLearner(nativePointer);
            case Unknown:
                 default:
                // Doing this will allow for all cases when a C object is made to be closed.
                closeInstance(nativePointer);
                throw new IllegalArgumentException("Unknown VW return type using command: " + command);
        }

    }

    /**
     * @param command The same string that is passed to VW, see
     *                <a href="https://github.com/JohnLangford/vowpal_wabbit/wiki/Command-line-arguments">here</a>
     *                for more information
     * @return
     */
    private static long initializeVWJni(final String command) {
        long nativePointer;
        try {
            nativePointer = initialize(command);
            loadedNativeLibrary = true;
        }
        catch (UnsatisfiedLinkError e) {
            loadNativeLibrary();
            nativePointer = initialize(command);
        }

        final String previousCommand = openPointers.putIfAbsent(nativePointer, command);
        if (null != previousCommand) {
            // TODO: nativePointer was already there.  Bad!  Should we do something?
            throw new IllegalStateException("Found learner already created with native pointer: " + nativePointer + ". Command: " + previousCommand);
        }
        else {
            System.out.println("Created learner with native pointer: " + nativePointer + " and command: " + command);
        }


//        if (!openPointers.add(nativePointer)) {
//            // TODO: nativePointer was already there.  Bad!  Should we do something?
//            System.out.println("Found learner already created with native pointer: " + nativePointer);
//        }
//        else {
//            System.out.println("Created learner with native pointer: " + nativePointer);
//        }

        return nativePointer;
    }

    private static void loadNativeLibrary() {
        // By making use of a static lock here we make sure this code is only executed once globally.
        if (!loadedNativeLibrary) {
            STATIC_LOCK.lock();
            try {
                if (!loadedNativeLibrary) {
                    NativeUtils.loadOSDependentLibrary("/vw_jni", ".lib");
                    loadedNativeLibrary = true;
                }
            }
            catch (IOException e) {
                // Here I've chosen to rethrow the exception as an unchecked exception because if the native
                // library cannot be loaded then the exception is not recoverable from.
                throw new RuntimeException(e);
            }
            finally {
                STATIC_LOCK.unlock();
            }
        }
    }

    private static native long initialize(String command);
    private static native VWReturnType getReturnType(long nativePointer);

    // Closing needs to be done here when initialization fails and by VWBase
    private static native void closeLearner(long nativePointer);

//    static void closeInstance(final long nativePointer) {
//        if (openPointers.remove(nativePointer)) {
//            closeLearner(nativePointer);
//        }
//    }

    static void closeInstance(final long nativePointer) {
        final String command = openPointers.remove(nativePointer);
        if (null != command) {
            closeLearner(nativePointer);
        }
        else {
            throw new IllegalArgumentException("Attempted to close non-open native pointer: " + nativePointer);
        }
    }

    private static class Closer extends Thread {
        @Override public void run() {
            System.out.println("Removing dangling native pointers.");
            while(!openPointers.isEmpty()) {
                final Map.Entry<Long, String> next = openPointers.entrySet().iterator().next();
                if (null != next) {
                    final Long nativePointer = next.getKey();
                    System.out.println("Cleaning up dangling native pointer: " + nativePointer + " with command: " + next.getValue());
                    closeLearner(nativePointer);
                    openPointers.remove(nativePointer);
                }
            }
        }
    }

//    private static class Closer extends Thread {
//        @Override public void run() {
//            System.out.println("Removing dangling native pointers.");
//            while(!openPointers.isEmpty()) {
//                final Long nativePointer = openPointers.iterator().next();
//                if (null != nativePointer) {
//                    System.out.println("Removing pointer: " + nativePointer);
//                    closeLearner(nativePointer);
//                    openPointers.remove(nativePointer);
//                }
//            }
//        }
//    }
}
