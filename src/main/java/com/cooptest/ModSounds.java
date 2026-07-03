package com.cooptest;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "testcoop");

    public static final ResourceLocation EPIC_DAP_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "epic_dap");
    public static final SoundEvent EPIC_DAP = SoundEvent.createVariableRangeEvent(EPIC_DAP_ID);
    public static final ResourceLocation EXPLOSION_IMPACT_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "explosion_impact");
    public static final SoundEvent EXPLOSION_IMPACT = SoundEvent.createVariableRangeEvent(EXPLOSION_IMPACT_ID);
    public static final ResourceLocation MAHITO_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "mahito");
    public static final SoundEvent MAHITO = SoundEvent.createVariableRangeEvent(MAHITO_ID);
    public static final ResourceLocation GALACTIC_DAP_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "galactic_dap");
    public static final SoundEvent GALACTIC_DAP = SoundEvent.createVariableRangeEvent(GALACTIC_DAP_ID);
    public static final ResourceLocation TRUE_FRIENDSHIP_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "true_friendship");
    public static final SoundEvent TRUE_FRIENDSHIP = SoundEvent.createVariableRangeEvent(TRUE_FRIENDSHIP_ID);
    public static final ResourceLocation IMPACT_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "impact");
    public static final SoundEvent IMPACT = SoundEvent.createVariableRangeEvent(IMPACT_ID);
    public static final ResourceLocation MARIO_JUMP_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "mariojump");
    public static final SoundEvent MARIO_JUMP = SoundEvent.createVariableRangeEvent(MARIO_JUMP_ID);
    public static final SoundEvent FIRE_IMPACT = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("testcoop", "fireimpact")
    );
    public static final ResourceLocation DAP_MISS_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "dap.miss");
    public static final SoundEvent DAP_MISS = SoundEvent.createVariableRangeEvent(DAP_MISS_ID);
    public static final ResourceLocation DAP_WEAK_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "dap.weak");
    public static final SoundEvent DAP_WEAK = SoundEvent.createVariableRangeEvent(DAP_WEAK_ID);
    public static final ResourceLocation SNAP_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "snap");
    public static final SoundEvent SNAP = SoundEvent.createVariableRangeEvent(SNAP_ID);
    public static final ResourceLocation DAP_HIT_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "dap.hit");
    public static final SoundEvent DAP_HIT = SoundEvent.createVariableRangeEvent(DAP_HIT_ID);
    public static final ResourceLocation SLAP_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "slap");
    public static final SoundEvent SLAP = SoundEvent.createVariableRangeEvent(SLAP_ID);
    public static final ResourceLocation PERFECT_DAP_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "cooldap");
    public static final SoundEvent PERFECT_DAP = SoundEvent.createVariableRangeEvent(PERFECT_DAP_ID);
    public static final ResourceLocation AURA_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "aura");
    public static final SoundEvent AURA = SoundEvent.createVariableRangeEvent(AURA_ID);
    public static final ResourceLocation HELI_ID = ResourceLocation.fromNamespaceAndPath("testcoop", "heli");
    public static final SoundEvent HELI = SoundEvent.createVariableRangeEvent(HELI_ID);
    public static final SoundEvent CLAP_1 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("testcoop", "clap1"));
    public static final SoundEvent CLAP_2 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("testcoop", "clap2"));
    public static final SoundEvent CLAP_3 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("testcoop", "clap3"));
    public static final SoundEvent CLAP_4 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("testcoop", "clap4"));
    public static final SoundEvent CLAP_5 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("testcoop", "clap5"));
    public static final SoundEvent CLAP_6 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("testcoop", "clap6"));
    public static final SoundEvent[] CLAP_SOUNDS = { CLAP_1, CLAP_2, CLAP_3, CLAP_4, CLAP_5, CLAP_6 };

    static {
        registerSound("epic_dap", EPIC_DAP);
        registerSound("explosion_impact", EXPLOSION_IMPACT);
        registerSound("mahito", MAHITO);
        registerSound("galactic_dap", GALACTIC_DAP);
        registerSound("true_friendship", TRUE_FRIENDSHIP);
        registerSound("impact", IMPACT);
        registerSound("mariojump", MARIO_JUMP);
        registerSound("fireimpact", FIRE_IMPACT);
        registerSound("dap.miss", DAP_MISS);
        registerSound("dap.weak", DAP_WEAK);
        registerSound("snap", SNAP);
        registerSound("dap.hit", DAP_HIT);
        registerSound("slap", SLAP);
        registerSound("cooldap", PERFECT_DAP);
        registerSound("aura", AURA);
        registerSound("heli", HELI);
        registerSound("clap1", CLAP_1);
        registerSound("clap2", CLAP_2);
        registerSound("clap3", CLAP_3);
        registerSound("clap4", CLAP_4);
        registerSound("clap5", CLAP_5);
        registerSound("clap6", CLAP_6);
    }

    private static void registerSound(String name, SoundEvent sound) {
        SOUNDS.register(name, () -> sound);
    }

    public static void registerNeoForge(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }

    public static void register() {
    }
}
