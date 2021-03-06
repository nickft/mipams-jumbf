package org.mipams.jumbf.privacy_security.services.boxes;

import java.io.OutputStream;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.mipams.jumbf.core.entities.ParseMetadata;
import org.mipams.jumbf.core.entities.ServiceMetadata;
import org.mipams.jumbf.core.services.boxes.BmffBoxService;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.privacy_security.entities.ProtectionDescriptionBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class ProtectionDescriptionBoxService extends BmffBoxService<ProtectionDescriptionBox> {

    private static final Logger logger = LoggerFactory.getLogger(ProtectionDescriptionBoxService.class);

    ServiceMetadata serviceMetadata;

    @PostConstruct
    void init() {
        ProtectionDescriptionBox box = initializeBox();
        serviceMetadata = new ServiceMetadata(box.getTypeId(), box.getType());
    }

    @Override
    protected ProtectionDescriptionBox initializeBox() {
        return new ProtectionDescriptionBox();
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    @Override
    protected void writeBmffPayloadToJumbfFile(ProtectionDescriptionBox protectionDescriptionBox,
            OutputStream outputStream)
            throws MipamsException {

        CoreUtils.writeIntAsSingleByteToOutputStream(protectionDescriptionBox.getMethodToggle(), outputStream);

        if (protectionDescriptionBox.isProtectionExternallyReferenced()) {
            final String encLabelWithEscapeCharacter = CoreUtils
                    .addEscapeCharacterToText(protectionDescriptionBox.getEncLabel());
            CoreUtils.writeTextToOutputStream(encLabelWithEscapeCharacter, outputStream);
        }

        if (protectionDescriptionBox.accessRulesExist()) {
            final String arLabelWithEscapeCharacter = CoreUtils
                    .addEscapeCharacterToText(protectionDescriptionBox.getArLabel());
            CoreUtils.writeTextToOutputStream(arLabelWithEscapeCharacter, outputStream);
        }

        if (protectionDescriptionBox.isAes256CbcWithIvProtection()) {
            CoreUtils.writeByteArrayToOutputStream(protectionDescriptionBox.getIv(), outputStream);
        }

    }

    @Override
    protected void populatePayloadFromJumbfFile(ProtectionDescriptionBox protectionDescriptionBox,
            ParseMetadata parseMetadata, InputStream input)
            throws MipamsException {

        logger.debug("Protection Description box");

        int methodToggle = CoreUtils.readSingleByteAsIntFromInputStream(input);
        protectionDescriptionBox.setMethodToggle(methodToggle);

        if (protectionDescriptionBox.isProtectionExternallyReferenced()) {
            String label = CoreUtils.readStringFromInputStream(input);
            protectionDescriptionBox.setEncLabel(label);
        }

        if (protectionDescriptionBox.accessRulesExist()) {
            String label = CoreUtils.readStringFromInputStream(input);
            protectionDescriptionBox.setArLabel(label);
        }

        if (protectionDescriptionBox.isAes256CbcWithIvProtection()) {
            int ivSize = 16;
            byte[] iv = CoreUtils.readBytesFromInputStream(input, ivSize);

            protectionDescriptionBox.setIv(iv);
        }

        logger.debug("Discovered box: " + protectionDescriptionBox.toString());
    }
}
