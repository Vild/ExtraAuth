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

package me.wildn00b.extraauth.api;

import me.wildn00b.extraauth.ExtraAuth;
import me.wildn00b.extraauth.io.PlayerStatusDB;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerInformation {
  PlayerStatusDB.playerstatus info = null;

  public PlayerInformation(String player) {
    info = ExtraAuth.INSTANCE.DB.Get(player);
  }

  public boolean getAuthed() {
    return info.Authed;
  }

  public boolean getExist() {
    return info != null;
  }

  public String getLastIP() {
    return info.LastIP;
  }

  public long getLastOnline() {
    return info.LastOnline;
  }

  public String getPlayer() {
    return info.Player;
  }

  public Player getPlayerObj() {
    return Bukkit.getPlayer(info.Player);
  }

  public void setAuthed(boolean authed) {
    info.Authed = authed;
  }

  public void setLastOnline(long lastOnline) {
    info.LastOnline = lastOnline;
  }

}
