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

public enum CommandAccountPermission {
  NEED_ACCOUNT(true, false), NEED_LOGGEDIN(true, true), NO_ACCOUNT(false, false);

  private boolean needAccount;

  private boolean needLoggedIn;

  CommandAccountPermission(boolean needAccount, boolean needLoggedIn) {
    this.needAccount = needAccount;
    this.needLoggedIn = needLoggedIn;
  }

  public boolean isNeedAccount() {
    return needAccount;
  }

  public boolean isNeedLoggedIn() {
    return needLoggedIn;
  }

}
