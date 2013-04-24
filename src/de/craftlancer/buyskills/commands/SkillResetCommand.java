package de.craftlancer.buyskills.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.craftlancer.buyskills.BuySkills;
import de.craftlancer.buyskills.SkillLanguage;
import de.craftlancer.buyskills.event.BuySkillsResetEvent;

public class SkillResetCommand extends SkillSubCommand
{
    
    public SkillResetCommand(String perm, BuySkills plugin)
    {
        super(perm, plugin);
    }
    
    @Override
    public void execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!sender.hasPermission(getPermission()))
            sender.sendMessage(SkillLanguage.COMMAND_PERMISSION);
        else if (args.length < 2)
            sender.sendMessage(SkillLanguage.COMMAND_ARGUMENTS);
        else if (plugin.getServer().getPlayerExact(args[1]) == null)
            sender.sendMessage(SkillLanguage.COMMAND_PLAYER_NOT_EXIST);
        else
        {
            plugin.getServer().getPluginManager().callEvent(new BuySkillsResetEvent(plugin.getServer().getPlayerExact(args[1])));
            
            for (String s : plugin.getPlayerManager().getRentedSkills(args[1]).keySet())
                plugin.getPlayerManager().revokeRented(args[1], s);
            
            for (String s : plugin.getPlayerManager().getBoughtSkills(args[1]))
                plugin.getPlayerManager().revokeSkill(args[1], s);
            
            sender.sendMessage(SkillLanguage.RESET_SUCCESS);
            plugin.getServer().getPlayerExact(args[1]).sendMessage(SkillLanguage.RESET_NOTIFY);
        }
        // TOTEST
    }
    
}