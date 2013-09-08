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

import java.util.ArrayList;

import me.wildn00b.extraauth.auth.key.Key;
import me.wildn00b.extraauth.auth.onetimekey.OneTimeKey;
import me.wildn00b.extraauth.auth.totp.TOTPAuth;

public class AuthManager {

  public static ArrayList<Class<? extends AuthMethod>> Methods = new ArrayList<Class<? extends AuthMethod>>();

  static {
    Methods.add(TOTPAuth.class);
    Methods.add(Key.class);
    Methods.add(OneTimeKey.class);
  }

  /**
   * Returns a instance of the AuthMethod, Null if it can't it.
   * 
   * @param name
   *          the method name
   * @return The instance
   */
  public static AuthMethod GetAuthMethod(String name) {
    try {
      for (final Class<? extends AuthMethod> item : Methods) {
        AuthMethod method;

        method = item.newInstance();

        if (method.GetName().equalsIgnoreCase(name))
          return method;
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
