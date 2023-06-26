package org.metamechanists.quaptics.implementation.base;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.points.ConnectionPoint;
import org.metamechanists.quaptics.storage.QuapticStorage;
import org.metamechanists.quaptics.utils.Transformations;
import org.metamechanists.quaptics.utils.id.ConnectionGroupID;
import org.metamechanists.quaptics.utils.id.ConnectionPointID;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;

public abstract class ConnectedBlock extends DisplayGroupTickerBlock {
    public final double maxPower;

    public ConnectedBlock(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, double maxPower) {
        super(group, item, recipeType, recipe);
        this.maxPower = maxPower;
    }

    private ConnectionGroup getGroup(Location location) {
        return ConnectionGroup.fromID(new ConnectionGroupID(getID(location).get()));
    }

    protected abstract float getRadius();
    protected abstract List<ConnectionPoint> generateConnectionPoints(ConnectionGroupID groupID, Player player, Location location);

    protected static double powerLoss(double inputPower, double maxPower, double powerLoss) {
        return inputPower - ((powerLoss/maxPower) * Math.pow(inputPower, 2));
    }

    @Override
    protected void onPlace(BlockPlaceEvent event) {
        final Location location = event.getBlock().getLocation();
        final ConnectionGroupID groupID = new ConnectionGroupID(getID(location).get());
        final List<ConnectionPoint> points = generateConnectionPoints(groupID, event.getPlayer(), location);
        new ConnectionGroup(groupID, this, points);
        QuapticStorage.addGroup(groupID);
    }

    protected void onBreak(Location location) {}

    @Override
    protected void onBreak(BlockBreakEvent event) {
        getGroup(event.getBlock().getLocation()).remove();
        onBreak(event.getBlock().getLocation());
    }

    @OverridingMethodsMustInvokeSuper
    public void connect(ConnectionPointID from, ConnectionPointID to) {
        ConnectionPoint.fromID(from).getGroup().changePointLocation(from, calculateNewLocation(from, to));
    }

    public void burnout(Location location) {
        onBreak(location);
        getDisplayGroup(location).getDisplays().values().forEach(Entity::remove);
        getDisplayGroup(location).remove();

        BlockStorage.clearBlockInfo(location);
        location.getBlock().setBlockData(Material.AIR.createBlockData());

        location.createExplosion(1.5F, true, false);
        location.getWorld().spawnParticle(new ParticleBuilder(Particle.FLASH).location(location).count(1).particle(), location, 20);
    }

    public void doBurnoutCheck(ConnectionGroup group, ConnectionPoint point) {
        if (point.hasLink() && point.getLink().getPower() > maxPower) {
            burnout(group.getLocation());
        }
    }

    public void onInputLinkUpdated(ConnectionGroup group) {}

    public Location calculateNewLocation(ConnectionPointID from, ConnectionPointID to) {
        final Location fromGroupLocation =  ConnectionPoint.fromID(from).getGroup().getLocation();
        final Location toGroupLocation =  ConnectionPoint.fromID(to).getGroup().getLocation();
        final Vector radiusDirection = Vector.fromJOML(Transformations.getDirection(fromGroupLocation, toGroupLocation).mul(getRadius()));
        return fromGroupLocation.clone().add(0.5, 0.5, 0.5).add(radiusDirection);
    }
}