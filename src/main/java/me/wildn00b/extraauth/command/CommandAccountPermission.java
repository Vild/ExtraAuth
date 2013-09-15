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

package me.wildn00b.extraauth.command;

import me.wildn00b.extraauth.ExtraAuth;
import me.wildn00b.extraauth.io.PlayerStatusDB;

public enum CommandAccountPermission {
  GOT_ACCOUNT_AND_LOGGED_IN(new Check() {
    @Override
    public boolean Run(String player) {
      if (DB.Contains(player))
        return DB.Get(player).Authed;
      else
        return false;
    }
  }), LOGGED_IN(new Check() {
    @Override
    public boolean Run(String player) {
      if (DB.Contains(player))
        return DB.Get(player).Authed;
      else
        return true;
    }
  }), NO_ACCOUNT(new Check() {
    @Override
    public boolean Run(String player) {
      return !DB.Contains(player);
    }
  }), NOT_LOGGED_IN(new Check() {
    @Override
    public boolean Run(String player) {
      if (DB.Contains(player))
        return !DB.Get(player).Authed;
      else
        return false;
    }
  });

  public abstract interface Check {
    PlayerStatusDB DB = ExtraAuth.INSTANCE.DB;

    public abstract boolean Run(String player);
  }

  private Check check;

  CommandAccountPermission(Check check) {
    this.check = check;
  }

  public Check getCheck() {
    return check;
  }

}
