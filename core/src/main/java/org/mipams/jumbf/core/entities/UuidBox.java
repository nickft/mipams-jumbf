package org.mipams.jumbf.core.entities;

import java.util.List;
import java.util.UUID;

import org.mipams.jumbf.core.util.BoxTypeEnum;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class UuidBox extends BmffBox implements ContentBox {

    private @Getter @Setter UUID uuid;

    @EqualsAndHashCode.Exclude
    private @Getter @Setter String fileUrl;

    @Override
    public long calculatePayloadSize() throws MipamsException {

        long sum = CoreUtils.UUID_BYTE_SIZE;
        sum += CoreUtils.getFileSizeFromPath(getFileUrl());

        return sum;
    }

    @Override
    public int getTypeId() {
        return BoxTypeEnum.UuidBox.getTypeId();
    }

    @Override
    public List<BmffBox> getBmffBoxes() {
        return List.of(this);
    }

    @Override
    public UUID getContentTypeUUID() {
        return BoxTypeEnum.UuidBox.getContentUuid();
    }

}
