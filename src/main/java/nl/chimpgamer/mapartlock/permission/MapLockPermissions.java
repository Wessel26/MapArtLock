package nl.chimpgamer.mapartlock.permission;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

public final class MapLockPermissions {
    public static final String USER = "mapart.*";
    public static final String ADMIN = "mapart.admin";

    private MapLockPermissions() {
    }

    public static boolean canOpenMapArt(CommandSender sender) {
        return hasUserRights(sender) || isAdmin(sender);
    }

    public static boolean canReload(CommandSender sender) {
        return isAdmin(sender);
    }

    public static boolean canViewVersion(CommandSender sender) {
        return isAdmin(sender);
    }

    public static boolean canManageOthers(CommandSender sender) {
        return isAdmin(sender);
    }

    public static boolean canBypassProtection(CommandSender sender) {
        return isAdmin(sender);
    }

    public static boolean hasUserRights(CommandSender sender) {
        return has(sender, USER);
    }

    public static boolean isAdmin(CommandSender sender) {
        return hasExact(sender, ADMIN);
    }

    private static boolean has(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }

    private static boolean hasExact(CommandSender sender, String permission) {
        return sender.getEffectivePermissions().stream()
                .filter(info -> info.getPermission().equalsIgnoreCase(permission))
                .findFirst()
                .map(PermissionAttachmentInfo::getValue)
                .orElse(false);
    }
}
