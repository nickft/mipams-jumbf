package org.mipams.jumbf.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mipams.jumbf.core.entities.ContiguousCodestreamBox;
import org.mipams.jumbf.core.entities.JsonBox;
import org.mipams.jumbf.core.entities.JumbfBox;
import org.mipams.jumbf.core.entities.JumbfBoxBuilder;
import org.mipams.jumbf.core.entities.XmlBox;
import org.mipams.jumbf.core.services.JpegCodestreamGenerator;
import org.mipams.jumbf.core.services.JpegCodestreamParser;
import org.mipams.jumbf.core.services.content_types.ContiguousCodestreamContentType;
import org.mipams.jumbf.core.services.content_types.JsonContentType;
import org.mipams.jumbf.core.services.content_types.XmlContentType;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.core.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class JpgCodestreamTests extends AbstractIntegrationTests {

    @Autowired
    JpegCodestreamGenerator jpegCodestreamGenerator;

    @Autowired
    JpegCodestreamParser jpegCodestreamParser;

    @Autowired
    Properties properties;

    @BeforeAll
    static void initRequest() throws IOException {
        generateFile();
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        fileCleanUp();
    }

    @Test
    void verifyJp2cParsing() throws MipamsException, FileNotFoundException {

        List<JumbfBox> jumbfBoxList = List.of(createJp2cJumbfFile(0));
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        String targetUrl = assetFileUrl + "-new";

        jpegCodestreamGenerator.generateJumbfMetadataToFile(jumbfBoxList, assetFileUrl, targetUrl);

        List<JumbfBox> resultList = jpegCodestreamParser.parseMetadataFromFile(targetUrl);

        assertEquals(1, resultList.size());
    }

    @Test
    void verifyParsing() throws MipamsException, FileNotFoundException {

        List<JumbfBox> jumbfBoxList = List.of(createJsonJumbfFile(10));
        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        String targetUrl1 = assetFileUrl + "-new1";
        String targetUrl2 = assetFileUrl + "-new2";

        jpegCodestreamGenerator.generateJumbfMetadataToFile(jumbfBoxList, assetFileUrl, targetUrl1);
        jpegCodestreamGenerator.generateJumbfMetadataToFile(jumbfBoxList, targetUrl1, targetUrl2);

        List<JumbfBox> resultList = jpegCodestreamParser.parseMetadataFromFile(targetUrl2);

        assertEquals(2, resultList.size());
    }

    @Test
    void generateLargeJumbfBoxToAsset() throws MipamsException, FileNotFoundException {
        List<JumbfBox> jumbfBoxList = List.of(createJsonJumbfFile(0xFFFF));

        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        String targetUrl1 = assetFileUrl + "-new1";
        String targetUrl2 = assetFileUrl + "-new2";

        jpegCodestreamGenerator.generateJumbfMetadataToFile(jumbfBoxList, assetFileUrl, targetUrl1);
        jpegCodestreamGenerator.generateJumbfMetadataToFile(jumbfBoxList, targetUrl1, targetUrl2);

        List<JumbfBox> resultList = jpegCodestreamParser.parseMetadataFromFile(targetUrl2);

        assertEquals(2, resultList.size());
    }

    @Test
    void testStripJumbfFromFile() throws MipamsException, FileNotFoundException {
        List<JumbfBox> jumbfBoxList = List.of(createJsonJumbfFile(0), createXmlJumbfFile(0));

        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        String targetUrl = assetFileUrl + "-new";

        jpegCodestreamGenerator.generateJumbfMetadataToFile(jumbfBoxList, assetFileUrl, targetUrl);

        String strippedAsset = targetUrl + ".tmp";
        jpegCodestreamGenerator.stripJumbfMetadataWithUuidEqualTo(targetUrl, strippedAsset,
                (new JsonContentType()).getContentTypeUuid());

        List<JumbfBox> resultList = jpegCodestreamParser.parseMetadataFromFile(strippedAsset);
        assertEquals(1, resultList.size());
    }

    @Test
    void testCorruptedJpegImage() throws MipamsException, FileNotFoundException {
        List<JumbfBox> jumbfBoxList = List.of(createJsonJumbfFile(0));

        String assetFileUrl = TEST_FILE_PATH;
        String targetUrl = assetFileUrl + "-new";

        assertThrows(MipamsException.class, () -> {
            jpegCodestreamGenerator.generateJumbfMetadataToFile(jumbfBoxList, assetFileUrl, targetUrl);
        });
    }

    private JumbfBox createXmlJumbfFile(int padding) throws MipamsException {

        XmlContentType xmlContentType = new XmlContentType();

        XmlBox xmlBox = new XmlBox();
        xmlBox.setContent(TEST_CONTENT.getBytes());

        JumbfBoxBuilder builder = new JumbfBoxBuilder(xmlContentType);
        builder.setPaddingSize(padding);
        builder.appendContentBox(xmlBox);

        JumbfBox givenJumbfBox = builder.getResult();
        return givenJumbfBox;
    }

    private JumbfBox createJsonJumbfFile(int padding) throws MipamsException {

        JsonContentType jsonContentType = new JsonContentType();

        JsonBox jsonBox = new JsonBox();
        jsonBox.setContent(TEST_CONTENT.getBytes());

        JumbfBoxBuilder builder = new JumbfBoxBuilder(jsonContentType);
        builder.setPaddingSize(padding);
        builder.appendContentBox(jsonBox);

        JumbfBox givenJumbfBox = builder.getResult();
        return givenJumbfBox;
    }

    private JumbfBox createJp2cJumbfFile(int padding) throws MipamsException, FileNotFoundException {

        String assetFileUrl = ResourceUtils.getFile("classpath:sample.jpeg").getAbsolutePath();

        ContiguousCodestreamBox jp2c = new ContiguousCodestreamBox();
        jp2c.setFileUrl(assetFileUrl);

        JumbfBoxBuilder builder = new JumbfBoxBuilder(new ContiguousCodestreamContentType());
        builder.setPaddingSize(padding);
        builder.appendContentBox(jp2c);

        JumbfBox givenJumbfBox = builder.getResult();
        return givenJumbfBox;
    }
}
