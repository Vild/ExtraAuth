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

import java.nio.ByteBuffer;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TOTP {

  public static String GeneratePrivateKey() {
    final byte[] key = new byte[16];
    new Random(System.nanoTime() + System.currentTimeMillis()).nextBytes(key);
    return Base32.encode(key).substring(0, 16);
  }

  public static String GenerateTOTP(String key, long time, int length,
      int SHAbit) throws Exception {
    String result = null;

    final byte[] msg = ByteBuffer.allocate(8).putLong(time).array();
    final byte[] k = Base32.decode(key);
    final byte[] hash = hmacSHA(SHAbit, k, msg);

    final int offset = hash[hash.length - 1] & 0xf;

    final int binary = ((hash[offset] & 0x7f) << 24)
        | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8)
        | (hash[offset + 3] & 0xff);

    final int otp = binary % (int) Math.pow(10, length);

    result = Integer.toString(otp);

    while (result.length() < length)
      result = "0" + result;

    return result;
  }

  private static byte[] hmacSHA(int SHAbit, byte[] key, byte[] data)
      throws Exception {
    if (SHAbit != 1 && SHAbit != 256 && SHAbit != 512)
      throw new Exception("SHAbit can only be 1, 256, 512!");

    final Mac hmac = Mac.getInstance("HmacSHA" + SHAbit);
    hmac.init(new SecretKeySpec(key, ""));
    return hmac.doFinal(data);
  }
}