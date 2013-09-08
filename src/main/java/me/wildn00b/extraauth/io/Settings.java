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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import me.wildn00b.extraauth.ExtraAuth;

import org.bukkit.configuration.file.YamlConfiguration;

public class Settings {

  private final YamlConfiguration file;
  private File path;

  public Settings(ExtraAuth extraauth) {
    file = new YamlConfiguration();

    try {
      path = new File(extraauth.getDataFolder().getAbsolutePath()
          + File.separator + "config.yml");
      if (path.exists())
        file.load(path);

      addDefaults();
      file.save(path);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public Object _(String path, Object value) {
    if (!file.contains(path))
      file.set(path, value);
    return file.get(path);
  }

  public void Set(String path, Object value) {
    file.set(path, value);
    try {
      file.save(path);
    } catch (final IOException e) {
    }
  }

  private void addDefaults() {
    final HashMap<String, Object> list = new HashMap<String, Object>();

    list.put("SettingsVersion", 1);
    list.put("Language", "en-US");
    list.put("FreezePlayer", true);
    list.put("BlockChat", true);
    list.put("Servername", ExtraAuth.INSTANCE.getServer().getServerName());
    list.put("ReauthenticateTimeout", 5);

    for (final Entry<String, Object> entry : list.entrySet())
      if (!file.contains(entry.getKey()))
        file.set(entry.getKey(), entry.getValue());
  }

}