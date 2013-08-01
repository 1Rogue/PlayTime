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
package com.rogue.playtime.command.commands;

import com.rogue.playtime.command.CommandBase;
import static com.rogue.playtime.Playtime.__;
import static com.rogue.playtime.command.CommandBase.plugin;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 *
 * @since 1.4.0
 * @author 1Rogue
 * @version 1.4.0
 */
public class PlayTopCommand implements CommandBase {

    public boolean execute(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        boolean scoreboard = false;
        if (sender instanceof Player) {
            scoreboard = true;
        }
        int i = 5;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("clear") && scoreboard) {
                Player p = (Player) sender;
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                return true;
            }
            try {
                i = Integer.parseInt(args[0]);
                if (i < 1) {
                    i = 1;
                }
                if (i > 10) {
                    i = 10;
                }
            } catch (NumberFormatException e) {}
        }
        Map<String, Integer> players = plugin.getDataManager().getDataHandler().getTopPlayers("playtime", i);
        if (players == null) {
            sender.sendMessage(__(plugin.getCipher().getString("command.commands.playtop.disabled-flatfile")));
        }
        if (scoreboard) {
            Player p = (Player)sender;
            ScoreboardManager sbm = Bukkit.getScoreboardManager();
            Scoreboard scoreBoard = sbm.getNewScoreboard();
            Objective objv = scoreBoard.registerNewObjective(plugin.getCipher().getString("command.commands.playtop.title"), "dummy");
            objv.setDisplaySlot(DisplaySlot.SIDEBAR);
            objv.setDisplayName(plugin.getCipher().getString("command.commands.playtop.title-shown"));
            Score score;
            for (String s : players.keySet()) {
                score = objv.getScore(Bukkit.getOfflinePlayer(s));
                score.setScore(players.get(s)/60);
            }
            p.setScoreboard(scoreBoard);
            p.sendMessage(__(plugin.getCipher().getString("command.commands.playtop.clear")));
            
        } else {
            StringBuilder sb = new StringBuilder(plugin.getCipher().getString("command.commands.playtop.console-title", i));
            for (String s : players.keySet()) {
                sb.append('\n').append(s).append(" - ").append(players.get(s)/60);
            }
            sender.sendMessage(sb.toString());
        }
        return true;
    }

    public String getName() {
        return "playtimetop";
    }
}
