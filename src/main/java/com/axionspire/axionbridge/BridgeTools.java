package com.axionspire.axionbridge;

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

    public static BridgeTools getInstance() {
        if (instance == null) {
            instance = new BridgeTools();
        }
        return instance;
    }

    public void setPlugin(AxionBridge plugin) {
        this.plugin = plugin;
    }

    public String getPrefix() {
        return (ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("Prefix"))) + " ");
    }

    public void checkForUpdates() throws JsonProcessingException {
        if (plugin.getConfig().getBoolean("CheckForUpdates")) {
            HttpRequest check = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://api.github.com/repos/AxionSpire/AxionBridge/releases/latest"))
                    .build();
            HttpResponse<String> response = null;
            try {
                response = HttpClient.newHttpClient().send(check, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                plugin.getLogger().info("Failed to check for updates.");
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

    URI getAPIURL() {
        if (plugin.getConfig().getString("api.url") == null) { return null; }
        return URI.create(Objects.requireNonNull(plugin.getConfig().getString("api.url")));
    }

    String getAPIKey() {
        if (plugin.getConfig().getString("api.key") == null) { return null; }
        return Objects.requireNonNull(plugin.getConfig().getString("api.key"));
    }
}

