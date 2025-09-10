package com.techelevator.controller;

import com.techelevator.dao.UserDao;
import com.techelevator.model.valet.InviteRequest;
import com.techelevator.model.valet.NewlyCreatedInvite;
import com.techelevator.model.auth.User;
import com.techelevator.model.valet.ValetInvite;
import com.techelevator.service.ValetInviteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/admin/valet-invites")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin
public class AdminController {
    private final ValetInviteService valetInviteService;
    private final UserDao userDao;

    public AdminController(ValetInviteService valetInviteService, UserDao userDao){
        this.valetInviteService = valetInviteService;
        this.userDao = userDao;
    }

    @RequestMapping(path="", method = RequestMethod.POST)
    public NewlyCreatedInvite create(@RequestBody InviteRequest inviteRequest, Principal principal){
        int requestValidPeriod = (inviteRequest.getRequestValidPeriod() == null) ? 60 : Math.max(1, Math.min(inviteRequest.getRequestValidPeriod(), 60*24*7));
        int useLimit = (inviteRequest.getUseLimit() == null) ? 1 : Math.max(1, Math.min(inviteRequest.getUseLimit(), 50));

        User admin = userDao.getUserByUsername(principal.getName());
        return valetInviteService.makeInvite(Duration.ofMinutes(requestValidPeriod), useLimit, admin.getId());
    }

    @RequestMapping(path="", method = RequestMethod.GET)
    public List<ValetInvite> listInvites(@RequestParam(name="active", defaultValue="true") boolean isActive){
        return valetInviteService.listInvites(isActive);
    }

    @RequestMapping(path="/{id}/revoke", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable int id){
        valetInviteService.revoke(id);
    }

    @RequestMapping(path="/{id}", method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public int delete(@PathVariable int id){
        return valetInviteService.delete(id);
    }

}
