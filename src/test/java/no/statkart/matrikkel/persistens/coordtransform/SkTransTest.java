package no.statkart.matrikkel.persistens.coordtransform;

import org.assertj.core.util.DoubleComparator;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * testklasse for funksjonalitet gjennomført i SkTrans
 */
public class SkTransTest {
   public SkTrans getSkTrans() {
      return SkTransFactory.getInstance();
   }

   public SkTrans getZIgnoringSkTrans() {
      return SkTransFactory.getZIgnoringInstance();
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

      Coordinate transformert = getSkTrans().transform(fraPos1.getX(), fraPos1.getY(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformert.getOest());
      fraPos1.setY(transformert.getNord());
      fraPos1.setZ(transformert.getHoyde());

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformert = getSkTrans().transform(fraPos2.getX(), fraPos2.getY(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformert.getOest());
      fraPos2.setY(transformert.getNord());
      fraPos2.setZ(transformert.getHoyde());

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

      Coordinate transformert = getSkTrans().transform(fraPos1.getX(), fraPos1.getY(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformert.getOest());
      fraPos1.setY(transformert.getNord());
      fraPos1.setZ(transformert.getHoyde());

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformert = getSkTrans().transform(fraPos2.getX(), fraPos2.getY(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformert.getOest());
      fraPos2.setY(transformert.getNord());
      fraPos2.setZ(transformert.getHoyde());

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

      Coordinate transformert = getSkTrans().transform(fraPos1.getX(), fraPos1.getY(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformert.getOest());
      fraPos1.setY(transformert.getNord());
      fraPos1.setZ(transformert.getHoyde());
      transformert = getSkTrans().transform(fraPos1.getX(), fraPos1.getY(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), tilSosiSys, fraSosiSys);
      fraPos1.setX(transformert.getOest());
      fraPos1.setY(transformert.getNord());
      fraPos1.setZ(transformert.getHoyde());

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformert = getSkTrans().transform(fraPos2.getX(), fraPos2.getY(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformert.getOest());
      fraPos2.setY(transformert.getNord());
      fraPos2.setZ(transformert.getHoyde());
      transformert = getSkTrans().transform(fraPos2.getX(), fraPos2.getY(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), tilSosiSys, fraSosiSys);
      fraPos2.setX(transformert.getOest());
      fraPos2.setY(transformert.getNord());
      fraPos2.setZ(transformert.getHoyde());

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

      Coordinate transformert = getSkTrans().transform(fraPos1.getX(), fraPos1.getY(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), fraSosiSys, tilSosiSys);
      fraPos1.setX(transformert.getOest());
      fraPos1.setY(transformert.getNord());
      fraPos1.setZ(transformert.getHoyde());
      transformert = getSkTrans().transform(fraPos1.getX(), fraPos1.getY(), fraPos1.getZ() == null ? 0d : fraPos1.getZ(), tilSosiSys, fraSosiSys);
      fraPos1.setX(transformert.getOest());
      fraPos1.setY(transformert.getNord());
      fraPos1.setZ(transformert.getHoyde());

      assertThat(fraPos1.isSamePosition(tilPos1))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();

      transformert = getSkTrans().transform(fraPos2.getX(), fraPos2.getY(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), fraSosiSys, tilSosiSys);
      fraPos2.setX(transformert.getOest());
      fraPos2.setY(transformert.getNord());
      fraPos2.setZ(transformert.getHoyde());
      transformert = getSkTrans().transform(fraPos2.getX(), fraPos2.getY(), fraPos2.getZ() == null ? 0d : fraPos2.getZ(), tilSosiSys, fraSosiSys);
      fraPos2.setX(transformert.getOest());
      fraPos2.setY(transformert.getNord());
      fraPos2.setZ(transformert.getHoyde());

      assertThat(fraPos2.isSamePosition(tilPos2))
              .withFailMessage("Transformert posisjon stemmer ikke med virkelige data!")
              .isTrue();
   }

   @Test
   public void testTrans_22_23() {
      getSkTrans().transform(12205.62, 220017.14, 0, 3, 22);
      getSkTrans().transform(12207.99, 220011.04, 0, 3, 22);
      getSkTrans().transform(12197.69, 220023.86, 0, 3, 22);
      getSkTrans().transform(12207.54, 220041.78, 0, 3, 22);
      getSkTrans().transform(12210.60, 220047.35, 0, 3, 22);
      getSkTrans().transform(12211.54, 220038.15, 0, 3, 22);
      getSkTrans().transform(12217.12, 220032.12, 0, 3, 22);
      getSkTrans().transform(12228.56, 220022.82, 0, 3, 22);
      getSkTrans().transform(12242.40, 220012.07, 0, 3, 22);
      getSkTrans().transform(12259.62, 219999.5, 0, 3, 22);
      getSkTrans().transform(12256.00, 219996.87, 0, 3, 22);
      getSkTrans().transform(12256.33, 219997.17, 0, 3, 22);
      getSkTrans().transform(12242.26, 219983.05, 0, 3, 22);
      getSkTrans().transform(12207.99, 220011.04, 0, 3, 22);
      getSkTrans().transform(12242.26, 219983.05, 0, 3, 22);
   }

   @Test
   public void testNegativeLimit() {
      getSkTrans().transform(12242.26, 219983.05, -500, 22, 84);
      assertThatThrownBy(()->getSkTrans().transform(12242.26, 219983.05, -501, 22, 84))
              .isInstanceOf(SkTransException.class)
              .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
   }

   @Test
   public void testThatZIgnoringInstanceReturnsZUnTouched() {
      double z = -501.0;
      assertThat(getZIgnoringSkTrans().transform(12242.26, 219983.05, z, 22, 84).getHoyde()).isEqualTo(z);
   }

   @Test
   public void testThatZIgnoringInstanceIsSeparateFromNormal() {
      assertThat(getZIgnoringSkTrans()).isNotSameAs(getSkTrans());
   }

   @Test
   public void testThatZIgnoringInstancesAreSame() {
      assertThat(getZIgnoringSkTrans()).isSameAs(getZIgnoringSkTrans());
   }

   @Test
   public void testThatPlainInstancesAreSame() {
      assertThat(getSkTrans()).isSameAs(getSkTrans());
   }

   @Test
   public void testArrayVersionGivesSameValuesAsCoordinatesVersion() {
      double x1 = 12242.26;
      double y1 = 219983.05;
      double z1 = 15;
      double x2 = 514390;
      double y2 = 8683370;
      double z2 = 30;
      int fraSosi = 22;
      int tilSosi = 84;
      Coordinate c1 = getSkTrans().transform(x1, y1, z1, fraSosi, tilSosi);
      Coordinate c2 = getSkTrans().transform(x2, y2, z2, fraSosi, tilSosi);
      assertThat(getSkTrans().transform(new double[]{x1, y1, z1, x2, y2, z2}, 3, fraSosi, tilSosi))
              .isEqualTo(new Coordinate[]{
                      new Coordinate(c1.getOest(), c1.getNord(), c1.getHoyde()),
                      new Coordinate(c2.getOest(), c2.getNord(), c2.getHoyde())
              });
   }

   @Test
   public void arrayVersionReturnsSameIfToAnfFromCodesAreTheSame() {
      double x = 2344312242.26234324343;
      double y = 4243219983.0525235423;
      double z = 3254252315.23453254251;
      assertThat(getSkTrans().transform(new double[]{x, y, z}, 3, 22, 22))
              .isEqualTo(new Coordinate[]{new Coordinate(x, y, z)});
   }

   @Test
   public void transformToEurefgeoDividesBy3600() {
      DoubleComparator comparator = new DoubleComparator(0.000000001);
      assertThat(getSkTrans().transform(514390.0, 8683370.0, 0.0, 23, 84))
              .usingComparatorForType(comparator, Double.class)
              .usingRecursiveComparison()
              .isEqualTo(new Coordinate(15.631590845, 78.223319472, 0.00));
   }

   @Test
   public void transformFromEurefgeoMultipliesBy3600() {
      DoubleComparator comparator = new DoubleComparator(0.001);
      assertThat(getSkTrans().transform(15.631590845, 78.223319472, 0.0,  84, 23))
              .usingComparatorForType(comparator, Double.class)
              .usingRecursiveComparison()
              .isEqualTo(new Coordinate(514390.0, 8683370.0, 0.00));
   }
}
