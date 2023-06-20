package org.metamechanists.death_lasers.beams.ticker.factory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.metamechanists.death_lasers.beams.ticker.ticker.DirectSinglePulseTicker;

public class DirectSinglePulseTickerFactory implements LaserBlockDisplayTickerFactory {
    private final Material material;
    private final Location source;
    private final Location target;

    public DirectSinglePulseTickerFactory(Material material, Location source, Location target) {
        this.material = material;
        this.source = source.clone();
        this.target = target.clone();
    }

    public DirectSinglePulseTicker build() {
        return new DirectSinglePulseTicker(material, source.clone(), target.clone());
    }
}