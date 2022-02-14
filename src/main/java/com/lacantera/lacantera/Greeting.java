package com.lacantera.lacantera;

import lombok.Getter;
import lombok.Setter;
import org.mongojack.Id;

@Setter @Getter
public class Greeting {

    @Id
    private String id;
    private String content;
}
