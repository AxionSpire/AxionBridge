package net.axionspire.axionbridge;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class APIStatManager {
    private static APIStatManager instance;
    private AxionBridge plugin;

    private final List<RegisteredStatProvider> registeredProviders = new ArrayList<>();
    private final List<APIStat> registeredStats = new ArrayList<>();

    public static APIStatManager getInstance() {
        if (instance == null) {
            instance = new APIStatManager();
        }
        return instance;
    }

    void setPlugin(AxionBridge plugin) {
        this.plugin = plugin;
    }

    public void registerStatProvider(RegisteredStatProvider provider) {
        registeredProviders.add(provider);
        plugin.getLogger().info("Registered stat provider: " + provider.getMethodID());
    }

    // TODO: Verify that attributes of each stat are valid
    void loadAPIStats() {
        Set<String> stats = plugin.statsConfig.getKeys(false);
        for (String statID : stats) {
            String source = plugin.statsConfig.getString(statID + ".source");
            String checkedPlayers = plugin.statsConfig.getString(statID + ".check");
            APIStat stat = null;
            try {
                if (Objects.equals(source, "PLACEHOLDERAPI")) {
                    stat = new APIStat(statID, APIStat.ResolveMethod.PLACEHOLDERAPI, APIStat.CheckedPlayers.valueOf(checkedPlayers), plugin.statsConfig.getString(statID + ".placeholder"), null);
                } else if (Objects.equals(source, "registered")) {
                    String registeredStatProvider = plugin.statsConfig.getString(statID + ".call");
                    for (RegisteredStatProvider provider : registeredProviders) {
                        if (provider.getMethodID().equals(registeredStatProvider)) {
                            stat = new APIStat(statID, APIStat.ResolveMethod.REGISTERED, APIStat.CheckedPlayers.valueOf(checkedPlayers), null, provider);
                        }
                    }
                    if (stat == null) {
                        plugin.getLogger().warning("No registered stat provider matches stat '" + statID + "', which requests '" + registeredStatProvider + "', skipping...");
                        continue;
                    }
                } else  {
                    plugin.getLogger().warning("Unknown stat source '" + source + "' for stat '" + statID + "', skipping...");
                    continue;
                }
                registeredStats.add(stat);
                plugin.getLogger().info("Registered stat '" + statID + "' successfully.");
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Checked players for stat '" + statID + "' is invalid, skipping...");
                e.printStackTrace();
            }
        }
    }

    void pullStats() {
        if (registeredStats.isEmpty()) {
            plugin.getLogger().warning("No API stats registered, skipping pulling stats.");
            return;
        }
        for (APIStat stat : registeredStats) {
            HashMap<UUID, String> playerStats = new HashMap<>();
            if (plugin.getConfig().getBoolean("Debug")) {
                plugin.getLogger().info("Debug message: Pulling stat '" + stat.getStatID() + "' from source '" + stat.getResolveMethod() + "', checking " + stat.getCheckedPlayers() + " players.");
                if (stat.getPlaceholder() != null) {
                    plugin.getLogger().info("Debug message: Stat placeholder: " + stat.getPlaceholder());
                } else if (stat.getRegisteredStatProvider() != null) {
                    plugin.getLogger().info("Debug message: Stat registered provider: " + stat.getRegisteredStatProvider().getMethodID());
                }
            }
            List<OfflinePlayer> players = new ArrayList<>();
            if (stat.getCheckedPlayers() == APIStat.CheckedPlayers.ALL) {
                players.addAll(Arrays.stream(plugin.getServer().getOfflinePlayers()).toList());
            } else if (stat.getCheckedPlayers() == APIStat.CheckedPlayers.ONLINE) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.isOnline()) {
                        players.add(player);
                    }
                }
            }
            if (stat.getResolveMethod() == APIStat.ResolveMethod.PLACEHOLDERAPI) {
                for (OfflinePlayer player : players) {
                    playerStats.put(player.getUniqueId(), PlaceholderAPI.setPlaceholders(player, stat.getPlaceholder()));
                }
            } else if (stat.getResolveMethod() == APIStat.ResolveMethod.REGISTERED) {
                for (OfflinePlayer player : players) {
                    playerStats.put(player.getUniqueId(), stat.getRegisteredStatProvider().pullStats(player));
                }
            }
            try {
                if (plugin.getConfig().getBoolean("Debug")) {
                    plugin.getLogger().info("Debug message: Pulled stats for " + playerStats.size() + " players.");
                }
                if (playerStats.isEmpty()) {
                    continue;
                }
                plugin.uploadStat(stat.getStatID(), playerStats);
                if (plugin.getConfig().getBoolean("Debug")) {
                    plugin.getLogger().info("Debug message: Uploaded stats for stat '" + stat.getStatID() + "'.");
                }
            } catch (Exception ignored) {}
        }
    }
}
