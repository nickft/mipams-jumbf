package org.mipams.jumbf.privacy_security.entities.replacement;

import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.mipams.jumbf.core.util.MipamsException;

import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class FileParamHandler implements ParamHandlerInterface {

    @Override
    public void populateParamFromRequest(ObjectNode input) throws MipamsException {
    }

    @Override
    public void writeParamToBytes(OutputStream outputStream) throws MipamsException {
    }

    @Override
    public void populateParamFromBytes(InputStream inputStream) throws MipamsException {
    }

    @Override
    public long getParamSize() throws MipamsException {
        return 0;
    }

}