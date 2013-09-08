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

/**
 * The PlayerInformation class is used to get information about a player, for example if the player is authenticated.
 * 
 * @author Dan "WildN00b" Printzell
 * @since 1.0.0
 */
public class PlayerInformation {
  PlayerStatusDB.playerstatus info = null;

  /**
   * Creates a PlayerInformation instance.
   * 
   * @since 1.0.0
   * @param player
   *          The player with you want the information about.
   * 
   */
  public PlayerInformation(String player) {
    info = ExtraAuth.INSTANCE.DB.Get(player);
  }

  /**
   * @since 1.0.0
   * @return Returns if the player is authenticated.
   */
  public boolean getAuthed() {
    return info.Authed;
  }

  /**
   * @since 1.0.0
   * @return Returns if the player exist in the database.
   */
  public boolean getExist() {
    return info != null;
  }

  /**
   * @since 1.0.0
   * @return Returns the IP of which the player was last connected or currently connected from.
   */
  public String getLastIP() {
    return info.LastIP;
  }

  /**
   * @since 1.0.0
   * @return Returns when the player was last connected in milliseconds from January 1, 1970 UTC.
   * @see System.currentTimeMillis
   */
  public long getLastOnline() {
    return info.LastOnline;
  }

  /**
   * @since 1.0.0
   * @return Returns the players name.
   */
  public String getPlayer() {
    return info.Player;
  }

  /**
   * @since 1.0.0
   * @return Returns the bukkit player object if the player is connected.
   */
  public Player getPlayerObj() {
    return Bukkit.getPlayer(info.Player);
  }

  /**
   * @since 1.2.0
   * @return Returns the private key.
   */
  public String getPrivateKey() {
    return info.PrivateKey;
  }

  /**
   * Sets if the player is authenticated.
   * 
   * @since 1.0.0
   * @param authed
   *          The value.
   */
  public void setAuthed(boolean authed) {
    info.Authed = authed;
  }

  /**
   * Sets when the player was last connect.
   * 
   * @since 1.0.0
   * @param lastOnline
   *          The time.
   * @see System.currentTimeMillis
   */
  public void setLastOnline(long lastOnline) {
    info.LastOnline = lastOnline;
  }

  /**
   * Sets the private key for the player.
   * 
   * @since 1.2.0
   * @param privateKey
   *          The key
   */
  public void setPrivateKey(String privateKey) {
    info.PrivateKey = privateKey;
  }
}
