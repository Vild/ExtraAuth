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

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import me.wildn00b.extraauth.ExtraAuth;

import org.bukkit.configuration.file.YamlConfiguration;

public class Language {

  private YamlConfiguration file;
  private File path;

  public Language(ExtraAuth extraauth) {
    file = new YamlConfiguration();
    final String partpath = extraauth.getDataFolder().getAbsolutePath()
        + File.separator + "lang" + File.separator;

    try {
      path = new File(partpath + extraauth.Settings._("Language") + ".yml");
      if (path.exists())
        file.load(path);
      else {
        extraauth._W("Couldn't find language file, reverting to en-US", false);
        path = new File(partpath + "en-US.yml");
        if (path.exists())
          file.load(path);
      }
      addDefaults();
      file.save(path);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public String _(String path) {
    if (file.contains(path))
      return file.getString(path);
    else
      return path;
  }

  public void Close() {
    file = null;
  }

  private void addDefaults() {
    final HashMap<String, String> list = new HashMap<String, String>();

    list.put("FreezeMessage", "Please authenticate with /auth <KEY>");
    list.put("ExtraAuth.Enable", "Enabled successfully.");
    list.put("ExtraAuth.Disable", "Disabled successfully.");
    list.put("ExtraAuth.Saving", "Saving database.");
    list.put("ExtraAuth.CorruptDB", "Database corrupted. Reverting to backup.");
    list.put("ExtraAuth.MissingBackup",
        "Backup missing! Starting on an new clean database.");

    list.put("NeedToAuthenticate",
        "You need to authenticate to use that command");

    list.put("Command.OnlyIngame",
        "The commands can only be run ingame by the player.");
    list.put("Command.Help", "(page number) - Shows this help.");
    list.put(
        "Command.Enable.TOTP.Help",
        "- Enabling a TOTP authentication on your account. (Uses the Google Authenticator app)");
    list.put("Command.Enable.Key.Help",
        "<KEY> - Enabling a key based authentication on your account.");

    list.put("Command.Disable.Help", "- Disabling ExtraAuth on your account.");
    list.put("Command.Auth.Help",
        "<KEY> - Authenticates with the key to your account.");
    list.put("Command.Title",
        "ExtraAuth V%VERSION% Page %PAGE%/%MAXPAGE% by %AUTHOR%");

    list.put("Command.Enable.Processing", "Creating a privatekey...");
    list.put("Command.Enable.UrlFailed",
        "Failed to create url, please try again later.");
    list.put(
        "Command.Enable.TOTP.Success",
        "Successfully enabled ExtraAuth on your account. Click on the following link and open the Google Authenticator app and scan the QR code. %URL%");
    list.put("Command.Enable.Key.Success",
        "Successfully enabled ExtraAuth on your account.");
    list.put("Command.Enable.Event.Failed",
        "Another plugin was blocking your access to enable ExtraAuth.");
    list.put("Command.Enable.AlreadyRegistered.Failed",
        "Failed to enable ExtraAuth on your account. Have you already got it enabled?");
    list.put("Command.Enable.InvalidMethod.Failed",
        "Invalid authentication method, please contact your server administrator.");
    list.put("Command.Enable.Unknown.Failed",
        "Unknown error, please contact your server administrator.");

    list.put("Command.Disable.Success",
        "Disabled successfully ExtraAuth on your account.");
    list.put("Command.Disable.Event.Failed",
        "Another plugin was blocking your access to disable ExtraAuth.");
    list.put("Command.Disable.NotRegistered.Failed",
        "You need to have it enabled before disabling.");
    list.put("Command.Disable.Unknown.Failed",
        "Unknown error, please contact your server administrator.");

    list.put("Command.Auth.Success", "Successfully authenticated.");
    list.put("Command.Auth.NotRegistered.Failed",
        "To authenticate you first need to enable ExtraAuth.");
    list.put("Command.Auth.AlreadyAuthed.Failed",
        "You are already authenticated.");
    list.put("Command.Auth.WrongKey.Failed", "Wrong key. Please try again.");
    list.put("Command.Auth.InvalidMethod.Failed",
        "Invalid authentication method, please contact your server administrator.");
    list.put("Command.Auth.Unknown.Failed",
        "Unknown error, please contact your server administrator.");

    list.put("Command.Auth.Event.Failed",
        "Another plugin was blocking your access to authenticate to ExtraAuth.");

    list.put("Command.Exception",
        "An exception has occured. Try again or contact an administrator.");

    for (final Entry<String, String> entry : list.entrySet())
      if (!file.contains(entry.getKey()))
        file.set(entry.getKey(), entry.getValue());
  }
}