package de.craftlancer.buyskills.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.craftlancer.buyskills.BuySkills;
import de.craftlancer.buyskills.Skill;
import de.craftlancer.buyskills.SkillLanguage;
import de.craftlancer.buyskills.SkillUtils;
import de.craftlancer.buyskills.event.BuySkillsRentEvent;

/**
 * Handles the /skill rent command
 */
public class SkillRentCommand extends SkillSubCommand
{
    public SkillRentCommand(String perm, BuySkills plugin)
    {
        super(perm, plugin);
    }
    
    @Override
    public void execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.hasPermission(getPermission()) && sender instanceof Player)
            sender.sendMessage(SkillLanguage.COMMAND_PERMISSION);
        else if (!(sender instanceof Player))
            sender.sendMessage(SkillLanguage.COMMAND_PLAYERONLY);
        else if (args.length < 2)
            sender.sendMessage(SkillLanguage.COMMAND_ARGUMENTS);
        else if (!plugin.hasSkill(args[1]))
            sender.sendMessage(SkillLanguage.COMMAND_SKILL_NOT_EXIST);
        else if (!plugin.getSkill(args[1]).isRentable())
            sender.sendMessage(SkillLanguage.RENT_NOT_RENTABLE);
        else if (plugin.getSkillCap() != 0 && plugin.getSkillCap() <= plugin.getPlayerManager().getSkills(sender.getName()).size() - plugin.getPlayerManager().getBonusCap(sender.getName()))
            sender.sendMessage(SkillLanguage.BUYRENT_SKILLCAP_REACHED);
        else if (plugin.getPlayerManager().getSkills(sender.getName()).contains(args[1]))
            sender.sendMessage(SkillLanguage.BUYRENT_ALREADY_OWN);
        else
        {
            Player p = (Player) sender;
            Skill s = plugin.getSkill(args[1]);
            
            if (!s.getWorlds().isEmpty() && !s.getWorlds().contains(p.getWorld().getName()))
                sender.sendMessage(SkillLanguage.BUYRENT_WRONG_WORLD);
            else if (!plugin.getPlayerManager().hasPermNeed(p, s))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_PERMISSION);
            else if (!plugin.getPlayerManager().hasGroupNeed(p, s))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_GROUP);
            else if (!plugin.getPlayerManager().followsSkilltree(p, s))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_SKILLTREE);
            else if (!SkillUtils.hasCurrency(p, s.getRentNeed()))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_CURRENCYS);
            else if (!SkillUtils.hasCurrency(p, s.getRentCosts()))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_AFFORD);
            else
            {
                BuySkillsRentEvent event = new BuySkillsRentEvent(s, p);
                plugin.getServer().getPluginManager().callEvent(event);
                
                if (event.isCancelled())
                    sender.sendMessage(SkillLanguage.BUYRENT_CANCELLED);
                else
                {
                    SkillUtils.withdraw(p, s.getRentCosts().entrySet());
                    
                    plugin.getPlayerManager().grantRented(p, s, s.getRenttime());
                    sender.sendMessage(SkillLanguage.RENT_SUCCESS);
                }
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(String[] args)
    {
        switch (args.length)
        {
            case 2:
                return SkillUtils.getMatches(args[1], plugin.getSkillMap().keySet());
            default:
                return null;
        }
    }
}
