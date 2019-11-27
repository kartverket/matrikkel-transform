package no.statkart.matrikkel.persistens.coordtransform;

import no.statkart.matrikkel.domene.geometri.koder.KoordinatsystemKodeId;
import no.statkart.matrikkel.domene.util.SkTransFactory;
import org.assertj.core.data.Offset;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testng.AssertJUnit.assertEquals;

public class SkTransBoundsTest {
    private final String transformationErrorsKey = "ignoreTransformationErrors";
    private String transformationErrorsSetting;

    @BeforeClass
    public void setup() {
        transformationErrorsSetting = System.getProperty(transformationErrorsKey);
        System.setProperty(transformationErrorsKey, "0");
    }

    @AfterClass
    public void teardown() {
        if (transformationErrorsSetting != null) {
            System.setProperty(transformationErrorsKey, transformationErrorsSetting);
        } else {
            System.clearProperty(transformationErrorsKey);
        }
    }

    @Test
    public void testBounds() {
        int fraSosikode = KoordinatsystemKodeId.NGOAkse3.getSosikode();
        int tilSosikode = KoordinatsystemKodeId.EUREFGeo.getSosikode();
        assertThatThrownBy(()->SkTransFactory.getSkTrans().transform(1226001, 0, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        SkTransFactory.getSkTrans().transform(1226000, 0, 0, fraSosikode, tilSosikode);
        SkTransFactory.getSkTrans().transform(-30000, 0, 0, fraSosikode, tilSosikode);
        assertThatThrownBy(()->SkTransFactory.getSkTrans().transform(-30001, 0, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        assertThatThrownBy(()->SkTransFactory.getSkTrans().transform(0, 350001, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        SkTransFactory.getSkTrans().transform(0, 350000, 0, fraSosikode, tilSosikode);
        SkTransFactory.getSkTrans().transform(0, -380000, 0, fraSosikode, tilSosikode);
        assertThatThrownBy(()->SkTransFactory.getSkTrans().transform(0, -380001, 0, fraSosikode, tilSosikode))
                .isInstanceOf(SkTransException.class)
                .hasMessage("Transformasjon feilet med kode 19: X eller Y-verdi utenfor område");
        double[] transformedMin = SkTransFactory.getSkTrans().transform(-30000, -380000, 0, fraSosikode, tilSosikode);
        assertEquals(3, transformedMin.length);
        assertEquals(207257.04470067387, transformedMin[0]);
        assertEquals(15704.106264764367, transformedMin[1]);
        assertEquals(0d, transformedMin[2]);
        double[] transformedMax = SkTransFactory.getSkTrans().transform(1226000, 350000, 0, fraSosikode, tilSosikode);
        assertEquals(3, transformedMax.length);
        assertEquals(247600.1145740121, transformedMax[0]);
        assertThat(transformedMax[1]).describedAs("øst verdi")
                .isCloseTo(69845.59064847845, Offset.offset(GeometriskeKonsepter.antallBuesekunder(60, 0.000_000_000_001)));
        assertEquals(0d, transformedMax[2]);
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
