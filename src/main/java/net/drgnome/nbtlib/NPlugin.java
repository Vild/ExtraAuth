// Bukkit Plugin "NBTLib" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by/3.0/

package net.drgnome.nbtlib;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

public class NPlugin extends JavaPlugin
{
    public void onEnable()
    {
        NBTLib._log.info("[NBTLib] Enabling");
        if(!NBTLib.enabled())
        {
            getPluginLoader().disablePlugin(this);
        }
    }
    
    public void onDisable()
    {
        NBTLib._log.info("[NBTLib] Disabling");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        sender.sendMessage(ChatColor.AQUA + "NBTLib version: " + NBTLib._version);
        return true;
    }
}