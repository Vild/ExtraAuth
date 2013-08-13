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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import me.wildn00b.extraauth.ExtraAuth;
import me.wildn00b.extraauth.api.PlayerInformation;
import me.wildn00b.extraauth.api.event.AuthenticateFailedEvent;
import me.wildn00b.extraauth.api.event.AuthenticateSuccessfullEvent;
import me.wildn00b.extraauth.api.event.FailedReason;
import me.wildn00b.extraauth.api.event.PreAuthenticateEvent;
import me.wildn00b.extraauth.api.event.PreRegistrationEvent;
import me.wildn00b.extraauth.api.event.PreUnregistrationEvent;
import me.wildn00b.extraauth.api.event.RegistrationFailedEvent;
import me.wildn00b.extraauth.api.event.RegistrationSuccessfullEvent;
import me.wildn00b.extraauth.api.event.UnregistrationFailedEvent;
import me.wildn00b.extraauth.api.event.UnregistrationSuccessfullEvent;
import me.wildn00b.extraauth.auth.AuthMethod;
import me.wildn00b.extraauth.auth.totp.TOTP;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {

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
                      AuthMethod.TOTP));
          break;
        }
        send(player, message);
      }
    }

    private final Player player;

    public ProcesssingTOTP(Player player) {
      this.player = player;
    }

    @Override
    public void run() {
      final String privatekey = TOTP.GeneratePrivateKey();
      FailedReason failedReason = extraauth.DB.Add(player, AuthMethod.TOTP,
          privatekey);
      final String url = "http://chart.googleapis.com/chart?chs=400x400&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/ExtraAuth@"
          + (String) extraauth.Settings._("Servername")
          + "?secret="
          + privatekey;
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
                  (String) extraauth.Settings._("Servername"))
              .replaceAll("%URL%", tinyurl);
        else if (failedReason == FailedReason.ALREADY_REGISTERED)
          message = extraauth.Lang._("Command.Enable.AlreadyRegistered.Failed");
        else if (failedReason == FailedReason.INVALID_METHOD)
          message = extraauth.Lang._("Command.Enable.InvalidMethod.Failed");
        else
          message = extraauth.Lang._("Command.Enable.Unknown.Failed");

      } else {
        message = extraauth.Lang._("Command.Enable.UrlFailed");
        extraauth.DB.Remove(player);
        failedReason = FailedReason.URL_FAILED;
      }
      extraauth
          .getServer()
          .getScheduler()
          .runTask(extraauth,
              new ProcesssingTOTP.Done(message, player, failedReason));
    }
  }

  private static final String tinyUrl = "http://tinyurl.com/api-create.php?url=";

  private final ExtraAuth extraauth;

  public AuthCommand(ExtraAuth extraauth) {
    this.extraauth = extraauth;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
      String[] args) {

    if (!(sender instanceof Player)) {
      send(sender, extraauth.Lang._("Command.OnlyIngame"));
      return true;
    }

    final Player player = (Player) sender;

    try {
      if (args.length > 0) {
        if (args[0].equalsIgnoreCase("disable")) {
          final PreUnregistrationEvent event = new PreUnregistrationEvent(
              new PlayerInformation(player.getName()));
          extraauth.getServer().getPluginManager().callEvent(event);

          if (event.isCancelled()) {
            extraauth
                .getServer()
                .getPluginManager()
                .callEvent(
                    new UnregistrationFailedEvent(new PlayerInformation(player
                        .getName()), FailedReason.CANCELED));
            send(sender, extraauth.Lang._("Command.Disable.Event.Failed"));
          } else if (extraauth.DB.Get(player.getName()) != null
              && !extraauth.DB.Get(player.getName()).Authed) {
            extraauth
                .getServer()
                .getPluginManager()
                .callEvent(
                    new UnregistrationFailedEvent(new PlayerInformation(player
                        .getName()), FailedReason.NEED_TO_AUTHENTICATE));
            send(sender, extraauth.Lang._("NeedToAuthenticate"));

          } else if (extraauth.DB.Remove(player)) {
            extraauth
                .getServer()
                .getPluginManager()
                .callEvent(new UnregistrationSuccessfullEvent(player.getName()));
            send(sender, extraauth.Lang._("Command.Disable.Success"));
          } else {
            extraauth
                .getServer()
                .getPluginManager()
                .callEvent(
                    new UnregistrationFailedEvent(new PlayerInformation(player
                        .getName()), FailedReason.NOT_REGISTERED));
            send(sender, extraauth.Lang._("Command.Disable.Failed"));
          }

        } else if (args[0].equalsIgnoreCase("help")) {
          if (args.length > 1)
            ShowHelp(sender, label, Integer.parseInt(args[1]));
          else
            ShowHelp(sender, label, 1);
        } else if (args[0].equalsIgnoreCase("enable")) {
          if (extraauth.DB.Get(player.getName()) != null
              && !extraauth.DB.Get(player.getName()).Authed) {
            extraauth
                .getServer()
                .getPluginManager()
                .callEvent(
                    new UnregistrationFailedEvent(new PlayerInformation(player
                        .getName()), FailedReason.NEED_TO_AUTHENTICATE));
            send(sender, extraauth.Lang._("NeedToAuthenticate"));

          } else {
            final PreRegistrationEvent event = new PreRegistrationEvent(player);
            extraauth.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
              extraauth
                  .getServer()
                  .getPluginManager()
                  .callEvent(
                      new RegistrationFailedEvent(player.getName(),
                          FailedReason.CANCELED, AuthMethod.INVALID));
              send(sender, extraauth.Lang._("Command.Enable.Event.Failed"));
            } else if (args[1].equalsIgnoreCase("totp")) {
              if ((Boolean) extraauth.Settings._("AuthMethod.TOTP")) {
                send(sender, extraauth.Lang._("Command.Enable.Processing"));
                extraauth
                    .getServer()
                    .getScheduler()
                    .runTaskAsynchronously(extraauth,
                        new ProcesssingTOTP(player));
              } else
                extraauth
                    .getServer()
                    .getPluginManager()
                    .callEvent(
                        new RegistrationFailedEvent(player.getName(),
                            FailedReason.CONFIG_BLOCK, AuthMethod.TOTP));
            } else if (args[1].equalsIgnoreCase("key")) {
              if ((Boolean) extraauth.Settings._("AuthMethod.TOTP")) {

                if (args.length > 2) {
                  String key = args[2];
                  for (int i = 3; i < args.length; i++)
                    key += " " + args[i];

                  final FailedReason reason = extraauth.DB.Add(player,
                      AuthMethod.KEY, key);
                  if (reason == FailedReason.SUCCESSFULL) {
                    extraauth
                        .getServer()
                        .getPluginManager()
                        .callEvent(
                            new RegistrationSuccessfullEvent(
                                new PlayerInformation(player.getName())));
                    send(sender, extraauth.Lang._("Command.Enable.Key.Success"));
                  } else {
                    extraauth
                        .getServer()
                        .getPluginManager()
                        .callEvent(
                            new RegistrationFailedEvent(player.getName(),
                                reason, AuthMethod.KEY));
                    if (reason == FailedReason.ALREADY_REGISTERED)
                      send(sender,
                          extraauth.Lang
                              ._("Command.Enable.AlreadyRegistered.Failed"));
                    else if (reason == FailedReason.INVALID_METHOD)
                      send(sender,
                          extraauth.Lang
                              ._("Command.Enable.InvalidMethod.Failed"));
                    else
                      send(sender,
                          extraauth.Lang._("Command.Enable.Unknown.Failed"));
                  }
                } else
                  extraauth
                      .getServer()
                      .getPluginManager()
                      .callEvent(
                          new RegistrationFailedEvent(player.getName(),
                              FailedReason.CONFIG_BLOCK, AuthMethod.KEY));
              } else
                ShowHelp(sender, label, 1);
            } else
              ShowHelp(sender, label, 1);
          }
        } else {
          final PreAuthenticateEvent event = new PreAuthenticateEvent(
              new PlayerInformation(player.getName()));
          extraauth.getServer().getPluginManager().callEvent(event);
          if (event.isCancelled()) {
            extraauth
                .getServer()
                .getPluginManager()
                .callEvent(
                    new AuthenticateFailedEvent(new PlayerInformation(player
                        .getName()), FailedReason.CANCELED));
            send(sender, extraauth.Lang._("Command.Auth.Event.Failed"));
          } else {
            String key = args[0];
            for (int i = 1; i < args.length; i++)
              key += " " + args[i];

            final FailedReason ret = extraauth.DB.Auth(player, key);
            if (ret == FailedReason.SUCCESSFULL) {
              extraauth
                  .getServer()
                  .getPluginManager()
                  .callEvent(
                      new AuthenticateSuccessfullEvent(new PlayerInformation(
                          player.getName())));
              send(sender, extraauth.Lang._("Command.Auth.Success"));
            } else {
              extraauth
                  .getServer()
                  .getPluginManager()
                  .callEvent(
                      new AuthenticateFailedEvent(new PlayerInformation(player
                          .getName()), ret));

              if (ret == FailedReason.NOT_REGISTERED)
                send(sender,
                    extraauth.Lang._("Command.Auth.NotRegistered.Failed"));
              else if (ret == FailedReason.ALREADY_AUTHED)
                send(sender,
                    extraauth.Lang._("Command.Auth.AlreadyAuthed.Failed"));
              else if (ret == FailedReason.WRONG_KEY)
                send(sender, extraauth.Lang._("Command.Auth.WrongKey.Failed"));
              else if (ret == FailedReason.INVALID_METHOD)
                send(sender,
                    extraauth.Lang._("Command.Auth.InvalidMethod.Failed"));
              else
                send(sender, extraauth.Lang._("Command.Auth.Unknown.Failed"));
            }
          }
        }
      } else
        ShowHelp(sender, label, 1);
    } catch (final ArrayIndexOutOfBoundsException e) {
      ShowHelp(sender, label, 1);
    } catch (final Exception e) {
      e.printStackTrace();
      send(sender, extraauth.Lang._("Command.Exception"));
    }

    return true;
  }

  public String shorter(String url) throws IOException {
    final String tinyUrlLookup = tinyUrl + URLEncoder.encode(url, "UTF-8");
    final BufferedReader reader = new BufferedReader(new InputStreamReader(
        new URL(tinyUrlLookup).openStream()));
    final String tinyUrl = reader.readLine();
    return tinyUrl;
  }

  private void send(CommandSender sender, String msg) {
    sender
        .sendMessage(ChatColor.YELLOW + "[ExtraAuth] " + ChatColor.GOLD + msg);
  }

  private void ShowHelp(CommandSender sender, String label, int page) {
    final ArrayList<String> cmds = new ArrayList<String>();

    cmds.add(ChatColor.YELLOW
        + "/"
        + label
        + " help "
        + extraauth.Lang._("Command.Help").replaceFirst("- ",
            ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
    cmds.add(ChatColor.YELLOW
        + "/"
        + label
        + " enable totp "
        + extraauth.Lang._("Command.Enable.TOTP.Help").replaceFirst("- ",
            ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
    cmds.add(ChatColor.YELLOW
        + "/"
        + label
        + " enable key "
        + extraauth.Lang._("Command.Enable.Key.Help").replaceFirst("- ",
            ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
    cmds.add(ChatColor.YELLOW
        + "/"
        + label
        + " disable "
        + extraauth.Lang._("Command.Disable.Help").replaceFirst("- ",
            ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));
    cmds.add(ChatColor.YELLOW
        + "/"
        + label
        + " "
        + extraauth.Lang._("Command.Auth.Help").replaceFirst("- ",
            ChatColor.DARK_AQUA + "-" + ChatColor.GOLD + " "));

    final int maxpage = 1 + cmds.size() / 6;
    if (page < 1)
      page = 1;
    else if (page > maxpage)
      page = maxpage;
    sender.sendMessage(""
        + ChatColor.RED
        + ChatColor.BOLD
        + extraauth.Lang._("Command.Title")
            .replaceAll("%VERSION%", extraauth.Version)
            .replaceAll("%PAGE%", "" + ChatColor.RED + page + ChatColor.AQUA)
            .replaceAll("%MAXPAGE%", "" + ChatColor.BLUE + maxpage)
        + ChatColor.RESET);
    try {
      for (int i = (page - 1) * 6; i < ((page - 1) * 6) + 6; i++)
        sender.sendMessage(cmds.get(i));

    } catch (final Exception e) {
    }
  }

}
