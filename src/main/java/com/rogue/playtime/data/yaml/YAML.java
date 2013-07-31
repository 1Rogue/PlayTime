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
package com.rogue.playtime.data.yaml;

import com.rogue.playtime.Playtime;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @since 1.3.0
 * @author 1Rogue
 * @version 1.3.0
 */
public class YAML {
    
    private YamlConfiguration yaml;
    private final String path;
    private Playtime plugin = Playtime.getPlugin();
    
    public YAML() {
        path = plugin.getDataFolder() + File.separator + "users.yml";
        File users = new File(path);
        try {
            yaml = this.makeYAML(users);
        } catch (IOException ex) {
            Logger.getLogger(YAML.class.getName()).log(Level.SEVERE, "Error creating user YAML file!", ex);
        }
    }
    
    /**
     * Increases an int value within the data file by 1, and fires events if triggered
     * 
     * For the users yml file, the key should be in the format:
     * "users.[username].[value]"
     * 
     * @since 1.3.0
     * @version 1.4.0
     * 
     * @param key The path to the data to edit
     */
    public void incrementValue(String key) {
        int i = yaml.getInt(key);
        i++;
        boolean eventFired = false;
        Map<String, Integer> events = plugin.getEventHandler().getTimes();
        List<String> fire = new ArrayList();
        for (String s : events.keySet()) {
            if (i == events.get(s)) {
                eventFired = true;
                fire.add(s);
            }
        }
        if (eventFired) {
            plugin.getEventHandler().fireYAMLEvents(fire, key.split(".")[1]);
        }
        yaml.set(key, i);
    }
    
    /**
     * Returns a YamlConfiguration of the data file.
     * 
     * @since 1.3.0
     * @version 1.3.0
     * 
     * @return The YamlConfiguration
     */
    public YamlConfiguration getFile() {
        return yaml;
    }
    
    /**
     * Forces a save of the current yaml data file in memory.
     * 
     * @since 1.3.0
     * @version 1.3.0
     */
    public void forceSave() {
        try {
            yaml.save(path);
        } catch (IOException ex) {
            Logger.getLogger(YAML.class.getName()).log(Level.SEVERE, "Error saving user yaml configuration!", ex);
        }
    }
    
    /**
     * Creates a YAML file if none exists yet, and returns it as a
     * YamlConfiguration
     * 
     * @since 1.3.0
     * @version 1.3.0
     * 
     * @param yaml The path to the file
     * @return the file path made into a YamlConfiguratino
     * @throws IOException 
     */
    private YamlConfiguration makeYAML(File yaml) throws IOException {
        if (yaml.exists()) {
            return YamlConfiguration.loadConfiguration(yaml);
        }
        yaml.createNewFile();
        return YamlConfiguration.loadConfiguration(yaml);
    }

}
