package com.myblogbackend.blog.request;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailRequest implements Serializable {
    private String from;
    private String to;
    private String subject;
    private String content;
    private Map<String, Object> templateVariables;
}
