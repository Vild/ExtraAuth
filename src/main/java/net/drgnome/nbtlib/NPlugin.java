// Bukkit Plugin "NBTLib" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by/3.0/

package net.drgnome.nbtlib;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class NPlugin extends JavaPlugin {
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label,
      String[] args) {
    sender.sendMessage(ChatColor.AQUA + "NBTLib version: " + NBTLib._version);
    return true;
  }

  @Override
  public void onDisable() {
    NBTLib._log.info("[NBTLib] Disabling");
  }

  @Override
  public void onEnable() {
    NBTLib._log.info("[NBTLib] Enabling");
    if (!NBTLib.enabled())
      getPluginLoader().disablePlugin(this);
  }
}