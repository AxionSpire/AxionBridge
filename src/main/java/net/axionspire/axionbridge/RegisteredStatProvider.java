package net.axionspire.axionbridge;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class RegisteredStatProvider {
    public abstract String pullStats(OfflinePlayer player);

    @NotNull
    public abstract String getMethodID();
}