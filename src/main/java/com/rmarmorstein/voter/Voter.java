/*
Copyright 2015 River Marmorstein

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.rmarmorstein.voter;


import com.vexsoftware.votifier.model.VotifierEvent;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by River on 1/24/2015.
 */
public class Voter extends JavaPlugin implements Listener {
    
    private Economy econ = null;
    Map<String,Double> voteRewardMap = new HashMap<String, Double>();
    List<String> bc;
    
    @Override
    public void onEnable() {
        try {
            setupEconomy();
        } catch(Exception ex) {
            ex.printStackTrace();
            getLogger().severe("Disabling Voter - unable to init vault.");
            getServer().getPluginManager().disablePlugin(this);
        }
        this.saveDefaultConfig();
        List<String> strings = (List<String>) getConfig().getList("service");
        for(String s : strings) {
            String svc = s.split(",")[1];
            double val = Double.parseDouble(s.split(",")[2]);
            voteRewardMap.put(svc, val);
        }
        bc = (List<String>) getConfig().getList("broadcast");
    }
    
    @Override
    public void onDisable() {
        
    }
    
    @EventHandler
    public void processVote(VotifierEvent event) {
        String service = event.getVote().getServiceName();
        for(String s : voteRewardMap.keySet()) {
            if(service.equalsIgnoreCase(s)) {
                econ.depositPlayer(Bukkit.getOfflinePlayer(UUIDLib.getID(event.getVote().getUsername())), voteRewardMap.get(s));
                doBroadcast(service, event.getVote().getUsername());
            }
        }
    }
    
    private void doBroadcast(String service, String user) {
        for(String s : bc) {
            s.replaceAll("%player%", user);
            s.replaceAll("%service%", service);
            ChatColor.translateAlternateColorCodes('&', s);
            Bukkit.broadcastMessage(s);
        }
    }
    
    private boolean setupEconomy() throws Exception {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            throw new Exception();
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new Exception();
        }
        econ = rsp.getProvider();
        return econ != null;
    }


}
