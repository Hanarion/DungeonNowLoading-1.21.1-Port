package dev.hexnowloading.dungeonnowloading.item;

public interface DNLAnimationState {
    String getName();

    // ✅ Improved method: Returns default value instead of crashing
    static <T extends Enum<T> & DNLAnimationState> T fromString(Class<T> enumClass, String name, T defaultState) {
        for (T state : enumClass.getEnumConstants()) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        System.err.println("⚠ ERROR: Unknown animation state '" + name + "' for " + enumClass.getSimpleName());
        return defaultState; // ✅ Return fallback state instead of crashing
    }
}
