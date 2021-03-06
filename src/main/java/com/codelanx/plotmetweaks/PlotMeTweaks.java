/*
 * Copyright (C) 2014 Codelanx
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.codelanx.plotmetweaks;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.commands.BiomeCommands;
import com.sk89q.worldedit.commands.ClipboardCommands;
import com.sk89q.worldedit.commands.RegionCommands;
import com.sk89q.worldedit.commands.UtilityCommands;
import com.sk89q.worldedit.regions.Region;
import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @since 1.0.0
 * @author 1Rogue
 * @version 1.0.0
 */
public class PlotMeTweaks extends JavaPlugin implements Listener {

    private final Set<String> cmds = new HashSet<>();
    private String prefix;

    /**
     * This will load any commands in use by WorldEdit under Command class
     * handlers that should be checked. It will remove known "okay" commands in
     * use at the time of this plugin's creation.<br /><br />
     *
     * {@inheritDoc}
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    @Override
    public void onEnable() {
        prefix = ChatColor.WHITE + "[" + ChatColor.RED + this.getName() + ChatColor.WHITE + "] ";
        
        this.addCommands(UtilityCommands.class.getDeclaredMethods());
        this.addCommands(BiomeCommands.class.getDeclaredMethods());
        this.addCommands(ClipboardCommands.class.getDeclaredMethods());
        this.addCommands(RegionCommands.class.getDeclaredMethods());

        //BiomeCommands.class
        cmds.remove("biomeinfo");
        cmds.remove("biomelist");

        //ClipboardCommands.class
        cmds.remove("clearclipboard");
        cmds.remove("paste");
        cmds.remove("flip");
        cmds.remove("load");
        cmds.remove("rotate");
        cmds.remove("save");
        cmds.remove("schematic");
        
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Adds commands from method names. For use with WorldEdit Command Handlers
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     * @param methods Any methods to add
     */
    public void addCommands(Method... methods) {
        for (Method m : methods) {
            String cmd = m.getName().toLowerCase();
            this.cmds.add(cmd);
        }
    }

    /**
     * Handles WorldEdit commands and verifies that they can be run and the edit
     * will be contained within a specific plot. If it isn't contained, the
     * command event is cancelled.<br /><br />
     *
     * {@inheritDoc}
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     * @param event The {@link PlayerCommandPreprocessEvent} to listen to
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().split(" ")[0];
        command = command.replaceAll("\\/", "");
        command = command.toLowerCase();
        if (this.cmds.contains(command)) {
            Region reg = null;
            try {
                reg = WorldEdit.getInstance().getSession(event.getPlayer().getName()).getSelection(BukkitUtil.getLocalWorld(event.getPlayer().getWorld()));
            } catch (IncompleteRegionException ex) {
                event.getPlayer().sendMessage(this.prefix + ChatColor.RED + "You must select a full region first!");
                return;
            }
            if (reg == null) {
                event.getPlayer().sendMessage(this.prefix + ChatColor.RED + "Error getting region selection!");
            }
            if (!event.getPlayer().hasPermission("weplotrestrict.override") && !this.verifyRegion(reg, event.getPlayer().getWorld())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(this.prefix + ChatColor.RED + "Selection area must remain wtihin a single plot!");
            }
        }
    }

    /**
     * Verifies if a region is contained within a single {@link Plot}
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     * @param reg The {@link Region} to check
     * @param world The {@link World} the region is in
     * @return {@code true} if it is contained in a single {@link Plot}
     */
    public boolean verifyRegion(Region reg, World world) {
        Vector max = reg.getMaximumPoint();
        Vector min = reg.getMinimumPoint();
        Plot maxp = PlotManager.getPlotById(new Location(world, max.getBlockX(), max.getBlockY(), max.getBlockZ()));
        Plot minp = PlotManager.getPlotById(new Location(world, min.getBlockX(), min.getBlockY(), min.getBlockZ()));
        if (minp == null || maxp == null) {
            return false;
        }
        return maxp.equals(minp);
    }

}
