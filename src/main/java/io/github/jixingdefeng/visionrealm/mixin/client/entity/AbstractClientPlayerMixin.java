package io.github.jixingdefeng.visionrealm.mixin.client.entity;

import io.github.jixingdefeng.visionrealm.core.world.entity.player.ClientMirrorData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Unique
    private final AbstractClientPlayer visionRealm$clientPlayer = (AbstractClientPlayer) (Object) this;
    @Unique
    private PlayerInfo visionRealm$targetPlayerInfo;

    @Inject(method = "getPlayerInfo", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerInfo(CallbackInfoReturnable<PlayerInfo> cir) {
        UUID targetUUID = ClientMirrorData.getMirrorTarget(this.visionRealm$clientPlayer.getUUID());
        if (targetUUID != null) {
            if (this.visionRealm$targetPlayerInfo == null) {
                this.visionRealm$targetPlayerInfo = Objects.requireNonNull(Minecraft.getInstance().getConnection())
                        .getPlayerInfo(targetUUID);
            }

            cir.setReturnValue(this.visionRealm$targetPlayerInfo);
        }
    }
}
