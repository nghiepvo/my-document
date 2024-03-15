package nv.streaming.pgdebeziumkafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.asm.Advice;
import nv.streaming.pgdebeziumkafka.entity.DownTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


@Service
@Slf4j
public class ManagementDownTimeService {

    private final ClassPathResource classPathResource;
    public ManagementDownTimeService() {
        classPathResource = new ClassPathResource("test.json");
    }

    public Timestamp getDownTime() throws IOException {
        try {
            if (generateFile()) {
                ObjectMapper mapper = new ObjectMapper();
                File file = classPathResource.getFile();

                DownTime downTime = mapper.readValue(file, DownTime.class);

                return downTime != null ? downTime.getDownTime() : null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }
    public void writeDownTime() throws IOException {
        try {
            if (generateFile()) {
                ObjectMapper mapper = new ObjectMapper();
                File file = classPathResource.getFile();
                DownTime downTime = new DownTime();

                downTime.setDownTime(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()));

                mapper.writeValue(file, downTime );
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean generateFile() throws IOException {
        File file = classPathResource.getFile();
        if (file.exists())  {
            return true;
        }
        return file.createNewFile();
    }
}
