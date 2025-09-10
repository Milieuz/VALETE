package com.techelevator.service;

import com.techelevator.dao.ValetInviteDao;
import com.techelevator.exception.DaoException;
import com.techelevator.model.valet.NewlyCreatedInvite;
import com.techelevator.model.valet.ValetInvite;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class ValetInviteService {
    private final ValetInviteDao valetInviteDao;
    private final SecureRandom secRand = new SecureRandom();
    private final PasswordEncoder pwEncoder = new BCryptPasswordEncoder();

    public ValetInviteService(ValetInviteDao valetInvitedao){
        this.valetInviteDao = valetInvitedao;
    }

    public NewlyCreatedInvite makeInvite(Duration inviteDuration, int useLimit, int creator){
        String token = generateToken();
        String hash = pwEncoder.encode(token);
        Instant expirationTime = Instant.now().plus(inviteDuration);
        int id = valetInviteDao.makeNewInvite(hash, expirationTime, useLimit, creator);
        String inviteCode = id + ":" + token;
        return new NewlyCreatedInvite(id, inviteCode, expirationTime, useLimit);
    }

    public void useInviteCode(String inviteCode){
        String[] codePartition = inviteCode.split(":",2);

        if(codePartition.length != 2){
            throw new IllegalArgumentException("Invalid code");
        }
        int id = Integer.parseInt(codePartition[0]);
        String code = codePartition[1];

        ValetInvite activeInvite = valetInviteDao.getActiveInviteById(id);

        if(activeInvite == null){
            throw new DaoException("Invite Not Found.");
        }
        if(!pwEncoder.matches(code, activeInvite.getValetSecretHash())){
            throw new DaoException("Invite Code invalid");
        }
        boolean isCodeUsed = valetInviteDao.markUsed(id);
        if(!isCodeUsed){
            throw new DaoException("Invite already used or expired");
        }
    }

    public List<ValetInvite> listInvites(boolean isActive){
        return valetInviteDao.findAllInvites(isActive);
    }

    public void revoke(int id){
        valetInviteDao.revokeInvite(id);
    }
    public int delete(int id){
        return valetInviteDao.deleteInvite(id);
    }
    public void purge(){
        valetInviteDao.flushExpiredInvite();
    }

    private String generateToken() {
        byte[] bytes = new byte[12];
        secRand.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

    }
}
