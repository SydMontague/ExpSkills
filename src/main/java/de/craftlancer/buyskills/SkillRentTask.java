package de.craftlancer.buyskills;

import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.buyskills.event.BuySkillsRentExpireEvent;

/**
 * Handles the expiration of rented skills
 */
public class SkillRentTask extends BukkitRunnable
{
    private final BuySkills plugin;
    
    public SkillRentTask(BuySkills plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public void run()
    {
        long time = System.currentTimeMillis();
        
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            SkillPlayer skillPlayer = plugin.getSkillPlayer(player);
            for (Entry<String, Long> set : skillPlayer.getRented().entrySet())
                if (set.getValue() < time)
                {
                    Skill skill = plugin.getSkillByKey(set.getKey());
                    skillPlayer.revokeRented(skill);
                    player.sendMessage(SkillLanguage.RENT_EXPIRED.getString().replace("%skill%", set.getKey()));
                    plugin.getServer().getPluginManager().callEvent(new BuySkillsRentExpireEvent(skill, skillPlayer));
                }
        }
    }
}
