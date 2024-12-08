package org.metamechanists.quaptics.implementation.multiblocks.infuser;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import org.metamechanists.metalib.utils.ParticleUtils;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.implementation.Settings;
import org.metamechanists.quaptics.implementation.attachments.ComplexMultiblock;
import org.metamechanists.quaptics.implementation.attachments.ItemHolderBlock;
import org.metamechanists.quaptics.implementation.attachments.ItemProcessor;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.items.Tier;
import org.metamechanists.quaptics.storage.QuapticTicker;
import org.metamechanists.quaptics.utils.BlockStorageAPI;
import org.metamechanists.quaptics.utils.Keys;
import org.metamechanists.quaptics.utils.Language;
import org.metamechanists.quaptics.utils.Particles;
import org.metamechanists.quaptics.utils.Utils;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;
import org.metamechanists.displaymodellib.models.ModelBuilder;
import org.metamechanists.displaymodellib.models.components.ModelCuboid;
import org.metamechanists.displaymodellib.models.components.ModelItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.metamechanists.quaptics.implementation.multiblocks.infuser.InfusionPillar.INFUSION_PILLAR;
import static org.metamechanists.quaptics.items.groups.CraftingComponents.*;


public class InfusionContainer extends ConnectedBlock implements ItemHolderBlock, ComplexMultiblock, ItemProcessor {
    public static final Settings INFUSION_CONTAINER_SETTINGS = Settings.builder()
            .tier(Tier.BASIC)
            .operatingPowerHidden(true)
            .timePerRecipe(5)
            .build();
    public static final SlimefunItemStack INFUSION_CONTAINER = new SlimefunItemStack(
            "QP_INFUSION_CONTAINER",
            Material.GRAY_CONCRETE,
            "&6Infusion Container",
            Lore.create(INFUSION_CONTAINER_SETTINGS,
                    Lore.multiblock(),
                    "&7● Infuses items",
                    "&7● &eRight Click &7with an item to start infusing"));

    private static final Map<Vector, ItemStack> PILLARS = Map.of(
            new Vector(2, 0, 0), INFUSION_PILLAR,
            new Vector(-2, 0, 0), INFUSION_PILLAR,
            new Vector(0, 0, 2), INFUSION_PILLAR,
            new Vector(0, 0, -2), INFUSION_PILLAR);

    private static final int PILLAR_PARTICLE_COUNT = 3;
    private static final double PILLAR_PARTICLE_ANIMATION_LENGTH_SECONDS = 0.5;
    private static final double CONTAINER_PARTICLE_RADIUS = 0.5;
    private static final int CONTAINER_PARTICLE_COUNT = 3;

    public InfusionContainer(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    public float getConnectionRadius() {
        return 0.0F;
    }
    @Override
    protected DisplayGroup initModel(final @NotNull Location location, final @NotNull Player player) {
        return new ModelBuilder()
                .add("base", new ModelCuboid()
                        .material(Material.GRAY_CONCRETE)
                        .lookAlong(player.getFacing())
                        .scale(0.9F, 0.6F, 0.9F)
                        .translate(0, -0.3F, 0))
                .add("pillar1", new ModelCuboid()
                        .material(Material.WHITE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(-0.4F, -0.1F, -0.4F)
                        .scale(0.2F, 0.8F, 0.2F))
                .add("pillar2", new ModelCuboid()
                        .material(Material.WHITE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(-0.4F, -0.1F, 0.4F)
                        .scale(0.2F, 0.8F, 0.2F))
                .add("pillar3", new ModelCuboid()
                        .material(Material.WHITE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(0.4F, -0.1F, -0.4F)
                        .scale(0.2F, 0.8F, 0.2F))
                .add("pillar4", new ModelCuboid()
                        .material(Material.WHITE_CONCRETE)
                        .lookAlong(player.getFacing())
                        .translate(0.4F, -0.1F, 0.4F)
                        .scale(0.2F, 0.8F, 0.2F))
                .add("item", new ModelItem()
                        .brightness(Utils.BRIGHTNESS_ON)
                        .translate(0, 0.3F, 0)
                        .scale(0.5F))
                .buildAtBlockCenter(location);
    }
    @Override
    protected List<ConnectionPoint> initConnectionPoints(final ConnectionGroupId groupId, final Player player, final Location location) {
        return new ArrayList<>();
    }
    @Override
    protected void initBlockStorage(final @NotNull Location location, @NotNull final Player player) {
        BlockStorageAPI.set(location, Keys.BS_CRAFT_IN_PROGRESS, false);
        BlockStorageAPI.set(location, Keys.BS_SECONDS_SINCE_CRAFT_STARTED, 0.0);
    }

    @Override
    protected boolean isTicker() {
        return true;
    }

    @Override
    protected void onBreak(@NotNull final Location location) {
        super.onBreak(location);
        onBreakItemHolderBlock(location, "item");
    }
    @Override
    protected boolean onRightClick(final @NotNull Location location, final @NotNull Player player) {
        if (multiblockInteract(location.getBlock(), player)) {
            return true;
        }
        itemHolderInteract(location, "item", player);
        return true;
    }
    @SuppressWarnings("unused")
    @Override
    public void onTick22(@NotNull final ConnectionGroup group, @NotNull final Location location) {
        BlockStorageAPI.set(location, Keys.BS_MULTIBLOCK_INTACT, isStructureValid(location.getBlock()));
    }
    @SuppressWarnings("unused")
    @Override
    public void onTick2(@NotNull final ConnectionGroup group, @NotNull final Location location) {
        if (!isProcessing(location)) {
            return;
        }

        if (!BlockStorageAPI.getBoolean(location, Keys.BS_MULTIBLOCK_INTACT) || !allPillarsPowered(location)) {
            cancelProcessing(location);
        }

        final double secondsSinceCraftStarted = BlockStorageAPI.getDouble(location, Keys.BS_SECONDS_SINCE_CRAFT_STARTED);

        tickProcessing(location, QuapticTicker.INTERVAL_TICKS_2);
        tickAnimation(location, secondsSinceCraftStarted);

        if (secondsSinceCraftStarted >= settings.getTimePerRecipe()) {
            completeProcessing(location);
        }
    }
    @Override
    public boolean onInsert(@NotNull final Location location, @NotNull final String name, @NotNull final ItemStack stack, @NotNull final Player player) {
        if (!isValidRecipe(stack)) {
            Language.sendLanguageMessage(player, "infuser.cannot-be-infused");
            return false;
        }

        if (!allPillarsPowered(location)) {
            Language.sendLanguageMessage(player, "infuser.pillars-not-powered");
            return false;
        }

        startProcessing(location);
        return true;
    }
    @Override
    public Optional<ItemStack> onRemove(@NotNull final Location location, @NotNull final String name, @NotNull final ItemStack stack) {
        cancelProcessing(location);
        return Optional.of(stack);
    }

    @Override
    public Map<ItemStack, ItemStack> getRecipes() {
        return Map.of(
                BLANK_MODULE_CARD, INFUSED_MODULE_CARD,
                new ItemStack(Material.QUARTZ), INFUSED_FREQUENCY_CRYSTAL);
    }
    @Override
    public Map<Vector, ItemStack> getStructure() {
        return PILLARS;
    }
    @Override
    public void tickAnimation(@NotNull final Location centerLocation, final double timeSeconds) {
        getStructure().keySet().forEach(pillarLocation -> animatePillar(centerLocation, centerLocation.clone().add(pillarLocation), timeSeconds));
        animateCenter(centerLocation);
    }

    private static boolean isPillarPowered(@NotNull final Location pillarLocation) {
        return BlockStorageAPI.getBoolean(pillarLocation, Keys.BS_POWERED);
    }
    private boolean allPillarsPowered(@NotNull final Location location) {
        return getStructure().keySet().stream().allMatch(vector -> isPillarPowered(location.clone().add(vector)));
    }
    private static void animatePillar(@NotNull final Location center, @NotNull final Location pillarLocation, final double timeSinceCraftStarted) {
        Particles.animatedLine(Particle.ELECTRIC_SPARK,
                pillarLocation.clone().toCenterLocation(),
                center.clone().toCenterLocation(),
                PILLAR_PARTICLE_COUNT,
                (timeSinceCraftStarted % PILLAR_PARTICLE_ANIMATION_LENGTH_SECONDS) / PILLAR_PARTICLE_ANIMATION_LENGTH_SECONDS,
                0);
    }
    private static void animateCenter(@NotNull final Location center) {
        ParticleUtils.randomParticle(center.clone().toCenterLocation(), Particle.ENCHANTMENT_TABLE, CONTAINER_PARTICLE_RADIUS, CONTAINER_PARTICLE_COUNT);
    }
}