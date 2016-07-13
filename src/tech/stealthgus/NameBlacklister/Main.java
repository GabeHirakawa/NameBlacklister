package tech.stealthgus.NameBlacklister;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import net.scrumplex.sprummlbot.plugins.Config;
import net.scrumplex.sprummlbot.plugins.SprummlbotPlugin;
import net.scrumplex.sprummlbot.plugins.events.ClientJoinEventHandler;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/*
*This program is free software: you can redistribute it and/or modify
*it under the terms of the GNU General Public License as published by
*the Free Software Foundation, either version 3 of the License, or
*(at your option) any later version.
*
*This program is distributed in the hope that it will be useful,
*but WITHOUT ANY WARRANTY; without even the implied warranty of
*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*GNU General Public License for more details.
*
*You should have received a copy of the GNU General Public License
*along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

public class Main extends SprummlbotPlugin {

    public void onEnable(){
        nameConfig();
        nameEvent();
    }

    public void nameConfig(){
        Config conf = getConfig();
        Ini defaultIni = new Ini();
        Profile.Section defaultSec = defaultIni.add("Settings");
        Profile.Section nameSec = defaultIni.add("Names");
        defaultSec.putComment("kickMessage", "Message that will be in the kick reason for having an illegal name");
        defaultSec.put("kickMessage", "Kicked for having the following in your username:");
        defaultSec.putComment("timer", "Time in seconds the plugin will check usernames. Default = 60 seconds");
        defaultSec.put("timer", "60");
        defaultSec.putComment("debug", "See what the plugin is doing");
        defaultSec.put("debug", false);
        nameSec.putComment("name", "If client's username CONTAINS the following string, they will be punished. WARNING SETTING THIS TO BLANK MAY KICK ALL USERS.");
        nameSec.put("name", "RANDOMSTRING");
        conf.setDefaultConfig(defaultIni);
        try {
            Ini ini = conf.compare().getIni();
        } catch (IOException e) {
            System.err.println("[NameBlacklister] SEVERE ERROR: CONFIG NOT GENERATED. Please check your permissions to assure that the bot can create configuration files.");
        }
    }

    public void nameEvent(){
        Ini config = getConfig().getIni();
        final Profile.Section settings = config.get("Settings");
        final Profile.Section names = config.get("Names");
        final Boolean debug = settings.get("debug", Boolean.class);
        final Integer timelimit = settings.get("timer", Integer.class);
        final String kickMessage = settings.get("kickMessage", String.class);
        getTasker().createScheduledTimerRunnable(new Runnable() {
            @Override
            public void run() {
                nameCheck(names.getAll("name", String[].class));
            }
        }, 0, timelimit, TimeUnit.SECONDS);
        getEventManager().addEventListener(new ClientJoinEventHandler() {
            @Override
            public void handleEvent(ClientJoinEvent e) {
                String[] badNames = names.getAll("name", String[].class);
                for (Client i : getAPI().getClients().getUninterruptibly()){
                    for (String name : badNames){
                        if (!name.equals("")) {
                            if (e.getClientNickname().contains(name)) {
                                getAPI().kickClientFromServer(kickMessage + " " + name, i.getId());
                                if (debug) {
                                    System.out.println("[NameBlacklister] Kicking user" + e.getClientNickname() + " due to having an illegal username");
                                }
                            }
                        }
                        else {
                            System.out.println("[NameBlacklister] SEVERE: Caught error where the name varriable is blank CONSIDER FIXING");
                        }
                    }
                }
            }
        });
    }

    public void nameCheck(String[] blacklistNames){
        Ini config = getConfig().getIni();
        final Profile.Section settings = config.get("Settings");
        final Boolean debug = settings.get("debug", Boolean.class);
        final String kickMessage = settings.get("kickMessage", String.class);
        if (debug) {
            System.out.println("[NameBlacklister] Received start -- Checking all clients for bad names");
        }
        for (Client i : getAPI().getClients().getUninterruptibly()){
            for (String name : blacklistNames){
                if (!name.equals("")) {
                    if (i.getNickname().contains(name)) {
                        getAPI().kickClientFromServer(kickMessage + " " + name, i.getId());
                    }
                }
                else {
                    if (debug){
                        System.out.println("[NameBlacklister] SEVERE: Caught error where the name varriable is blank CONSIDER FIXING");
                    }
                }
            }
        }
    }


}
