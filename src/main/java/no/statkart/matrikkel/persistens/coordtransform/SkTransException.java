package no.statkart.matrikkel.persistens.coordtransform;

import java.util.HashMap;
import java.util.Map;

public class SkTransException extends RuntimeException {
    static final long serialVersionUID = 1L;
    private ErrorCode errorCode;

    public enum ErrorCode {
        CORR_Z(1),
        TO_FROM_INDEX_ERROR_5(5),
        TOO_LARGE_X(10),
        INSUFFICIENT_OR_OUTSIDE_EXTRA_POINTS(18),
        X_OR_Y_OUTSIDE_AREA(19),
        WRONG_POSITION(21),
        WRONG_AXIS(34),
        INPUT_ERROR_FROM_TO_SYS(35),
        INPUT_ERROR_36(36),
        INPUT_ERROR_38(38),
        WRONG_NGO_AXIS(41),
        WRONG_UTM_ZONE(42),
        X_OUTSIDE_NORWAY(43),
        TO_FROM_INDEX_ERROR_51(51),
        WRONG_ELLIPSIS_INDEX(52),
        WRONG_ISOMETRIC_WIDTH(53),
        BRQ_ERROR(54),
        GREAT_HALF_AXIS_ERROR(61),
        FLATTENING_ERROR(62),
        WIDTH_ERROR_63(63),
        LENGTH_ERROR_64(64),
        GEOIDE_READ_ERROR_70(70),
        GEOIDE_READ_ERROR_270(270),
        OUTSIDE_GEOIDE_COVERAGE_71(71),
        OUTSIDE_GEOIDE_COVERAGE_271(271),
        GEOIDE_INFO_ERROR_73(73),
        GEOIDE_INFO_ERROR_273(273),
        COMMON_POINT_FILE_OPEN_ERROR(75),
        WIDTH_ERROR_81(81),
        LENGTH_ERROR_82(82),
        OUTSIDE_AREA_90(90),
        X_COORD_ERROR(91),
        Y_COORD_ERROR(92),
        Z_COORD_ERROR(93),
        SYSTEM_NO_ERROR(95),
        GDEC_ERROR_UNINITIALIZED_GEOIDE(106),
        OUTSIDE_AREA_94(94),
        GEOTRANS_ERROR_UNINITIALIZED_GEOIDE(306);

        private final int value;

        ErrorCode(int value) {
            this.value = value;
        }

        public static ErrorCode fromInt(int code) {
            switch(code) {
                case 1:
                    return CORR_Z;
                case 5:
                    return TO_FROM_INDEX_ERROR_5;
                case 10:
                    return TOO_LARGE_X;
                case 18:
                    return INSUFFICIENT_OR_OUTSIDE_EXTRA_POINTS;
                case 19:
                    return X_OR_Y_OUTSIDE_AREA;
                case 21:
                    return WRONG_POSITION;
                case 34:
                    return WRONG_AXIS;
                case 35:
                    return INPUT_ERROR_FROM_TO_SYS;
                case 36:
                    return INPUT_ERROR_36;
                case 38:
                    return INPUT_ERROR_38;
                case 41:
                    return WRONG_NGO_AXIS;
                case 42:
                    return WRONG_UTM_ZONE;
                case 43:
                    return X_OUTSIDE_NORWAY;
                case 51:
                    return TO_FROM_INDEX_ERROR_51;
                case 52:
                    return WRONG_ELLIPSIS_INDEX;
                case 53:
                    return WRONG_ISOMETRIC_WIDTH;
                case 54:
                    return BRQ_ERROR;
                case 61:
                    return GREAT_HALF_AXIS_ERROR;
                case 62:
                    return FLATTENING_ERROR;
                case 63:
                    return WIDTH_ERROR_63;
                case 64:
                    return LENGTH_ERROR_64;
                case 70:
                    return GEOIDE_READ_ERROR_70;
                case 270:
                    return GEOIDE_READ_ERROR_270;
                case 71:
                    return OUTSIDE_GEOIDE_COVERAGE_71;
                case 271:
                    return OUTSIDE_GEOIDE_COVERAGE_271;
                case 73:
                    return GEOIDE_INFO_ERROR_73;
                case 273:
                    return GEOIDE_INFO_ERROR_273;
                case 75:
                    return COMMON_POINT_FILE_OPEN_ERROR;
                case 81:
                    return WIDTH_ERROR_81;
                case 82:
                    return LENGTH_ERROR_82;
                case 90:
                    return OUTSIDE_AREA_90;
                case 91:
                    return X_COORD_ERROR;
                case 92:
                    return Y_COORD_ERROR;
                case 93:
                    return Z_COORD_ERROR;
                case 95:
                    return SYSTEM_NO_ERROR;
                case 106:
                    return GDEC_ERROR_UNINITIALIZED_GEOIDE;
                case 94:
                    return OUTSIDE_AREA_94;
                case 306:
                    return GEOTRANS_ERROR_UNINITIALIZED_GEOIDE;
                default:
                    throw new RuntimeException(String.format(
                            "SkTransException.ErrorCode: Attempt to retrieve enum constant from invalid value: %d", code));
            }
        }
    }

    private final static Map<Integer, String> errorCodes;
    static {
        errorCodes = new HashMap<>();
        errorCodes.put(ErrorCode.CORR_Z.value, "KORR->Z");
        errorCodes.put(ErrorCode.TO_FROM_INDEX_ERROR_5.value, "Feil i til/fra indeks");
        errorCodes.put(ErrorCode.TOO_LARGE_X.value, "For stor X");
        errorCodes.put(ErrorCode.INSUFFICIENT_OR_OUTSIDE_EXTRA_POINTS.value, "For få, eller utenfor området til angitte ekstrapunkter");
        errorCodes.put(ErrorCode.X_OR_Y_OUTSIDE_AREA.value, "X eller Y-verdi utenfor område");
        errorCodes.put(ErrorCode.WRONG_POSITION.value, "Feil posisjon");
        errorCodes.put(ErrorCode.WRONG_AXIS.value, "Feil akse");
        errorCodes.put(ErrorCode.INPUT_ERROR_FROM_TO_SYS.value, "Feil i inndata / Feil Frasys eller Tilsys");
        errorCodes.put(ErrorCode.INPUT_ERROR_36.value, "Feil i inndata");
        errorCodes.put(ErrorCode.INPUT_ERROR_38.value, "Feil i inndata");
        errorCodes.put(ErrorCode.WRONG_NGO_AXIS.value, "Feil NGO-akse");
        errorCodes.put(ErrorCode.WRONG_UTM_ZONE.value, "Feil UTM-sone");
        errorCodes.put(ErrorCode.X_OUTSIDE_NORWAY.value, "X utenfor Norge");
        errorCodes.put(ErrorCode.TO_FROM_INDEX_ERROR_51.value, "Feil i til/fra indeks");
        errorCodes.put(ErrorCode.WRONG_ELLIPSIS_INDEX.value, "Feil ellipseindeks");
        errorCodes.put(ErrorCode.WRONG_ISOMETRIC_WIDTH.value, "Feil isometrisk bredde");
        errorCodes.put(ErrorCode.BRQ_ERROR.value, "Feil i BRQ, neg. isometrisk bredde");
        errorCodes.put(ErrorCode.GREAT_HALF_AXIS_ERROR.value, "Feil vedr. store halvakse");
        errorCodes.put(ErrorCode.FLATTENING_ERROR.value, "Feil vedr. flattrykking");
        errorCodes.put(ErrorCode.WIDTH_ERROR_63.value, "Feil bredde");
        errorCodes.put(ErrorCode.LENGTH_ERROR_64.value, "Feil lengde");
        errorCodes.put(ErrorCode.GEOIDE_READ_ERROR_70.value, "Feil under lesing av Geoide-informasjon");
        errorCodes.put(ErrorCode.GEOIDE_READ_ERROR_270.value, "Feil under lesing av Geoide-informasjon");
        errorCodes.put(ErrorCode.OUTSIDE_GEOIDE_COVERAGE_71.value, "Utenfor dekning av Geoidefilen");
        errorCodes.put(ErrorCode.OUTSIDE_GEOIDE_COVERAGE_271.value, "Utenfor dekning av Geoidefilen");
        errorCodes.put(ErrorCode.GEOIDE_INFO_ERROR_73.value, "Geoidefilen inneholder ikke riktig informasjon");
        errorCodes.put(ErrorCode.GEOIDE_INFO_ERROR_273.value, "Geoidefilen inneholder ikke riktig informasjon");
        errorCodes.put(ErrorCode.COMMON_POINT_FILE_OPEN_ERROR.value, "Feil under åpning av fellespunkt fil");
        errorCodes.put(ErrorCode.WIDTH_ERROR_81.value, "Feil bredde");
        errorCodes.put(ErrorCode.LENGTH_ERROR_82.value, "Feil lengde");
        errorCodes.put(ErrorCode.OUTSIDE_AREA_90.value, "Utenfor område");
        errorCodes.put(ErrorCode.X_COORD_ERROR.value, "Feil X-koordinat");
        errorCodes.put(ErrorCode.Y_COORD_ERROR.value, "Feil Y-koordinat");
        errorCodes.put(ErrorCode.Z_COORD_ERROR.value, "Feil Z-koordinat");
        errorCodes.put(ErrorCode.SYSTEM_NO_ERROR.value, "Feil i systemnummer (1-6)");
        errorCodes.put(ErrorCode.GDEC_ERROR_UNINITIALIZED_GEOIDE.value, "Fra rutinen gdec når geoidefilen ikke er initiert");
        errorCodes.put(ErrorCode.OUTSIDE_AREA_94.value, "Utenfor område");
        errorCodes.put(ErrorCode.GEOTRANS_ERROR_UNINITIALIZED_GEOIDE.value, "Fra rutinen GeoTrans når geoidefilen ikke er initiert");
    }

    public SkTransException(ErrorCode code) {
        super(String.format("Transformasjon feilet med kode %d: %s", code.value, errorCodes.get(code.value)));
        this.errorCode = code;
    }

    public static boolean isError(int code) {
        return errorCodes.containsKey(code);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public static String descriptionFromCode(int code) {
        if (errorCodes.containsKey(code)) {
            return errorCodes.get(code);
        }
        return null;
    }
}
