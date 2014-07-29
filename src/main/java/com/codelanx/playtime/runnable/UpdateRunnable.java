/*
 * Copyright (C) 2013 AE97
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
package com.codelanx.playtime.runnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import com.codelanx.playtime.Playtime;

/**
 * @since 1.1
 * @author Lord_Ralex
 * @author 1Rogue
 * @version 1.4.0
 */
public class UpdateRunnable implements Runnable {

    private static final String VERSION_URL = "https://raw.github.com/1Rogue/Playtime/master/VERSION";
    private Boolean isLatest = null;
    private String latest;
    private final String version;
    private final Playtime plugin;

    public UpdateRunnable(Playtime plugin) {
        this.plugin = plugin;
        this.version = plugin.getDescription().getVersion();
    }

    public void run() {
        if (version.endsWith("SNAPSHOT") || version.endsWith("DEV")) {
            plugin.getLogger().warning(plugin.getCipher().getString("runnable.update.dev"));
            isLatest = true;
            return;
        }
        try {
            URL call = new URL(VERSION_URL);
            InputStream stream = call.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            latest = reader.readLine();
            reader.close();
            this.plugin.setUpdateStatus(!latest.equalsIgnoreCase(version));
        } catch (MalformedURLException ex) {
            plugin.getLogger().log(Level.SEVERE, plugin.getCipher().getString("runnable.update.error"), this.plugin.getDebug() >= 3 ? ex : "");
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, plugin.getCipher().getString("runnable.update.error"), this.plugin.getDebug() >= 3 ? ex : "");
        }
    }

    /**
     * Returns whether or not there is an update
     * 
     * @since 1.1
     * @version 1.4.0
     * 
     * @return True if there is an update, false otherwise
     * @throws IllegalStateException 
     */
    public boolean isUpdate() throws IllegalStateException {
        if (isLatest == null) {
            throw new IllegalStateException(plugin.getCipher().getString("runnable.update.version-error"));
        }
        return !isLatest;
    }
}
