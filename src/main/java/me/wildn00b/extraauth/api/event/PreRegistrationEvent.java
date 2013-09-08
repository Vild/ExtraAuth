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

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PreRegistrationEvent is an event that is called when an authentication was failed.
 * 
 * @author Dan "WildN00b" Printzell
 * @since 1.0.0
 */
public class PreRegistrationEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  private Boolean cancelled;
  private final String player;

  /**
   * Creates a PreRegistrationEvent instance.
   * 
   * @since 1.0.0
   * @param playerInformation
   *          The player
   * 
   */
  public PreRegistrationEvent(String player) {
    super(false);
    this.player = player;
    cancelled = false;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  /**
   * @return Returns the player information.
   */
  public String getPlayer() {
    return player;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

}
