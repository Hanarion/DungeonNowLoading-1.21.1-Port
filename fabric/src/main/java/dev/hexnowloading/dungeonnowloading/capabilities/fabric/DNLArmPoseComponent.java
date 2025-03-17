package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public interface DNLArmPoseComponent extends ComponentV3, AutoSyncedComponent {

    void setArmPose(DNLArmPose pose);

    DNLArmPose getArmPose();
}
