package com.techelevator.controller;

import com.techelevator.model.auth.LoginDto;
import com.techelevator.model.auth.LoginResponseDto;
import com.techelevator.model.auth.RegisterUserDto;
import com.techelevator.model.auth.User;
import com.techelevator.service.ValetInviteService;
import jakarta.validation.Valid;

import com.techelevator.exception.DaoException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.techelevator.dao.UserDao;
import com.techelevator.security.jwt.JWTFilter;
import com.techelevator.security.jwt.TokenProvider;

import java.util.List;

@RestController
@CrossOrigin
public class AuthenticationController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final ValetInviteService valetInviteService;
    private UserDao userDao;
    
    //@Value("${jwt.secret}")
    //private String valetSecretCode;

    public AuthenticationController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserDao userDao, ValetInviteService valetInviteService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
        this.valetInviteService = valetInviteService;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);

        User user;
        try {
            user = userDao.getUserByUsername(loginDto.getUsername());
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect.");
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new LoginResponseDto(jwt, user), httpHeaders, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public void register(@Valid @RequestBody RegisterUserDto newUser) {

        // Extract the role name text and assess it
        // If role String is not available, throw an exception.
        String roleName = newUser.getRole();
        if (roleName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role required");
        }
        roleName = roleName.trim();
        // Extract the actual role name following ROLE_
        String roleNameActual = roleName.toUpperCase().startsWith("ROLE_") ?
                roleName.substring("ROLE_".length())  : roleName;
        roleNameActual = roleNameActual.toUpperCase();

        // Only 3 roles: PATRON, VALET, ADMIN allowed
        if(!List.of("PATRON", "VALET", "ADMIN").contains(roleNameActual)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is invalid.");
        }

        /* Here, depending on any insight we get on how administrator is registered, we can add
           a check to prohibit a user from self-registering as an admin and from gaining arbitrary
           superuser authority*/

        // Once role information passes, and is now valid, valet role selection will undergo this check.
        if(roleNameActual.equals("VALET")){
            /*String userAttemptedSecret = newUser.getSecretCode();
            if(userAttemptedSecret == null || userAttemptedSecret.isBlank()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secret code required for registering as a valet.");
            }

            try {
                valetInviteService.useInviteCode(userAttemptedSecret);
            } catch (IllegalArgumentException iae){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, iae.getMessage());
            }*/
        }

        try {
            if (userDao.getUserByUsername(newUser.getUsername()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists.");
            } else {
                userDao.createUser(newUser);
            }
        }
        catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User registration failed.");
        }
    }

}
