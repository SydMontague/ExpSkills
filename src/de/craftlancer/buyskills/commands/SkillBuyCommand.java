package de.craftlancer.buyskills.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.craftlancer.buyskills.BuySkills;
import de.craftlancer.buyskills.Skill;
import de.craftlancer.buyskills.SkillLanguage;
import de.craftlancer.buyskills.SkillUtils;
import de.craftlancer.buyskills.event.BuySkillsBuyEvent;

/**
 * Handles the /skill buy command
 */
public class SkillBuyCommand extends SkillSubCommand
{
    public SkillBuyCommand(String perm, BuySkills plugin)
    {
        super(perm, plugin);
    }
    
    @Override
    protected void execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.hasPermission(getPermission()) && sender instanceof Player)
            sender.sendMessage(SkillLanguage.COMMAND_PERMISSION);
        else if (!(sender instanceof Player))
            sender.sendMessage(SkillLanguage.COMMAND_PLAYERONLY);
        else if (args.length < 2)
            sender.sendMessage(SkillLanguage.COMMAND_ARGUMENTS);
        else if (!plugin.hasSkill(args[1]))
            sender.sendMessage(SkillLanguage.COMMAND_SKILL_NOT_EXIST);
        else if (!plugin.getSkill(args[1]).isBuyable())
            sender.sendMessage(SkillLanguage.BUY_NOT_BUYABLE);
        else if (plugin.getSkillCap() != 0 && plugin.getSkillCap() <= plugin.getPlayerManager().getSkills(sender.getName()).size() - plugin.getPlayerManager().getBonusCap(sender.getName()))
            sender.sendMessage(SkillLanguage.BUYRENT_SKILLCAP_REACHED);
        else if (plugin.getPlayerManager().getSkills(sender.getName()).contains(args[1]))
            sender.sendMessage(SkillLanguage.BUYRENT_ALREADY_OWN);
        else
        {
            Player p = (Player) sender;
            Skill s = plugin.getSkill(args[1]);
            
            if (!s.getWorlds().contains(p.getWorld().getName()))
                sender.sendMessage(SkillLanguage.BUYRENT_WRONG_WORLD);
            else if (plugin.getPlayerManager().hasPermNeed(p, s))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_PERMISSION);
            else if (plugin.getPlayerManager().hasPermNeed(p, s))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_GROUP);
            else if (!plugin.getPlayerManager().followsSkilltree(p, s))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_SKILLTREE);
            else if (!SkillUtils.hasCurrency(p, s.getBuyNeed()))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_CURRENCYS);
            else if (!SkillUtils.hasCurrency(p, s.getBuyCosts()))
                sender.sendMessage(SkillLanguage.BUYRENT_NOT_AFFORD);
            else
            {
                BuySkillsBuyEvent event = new BuySkillsBuyEvent(s, p);
                plugin.getServer().getPluginManager().callEvent(event);
                
                if (event.isCancelled())
                    sender.sendMessage(SkillLanguage.BUYRENT_CANCELLED);
                else
                {
                    SkillUtils.withdraw(p, s.getBuyCosts().entrySet());
                    
                    plugin.getPlayerManager().grantSkill(p, s);
                    sender.sendMessage(SkillLanguage.BUY_SUCCESS);
                }
            }
        }
    }
    
    @Override
    protected List<String> onTabComplete(String[] args)
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
