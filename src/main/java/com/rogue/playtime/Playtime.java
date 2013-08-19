/*
 * Copyright (C) 2013 Spencer Alderman
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
package com.rogue.playtime;

import com.rogue.playtime.command.CommandHandler;
import com.rogue.playtime.config.ConfigurationLoader;
import com.rogue.playtime.data.DataManager;
import com.rogue.playtime.event.EventHandler;
import com.rogue.playtime.executables.ExecutiveManager;
import com.rogue.playtime.lang.Cipher;
import com.rogue.playtime.listener.ListenerManager;
import com.rogue.playtime.listener.listeners.AFKListener;
import com.rogue.playtime.metrics.Metrics;
import com.rogue.playtime.player.PlayerHandler;
import com.rogue.playtime.runnable.AFKRunnable;
import com.rogue.playtime.runnable.UpdateRunnable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class
 *
 * @since 1.0
 * @author 1Rogue
 * @version 1.4.1
 */
public class Playtime extends JavaPlugin {

    protected int debug = 0;
    protected ExecutiveManager execmanager;
    protected ListenerManager listener;
    protected PlayerHandler phandler;
    protected CommandHandler chandler;
    protected DataManager dmanager;
    protected ConfigurationLoader cloader;
    protected EventHandler ehandler;
    protected Cipher lang;
    private boolean isUpdate = false;
    private boolean isBusy = true;
    private boolean reloaded = false;

    /**
     * Registers the plugin configuration file and language system.
     *
     * @since 1.0
     * @version 1.3.0
     */
    @Override
    public void onLoad() {

        this.getLogger().info("Loading Configuration mananger...");
        cloader = new ConfigurationLoader(this);

        try {
            Thread.sleep(500L);
        } catch (InterruptedException ex) {
            Logger.getLogger(Playtime.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.getLogger().info("Loading language manager...");
        lang = new Cipher(this);

    }

    /**
     * Registers debug, metrics, commands, data management, and listeners.
     *
     * @since 1.0
     * @version 1.4.1
     */
    @Override
    public void onEnable() {
        final long startTime = System.nanoTime();

        if (Bukkit.getOnlinePlayers().length > 0) {
            reloaded = true;
        }

        debug = cloader.getInt("general.debug-level");
        if (debug > 3) {
            debug = 3;
        }
        if (debug < 0) {
            debug = 0;
        }
        if (debug >= 1) {
            this.getLogger().info(lang.getString("main.debug", debug));
        }

        try {
            Metrics metrics = new Metrics(this);
            getLogger().info(lang.getString("main.metrics"));
            metrics.start();
        } catch (IOException ex) {
            Logger.getLogger(Playtime.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.getLogger().info(lang.getString("main.execs"));
        execmanager = new ExecutiveManager(this);

        if (cloader.getBoolean("general.update-check")) {
            execmanager.runAsyncTask(new UpdateRunnable(this), 10L);
        } else {
            this.getLogger().info(lang.getString("main.update"));
        }

        this.getLogger().info(lang.getString("main.data"));
        dmanager = new DataManager(this, true);

        this.getLogger().info(lang.getString("main.command"));
        chandler = new CommandHandler(this);
        chandler.registerExecs();
        
        boolean deathEnabled = this.cloader.getBoolean("check.death-time");
        boolean onlineEnabled = this.cloader.getBoolean("check.online-time");


        if (this.cloader.getBoolean("afk.enabled")) {
            this.getLogger().info(lang.getString("main.player"));
            phandler = new PlayerHandler(this, cloader.getInt("afk.interval"), cloader.getInt("afk.timeout"));
            execmanager.runAsyncTaskRepeat(new AFKRunnable(this), phandler.getAFKCheckInterval(), phandler.getAFKCheckInterval());
        } else {
            this.getLogger().info(lang.getString("main.afk"));
            phandler = null;
        }

        if (this.cloader.getBoolean("events.enabled")) {
            this.getLogger().info(lang.getString("main.event"));
            ehandler = new EventHandler(this);
        } else {
            this.getLogger().info(lang.getString("main.event-disabled"));
        }

        this.getLogger().info(lang.getString("main.listener"));
        listener = new ListenerManager(this);

        final long endTime = System.nanoTime();
        if (reloaded) {
            AFKListener afkl = (AFKListener) listener.getListener("afk");
            if (afkl != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    afkl.onPlayerJoin(new PlayerJoinEvent(p, ""));
                }
            }
        }
        this.setBusy(false);
        if (debug >= 1) {
            final long duration = endTime - startTime;
            this.getLogger().info(lang.getString("main.enabled", this.readableProfile(duration)));
        }
    }

    /**
     * Closes tasks and sql connections on plugin disabling.
     *
     * @since 1.0
     * @version 1.3.0
     */
    @Override
    public void onDisable() {
        execmanager.cancelAllTasks();
        dmanager.getDataHandler().cleanup();
    }

    /**
     * Reloads the plugin
     *
     * @since 1.4.0
     * @version 1.4.0
     *
     * @param names Players to notify when the reload is complete
     */
    public void reload(String... names) {
        String reloadDone = lang.getString("main.reloaded");
        this.setBusy(true);
        this.onDisable();
        try {
            Thread.sleep(250L);
        } catch (InterruptedException ex) {
            Logger.getLogger(Playtime.class.getName()).log(Level.SEVERE, null, ex);
        }
        debug = 0;
        execmanager = null;
        listener = null;
        phandler = null;
        chandler = null;
        dmanager = null;
        cloader = null;
        ehandler = null;
        lang = null;
        isUpdate = false;
        this.onLoad();
        this.onEnable();
        this.getLogger().info(reloadDone);
        for (String s : names) {
            this.getServer().getPlayer(s).sendMessage(_(reloadDone));
        }
    }

    /**
     * Makes a long-type time value into a readable string.
     *
     * @since 1.0
     * @version 1.0
     *
     * @param time The time value as a long
     * @return Readable String of the time
     */
    private String readableProfile(long time) {
        int i;
        String[] units = new String[]{"ms", "s", "m", "hr", "day", "week", "mnth", "yr"};
        int[] metric = new int[]{1000, 60, 60, 24, 7, 30, 12};
        long current = TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);

        for (i = 0; current > metric[i]; i++) {
            current /= metric[i];
        }

        return current + " " + units[i] + ((current > 1 && i > 1) ? "s" : "");
    }

    public String readableHours(long time) {
        return null;
    }

    /**
     * Finds a match for an online player, or returns the provided string if
     * there is no good match.
     *
     * @since 1.1
     * @version 1.1
     *
     * @param username The player to look up
     * @return A potential match for a player
     */
    public String getBestPlayer(String username) {
        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            username = player.getName();
        }
        return username;
    }

    /**
     * Gets the level of debugging for players
     *
     * @since 1.1
     * @version 1.1
     *
     * @return The debug level
     */
    public int getDebug() {
        return debug;
    }

    /**
     * Returns an instance of the Playtime Plugin
     *
     * @since 1.2.0
     * @version 1.2.0
     *
     * @return The Playtime plugin instance
     */
    public static Playtime getPlugin() {
        return (Playtime) Bukkit.getPluginManager().getPlugin("Playtime");
    }

    /**
     * Returns Playtime's listener manager
     *
     * @since 1.2.0
     * @version 1.4.1
     *
     * @return The listener manager for Playtime
     */
    public ListenerManager getListenerManager() {
        return listener;
    }

    /**
     * Returns the player handler for Playtime NOTE: If AFK is disabled, this
     * will return null
     *
     * @since 1.2.0
     * @version 1.2.0
     *
     * @return The plugin's player handler
     */
    public PlayerHandler getPlayerHandler() {
        return phandler;
    }

    /**
     * Returns the status of the update check
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return true if update, false if none or no check made.
     */
    public boolean isUpdateAvailable() {
        return isUpdate;
    }

    /**
     * Sets whether or not an update is available. Should only be called by the
     * update task.
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @param status true if latest version, otherwise false
     * @return The updated value
     */
    public boolean setUpdateStatus(boolean status) {
        isUpdate = status;
        return isUpdate;
    }

    /**
     * Converts pre-made strings to have chat colors in them and adds a tag for
     * the plugin name.
     *
     * @param encoded String with unconverted color codes
     * @return string with correct chat colors included
     */
    public static String _(String encoded) {
        return ChatColor.translateAlternateColorCodes('&', "[&e" + Playtime.getPlugin().getDescription().getName() + "&f] &6" + encoded);
    }

    /**
     * Returns Playtime's abstract data manager
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return abstract data manager for Playtime
     */
    public DataManager getDataManager() {
        return dmanager;
    }

    /**
     * Gets the configuration manager for Playtime
     *
     * @since 1.3.0
     * @version 1.3.0
     *
     * @return The main ConfigurationLoader
     */
    public ConfigurationLoader getConfigurationLoader() {
        return cloader;
    }

    /**
     * Returns the language loader for Playtime
     *
     * @since 1.4.0
     * @version 1.4.0
     *
     * @return The language file loader
     */
    public Cipher getCipher() {
        return lang;
    }

    /**
     * Returns the event system for Playtime
     *
     * @since 1.4.0
     * @version 1.4.0
     *
     * @return Playtime's event handler
     */
    public EventHandler getEventHandler() {
        return ehandler;
    }

    /**
     * Returns the manager for runnables that Playtime uses
     *
     * @since 1.4.0
     * @version 1.4.0
     *
     * @return Playtime's executive manager
     */
    public ExecutiveManager getExecutiveManager() {
        return execmanager;
    }

    /**
     * Returns whether or not the plugin is busy. Used mostly for when the
     * database is being converted.
     *
     * @since 1.4.0
     * @version 1.4.0
     *
     * @return true if busy, false if not
     */
    public boolean isBusy() {
        return isBusy;
    }

    /**
     * Sets the plugin's "busy" mode".
     *
     * @since 1.4.0
     * @version 1.4.0
     *
     * @param busy What value to set
     * @return The updated busy status
     */
    public boolean setBusy(boolean busy) {
        isBusy = busy;
        return isBusy;
    }
    
    /**
     * Returns whether the plugin is running for the first time or not
     * 
     * This is used for verifying whether or not to run functions that are used
     * when the server first starts up
     * 
     * @since 1.4.0
     * @version 1.4.1
     * 
     * @return If this is the plugin's first run from booting
     */
    public boolean firstRun() {
        return !reloaded;
    }
}