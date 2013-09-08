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

package me.wildn00b.extraauth.io;

import java.util.logging.Level;

import me.wildn00b.extraauth.ExtraAuth;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {

  private ExtraAuth extraauth;
  private Permission permissions;

  public Vault(ExtraAuth extraauth) {
    this.extraauth = extraauth;
    if (extraauth.getServer().getPluginManager().getPlugin("Vault") == null) {
      extraauth.Log.log(Level.SEVERE,
          "[ExtraAuth] " + extraauth.Lang._("Vault.NotFound"));
      extraauth.getServer().getPluginManager().disablePlugin(extraauth);
    } else {
      final RegisteredServiceProvider<Permission> perm = extraauth.getServer()
          .getServicesManager().getRegistration(Permission.class);
      if (perm == null) {
        extraauth.Log.log(Level.SEVERE,
            "[ExtraAuth] " + extraauth.Lang._("Vault.PermissionNotFound"));
        extraauth.getServer().getPluginManager().disablePlugin(extraauth);
        return;
      }
      permissions = perm.getProvider();
    }
  }

  public boolean HasPermissions(Player player, String permission) {
    return permissions.has(player, permission);
  }

}
