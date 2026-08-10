package project;

import nl.jellejurre.seedchecker.SeedChecker;
import nl.jellejurre.seedchecker.SeedCheckerDimension;
import nl.jellejurre.seedchecker.TargetState;
import nl.jellejurre.seedchecker.serverMocks.FakeSaveProperties;

import java.util.function.Supplier;

public final class SeedCheckerFactory {
    private SeedCheckerFactory() {}

    public static SeedChecker create(long seed, TargetState state, SeedCheckerDimension dimension, WorldPresetMode worldPresetMode) {
        return withPreset(worldPresetMode, () -> new SeedChecker(seed, state, dimension));
    }

    public static SeedChecker create(long seed, int targetLevel, SeedCheckerDimension dimension, WorldPresetMode worldPresetMode) {
        return withPreset(worldPresetMode, () -> new SeedChecker(seed, targetLevel, dimension));
    }

    public static void runWithPreset(WorldPresetMode worldPresetMode, Runnable runnable) {
        withPreset(worldPresetMode, () -> {
            runnable.run();
            return null;
        });
    }

    private static <T> T withPreset(WorldPresetMode worldPresetMode, Supplier<T> supplier) {
        FakeSaveProperties.setWorldPresetMode(worldPresetMode);
        try {
            return supplier.get();
        } finally {
            FakeSaveProperties.clearWorldPresetMode();
        }
    }
}
