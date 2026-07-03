package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class PayloadTypeRegistry {
    private static final Registry PLAY_C2S = new Registry(true);
    private static final Registry PLAY_S2C = new Registry(false);

    private PayloadTypeRegistry() {
    }

    public static Registry playC2S() {
        return PLAY_C2S;
    }

    public static Registry playS2C() {
        return PLAY_S2C;
    }

    public static final class Registry {
        private final boolean serverbound;

        private Registry(boolean serverbound) {
            this.serverbound = serverbound;
        }

        public <B extends FriendlyByteBuf, T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> id, StreamCodec<B, T> codec) {
            NeoForgePayloadBridge.register(id, codec, serverbound);
        }
    }
}
