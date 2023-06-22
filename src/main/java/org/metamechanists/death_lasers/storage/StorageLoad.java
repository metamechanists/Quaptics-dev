package org.metamechanists.death_lasers.storage;

import io.github.bakedlibs.dough.common.ChatColors;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.metamechanists.death_lasers.DEATH_LASERS;
import org.metamechanists.death_lasers.connections.ConnectionGroup;
import org.metamechanists.death_lasers.connections.ConnectionPointStorage;
import org.metamechanists.death_lasers.utils.Language;

import java.io.File;
import java.util.Map;

public class StorageLoad {
    public static void load() {

        final File file = new File(DEATH_LASERS.getInstance().getDataFolder(), StorageSaveRunnable.FILE_NAME);
        final FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        if (!file.exists()) {
            DEATH_LASERS.getInstance().getLogger().warning(ChatColors.color(Language.getLanguageEntry("load.no-data-file")));
            return;
        }

        try {
            final Map<Location, ConnectionGroup> groups = SerializationUtils.deserializeMap(
                    data.getConfigurationSection("groups").getValues(true),
                    "Location", "ConnectionGroup");



            final Map<Location, Location> groupIdsFromPointLocations = SerializationUtils.deserializeMap(
                    data.getConfigurationSection("groupIdsFromPointLocations").getValues(true),
                    "GroupLocation", "PointLocation");

            groups.remove(null);
            groupIdsFromPointLocations.remove(null);

            ConnectionPointStorage.setGroups(groups);
            ConnectionPointStorage.setGroupIdsFromPointLocations(groupIdsFromPointLocations);

        } catch (Exception e) {
            DEATH_LASERS.getInstance().getLogger().warning(ChatColors.color(Language.getLanguageEntry("load.failed")));
            e.printStackTrace();
            throw new RuntimeException("Failed to load laser data");
        }

        DEATH_LASERS.getInstance().getLogger().info(ChatColors.color(Language.getLanguageEntry("load.success")));
    }
}