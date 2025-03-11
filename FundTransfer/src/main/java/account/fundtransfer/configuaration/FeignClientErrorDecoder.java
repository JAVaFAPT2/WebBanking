package account.fundtransfer.configuaration;


import account.fundtransfer.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        GlobalException globalException = extractGlobalException(response);

        log.info("Response status: {}", response.status());
        if (response.status() == 400) {
            log.error("Error in request went through feign client: {} - {}", globalException.getErrorMessage(), globalException.getErrorCode());
            return globalException;
        }
        log.error("General exception went through feign client");
        return new Exception("General exception occurred");
    }

    private GlobalException extractGlobalException(Response response) {
        GlobalException globalException = null;
        Reader reader = null;

        try {
            reader = new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8);
            String result = IOUtils.readInputStreamToString(response.body().asInputStream(), StandardCharsets.UTF_8);
            log.error("Exception message: {}", result);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            globalException = mapper.readValue(result, GlobalException.class);
            log.error("Parsed exception: {}", globalException);
        } catch (IOException e) {
            log.error("IO Exception while reading exception message", e);
        } finally {
            if (!Objects.isNull(reader)) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("IO Exception while closing reader", e);
                }
            }
        }
        return globalException;
    }
}