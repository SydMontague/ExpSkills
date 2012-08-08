package com.syd.expskills;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class ServerEntityListener implements Listener
{
    ExpSkills plugin;
    
    public ServerEntityListener(ExpSkills instance)
    {
        plugin = instance;
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        // modify ORB drop on player death
        if (event.getEntity() instanceof Player && ExpSkills.config.getBoolean("general.change_expdrop", false))
        {
            PlayerDeathEvent death = (PlayerDeathEvent) event;
            Player p = (Player) event.getEntity();
            
            // int xp = p.getExp();
            int totalxp = p.getTotalExperience();
            int newxp = funcs.getXpatLevel(funcs.getLevel(p));
            int xp = totalxp - newxp;
            
            YamlConfiguration pconfig = FileManager.loadPF(p);
            
            // Workaround for Bukkit Bug #1595
            if (newxp < pconfig.getInt("experience", 0))
                newxp = pconfig.getInt("experience", 0);
            
            pconfig.set("experience", newxp);
            FileManager.savePF(p, pconfig);
            
            double level = p.getLevel() + p.getExp();
            level = 1.75 * (level * level) + 4.5 * level;
            
            // default usage of EXP Drop - add dynamic method with config usage
            Item item = p.getWorld().dropItem(p.getLocation(), new ItemStack(384, 1));
            item.setMetadata("expskills.exp", new FixedMetadataValue(plugin, xp));
            item.setMetadata("expskills.level", new FixedMetadataValue(plugin, level));
            
            event.setDroppedExp(0);
            death.setNewLevel(0);
        }
        else
            return;
    }
    
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event)
    {
        if (event.getItem().getItemStack().getType() == Material.EXP_BOTTLE)
        {
            Item item = event.getItem();
            if (item.hasMetadata("expskills.exp"))
            {
                
                Player player = event.getPlayer();
                
                double level = player.getLevel() + player.getExp();
                level = 1.75 * (level * level) + 4.5 * level;
                level = level + item.getMetadata("expskills.level").get(0).asInt();
                
                int i = 0;
                double value;
                do
                {
                    i++;
                    value = 1.75 * (i * i) + 4.5 * i;
                }
                while (value < level);
                
                player.setTotalExperience(player.getTotalExperience() + item.getMetadata("expskills.exp").get(0).asInt());
                player.setLevel(i-1);
                event.getItem().remove();
                event.setCancelled(true);
            }
        }
    }
    
}
