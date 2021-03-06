package org.mipams.jumbf.core.entities;

import org.mipams.jumbf.core.util.MipamsException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class PaddingBox extends BmffBox {

    public static int PADDING_VALUE = 0x00;

    private @Getter @Setter long paddingSize;

    @Override
    public int getTypeId() {
        return 0x66726565;
    }

    @Override
    public String getType() {
        return "free";
    }

    @Override
    protected long calculatePayloadSize() throws MipamsException {
        return getPaddingSize();
    }

}
