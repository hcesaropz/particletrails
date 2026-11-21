package pw.kaboom.particletrails;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import pw.kaboom.particletrails.command.CommandParticleTrails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {
    public final Map<UUID, ParticleData> particles = new HashMap<>();

    @Override
    public void onEnable() {
        new ParticleCreator(this);

        final CommandParticleTrails command = new CommandParticleTrails(this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final LiteralArgumentBuilder<CommandSourceStack> builder
                    = Commands.literal(command.getLabel());
            command.build(builder);
            event.registrar().register(
                    builder.build(), command.getDescription(), List.of()
            );
        });
    }
}