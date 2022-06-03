package org.mipams.jumbf.core.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mipams.jumbf.core.entities.BoxSegment;
import org.mipams.jumbf.core.entities.JumbfBox;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.core.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JpegCodestreamParser implements ParserInterface {

    private static final Logger logger = LoggerFactory.getLogger(JpegCodestreamParser.class);

    @Autowired
    Properties properties;

    @Autowired
    CoreParserService coreParserService;

    @Override
    public List<JumbfBox> parseMetadataFromFile(String assetUrl) throws MipamsException {

        Map<String, List<BoxSegment>> boxSegmentMap = parseBoxSegmentMapFromFile(assetUrl);

        return mergeBoxSegmentsAndParseJumbfBoxes(boxSegmentMap);
    }

    public Map<String, List<BoxSegment>> parseBoxSegmentMapFromFile(String assetUrl) throws MipamsException {

        Map<String, List<BoxSegment>> boxSegmentMap = new HashMap<>();

        String appMarkerAsHex;

        try (InputStream is = new FileInputStream(assetUrl)) {

            appMarkerAsHex = CoreUtils.readTwoByteWordAsHex(is);
            if (!CoreUtils.isStartOfImageAppMarker(appMarkerAsHex)) {
                throw new MipamsException("Start of image (SOI) marker is missing.");
            }

            while (is.available() > 0) {

                appMarkerAsHex = CoreUtils.readTwoByteWordAsHex(is);

                logger.debug(appMarkerAsHex);

                if (CoreUtils.isEndOfImageAppMarker(appMarkerAsHex)) {
                    break;
                } else if (CoreUtils.isStartOfScanMarker(appMarkerAsHex)) {
                    skipStartOfScanMarker(is);
                    continue;
                } else if (CoreUtils.isApp11Marker(appMarkerAsHex)) {
                    parseJumbfSegmentInApp11Marker(is, boxSegmentMap);
                } else {
                    int markerSegmentSize = CoreUtils.readTwoByteWordAsInt(is);
                    is.skip(markerSegmentSize - 2);
                }
            }

            return boxSegmentMap;
        } catch (IOException e) {
            throw new MipamsException(e);
        }
    }

    private void skipStartOfScanMarker(InputStream is) throws IOException, MipamsException {

        int currentByte, previousByte = 0;

        while (is.available() > 0) {

            currentByte = CoreUtils.readSingleByteAsIntFromInputStream(is);

            if (previousByte == 0xFF && currentByte != 0xFF && !(currentByte >= 0xD0 && currentByte <= 0xD7)) {
                break;
            }

            previousByte = currentByte;
        }
    }

    private void parseJumbfSegmentInApp11Marker(InputStream is, Map<String, List<BoxSegment>> boxSegmentMap)
            throws MipamsException, IOException {

        int markerSegmentSize = CoreUtils.readTwoByteWordAsInt(is);

        byte[] commonIdentifierAsByteArray = CoreUtils.readBytesFromInputStream(is, 2);
        String commonIdentifier = Integer.toHexString(CoreUtils.readTwoByteWordAsInt(commonIdentifierAsByteArray));

        if (!commonIdentifier.equalsIgnoreCase("4A50")) {
            is.skip(markerSegmentSize - 2 - 2);
            return;
        }

        int boxInstanceNumber = CoreUtils.readTwoByteWordAsInt(is);

        int packetSequenceNumber = CoreUtils.readIntFromInputStream(is);

        int boxLength = CoreUtils.readIntFromInputStream(is);

        int boxType = CoreUtils.readIntFromInputStream(is);

        Long boxLengthExtension = null;

        if (boxLength == 1) {
            boxLengthExtension = CoreUtils.readLongFromInputStream(is);
        }

        String boxSegmentId = String.format("%d-%d", boxType, boxInstanceNumber);

        String randomFileName = CoreUtils.randomStringGenerator();
        String payloadFileUrl = CoreUtils.getFullPath(properties.getFileDirectory(), randomFileName);

        long payloadSize = markerSegmentSize - (2 + 2 + 2 + 4 + 4 + 4) - ((boxLengthExtension == null) ? 0 : 8);

        CoreUtils.writeBytesFromInputStreamToFile(is, payloadSize, payloadFileUrl);

        BoxSegment boxSegment = new BoxSegment(markerSegmentSize, boxInstanceNumber, packetSequenceNumber, boxLength,
                boxType, boxLengthExtension, payloadFileUrl);

        addBoxSegmentToMap(boxSegmentMap, boxSegmentId, boxSegment);
    }

    private void addBoxSegmentToMap(Map<String, List<BoxSegment>> boxSegmentMap, String boxSegmentId,
            BoxSegment boxSegment) {

        List<BoxSegment> boxSegmentList = boxSegmentMap.get(boxSegmentId);

        if (boxSegmentList == null) {
            boxSegmentList = new ArrayList<>();
        }

        boxSegmentList.add(boxSegment);

        boxSegmentMap.put(boxSegmentId, boxSegmentList);
    }

    private List<JumbfBox> mergeBoxSegmentsAndParseJumbfBoxes(Map<String, List<BoxSegment>> boxSegmentMap)
            throws MipamsException {

        List<JumbfBox> result = new ArrayList<>();

        for (String boxSegmentId : boxSegmentMap.keySet()) {

            String jumbfFileUrl = CoreUtils.getFullPath(properties.getFileDirectory(), boxSegmentId + ".jumbf");

            List<BoxSegment> boxSegmentList = boxSegmentMap.get(boxSegmentId);

            printBoxSegmentList(boxSegmentId, boxSegmentList);

            Collections.sort(boxSegmentList);

            printBoxSegmentList(boxSegmentId, boxSegmentList);

            try (OutputStream os = new FileOutputStream(jumbfFileUrl)) {

                BoxSegment bs = boxSegmentList.get(0);

                byte[] bmffHeader = CoreUtils.getBmffHeaderBuffer(bs.getLBox(), bs.getTBox(), bs.getXlBox());

                CoreUtils.writeByteArrayToOutputStream(bmffHeader, os);

                for (BoxSegment boxSegment : boxSegmentList) {
                    CoreUtils.writeFileContentToOutput(boxSegment.getPayloadUrl(), os);
                }

                List<JumbfBox> boxList = coreParserService.parseMetadataFromFile(jumbfFileUrl);
                result.addAll(boxList);

                deleteBoxSegmentFiles(boxSegmentList);
            } catch (IOException e) {
                throw new MipamsException(e);
            }
        }

        return result;
    }

    private void deleteBoxSegmentFiles(List<BoxSegment> boxSegmentList) {
        boxSegmentList.forEach(bs -> CoreUtils.deleteFile(bs.getPayloadUrl()));
    }

    private void printBoxSegmentList(String type, List<BoxSegment> boxSegmentList) {

        for (BoxSegment bs : boxSegmentList) {
            logger.debug(String.format("%s - part: %d", type, bs.getPacketSequenceNumber()));
        }
    }

}