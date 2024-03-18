package org.metamechanists.quaptics.items.groups;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import lombok.experimental.UtilityClass;
import org.metamechanists.quaptics.Quaptics;
import org.metamechanists.quaptics.implementation.blocks.concentrators.CreativeConcentrator;
import org.metamechanists.quaptics.items.Groups;

@UtilityClass
public class CreativeComponents {

    public void initialize() {
        final SlimefunAddon addon = Quaptics.getInstance();

        new CreativeConcentrator(Groups.CREATIVE, CreativeConcentrator.CREATIVE_CONCENTRATOR).register(addon);
    }
}
