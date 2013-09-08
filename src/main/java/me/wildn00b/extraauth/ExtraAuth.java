/*
 * ExtraAuth - Extra authentication for bukkit, for accessing account or other plugins (which uses my API)
 * Copyright (C) 2013 Dan Printzell
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package me.wildn00b.extraauth;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.wildn00b.extraauth.command.AuthCommand;
import me.wildn00b.extraauth.io.Language;
import me.wildn00b.extraauth.io.PlayerStatusDB;
import me.wildn00b.extraauth.io.Settings;
import me.wildn00b.extraauth.io.Vault;
import me.wildn00b.extraauth.listener.PlayerListener;

import org.bukkit.plugin.java.JavaPlugin;

public class ExtraAuth extends JavaPlugin {

  public static ExtraAuth INSTANCE;

  public PlayerStatusDB DB = null;
  public Language Lang = null;
  public Logger Log = Logger.getLogger("Minecraft");
  public Settings Settings = null;
  public Vault Vault = null;

  public String Version;

  public ExtraAuth() {
    INSTANCE = this;
  }

  @Override
  public void onDisable() {
    Log.log(Level.INFO, Lang._("ExtraAuth.Disable"));
  }

  @Override
  public void onEnable() {
    Settings = new Settings(this);
    Lang = new Language(this);
    DB = new PlayerStatusDB(this);
    Vault = new Vault(this);

    getCommand("auth").setExecutor(new AuthCommand(this));
    getServer().getPluginManager().registerEvents(new PlayerListener(this),
        this);

    Version = getDescription().getVersion();

    try {
      final Metrics metrics = new Metrics(this);
      metrics.findCustomData();
      metrics.start();
    } catch (final Exception e) {
    }

    Log.log(Level.INFO, Lang._("ExtraAuth.Enable"));
  }

  public void Reload() {
    Settings = new Settings(this);
    Lang = new Language(this);
  }

}