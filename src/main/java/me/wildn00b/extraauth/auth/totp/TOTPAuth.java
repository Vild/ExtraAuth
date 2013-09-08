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

package me.wildn00b.extraauth.auth.totp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import me.wildn00b.extraauth.ExtraAuth;
import me.wildn00b.extraauth.api.AuthManager;
import me.wildn00b.extraauth.api.AuthMethod;
import me.wildn00b.extraauth.api.PlayerInformation;
import me.wildn00b.extraauth.api.event.FailedReason;
import me.wildn00b.extraauth.api.event.RegistrationFailedEvent;
import me.wildn00b.extraauth.api.event.RegistrationSuccessfullEvent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TOTPAuth extends AuthMethod {

  class ProcesssingTOTP implements Runnable {
    class Done implements Runnable {
      private final FailedReason failedReason;
      private final String message;
      private final Player player;

      public Done(String message, Player player, FailedReason failedReason) {
        this.message = message;
        this.player = player;
        this.failedReason = failedReason;
      }

      @Override
      public void run() {
        switch (failedReason) {
        case SUCCESSFULL:
          extraauth
              .getServer()
              .getPluginManager()
              .callEvent(
                  new RegistrationSuccessfullEvent(new PlayerInformation(player
                      .getName())));
          break;
        default:
          extraauth
              .getServer()
              .getPluginManager()
              .callEvent(
                  new RegistrationFailedEvent(player.getName(), failedReason,
                      AuthManager.GetAuthMethod("TOTP")));
          break;
        }
        send(player, message);
      }
    }

    private static final String tinyUrl = "http://tinyurl.com/api-create.php?url=";

    private final Player player;

    public ProcesssingTOTP(Player player) {
      this.player = player;
    }

    @Override
    public void run() {
      final String privatekey = TOTP.GeneratePrivateKey();
      FailedReason failedReason = extraauth.DB.Add(player.getName(),
          AuthManager.GetAuthMethod("TOTP"), privatekey);
      final String url = "http://chart.googleapis.com/chart?chs=400x400&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/ExtraAuth@"
          + (String) extraauth.Settings._("Servername", "Unknown server")
          + "?secret=" + privatekey;
      String message = null;
      String tinyurl = null;
      try {
        tinyurl = shorter(url);
      } catch (final IOException e) {
      }

      if (tinyurl != null) {
        if (failedReason == FailedReason.SUCCESSFULL)
          message = extraauth.Lang
              ._("Command.Enable.TOTP.Success")
              .replaceAll("%SERVERNAME%",
                  (String) extraauth.Settings._("Servername", "Unknown server"))
              .replaceAll("%URL%", tinyurl);
        else if (failedReason == FailedReason.ALREADY_REGISTERED)
          message = extraauth.Lang._("Command.Enable.AlreadyRegistered.Failed");
        else if (failedReason == FailedReason.INVALID_METHOD)
          message = extraauth.Lang._("Command.Enable.InvalidMethod.Failed");
        else
          message = extraauth.Lang._("Command.Enable.Unknown.Failed");

      } else {
        message = extraauth.Lang._("Command.Enable.UrlFailed");
        extraauth.DB.Remove(player.getName());
        failedReason = FailedReason.URL_FAILED;
      }
      extraauth
          .getServer()
          .getScheduler()
          .runTask(extraauth,
              new ProcesssingTOTP.Done(message, player, failedReason));
    }

    public String shorter(String url) throws IOException {
      final String tinyUrlLookup = tinyUrl + URLEncoder.encode(url, "UTF-8");
      final BufferedReader reader = new BufferedReader(new InputStreamReader(
          new URL(tinyUrlLookup).openStream()));
      final String tinyUrl = reader.readLine();
      return tinyUrl;
    }
  }

  ExtraAuth extraauth = ExtraAuth.INSTANCE;

  @Override
  public boolean AllowOtherToEnable() {
    return false;
  }

  @Override
  public FailedReason Authenticate(PlayerInformation information,
      Object... args) {
    if (args.length > 0) {
      String key = (String) args[0];

      for (int i = 1; i < args.length; i++)
        key += " " + (String) args[1];
      final long time = System.currentTimeMillis() / 30000L;
      final String PrivateKey = information.getPrivateKey();
      try {
        if (TOTP.GenerateTOTP(PrivateKey, time, 6, 1).trim()
            .equalsIgnoreCase(key.trim())) {
          information.setAuthed(true);
          return FailedReason.SUCCESSFULL;
        } else
          return FailedReason.WRONG_KEY;
      } catch (final Exception e) {
        return FailedReason.UNKNOWN;
      }
    } else
      return FailedReason.INVALID_ARGS;
  }

  @Override
  public String GetHelpLine(String language) {
    return ExtraAuth.INSTANCE.Lang._("Command.Enable.TOTP.Help");
  }

  @Override
  public String GetName() {
    return "TOTP";
  }

  @Override
  public String GetOtherHelpLine(String language) {
    return "";
  }

  @Override
  public FailedReason OnEnable(PlayerInformation information, Object... args) {
    extraauth
        .getServer()
        .getScheduler()
        .runTaskAsynchronously(extraauth,
            new ProcesssingTOTP(information.getPlayerObj()));
    return FailedReason.SUCCESSFULL;
  }

  private void send(CommandSender sender, String msg) {
    sender
        .sendMessage(ChatColor.YELLOW + "[ExtraAuth] " + ChatColor.GOLD + msg);
  }

}
