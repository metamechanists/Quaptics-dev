package org.metamechanists.quaptics.implementation.multiblocks.infuser;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.displaymodellib.models.ModelBuilder;
import org.metamechanists.displaymodellib.models.components.ModelCuboid;
import org.metamechanists.displaymodellib.models.components.ModelItem;
import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import org.metamechanists.quaptics.implementation.Settings;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.items.Tier;
import org.metamechanists.quaptics.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.metamechanists.quaptics.implementation.multiblocks.infuser.IndustrialInfusionPillar.INDUSTRIAL_INFUSION_PILLAR;


public class IndustrialInfusionContainer extends InfusionContainer {
    public static final Settings INDUSTRIAL_INFUSION_CONTAINER_SETTINGS = Settings.builder()
            .tier(Tier.INTERMEDIATE)
            .operatingPowerHidden(true)
            .timePerRecipe(15)
            .build();
    public static final SlimefunItemStack INDUSTRIAL_INFUSION_CONTAINER = new SlimefunItemStack(
            "QP_INDUSTRIAL_INFUSION_CONTAINER",
            Material.GRAY_CONCRETE,
            "&6Industrial Infusion Container",
            Lore.create(INDUSTRIAL_INFUSION_CONTAINER_SETTINGS,
                    Lore.multiblock(),
                    "&7● Infuses items",
                    "&7● Can infuse up to 16 items at once",
                    "&7● &eRight Click &7with an item to start infusing"));

    private static final Map<Vector, ItemStack> PILLARS = Map.of(
            new Vector(3, 0, 0), INDUSTRIAL_INFUSION_PILLAR,
            new Vector(2, 0, 2), INDUSTRIAL_INFUSION_PILLAR,
            new Vector(0, 0, 3), INDUSTRIAL_INFUSION_PILLAR,
            new Vector(-2, 0, 2), INDUSTRIAL_INFUSION_PILLAR,
            new Vector(-3, 0, 0), INDUSTRIAL_INFUSION_PILLAR,
            new Vector(-2, 0, -2), INDUSTRIAL_INFUSION_PILLAR,
            new Vector(0, 0, -3), INDUSTRIAL_INFUSION_PILLAR,
            new Vector(2, 0, -2), INDUSTRIAL_INFUSION_PILLAR);

    public IndustrialInfusionContainer(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    protected boolean onRightClick(@NotNull Location location, @NotNull Player player) {
        if (multiblockInteract(location.getBlock(), player)) {
            return true;
        }
        itemHolderInteract(location, "item", player, 16);
        return true;
    }

    @Override
    protected DisplayGroup initModel(final @NotNull Location location, final @NotNull Player player) {
        return new ModelBuilder()
                .add("base", new ModelCuboid()
                        .material(Material.GRAY_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(0, -0.3F, 0))
                        .scale(1.3F, 0.4F, 1.3F)
                .add("pillar1", new ModelCuboid()
                        .material(Material.ORANGE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(-0.6F, -0.1F, -0.6F)
                        .scale(0.25F, 0.8F, 0.25F))
                .add("pillar2", new ModelCuboid()
                        .material(Material.ORANGE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(-0.6F, -0.1F, 0.6F)
                        .scale(0.25F, 0.8F, 0.25F))
                .add("pillar3", new ModelCuboid()
                        .material(Material.ORANGE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(0.6F, -0.1F, -0.6F)
                        .scale(0.25F, 0.8F, 0.25F))
                .add("pillar4", new ModelCuboid()
                        .material(Material.ORANGE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(0.6F, -0.1F, 0.6F)
                        .scale(0.25F, 0.8F, 0.25F))
                .add("plate", new ModelCuboid()
                        .material(Material.WHITE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(0, -0.1F, 0)
                        .scale(0.6F, 0.1F, 0.6F))
                .add("item", new ModelItem()
                        .brightness(Utils.BRIGHTNESS_ON)
                        .translate(0, 0.4F, 0)
                        .scale(0.8F))
                .buildAtBlockCenter(location);
    }

    @Override
    public Map<ItemStack, ItemStack> getRecipes() {
        final Map<ItemStack, ItemStack> recipes = new HashMap<>();
        for (final Entry<ItemStack, ItemStack> recipe : super.getRecipes().entrySet()) {
            for (int i = 0; i < 16; i++) {
                final ItemStack input = recipe.getKey().clone();
                final ItemStack output = recipe.getValue().clone();
                input.add(i);
                output.add(i);
                recipes.put(input, output);
            }
        }
        return recipes;
    }

    @Override
    public Map<Vector, ItemStack> getStructure() {
        return PILLARS;
    }
}
