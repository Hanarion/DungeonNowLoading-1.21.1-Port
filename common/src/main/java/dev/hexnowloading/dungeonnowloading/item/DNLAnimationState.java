package dev.hexnowloading.dungeonnowloading.item;

public interface DNLAnimationState {
    String getName();

    static <T extends Enum<T> & DNLAnimationState> T fromString(Class<T> enumClass, String name) {
        for (T state : enumClass.getEnumConstants()) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown animation state: " + name);
    }
}
