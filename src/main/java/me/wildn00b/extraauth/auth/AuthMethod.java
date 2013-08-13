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

package me.wildn00b.extraauth.auth;

public enum AuthMethod {
  INVALID(-1, "INVALID"), KEY(1, "KEY"), TOTP(0, "TOTP");

  private int id;
  private String name;

  AuthMethod(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int GetID() {
    return id;
  }

  public String GetName() {
    return name;
  }

  public static AuthMethod GetAuthMethod(int id) {
    for (final AuthMethod item : values())
      if (item.GetID() == id)
        return item;
    return INVALID;
  }

  public static AuthMethod GetAuthMethod(String name) {
    for (final AuthMethod item : values())
      if (item.GetName().equalsIgnoreCase(name))
        return item;
    return INVALID;
  }
}
