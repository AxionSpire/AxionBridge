package com.axionspire.axionbridge;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class AxionBridge extends JavaPlugin {
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new BridgeListener(this), this);
        BridgeTools.getInstance().setPlugin(this);
        BridgeTools.getInstance().checkForUpdates();
        testConnection();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    void testConnection() {
        HttpRequest check = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BridgeTools.getInstance().getAPIURL() + "/healthcheck"))
                .header("Authorization", "Bearer " + BridgeTools.getInstance().getAPIKey())
                .header("Content-Type", "application/json")
                .header("AxionBridge-Version", getDescription().getVersion())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(check, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            getLogger().info("Failed to check for updates.");
            e.printStackTrace();
        }
        assert response != null;
        String json = response.body();
    }
}
