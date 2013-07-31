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
package com.rogue.playtime.event;

import com.rogue.playtime.Playtime;
import com.rogue.playtime.runnable.EventRunnable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @since 1.4.0
 * @author 1Rogue
 * @version 1.4.0
 */
public class EventHandler {

    private Playtime plugin;
    private YamlConfiguration yaml = null;
    private File file;
    private Map<String, BukkitTask> events = new HashMap();
    private Map<String, Event> yamlEvents = new HashMap();
    private Map<String, Integer> eventTimes = new HashMap();

    public EventHandler(Playtime p) {
        plugin = p;
        file = new File(plugin.getDataFolder(), "events.yml");
    }

    public void loadEvents() {
        if (plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!file.exists()) {
            plugin.saveResource("events.yml", true);
            return;
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection eventSection = yaml.getConfigurationSection("events");
        int interval = plugin.getConfigurationLoader().getInt("events.interval");
        if (plugin.getDataManager().getDataHandler().getName().equals("flatfile")) {
            for (String s : eventSection.getKeys(false)) {
                evalYAMLEvent(s);
            }
        } else {
            for (String s : eventSection.getKeys(false)) {
                evalEvent(s, interval);
            }
        }

    }

    private void evalEvent(String name, int interval) {
        String timer = yaml.getString("events." + name + ".timer");
        if (!timer.equalsIgnoreCase("deathtime") && !timer.equalsIgnoreCase("onlinetime") && !timer.equalsIgnoreCase("playtime")) {
            timer = "playtime";
        }
        List<String> commands = yaml.getStringList("events." + name + ".commands");
        if (commands.isEmpty()) {
        }
        Integer seconds = yaml.getInt("events." + name + ".time");
        boolean repeat = yaml.getBoolean("events." + name + ".repeat");
        events.put(name, Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new EventRunnable(plugin, name, timer, seconds / 60, (seconds + interval) / 60, commands, repeat), interval * 20L, interval * 20L));
    }

    private void evalYAMLEvent(String name) {
        String timer = yaml.getString("events." + name + ".timer");
        if (!timer.equalsIgnoreCase("deathtime") && !timer.equalsIgnoreCase("onlinetime") && !timer.equalsIgnoreCase("playtime")) {
            timer = "playtime";
        }
        List<String> commands = yaml.getStringList("events." + name + ".commands");
        if (commands.isEmpty()) {
        }
        Integer seconds = yaml.getInt("events." + name + ".time");
        boolean repeat = yaml.getBoolean("events." + name + ".repeat");
        eventTimes.put(name, seconds / 60);
        yamlEvents.put(name, new Event(name, timer, seconds/60, commands, repeat));
    }

    public Map<String, Integer> getTimes() {
        return eventTimes;
    }

    public void fireYAMLEvents(List<String> fire, String username) {
        ConsoleCommandSender ccs = Bukkit.getConsoleSender();
        for (String s : fire) {
            for (String c : yamlEvents.get(s).getCommands()) {
                Bukkit.dispatchCommand(ccs, c.replace("%u", username));
            }
        }
    }
    
    public void cancelChecks() {
        for (String s : events.keySet()) {
            events.get(s).cancel();
        }
    }
}
