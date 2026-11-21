package pw.kaboom.particletrails;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;

public final class ParticleCreator {
    private static final int NUM_PARTICLES = 12;
    private static final double OFFSET_X = 0.5;
    private static final double OFFSET_Y = 1;
    private static final double OFFSET_Z = 0.5;

    ParticleCreator(final Main plugin) {
        schedule(plugin);
    }

    private static void schedule(final Main plugin) {
        plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> {
                    for (final Player player : Bukkit.getOnlinePlayers()) {
                        if (player.isInvisible()) {
                            continue;
                        }

                        final GameMode gameMode = player.getGameMode();
                        if (gameMode.equals(GameMode.SPECTATOR)) {
                            continue;
                        }

                        final ParticleData particleData = plugin.particles.get(
                                player.getUniqueId()
                        );
                        if (particleData == null) {
                            continue;
                        }

                        final Particle particle = particleData.particle();
                        final Location location = player.getLocation();
                        location.add(location.getDirection().normalize().multiply(-1.5));
                        final World world = location.getWorld();
                        /*double data = 0;

                        if (particle.equals(Particle.NOTE)) {
                            // Colored notes
                            data = ThreadLocalRandom.current().nextInt(0, 16);
                        }*/

                        try {
                            world.spawnParticle(particle, location, NUM_PARTICLES,
                                    OFFSET_X, OFFSET_Y, OFFSET_Z, particleData.data());
                        } catch (final Exception e) {
                            plugin.particles.remove(
                                    player.getUniqueId()
                            );

                            player.sendMessage(
                                    Component.text(
                                            "Something went wrong while displaying particle trail. "
                                                    + "Please check console",
                                            NamedTextColor.RED
                                    )
                            );
                            plugin.getSLF4JLogger().error(
                                    "Failed to spawn particle of type {}",
                                    particle.getKey(),
                                    e
                            );

                            plugin.saveConfig();
                        }
                    }
                },
                0,
                2
        );
    }
}
