package net.axionspire.axionbridge;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerEXPStat extends RegisteredStatProvider {
    @Override
    public String pullStats(OfflinePlayer player) {
        if (!player.isOnline()) { return null; }
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) { return null; }
        return onlinePlayer.getLevel() + "";
    }

    @Override
    public @NotNull String getMethodID() {
        return "Base:ExperienceLevels";
    }
}
