package org.mipams.jumbf.privacy_security.entities.replacement;

import java.io.InputStream;
import java.io.OutputStream;

import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class RoiParamHandler implements ParamHandlerInterface {

    private @Getter @Setter int offsetX;
    private @Getter @Setter int offsetY;

    @Override
    public void writeParamToBytes(OutputStream outputStream) throws MipamsException {
        CoreUtils.writeIntToOutputStream(getOffsetX(), outputStream);
        CoreUtils.writeIntToOutputStream(getOffsetY(), outputStream);
    }

    @Override
    public void populateParamFromBytes(InputStream inputStream) throws MipamsException {

        int offsetX = CoreUtils.readIntFromInputStream(inputStream);
        int offsetY = CoreUtils.readIntFromInputStream(inputStream);

        setOffsetX(offsetX);
        setOffsetY(offsetY);
    }

    @Override
    public long getParamSize() throws MipamsException {
        return 2 * CoreUtils.INT_BYTE_SIZE;
    }

}
