package vn.tayjava.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class SampleDto implements Serializable {
    private Integer id;
    private String name;

}
