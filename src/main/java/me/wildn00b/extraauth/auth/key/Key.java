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

package me.wildn00b.extraauth.auth.key;

import me.wildn00b.extraauth.ExtraAuth;
import me.wildn00b.extraauth.api.AuthMethod;
import me.wildn00b.extraauth.api.PlayerInformation;
import me.wildn00b.extraauth.api.event.FailedReason;

public class Key extends AuthMethod {

  @Override
  public boolean AllowOtherToEnable() {
    return true;
  }

  @Override
  public FailedReason Authenticate(PlayerInformation information,
      Object... args) {
    if (args.length > 0) {
      String key = (String) args[0];

      for (int i = 1; i < args.length; i++)
        key += " " + (String) args[1];

      if (key.equals(information.getPrivateKey())) {
        information.setAuthed(true);
        return FailedReason.SUCCESSFULL;
      } else
        return FailedReason.WRONG_KEY;
    } else
      return FailedReason.INVALID_ARGS;
  }

  @Override
  public String GetHelpLine(String language) {
    return ExtraAuth.INSTANCE.Lang._("Command.Enable.Key.Help");
  }

  @Override
  public String GetName() {
    return "Key";
  }

  @Override
  public String GetOtherHelpLine(String language) {
    return ExtraAuth.INSTANCE.Lang._("Command.Enable.Key.Other.Help");
  }

  @Override
  public FailedReason OnEnable(PlayerInformation information, Object... args) {
    if (args.length > 0) {
      String key = (String) args[0];

      for (int i = 1; i < args.length; i++)
        key += " " + (String) args[1];

      information.setPrivateKey(key);
      information.setAuthed(true);
      return FailedReason.SUCCESSFULL;
    } else
      return FailedReason.INVALID_ARGS;
  }

}
