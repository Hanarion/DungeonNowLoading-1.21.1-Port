package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

// 1.21 / CCA 6.1.3: ComponentV3 was merged into Component.
public interface DNLArmPoseComponent extends Component, AutoSyncedComponent {

    void setArmPose(DNLArmPose pose);

    DNLArmPose getArmPose();
}
