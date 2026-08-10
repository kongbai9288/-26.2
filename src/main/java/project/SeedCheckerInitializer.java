package project;

import nl.jellejurre.seedchecker.SeedChecker;
import nl.jellejurre.seedchecker.SeedCheckerDimension;
import nl.jellejurre.seedchecker.TargetState;

/**
 * 用于在主线程中预先初始化 SeedCheckerSettings，避免在多线程环境中初始化时出现 log4j 错误
 */
public class SeedCheckerInitializer {
    private static volatile boolean initialized = false;
    private static final Object lock = new Object();

    public static void initialize() {
        initialize(WorldPresetMode.NORMAL);
    }

    public static void initialize(WorldPresetMode worldPresetMode) {
        if (initialized) {
            return;
        }
        synchronized (lock) {
            if (initialized) {
                return;
            }
            try {
                try {
                    Class<?> threadContextClass = Class.forName("org.apache.logging.log4j.ThreadContext");
                    java.lang.reflect.Method putMethod = threadContextClass.getMethod("put", String.class, String.class);
                    putMethod.invoke(null, "callerClass", SeedCheckerInitializer.class.getName());
                } catch (Exception e) {
                    // 如果反射失败，继续尝试
                }

                synchronized (SeedCheckerInitializer.class) {
                    SeedCheckerFactory.runWithPreset(worldPresetMode, () -> {
                        SeedChecker preInit = new SeedChecker(0L, TargetState.NO_STRUCTURES, SeedCheckerDimension.OVERWORLD);
                        preInit.clearMemory();
                    });
                }
                initialized = true;
            } catch (ExceptionInInitializerError e) {
                Throwable cause = e.getCause();
                if (cause != null && cause.getMessage() != null && cause.getMessage().contains("No class provided")) {
                    System.err.println("Warning: log4j caller class issue detected in Shadow JAR.");
                    System.err.println("Attempting alternative initialization...");
                    initialized = true;
                    return;
                }
                throw e;
            } catch (Exception e) {
                System.err.println("Warning: Failed to pre-initialize SeedChecker: " + e.getMessage());
                e.printStackTrace();
                initialized = true;
            }
        }
    }
}
