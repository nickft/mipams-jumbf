package org.mipams.jumbf.core.services.boxes;

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;

import org.mipams.jumbf.core.entities.EmbeddedFileDescriptionBox;
import org.mipams.jumbf.core.entities.ParseMetadata;
import org.mipams.jumbf.core.entities.ServiceMetadata;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmbeddedFileDescriptionBoxService extends BmffBoxService<EmbeddedFileDescriptionBox> {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedFileDescriptionBoxService.class);

    ServiceMetadata serviceMetadata;

    @PostConstruct
    void init() {
        EmbeddedFileDescriptionBox box = initializeBox();
        serviceMetadata = new ServiceMetadata(box.getTypeId(), box.getType());
    }

    @Override
    protected EmbeddedFileDescriptionBox initializeBox() {
        return new EmbeddedFileDescriptionBox();
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    @Override
    protected void writeBmffPayloadToJumbfFile(EmbeddedFileDescriptionBox embeddedFileDescriptionBox,
            OutputStream outputStream) throws MipamsException {

        CoreUtils.writeIntAsSingleByteToOutputStream(embeddedFileDescriptionBox.getToggle(), outputStream);

        final String mediaTypeAsString = embeddedFileDescriptionBox.getMediaType().toString();

        CoreUtils.writeTextToOutputStream(CoreUtils.addEscapeCharacterToText(mediaTypeAsString), outputStream);

        if (embeddedFileDescriptionBox.fileNameExists()) {
            final String fileName = embeddedFileDescriptionBox.getFileName();
            CoreUtils.writeTextToOutputStream(CoreUtils.addEscapeCharacterToText(fileName), outputStream);
        }

    }

    @Override
    protected void populatePayloadFromJumbfFile(EmbeddedFileDescriptionBox embeddedFileDescriptionBox,
            ParseMetadata parseMetadata, InputStream input) throws MipamsException {
        logger.debug("Embedded File Description box");

        int toggleValue = CoreUtils.readSingleByteAsIntFromInputStream(input);
        embeddedFileDescriptionBox.setToggle(toggleValue);

        String mediaTypeAsString = CoreUtils.readStringFromInputStream(input);

        CoreUtils.addEscapeCharacterToText(mediaTypeAsString).length();

        embeddedFileDescriptionBox.setMediaTypeFromString(mediaTypeAsString);

        String fileName = embeddedFileDescriptionBox.fileNameExists() ? CoreUtils.readStringFromInputStream(input)
                : embeddedFileDescriptionBox.getRandomFileName();

        CoreUtils.addEscapeCharacterToText(fileName).length();

        embeddedFileDescriptionBox.setFileName(fileName);

        logger.debug("Discovered box: " + embeddedFileDescriptionBox.toString());
    }
}