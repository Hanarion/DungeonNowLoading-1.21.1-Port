package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IntComponent extends ComponentV3, AutoSyncedComponent {

    int getValue();

    void setValue(int i);
}
