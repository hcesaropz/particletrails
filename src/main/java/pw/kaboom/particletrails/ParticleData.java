package pw.kaboom.particletrails;

import org.bukkit.Particle;

public record ParticleData(Particle particle, Object data) {
    public ParticleData(Particle particle) {
        this(particle, null);
    }
}
