package rip.diamond.practice.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import rip.diamond.practice.Eden;
import rip.diamond.practice.Language;
import rip.diamond.practice.match.team.Team;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Util {

    public static boolean isNull(String str) {
        return str == null || str.equalsIgnoreCase("null");
    }

    public static boolean isNotNull(String str) {
        return !isNull(str);
    }

    public static void damage(Player player, double damage) {
        EntityDamageEvent event = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, damage);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            player.damage(damage);
        }
    }

    public static void setBlockFast(final Location location, final Material material, final boolean applyPhysics) {
        setBlockFast(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), material.getId(), (byte) 0, applyPhysics);
    }
    public static void setBlockFast(final World world, final int x, final int y, final int z, final int blockId, final byte data, final boolean applyPhysics) {
        try {
            final net.minecraft.server.v1_8_R3.World w = ((CraftWorld) world).getHandle();
            final net.minecraft.server.v1_8_R3.Chunk chunk = w.getChunkAt(x >> 4, z >> 4);
            final BlockPosition bp = new BlockPosition(x, y, z);
            final int combined = blockId + (data << 12);
            final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);
            w.setTypeAndData(bp, ibd, applyPhysics ? 3 : 2);
            chunk.a(bp, ibd);
        } catch (final Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static Location getBedBlockNearBy(Location location) {
        Location bedLocation2 = location.clone();

        if (location.clone().add(1,0,0).getBlock().getType() == Material.BED_BLOCK) {
            return location.clone().add(1,0,0);
        }if (location.clone().add(-1,0,0).getBlock().getType() == Material.BED_BLOCK) {
            return location.clone().add(-1,0,0);
        }if (location.clone().add(0,0,1).getBlock().getType() == Material.BED_BLOCK) {
            return location.clone().add(0,0,1);
        }if (location.clone().add(0,0,-1).getBlock().getType() == Material.BED_BLOCK) {
            return location.clone().add(0,0,-1);
        }
        Common.log("Cannot get another side of bed block!");
        return bedLocation2;
    }

    public static int getNewRating(int rating, int opponentRating, double score) {
        double kFactor = 32;
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, ((double) (opponentRating - rating) / 400.0)));
        return rating + (int) (kFactor * (score - expectedScore));
    }

    public static String getProgressBar(int current, int max, int totalBars, String symbol, String completedColor, String notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) ((int) totalBars * percent);
        int leftOver = (totalBars - progressBars);
        StringBuilder sb = new StringBuilder();
        sb.append(CC.translate(completedColor));
        for (int i = 0; i < progressBars; i++) {
            sb.append(symbol);
        }
        sb.append(CC.translate(notCompletedColor));
        for (int i = 0; i < leftOver; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    public static String renderPointsAsBar(Team team) {
        return Util.getProgressBar(team.getPoints(), Eden.INSTANCE.getConfigFile().getInt("match.maximum-points"), Eden.INSTANCE.getConfigFile().getInt("match.maximum-points"), "⬤", team.getTeamColor().getColor(), CC.GRAY);
    }

    public static String renderBuildLimit(int current, int max) {
        int height = max - current;
        if (height <= 0) {
            return CC.GRAY + " ┃ " + CC.RED + Language.REACHED_BUILD_LIMIT.toString();
        } else if (height <= 5) {
            return CC.GRAY + " ┃ " + CC.GOLD + height;
        } else if (height <= 10) {
            return CC.GRAY + " ┃ " + CC.YELLOW + height;
        } else if (height <= 15) {
            return CC.GRAY + " ┃ " + CC.GREEN + height;
        }
        return "";
    }

    public static Collection<Class<?>> getClassesInPackage(Plugin plugin, String packageName) {
        Collection<Class<?>> classes = new ArrayList<>();

        CodeSource codeSource = plugin.getClass().getProtectionDomain().getCodeSource();
        URL resource = codeSource.getLocation();
        String relPath = packageName.replace('.', '/');
        String resPath = resource.getPath().replace("%20", " ");
        String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
        JarFile jarFile;

        try {
            jarFile = new JarFile(jarPath);
        } catch (IOException e) {
            throw (new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e));
        }

        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            String className = null;

            if (entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
                className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
            }

            if (className != null) {
                Class<?> clazz = null;

                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        }

        try {
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (ImmutableSet.copyOf(classes));
    }

    //Credit: https://github.com/lulu2002/DatouNms/blob/master/src/main/java/me/lulu/datounms/v1_8_R3/CraftCommonNMS.java
    public static void playDeathAnimation(Player player, List<Player> viewers) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = (( CraftWorld ) player.getWorld()).getHandle();
        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld, cp.getProfile(), new PlayerInteractManager(nmsWorld));
        npc.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, cp.getHandle());
        PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc);
        PacketPlayOutNamedEntitySpawn entitySpawn = new PacketPlayOutNamedEntitySpawn(npc);
        PacketPlayOutEntityStatus entityDeath = new PacketPlayOutEntityStatus(npc, ( byte ) 3);

        for (Player o : viewers) {
            PlayerConnection connection = (( CraftPlayer ) o).getHandle().playerConnection;
            connection.sendPacket(removePlayer);
            connection.sendPacket(addPlayer);
            connection.sendPacket(entitySpawn);
            connection.sendPacket(entityDeath);
        }
        Tasks.runLater(()-> {
            for (Player o : viewers) {
                if (o.isOnline()) {
                    PlayerConnection connection = (( CraftPlayer ) o).getHandle().playerConnection;
                    connection.sendPacket(removePlayer);
                }
            }
        }, 1L);
    }

}