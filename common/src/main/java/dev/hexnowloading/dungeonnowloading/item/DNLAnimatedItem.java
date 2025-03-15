package dev.hexnowloading.dungeonnowloading.item;

public interface DNLAnimatedItem<T extends Enum<T> & DNLAnimationState> {

    Class<T> getAnimationEnum();

    default T getDefaultAnimationState() {
        return null;
    }
}