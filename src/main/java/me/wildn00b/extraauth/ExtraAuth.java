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

import java.util.logging.Logger;

import me.wildn00b.extraauth.command.AuthCommand;
import me.wildn00b.extraauth.io.Language;
import me.wildn00b.extraauth.io.PlayerStatusDB;
import me.wildn00b.extraauth.io.Settings;

import org.bukkit.plugin.java.JavaPlugin;

public class ExtraAuth extends JavaPlugin {

  public static ExtraAuth INSTANCE;

  public static final Logger log = Logger.getLogger("Minecraft");

  public PlayerStatusDB DB;
  public Language Lang;
  public Settings Settings;

  public String Version;

  public ExtraAuth() {
    INSTANCE = this;
  }

  public void _(String msg) {
    _(msg, true);
  }

  public void _(String msg, boolean lang) {
    if (lang && Lang != null)
      log.info("[ExtraAuth] " + Lang._(msg));
    else
      log.info("[ExtraAuth] " + msg);
  }

  public void _E(String msg) {
    _E(msg, true);
  }

  public void _E(String msg, boolean lang) {
    if (lang && Lang != null)
      log.severe("[ExtraAuth] " + Lang._(msg));
    else
      log.severe("[ExtraAuth] " + msg);
  }

  public void _W(String msg) {
    _W(msg, true);
  }

  public void _W(String msg, boolean lang) {
    if (lang && Lang != null)
      log.warning("[ExtraAuth] " + Lang._(msg));
    else
      log.warning("[ExtraAuth] " + msg);
  }

  @Override
  public void onDisable() {
    DB.Save();
    DB = null;
    _(Lang._("ExtraAuth.Disable"));
    Lang.Close();
    Lang = null;
    Settings.Close();
    Settings = null;
  }

  @Override
  public void onEnable() {
    Settings = new Settings(this);
    Lang = new Language(this);
    DB = new PlayerStatusDB(this);

    getCommand("auth").setExecutor(new AuthCommand(this));
    getServer().getPluginManager().registerEvents(new PlayerListener(this),
        this);

    Version = getDescription().getVersion();

    _(Lang._("ExtraAuth.Enable"));
  }

}
