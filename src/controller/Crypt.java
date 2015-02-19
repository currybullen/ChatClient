package controller;

/* Crypt.java
 * Given fil till laboration 2 - Distribuerad Chat pa kursen Datakommunikation
 * och Datornat C, 5p vid Umea Universitet ht 2001 och vt 2002.
 * Av Per Nordlinder (per@cs.umu.se) och Jon Hollstrom (jon@cs.umu.se)
 */

public class Crypt {

   /* Namn:       {en, de}crypt
    * Purpose:    Krypterar eller dekrypterar data
    * Argument:   src - Buffert med datan som ska behandlas
    *             srclen - Langden i bytes pa src
    *             key - Krypteringsnyckel som skall anvandas
    *             keylen - Langden i bytes pa krypteringsnyckeln
    * Returnerar: Ingenting
    */

   public static void encrypt(byte[] src, int srclen, byte[] key, int keylen) {

      for(int i=0; i<srclen; i++)
         src[i] ^= key[i%keylen];
   }

  	public static void decrypt(byte[] src, int srclen, byte[] key, int keylen) {
      encrypt(src, srclen, key, keylen);
  	}

}
