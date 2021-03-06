package org.mipams.jumbf.privacy_security.entities.replacement;

import org.mipams.jumbf.core.util.MipamsException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum ReplacementType {

    BOX("box", 0),
    APP("app", 1),
    ROI("roi", 2),
    FILE("file", 3);

    private @Getter @Setter String type;
    private @Getter @Setter int id;

    public static ReplacementType getTypeFromString(String type) throws MipamsException {
        for (ReplacementType val : values()) {
            if (val.getType().equals(type)) {
                return val;
            }
        }
        throw new MipamsException(getErrorMessage());
    }

    public static ReplacementType getTypeFromId(int id) throws MipamsException {
        for (ReplacementType val : values()) {
            if (val.getId() == id) {
                return val;
            }
        }
        throw new MipamsException(getErrorMessage());
    }

    static String getErrorMessage() {
        return String.format("Method is not supported. Supported methods are: %s, %s, %s, %s",
                BOX.getType(), APP.getType(), ROI.getType(), FILE.getType());
    }
}