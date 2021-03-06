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

package me.wildn00b.extraauth.api.event;

import me.wildn00b.extraauth.api.PlayerInformation;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * RegistrationSuccessfullEvent is an event that is called when an authentication was failed.
 * 
 * @author Dan "WildN00b" Printzell
 * @since 1.0.0
 */
public class RegistrationSuccessfullEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final PlayerInformation playerInformation;

  /**
   * Creates a RegistrationSuccessfullEvent instance.
   * 
   * @since 1.0.0
   * @param playerInformation
   *          The player
   * 
   */
  public RegistrationSuccessfullEvent(PlayerInformation playerInformation) {
    super(false);
    this.playerInformation = playerInformation;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  /**
   * @return Returns the player information.
   */
  public PlayerInformation getPlayer() {
    return playerInformation;
  }

}
