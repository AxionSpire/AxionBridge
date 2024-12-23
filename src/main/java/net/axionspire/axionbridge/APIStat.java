package net.axionspire.axionbridge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APIStat {
    private String statID;
    private ResolveMethod resolveMethod;
    private CheckedPlayers checkedPlayers;
    private String placeholder = null;
    private RegisteredStatProvider registeredStatProvider = null;

    public APIStat(@NotNull String statID, @NotNull ResolveMethod resolveMethod, @NotNull CheckedPlayers checkedPlayers, @Nullable String placeholder, @Nullable RegisteredStatProvider registeredStatProvider) {
        this.statID = statID;
        this.resolveMethod = resolveMethod;
        this.checkedPlayers = checkedPlayers;
        if (placeholder != null && resolveMethod.equals(ResolveMethod.PLACEHOLDERAPI)) { this.placeholder = placeholder; }
        if (registeredStatProvider != null && resolveMethod.equals(ResolveMethod.REGISTERED)) { this.registeredStatProvider = registeredStatProvider; }
    }

    public String getStatID() { return statID; }
    public ResolveMethod getResolveMethod() { return resolveMethod; }
    public CheckedPlayers getCheckedPlayers() { return checkedPlayers; }
    public String getPlaceholder() { return placeholder; }
    public RegisteredStatProvider getRegisteredStatProvider() { return registeredStatProvider; }

    public enum ResolveMethod {
        REGISTERED,
        PLACEHOLDERAPI
    }
    public enum CheckedPlayers {
        ALL,
        ONLINE
    }
}
