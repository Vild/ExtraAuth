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
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import me.wildn00b.extraauth.ExtraAuth;
import me.wildn00b.extraauth.api.event.FailedReason;
import me.wildn00b.extraauth.auth.AuthMethod;
import me.wildn00b.extraauth.auth.totp.TOTP;

import org.bukkit.entity.Player;

public class PlayerStatusDB {

  public class playerstatus {
    public boolean Authed;
    public String LastIP;
    public long LastOnline;
    public AuthMethod Method;
    public String Player;
    public String PrivateKey;

    public playerstatus(ObjectInputStream in) {
      try {
        readObject(in);
      } catch (final Exception e) {
      }
    }

    public playerstatus(String player, boolean authed, long lastOnline,
        String lastIP, String privateKey, AuthMethod method) {
      this.Player = player;
      this.Authed = authed;
      this.LastOnline = lastOnline;
      this.LastIP = lastIP;
      this.PrivateKey = privateKey;
      this.Method = method;
    }

    public void readObject(ObjectInputStream in) throws IOException {
      Player = in.readUTF();
      Authed = in.readBoolean();
      LastOnline = in.readLong();
      LastIP = in.readUTF();
      PrivateKey = in.readUTF();
      Method = AuthMethod.GetAuthMethod(in.readInt());
    }

    @Override
    public String toString() {
      return "playerstatus [Player=" + Player + ", Authed=" + Authed
          + ", LastOnline=" + LastOnline + ", LastIP=" + LastIP
          + ", PrivateKey=" + PrivateKey + ", Method=" + Method + "]";
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeUTF(Player);
      out.writeBoolean(Authed);
      out.writeLong(LastOnline);
      out.writeUTF(LastIP);
      out.writeUTF(PrivateKey);
      out.writeInt(Method.GetID());
    }

  }

  public static final int CURRENT_VERSION = 1;
  private ArrayList<playerstatus> db = new ArrayList<playerstatus>();
  private final ExtraAuth extraauth;
  private final File file;

  private final File filebackup;

  public PlayerStatusDB(ExtraAuth totpAuth) {
    this.extraauth = totpAuth;
    file = new File(totpAuth.getDataFolder().getAbsolutePath() + File.separator
        + "PlayerStatusDB.db");
    filebackup = new File(totpAuth.getDataFolder().getAbsolutePath()
        + File.separator + "PlayerStatusDB.db.bak");
    Load();
  }

  public FailedReason Add(Player player, AuthMethod method, String key) {
    if (Contains(player.getName()))
      return FailedReason.ALREADY_REGISTERED;
    if (method != AuthMethod.TOTP && method != AuthMethod.KEY)
      return FailedReason.INVALID_METHOD;

    if (!db
        .add(new playerstatus(player.getName(), true, System
            .currentTimeMillis(), player.getAddress().getHostString(), key,
            method)))
      return FailedReason.UNKNOWN;
    Save();

    return FailedReason.SUCCESSFULL;
  }

  public FailedReason Auth(Player player, String key) {
    final playerstatus p = Get(player.getName());
    if (p == null)
      return FailedReason.NOT_REGISTERED;

    if (p.Authed)
      return FailedReason.ALREADY_AUTHED;

    try {
      switch (p.Method) {
      case TOTP:
        final long time = System.currentTimeMillis() / 30000L;
        final String PrivateKey = p.PrivateKey;
        if (TOTP.GenerateTOTP(PrivateKey, time, 6, 1).trim()
            .equalsIgnoreCase(key.trim())) {
          p.Authed = true;
          player.setFlying(false);
          return FailedReason.SUCCESSFULL;
        } else
          return FailedReason.WRONG_KEY;
      case KEY:
        if (key.equals(p.PrivateKey)) {
          p.Authed = true;
          return FailedReason.SUCCESSFULL;
        } else
          return FailedReason.WRONG_KEY;
      default:
        return FailedReason.INVALID_METHOD;
      }

    } catch (final Exception e) {
      e.printStackTrace();
    }
    return FailedReason.INVALID_METHOD;
  }

  public void Connecting(Player player, String IP) {
    if (Contains(player.getName())) {
      final playerstatus ps = Get(player.getName());

      db.remove(ps);
      if (ps.LastIP.equalsIgnoreCase(IP)
          && System.currentTimeMillis() - ps.LastOnline < (Integer) extraauth.Settings
              ._("ReauthenticateTimeout") * 1000 * 60) {
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
      final ObjectInputStream in = new ObjectInputStream(new FileInputStream(
          file));
      final int version = in.readInt();
      if (version != CURRENT_VERSION)
        throw new Exception("");

      final int size = in.readInt();
      db = new ArrayList<PlayerStatusDB.playerstatus>(size);

      for (int i = 0; i < size; i++)
        db.add(new playerstatus(in));

      in.close();
    } catch (final FileNotFoundException e) {
    } catch (final NullPointerException e) {
      extraauth._E("ExtraAuth.CorruptDB");

      try {
        file.delete();
      } catch (final Exception ee) {
      }

      if (!filebackup.exists()) {
        extraauth._E("ExtraAuth.MissingBackup");
        db = new ArrayList<playerstatus>();
        return;
      }

      try {
        copyFile(filebackup, file);
        filebackup.renameTo(new File(filebackup.getPath() + ".finalbak"));
      } catch (final Exception ee) {
      }
      Load();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public boolean Remove(Player player) {
    if (!Contains(player.getName()) || !Get(player.getName()).Authed)
      return false;

    final boolean ret = db.remove(Get(player.getName()));

    Save();

    return ret;
  }

  public void Save() {
    extraauth._("ExtraAuth.Saving");
    try {
      filebackup.delete();
    } catch (final Exception e) {
    }
    try {
      file.renameTo(filebackup);
    } catch (final Exception e) {
    }

    try {
      final ObjectOutputStream out = new ObjectOutputStream(
          new FileOutputStream(file));

      out.writeInt(CURRENT_VERSION);
      out.writeInt(db.size());
      for (final playerstatus ps : db)
        ps.writeObject(out);
      out.flush();
      out.close();
    } catch (final Exception e) {
      e.printStackTrace();
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
