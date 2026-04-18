package dev.hexnowloading.dungeonnowloading.entity.client.animation_duration.seeping_soul;

public enum SeepingSoulAnimationDuration implements SeepingSoulDuration {
    DEFAULT {
        public float idle() { return 3.0F; }
        public float idleBreak() { return 2.0F; }
        public float spawn() { return 5.5F; }
        public float idleTick() { return 2.0F; }
        public float hurt() { return 0.5F; }
        public float recalling() { return 6.0F; }
    },
    SERPENT_CALLER {
        public float idle() { return 4.0F; }
        public float idleBreak() { return 2.0F; }
        public float spawn() { return 4.25F; }
        public float idleTick() { return 2.125F; }
        public float hurt() { return 0.5F; }
        public float recalling() { return 6.0F; }
    };
}
