package org.mipams.jumbf.core.services;

import java.util.List;

import org.mipams.jumbf.core.entities.JumbfBox;
import org.mipams.jumbf.core.util.MipamsException;

public interface GeneratorInterface {
    public String generateJumbfMetadataToFile(List<JumbfBox> jumbfBoxList, String assetUrl) throws MipamsException;
}