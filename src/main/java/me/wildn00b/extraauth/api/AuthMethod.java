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

import me.wildn00b.extraauth.api.event.FailedReason;

/**
 * The interface for implention a authentication method
 * 
 * @author Dan "WildN00b" Printzell
 * @since 1.2.0
 */
public abstract class AuthMethod {

  /**
   * @return If other players(admins) are allowed to enable this authentication on other players.
   */
  public abstract boolean AllowOtherToEnable();

  /**
   * This function is called when the authentication is called.
   * 
   * @since 1.2.0
   * @param information
   *          The player information.
   * @param args
   *          The args
   * @return If the authentication was successfull.
   */
  public abstract FailedReason Authenticate(PlayerInformation information,
      Object... args);

  /**
   * Returns the help line, like "<KEY> - Authenticates with the key to your account." or "- Enabling a TOTP authentication on your account. (Uses the Google Authenticator app)"
   * 
   * @since 1.2.0
   * @param The
   *          language
   * @return The help line.
   */
  public abstract String GetHelpLine(String language);

  /**
   * @since 1.2.0
   * @return The name of the authentication, used in the help.
   */
  public abstract String GetName();

  /**
   * Like GetHelpLine but for when enabling on other players.
   * 
   * @since 1.2.0
   * @see GetHelpLine
   * @param The
   *          language
   * @return The help line.
   */
  public abstract String GetOtherHelpLine(String language);

  /**
   * Will be called when the authentication was enabled
   * 
   * @since 1.2.0
   * @param information
   *          The player information.
   * @param args
   *          The args
   */
  public abstract FailedReason OnEnable(PlayerInformation information,
      Object... args);
}
