package controller;

/* Checksum.java
 * Given fil till laboration 2 - Distribuerad Chat pa kursen Datakommunikation
 * och Datornat C, 5p vid Umea Universitet ht 2001 och vt 2002
 * Av Per Nordlinder (per@cs.umu.se) och Jon Hollstrom (jon@cs.umu.se)
 */

public class Checksum {

  /* Namn: calc
   * Syfte: Beraknar checksumma pa en byte-array.
   * Argument: buf   - Datat som checksumman skall beraknas pa.
   *           count - Det antal bytes som checksumman skall beraknas pa.
   * Returnerar: checksumman som en byte.
   */
   public static byte calc(byte[] buf, int count) {
      int sum = 0;
      int i = 0;

      while((count--) != 0) {
         sum += (buf[i] & 0x000000FF);
         i++;
         if((sum & 0x00000100) != 0) {
            sum &= 0x000000FF;
            sum++;
         }
      }

      return (byte)~(sum & 0xFF);
   }
}

