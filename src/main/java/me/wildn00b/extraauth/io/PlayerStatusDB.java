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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import me.wildn00b.extraauth.ExtraAuth;
import me.wildn00b.extraauth.api.AuthManager;
import me.wildn00b.extraauth.api.AuthMethod;
import me.wildn00b.extraauth.api.PlayerInformation;
import me.wildn00b.extraauth.api.event.FailedReason;
import net.drgnome.nbtlib.NBT;
import net.drgnome.nbtlib.Tag;

import org.bukkit.entity.Player;

public class PlayerStatusDB {

  public class playerstatus {
    public boolean Authed;
    public String LastIP;
    public long LastOnline;
    public AuthMethod Method;
    public String Player;
    public String PrivateKey;

    public playerstatus() {

    }

    public playerstatus(String player, boolean authed, long lastOnline,
        String lastIP, AuthMethod method) {
      this.Player = player;
      this.Authed = authed;
      this.LastOnline = lastOnline;
      this.LastIP = lastIP;
      this.PrivateKey = "";
      this.Method = method;
    }

    public playerstatus(String player, Map<String, Tag> in) {
      try {
        Player = player;
        readObject(in);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }

    public void readObject(Map<String, Tag> in) throws Exception {
      final Map<String, Tag> player = (Map<String, Tag>) in.get(Player).get();

      LastOnline = (Long) player.get("LastOnline").get();
      LastIP = (String) player.get("LastIP").get();
      PrivateKey = (String) player.get("PrivateKey").get();
      Method = AuthManager.GetAuthMethod((String) player.get("Method").get());
    }

    @Override
    public String toString() {
      return "playerstatus [Player=" + Player + ", Authed=" + Authed
          + ", LastOnline=" + LastOnline + ", LastIP=" + LastIP
          + ", PrivateKey=" + PrivateKey + ", Method=" + Method + "]";
    }

    private void writeObject(Map<String, Tag> out) throws Exception {
      final Map<String, Tag> map = new HashMap<String, Tag>();

      if (Method == null)
        return;

      map.put("LastOnline", Tag.newLong(LastOnline));
      map.put("LastIP", Tag.newString(LastIP));
      map.put("PrivateKey", Tag.newString(PrivateKey));
      map.put("Method", Tag.newString(Method.GetName()));

      out.put(Player, Tag.newCompound(map));
    }
  }

  public static final int CURRENT_VERSION = 2;
  private ArrayList<playerstatus> db = new ArrayList<playerstatus>();
  private final ExtraAuth extraauth;
  private final File file;

  public PlayerStatusDB(ExtraAuth extraAuth) {
    this.extraauth = extraAuth;

    if (new File(extraAuth.getDataFolder().getAbsolutePath() + File.separator
        + "PlayerStatusDB.db").exists())
      ConvertOld();

    file = new File(extraAuth.getDataFolder().getAbsolutePath()
        + File.separator + "PlayerStatusDB.nbt");

    Load();
  }

  public FailedReason Add(String player, AuthMethod method, Object... args) {
    if (method == null)
      return FailedReason.INVALID_METHOD;

    if (Contains(player))
      return FailedReason.ALREADY_REGISTERED;

    if (db.add(new playerstatus(player, true, System.currentTimeMillis(), "",
        method))) {
      final FailedReason fr = method.OnEnable(new PlayerInformation(player),
          args);
      if (fr != FailedReason.SUCCESSFULL) {
        Remove(player);
        return fr;
      }
      Save();
      return FailedReason.SUCCESSFULL;
    } else
      return FailedReason.UNKNOWN;
  }

  public FailedReason Auth(Player player, Object... args) {
    final playerstatus p = Get(player.getName());
    if (p == null)
      return FailedReason.NOT_REGISTERED;

    if (p.Authed)
      return FailedReason.ALREADY_AUTHED;

    if (p.Method == null)
      return FailedReason.INVALID_METHOD;

    return p.Method.Authenticate(new PlayerInformation(player.getName()), args);
  }

  public void Connecting(Player player, String IP) {
    if (Contains(player.getName())) {
      final playerstatus ps = Get(player.getName());

      db.remove(ps);
      if (ps.LastIP != null
          && ps.LastIP.equalsIgnoreCase(IP)
          && System.currentTimeMillis() - ps.LastOnline < (Integer) extraauth.Settings
              ._("ReauthenticateTimeout", 5) * 1000 * 60) {
        ps.Authed = true;
        ps.LastIP = IP;
      }

      db.add(ps);
    } else
      player.hidePlayer(player);
  }

  public boolean Contains(String name) {
    for (final playerstatus ps : db)
      if (ps.Player.equalsIgnoreCase(name))
        return true;
    return false;
  }

  public void Disconnect(Player player) {
    if (Contains(player.getName())) {
      final playerstatus ps = Get(player.getName());
      db.remove(ps);

      ps.Authed = false;
      ps.LastOnline = System.currentTimeMillis();

      db.add(ps);

      Save();
    }
  }

  public playerstatus Get(String name) {
    for (final playerstatus ps : db)
      if (ps.Player.equalsIgnoreCase(name))
        return ps;
    return null;
  }

  public boolean IsAuth(Player player) {
    if (Contains(player.getName()))
      return Get(player.getName()).Authed;
    else
      return true;
  }

  public void Load() {
    try {
      final FileInputStream in = new FileInputStream(file);
      final Map<String, Tag> nbt = NBT.NBTToMap(NBT.loadNBT(in));

      db = new ArrayList<PlayerStatusDB.playerstatus>();

      final Map<String, Tag> players = (Map<String, Tag>) nbt.get("Players")
          .get();

      for (final String player : players.keySet())
        try {
          db.add(new playerstatus(player, players));
        } catch (final Exception ee) {
        }

      in.close();
    } catch (final FileNotFoundException e) {
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public FailedReason Remove(String player) {
    return Remove(player, true);
  }

  public FailedReason Remove(String player, boolean needToAuth) {
    if (!Contains(player))
      return FailedReason.NOT_REGISTERED;
    if (needToAuth && !Get(player).Authed)
      return FailedReason.NEED_TO_AUTHENTICATE;

    db.remove(Get(player));

    Save();

    return FailedReason.SUCCESSFULL;
  }

  public void Save() {
    extraauth.Log.log(Level.INFO, extraauth.Lang._("ExtraAuth.Saving"));

    try {
      final FileOutputStream out = new FileOutputStream(file);
      final Map<String, Tag> nbt = new HashMap<String, Tag>();
      final Map<String, Tag> players = new HashMap<String, Tag>();

      for (final playerstatus ps : db)
        try {
          ps.writeObject(players);
        } catch (final Exception ee) {
        }

      nbt.put("Players", Tag.newCompound(players));

      NBT.saveNBT(out, NBT.tagToNBT("root", Tag.newCompound(nbt)));
      out.flush();
      out.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private void ConvertOld() {
    final File file = new File(extraauth.getDataFolder().getAbsolutePath()
        + File.separator + "PlayerStatusDB.db");
    new File(extraauth.getDataFolder().getAbsolutePath() + File.separator
        + "PlayerStatusDB.db.bak");
    playerstatus ps;
    int method;
    extraauth.Log.log(Level.WARNING, "ExtraAuth.Converting");

    try {
      final ObjectInputStream in = new ObjectInputStream(new FileInputStream(
          file));
      final int version = in.readInt();
      if (version != 1)
        throw new Exception("Invalid version");

      final int size = in.readInt();
      new HashMap<String, Tag>(size);

      for (int i = 0; i < size; i++) {
        ps = new playerstatus();
        ps.Player = in.readUTF();
        ps.Authed = in.readBoolean();
        ps.LastOnline = in.readLong();
        ps.LastIP = in.readUTF();
        ps.PrivateKey = in.readUTF();

        method = in.readInt();

        if (method == 1)
          ps.Method = AuthManager.GetAuthMethod("Key");
        else if (method == 2)
          ps.Method = AuthManager.GetAuthMethod("TOTP");
        else
          continue;

        db.add(ps);
      }

      in.close();
    } catch (final Exception e) {
      extraauth.Log.log(Level.SEVERE, e.getMessage(), false);
    }
  }

  private void copyFile(File sourceFile, File destFile) throws IOException {
    if (!destFile.exists())
      destFile.createNewFile();

    FileChannel source = null;
    FileChannel destination = null;

    try {
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
    } finally {
      if (source != null)
        source.close();
      if (destination != null)
        destination.close();
    }
  }

}
