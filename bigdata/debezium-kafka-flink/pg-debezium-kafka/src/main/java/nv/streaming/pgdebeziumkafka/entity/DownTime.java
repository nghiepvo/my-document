package nv.streaming.pgdebeziumkafka.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class DownTime {
    private Timestamp downTime;
}
