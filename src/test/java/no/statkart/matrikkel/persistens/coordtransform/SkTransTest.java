package no.statkart.matrikkel.persistens.coordtransform;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * testklasse for funksjonalitet gjennomført i SkTrans
 */
public class SkTransTest {
   public SkTrans getSkTrans() {
      return SkTransFactory.getInstance();
   }

   static class Position {
      private Double x;
      private Double y;
      private Double z;

      public void setY(Double y) {
         this.y = y;
      }

      public void setZ(Double z) {
         this.z = z;
      }

      public void setX(Double x) {
         this.x = x;
      }

      public Position(double x, double y) {
         this.x = x;
         this.y = y;
      }

      public Double getX() {
         return x;
      }

      public Double getY() {
         return y;
      }

      public Double getZ() {
         return z;
      }

      private boolean compare(double d1, double d2) {
         int scale = 2;
         RoundingMode rounding = RoundingMode.HALF_UP;
         return new BigDecimal(d1).setScale(scale, rounding).equals(new BigDecimal(d2).setScale(scale, rounding));
      }

      public boolean isSamePosition(Position other) {
         if (z == null && other.z == null || z == null && other.z == 0 || z == 0 && other.z == null) {
            return compare(x, other.x) && compare(y, other.y);
         }
         return compare(x, other.x) && compare(y, other.y) && compare(z, other.z);
      }
   }

   @Test
   public void testInitialiseringAvBibliotek() {
      assertThat(SkTransFactory.getInstance())
              .isNotNull();
   }

   //endret mhp x = nord >_> men ikke hos oss.
   @Test
   public void testTransformeringAvEnkeltDataFraSosi_3_Til_22() {
      int fraSosiSys = 3;
      int tilSosiSys = 22;

      Position fraPos1 = new Position(-350000.00, 150000.00);
      Position fraPos2 = new Position(-340000.00, 150000.00);

      Position tilPos1 = new Position(248070.73, 6570974.38);
      Position tilPos2 = new Position(258055.97, 6571233.00);

      double[] transformertXYZ;
      transformertXYZ = getSkTrans().transform(fraPos1.getY(), fraPos1.getX(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformertXYZ[1]);
      fraPos1.setY(transformertXYZ[0]);
      fraPos1.setZ(transformertXYZ[2]);

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformertXYZ = getSkTrans().transform(fraPos2.getY(), fraPos2.getX(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformertXYZ[1]);
      fraPos2.setY(transformertXYZ[0]);
      fraPos2.setZ(transformertXYZ[2]);

      assertThat(fraPos2.isSamePosition(tilPos2))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();
   }

   @Test
   public void testTransformeringAvEnkeltDataFraSosi_3_Til_23() {
      int fraSosiSys = 3;
      int tilSosiSys = 23;

      Position fraPos1 = new Position(-350000.00, 150000.00);
      Position fraPos2 = new Position(-340000.00, 150000.00);

      Position tilPos1 = new Position(-93162.50, 6609129.90);
      Position tilPos2 = new Position(-83159.84, 6608484.13);

      double[] transformertXYZ;
      transformertXYZ = getSkTrans().transform(fraPos1.getY(), fraPos1.getX(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformertXYZ[1]);
      fraPos1.setY(transformertXYZ[0]);
      fraPos1.setZ(transformertXYZ[2]);

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformertXYZ = getSkTrans().transform(fraPos2.getY(), fraPos2.getX(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformertXYZ[1]);
      fraPos2.setY(transformertXYZ[0]);
      fraPos2.setZ(transformertXYZ[2]);

      assertThat(fraPos2.isSamePosition(tilPos2))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();
   }

   @Test
   public void testTransformeringAvEnkeltDataFraSosi_3_Til_23_Til_3() {
      int fraSosiSys = 3;
      int tilSosiSys = 23;

      Position fraPos1 = new Position(-350000.00, 150000.00);
      Position fraPos2 = new Position(-340000.00, 150000.00);

      Position tilPos1 = new Position(-350000.00, 150000.00);
      Position tilPos2 = new Position(-340000.00, 150000.00);

      double[] transformertXYZ;
      transformertXYZ = getSkTrans().transform(fraPos1.getY(), fraPos1.getX(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformertXYZ[1]);
      fraPos1.setY(transformertXYZ[0]);
      fraPos1.setZ(transformertXYZ[2]);
      transformertXYZ = getSkTrans().transform(fraPos1.getY(), fraPos1.getX(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), tilSosiSys, fraSosiSys);
      fraPos1.setX(transformertXYZ[1]);
      fraPos1.setY(transformertXYZ[0]);
      fraPos1.setZ(transformertXYZ[2]);

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformertXYZ = getSkTrans().transform(fraPos2.getY(), fraPos2.getX(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformertXYZ[1]);
      fraPos2.setY(transformertXYZ[0]);
      fraPos2.setZ(transformertXYZ[2]);
      transformertXYZ = getSkTrans().transform(fraPos2.getY(), fraPos2.getX(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), tilSosiSys, fraSosiSys);
      fraPos2.setX(transformertXYZ[1]);
      fraPos2.setY(transformertXYZ[0]);
      fraPos2.setZ(transformertXYZ[2]);

      assertThat(fraPos2.isSamePosition(tilPos2))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();
   }

   @Test
   public void testTransformeringAvEnkeltDataFraSosi_3_Til_22_Til_3() {
      int fraSosiSys = 3;
      int tilSosiSys = 22;

      Position fraPos1 = new Position(-350000.00, 150000.00);
      Position fraPos2 = new Position(-340000.00, 150000.00);

      Position tilPos1 = new Position(-350000.00, 150000.00);
      Position tilPos2 = new Position(-340000.00, 150000.00);

      double[] transformertXYZ;
      transformertXYZ = getSkTrans().transform(fraPos1.getY(), fraPos1.getX(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformertXYZ[1]);
      fraPos1.setY(transformertXYZ[0]);
      fraPos1.setZ(transformertXYZ[2]);
      transformertXYZ = getSkTrans().transform(fraPos1.getY(), fraPos1.getX(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), tilSosiSys, fraSosiSys);
      fraPos1.setX(transformertXYZ[1]);
      fraPos1.setY(transformertXYZ[0]);
      fraPos1.setZ(transformertXYZ[2]);

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformertXYZ = getSkTrans().transform(fraPos2.getY(), fraPos2.getX(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformertXYZ[1]);
      fraPos2.setY(transformertXYZ[0]);
      fraPos2.setZ(transformertXYZ[2]);
      transformertXYZ = getSkTrans().transform(fraPos2.getY(), fraPos2.getX(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), tilSosiSys, fraSosiSys);
      fraPos2.setX(transformertXYZ[1]);
      fraPos2.setY(transformertXYZ[0]);
      fraPos2.setZ(transformertXYZ[2]);

      assertThat(fraPos2.isSamePosition(tilPos2))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();
   }

   @Test
   public void testTrans_22_23() {
//      double[] transformert1 = getSkTrans().transform(6649996, 593029, 0, 22, 23);
//      double[] transformert2 = getSkTrans().transform(6673672, 611324, 0, 22, 23);

      double[] transformert3 = getSkTrans().transform(220017.14, 12205.62, 0, 3, 22);
      double[] transformert4 = getSkTrans().transform(220011.04, 12207.99, 0, 3, 22);
      double[] transformert5 = getSkTrans().transform(220023.86, 12197.69, 0, 3, 22);
      double[] transformert6 = getSkTrans().transform(220041.78, 12207.54, 0, 3, 22);
      double[] transformert7 = getSkTrans().transform(220047.35, 12210.60, 0, 3, 22);
      double[] transformert8 = getSkTrans().transform(220038.15, 12211.54, 0, 3, 22);
      double[] transformert9 = getSkTrans().transform(220032.12, 12217.12, 0, 3, 22);
      double[] transformert10 = getSkTrans().transform(220022.82, 12228.56, 0, 3, 22);
      double[] transformert11 = getSkTrans().transform(220012.07, 12242.40, 0, 3, 22);
      double[] transformert12 = getSkTrans().transform(219999.5, 12259.62, 0, 3, 22);
      double[] transformert13 = getSkTrans().transform(219996.87, 12256.00, 0, 3, 22);
      double[] transformert14 = getSkTrans().transform(219997.17, 12256.33, 0, 3, 22);
      double[] transformert15 = getSkTrans().transform(219983.05, 12242.26, 0, 3, 22);
      double[] transformert16 = getSkTrans().transform(220011.04, 12207.99, 0, 3, 22);
      double[] transformert17 = getSkTrans().transform(219983.05, 12242.26, 0, 3, 22);
   }
}
