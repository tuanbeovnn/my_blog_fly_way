package com.myblogbackend.blog.config.security;


import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.config.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {

        UserEntity userEntity = usersRepository.findByEmail(username)
                .orElseThrow(() ->
                        new BlogRuntimeException(ErrorCode.USER_COULD_NOT_FOUND)
                );

        return UserPrincipal.build(userEntity);
    }
}
