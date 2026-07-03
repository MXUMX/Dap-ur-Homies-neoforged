package net.fabricmc.fabric.api.itemgroup.v1;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.ArrayList;
import java.util.List;

public final class ItemGroupEvents {
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private ItemGroupEvents() {
    }

    public static ModifyEntries modifyEntriesEvent(ResourceKey<CreativeModeTab> group) {
        return new ModifyEntries(group);
    }

    public static void apply(BuildCreativeModeTabContentsEvent event) {
        for (Entry entry : ENTRIES) {
            if (entry.group.equals(event.getTabKey())) {
                entry.callback.modifyEntries(stack -> {
                    try {
                        event.accept(stack);
                    } catch (IllegalArgumentException ignored) {
                        // NeoForge rejects duplicate creative-tab entries more strictly than Fabric.
                    }
                });
            }
        }
    }

    public static final class ModifyEntries {
        private final ResourceKey<CreativeModeTab> group;

        private ModifyEntries(ResourceKey<CreativeModeTab> group) {
            this.group = group;
        }

        public void register(ModifyEntriesCallback callback) {
            ENTRIES.add(new Entry(group, callback));
        }
    }

    @FunctionalInterface
    public interface ModifyEntriesCallback {
        void modifyEntries(Content content);
    }

    @FunctionalInterface
    public interface Content {
        void add(ItemStack stack);

        default void accept(ItemStack stack) {
            add(stack);
        }
    }

    private record Entry(ResourceKey<CreativeModeTab> group, ModifyEntriesCallback callback) {
    }
}
