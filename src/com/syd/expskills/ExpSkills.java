package com.syd.expskills;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;

public class ExpSkills extends JavaPlugin
{
    protected static Server server;
    protected static ExpSkills plugin;
    protected static FileConfiguration config;
    protected static YamlConfiguration skilltree;
    protected static YamlConfiguration lang;
    protected final static Logger log = Logger.getLogger("Minecraft");
    public final ServerEntityListener entityListener = new ServerEntityListener(this);
    public final ServerPlayerListener playerListener = new ServerPlayerListener(this);
    private CommandManager command = new CommandManager(this);
    public final PermissionsSystem permSys = new PermissionsSystem();
    protected static Method method;
    protected static Economy economy = null;

    public void onEnable()
    {
        PluginDescriptionFile pdffile = this.getDescription();
        server = getServer();
        // initializing config.yml
        config = getConfig();

        String configversion = config.getString("version");
        String version = pdffile.getVersion();
        if (!configversion.equalsIgnoreCase(version))
        {
            if (config.get("version") == null)
            {
                config.addDefault("general.skillpoint_modifier", 2.0);
                config.addDefault("general.currency", "Dollar");
                config.addDefault("general.use_skilltree", false);
                config.addDefault("general.use_economy", false);
                config.addDefault("general.updatetime", 6000);
                config.addDefault("general.formula", 0);
                config.addDefault("general.formula_a", 0);
                config.addDefault("general.formula_b", 0);
                config.addDefault("general.formula_c", 0);
                config.addDefault("general.formula_d", 0);
                config.addDefault("general.formula_e", 0);
                config.addDefault("general.skill_cap", 0);
                config.addDefault("skills.skill0.name", "testskill");
                config.addDefault("skills.skill0.description", "Just a example");
                config.addDefault("skills.skill0.info", "This Skill was created to show Admins how to use this configfile!");
                config.addDefault("skills.skill0.cost_type", "both");
                config.addDefault("skills.skill0.skillpoints", 2);
                config.addDefault("skills.skill0.money", 1000);
                List<String> need = new ArrayList<String>();
                need.add("foo.bar");
                config.addDefault("skills.skill0.permissions_need", need);
                List<String> earn = new ArrayList<String>();
                earn.add("bar.foo");
                earn.add("bar.bar");
                config.addDefault("skills.skill0.permissions_earn", earn);
                List<String> needgrp = new ArrayList<String>();
                needgrp.add("builder");
                config.addDefault("skills.skill0.groups_need", needgrp);
                List<String> earngrp = new ArrayList<String>();
                earngrp.add("mod");
                earngrp.add("admin");
                config.addDefault("skills.skill0.groups_earn", earngrp);
                config.addDefault("skills.skill0.revoke_need_groups", false);
                List<String> cat = new ArrayList<String>();
                cat.add("exaple");
                config.addDefault("skills.skill0.categories", cat);
                config.addDefault("skills.skill0.level_need", 0);
            }
            else if (configversion.equalsIgnoreCase(version))
            {
                if (configversion.equals("0.7.0_RC2"))
                {
                    config.addDefault("general.updatetime", 6000);
                }
                else if (configversion.equals("0.6.4"))
                {
                    config.addDefault("general.formula", 0);
                    config.addDefault("general.formula_a", 0);
                    config.addDefault("general.formula_b", 0);
                    config.addDefault("general.formula_c", 0);
                    config.addDefault("general.formula_d", 0);
                    config.addDefault("general.formula_e", 0);
                    config.addDefault("general.skill_cap", 0);
                    config.addDefault("general.updatetime", 6000);
                }
            }

            config.addDefault("version", pdffile.getVersion());
            config.set("version", pdffile.getVersion());
            config.options().copyDefaults(true);
            saveConfig();
        }
        // end of config.yml

        // start of lang.yml
        File langfile = new File(this.getDataFolder() + File.separator + "lang.yml");

        if (!langfile.exists())
        {
            lang = YamlConfiguration.loadConfiguration(getResource("lang.yml"));
            try
            {
                lang.save(langfile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
            lang = YamlConfiguration.loadConfiguration(langfile);

        // start skilltree
        if (config.getBoolean("general.use_skilltree", false) == true)
            skilltree = FileManager.loadSkilltree();

        // start rented
        long delay = config.getLong("general.updatetime", 6000);

        server.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
        {
            public void run()
            {
                RentingManager.update();
            }
        }, 0, delay);

        // initialize events and commands
        getCommand("exp").setExecutor(command);
        registerEvent();

        // start permissions section
        permSys.start();

        // start economy section
        if ((config.getBoolean("general.use_economy", false)))
        {
            if (ExpSkills.server.getPluginManager().getPlugin("Vault") != null)
            {
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economyProvider != null)
                {
                    economy = economyProvider.getProvider();

                    log.info("[ExpSkills] " + economy.getName() + " hooked");
                }
            }
            if (ExpSkills.server.getPluginManager().getPlugin("Register") != null)
            {
                this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
                {
                    public void run()
                    {
                        boolean a = Methods.setMethod(getServer().getPluginManager());
                        if (a)
                        {
                            method = Methods.getMethod();
                            log.info("[ExpSkills] " + method.getName() + " hooked");
                        }
                        else
                            log.severe("[ExpSkills] Hooking Economy Failed");
                    }
                }, 0L);
            }
        }
        log.info("[ExpSkills] " + pdffile.getName() + " " + pdffile.getVersion() + " enabled");
    }

    public void onDisable()
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[ExpSkills] " + pdfFile.getName() + " disabled");
    }

    private void registerEvent()
    {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(entityListener, this);
    }
}
