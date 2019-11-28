package no.statkart.matrikkel.persistens.coordtransform;

import org.assertj.core.data.Offset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class SkTransBoundsTest {
    private static final String transformationErrorsKey = "ignoreTransformationErrors";
    private static String transformationErrorsSetting;

    public SkTrans getSkTrans() {
        return SkTransFactory.getInstance();
    }

    @BeforeClass
    public static void setup() {
        transformationErrorsSetting = System.getProperty(transformationErrorsKey);
        System.setProperty(transformationErrorsKey, "0");
    }

    @AfterClass
    public static void teardown() {
        if (transformationErrorsSetting != null) {
            System.setProperty(transformationErrorsKey, transformationErrorsSetting);
        } else {
            System.clearProperty(transformationErrorsKey);
        }
    }

    @Test
    public void testBounds() {
        int fraSosikode = 3; // NGOAkse3
        int tilSosikode = 84; // EUREFGeo
        assertThatThrownBy(()->getSkTrans().transform(1226001, 0, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        getSkTrans().transform(1226000, 0, 0, fraSosikode, tilSosikode);
        getSkTrans().transform(-30000, 0, 0, fraSosikode, tilSosikode);
        assertThatThrownBy(()->getSkTrans().transform(-30001, 0, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        assertThatThrownBy(()->getSkTrans().transform(0, 350001, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        getSkTrans().transform(0, 350000, 0, fraSosikode, tilSosikode);
        getSkTrans().transform(0, -380000, 0, fraSosikode, tilSosikode);
        assertThatThrownBy(()->getSkTrans().transform(0, -380001, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        double[] transformedMin = getSkTrans().transform(-30000, -380000, 0, fraSosikode, tilSosikode);
        assertThat(transformedMin.length).isEqualTo(3);
        assertThat(transformedMin[0]).isEqualTo(207257.04470067387);
        assertThat(transformedMin[1]).isEqualTo(15704.106264764367);
        assertThat(transformedMin[2]).isEqualTo(0d);
        double[] transformedMax = getSkTrans().transform(1226000, 350000, 0, fraSosikode, tilSosikode);
        assertThat(transformedMax.length).isEqualTo(3);
        assertThat(transformedMax[0]).isEqualTo(247600.1145740121);
        assertThat(transformedMax[1]).describedAs("øst verdi")
                .isCloseTo(69845.59064847845, Offset.offset(GeometriskeKonsepter.antallBuesekunder(60, 0.000_000_000_001)));
        assertThat(transformedMax[2]).isEqualTo(0d);
    }

    static class GeometriskeKonsepter {
        public final static double ekvatorOmkretsKilometer = 40_000;
        public final static double ekvatorOmkretsMeter = ekvatorOmkretsKilometer * 1000;
        public final static double ekvatorOmkretsBuesekunder = 360 * 3600; //grader * sekunder

        public static double antallBuesekunder(double graderNord, double meter) {
            return meter * omkretsVedGraderNord(graderNord) / ekvatorOmkretsBuesekunder;
        }

        private static double omkretsVedGraderNord(double graderNord) {
            return ekvatorOmkretsMeter * Math.cos(graderNord * Math.PI / 180);
        }
    }
}
