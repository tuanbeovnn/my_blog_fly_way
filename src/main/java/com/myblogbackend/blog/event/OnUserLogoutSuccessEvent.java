package com.myblogbackend.blog.event;

import java.io.Serial;
import java.time.Instant;
import java.util.Date;

import com.myblogbackend.blog.request.LogOutRequest;
import org.springframework.context.ApplicationEvent;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnUserLogoutSuccessEvent extends ApplicationEvent {

	@Serial
    private static final long serialVersionUID = 1L;
	private final String userEmail;
    private final String token;
    private final transient LogOutRequest logOutRequest;
    private final Date eventTime;
    
    public OnUserLogoutSuccessEvent(final String userEmail, final String token, final LogOutRequest logOutRequest) {
        super(userEmail);
        this.userEmail = userEmail;
        this.token = token;
        this.logOutRequest = logOutRequest;
        this.eventTime = Date.from(Instant.now());
    }
}