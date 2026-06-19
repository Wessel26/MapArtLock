package nl.chimpgamer.mapartlock.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class PluginSettings {
    private boolean openOnRightClick;
    private boolean enableGlint;
    private boolean showOwnerInLore;
    private boolean blockAnvil;
    private boolean blockCrafting;
    private boolean blockCartography;
    private boolean blockCrafter;
    private boolean adminBypassProtection;

    public PluginSettings(FileConfiguration config) {
        reload(config);
    }

    public void reload(FileConfiguration config) {
        this.openOnRightClick = config.getBoolean("open-on-right-click", true);
        this.enableGlint = config.getBoolean("enable-glint", true);
        this.showOwnerInLore = config.getBoolean("show-owner-in-lore", true);
        this.blockAnvil = config.getBoolean("block-anvil", true);
        this.blockCrafting = config.getBoolean("block-crafting", true);
        this.blockCartography = config.getBoolean("block-cartography", true);
        this.blockCrafter = config.getBoolean("block-crafter", true);
        this.adminBypassProtection = config.getBoolean("admin-bypass-protection", false);
    }

    public boolean openOnRightClick() {
        return openOnRightClick;
    }

    public boolean enableGlint() {
        return enableGlint;
    }

    public boolean showOwnerInLore() {
        return showOwnerInLore;
    }

    public boolean blockAnvil() {
        return blockAnvil;
    }

    public boolean blockCrafting() {
        return blockCrafting;
    }

    public boolean blockCartography() {
        return blockCartography;
    }

    public boolean blockCrafter() {
        return blockCrafter;
    }

    public boolean adminBypassProtection() {
        return adminBypassProtection;
    }
}
