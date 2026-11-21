package pw.kaboom.particletrails.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import pw.kaboom.particletrails.Main;
import pw.kaboom.particletrails.ParticleData;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static io.papermc.paper.command.brigadier.Commands.*;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.blockState;

public final class CommandParticleTrails {
    private static final String D = "data";

    private final Main plugin;

    public CommandParticleTrails(final Main plugin) {
        this.plugin = plugin;
    }

    public String getLabel() {
        return "particletrails";
    }

    public String getDescription() {
        return "Creates a particle trail behind you";
    }

    public void build(final LiteralArgumentBuilder<CommandSourceStack> builder) {

        builder
                .requires(restricted(src ->
                        src.getSender().hasPermission("particletrails.command")
                ))
                .then(literal("off")
                        .executes(ctx -> {
                            final Player sender = ctx.getSource().getPlayerOrThrow();
                            plugin.particles.remove(sender.getUniqueId());
                            sender.sendMessage(Component.text(
                                    "You no longer have a particle trail"
                            ));
                            return Command.SINGLE_SUCCESS;
                        })
                );

        for (final Particle particle: Particle.values()) {
            tryRegisterParticleWithData(
                    builder,
                    particle
            );
        }
    }

    public void tryRegisterParticleWithData(
            final LiteralArgumentBuilder<CommandSourceStack> builder,
            final Particle particle
    ) {
        final Class<?> dataType = particle.getDataType();

        final LiteralArgumentBuilder<CommandSourceStack> literal =
                literal(particle.getKey().asMinimalString());

        // TODO: Replace with a nice switch statement once Java supports it... hopefully
        if (dataType.equals(Void.class)) {
            literal.executes(ctx ->
                    handleParticleSet(
                            ctx,
                            new ParticleData(particle)
                    )
            );
        } else if (dataType.equals(Double.class)) {
            literal.then(argument(D, doubleArg())
                    .executes(
                            ctx -> handleParticleSet(
                                    ctx,
                                    new ParticleData(
                                            particle,
                                            getDouble(
                                                    ctx,
                                                    D
                                            )
                                    )
                            )
                    )
            );
        } else if (dataType.equals(Float.class)) {
            literal.then(argument(D, floatArg())
                    .executes(
                            ctx ->
                                    handleParticleSet(
                                            ctx,
                                            new ParticleData(
                                                    particle,
                                                    getFloat(
                                                            ctx,
                                                            D
                                                    )
                                            )
                                    )
                    )
            );
        } else if (dataType.equals(Integer.class)) {
            literal.then(argument(D, integer())
                    .executes(
                            ctx ->
                                    handleParticleSet(
                                            ctx,
                                            new ParticleData(
                                                    particle,
                                                    getInteger(
                                                            ctx,
                                                            D
                                                    )
                                            )
                                    )
                    )
            );
        } else if (dataType.equals(BlockData.class)) {
            literal.then(argument(D, blockState())
                    .executes(ctx -> {
                        final BlockState state = ctx.getArgument(
                                D,
                                BlockState.class
                        );

                        final BlockData data = state.getBlockData();

                        return handleParticleSet(
                                ctx,
                                new ParticleData(
                                        particle,
                                        data
                                )
                        );
                    })
            );
        } else {
            // TODO: Remove when all implementable types are implemented
            System.out.println(
                    "Unsupported data type "
                            + particle.getDataType().getTypeName()
                            + " for "
                            + particle.getKey()
            );
            return;
        }

        builder.then(literal);
    }

    private int handleParticleSet(final CommandContext<CommandSourceStack> ctx,
                                  final ParticleData particleData)
            throws CommandSyntaxException {
        final Player sender = ctx.getSource().getPlayerOrThrow();
        plugin.particles.put(sender.getUniqueId(), particleData);
        sender.sendMessage(Component.text("You now have a particle trail"));
        return Command.SINGLE_SUCCESS;
    }
}
