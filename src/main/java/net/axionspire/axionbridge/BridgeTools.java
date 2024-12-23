package net.axionspire.axionbridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

class BridgeTools {
    private AxionBridge plugin;
    private static BridgeTools instance;

    static BridgeTools getInstance() {
        if (instance == null) {
            instance = new BridgeTools();
        }
        return instance;
    }

    void setPlugin(AxionBridge plugin) {
        this.plugin = plugin;
    }

    String getPrefix() {
        if (plugin.getConfig().getString("Prefix") == null) { return ""; }
        return (ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("Prefix"))) + " ");
    }

    URI getAPIURL() {
        if (plugin.getConfig().getString("api.url") == null) { return null; }
        return URI.create(Objects.requireNonNull(plugin.getConfig().getString("api.url")));
    }

    String getAPIKey() {
        if (plugin.getConfig().getString("api.key") == null) { return null; }
        return Objects.requireNonNull(plugin.getConfig().getString("api.key"));
    }

    void checkConfig() {
        plugin.reloadConfig();
        boolean changed = false;
        if (getAPIURL() == null) {
            plugin.getLogger().severe("You must set your AxionSpire API URL in the config file.");
        }
        if (getAPIKey() == null || Objects.equals(getAPIKey(), "PLACEHOLDER")) {
            plugin.getLogger().severe("You must set your AxionSpire API key in the config file.");
        }
        if (plugin.getConfig().getString("Prefix") == null) {
            plugin.getLogger().warning("No message prefix was set in the config, setting a default...");
            plugin.getConfig().set("Prefix", "&8[&6&lAxionBridge&r&8]");
            changed = true;
        }
        if (!plugin.getConfig().isSet("CheckForUpdates")) {
            plugin.getLogger().warning("No check for updates value was set in the config, setting a default...");
            plugin.getConfig().set("CheckForUpdates", true);
            changed = true;
        }
        if (plugin.getConfig().getInt("StatsTimer") > 0) {
            plugin.getLogger().warning("No stats timer value was set in the config, setting a default...");
            plugin.getConfig().set("StatsTimer", 300);
            changed = true;
        }
        if (changed) { plugin.saveConfig(); }
    }

    void checkForUpdates() throws JsonProcessingException {
        if (plugin.getConfig().getBoolean("CheckForUpdates")) {
            HttpRequest check = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://api.github.com/repos/AxionSpire/AxionBridge/releases/latest"))
                    .build();
            HttpResponse<String> response = null;
            try {
                response = HttpClient.newHttpClient().send(check, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                plugin.getLogger().warning("Failed to check for updates.");
                e.printStackTrace();
            }
            assert response != null;
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            String latestVersion = jsonNode.get("tag_name").asText();
            String version = "v" + plugin.getDescription().getVersion();
            if (!version.equals(latestVersion)) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getLogger().info("An update is available!");
                    plugin.getLogger().info("Your version: " + version + " - Latest version: " + latestVersion);
                    plugin.getLogger().info("Please update at https://github.com/AxionSpire/AxionBridge/releases!");
                }, 60L);
            }
        }
    }
}

