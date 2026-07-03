package net.fabricmc.fabric.api.command.v2;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CommandRegistrationCallback {
    public static final Event EVENT = new Event();

    private CommandRegistrationCallback() {
    }

    @FunctionalInterface
    public interface Callback {
        void register(CommandDispatcher<CommandSourceStack> dispatcher,
                      CommandBuildContext registryAccess,
                      Object environment);
    }

    public static final class Event {
        private final List<Callback> callbacks = new CopyOnWriteArrayList<>();

        public void register(Callback callback) {
            callbacks.add(callback);
        }

        public void invoke(RegisterCommandsEvent event) {
            for (Callback callback : callbacks) {
                callback.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
            }
        }
    }
}
