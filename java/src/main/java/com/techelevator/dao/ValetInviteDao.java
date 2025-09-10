package com.techelevator.dao;

import com.techelevator.model.valet.ValetInvite;
import java.time.Instant;
import java.util.List;

public interface ValetInviteDao {
    // create a new invite and retrieve its id upon generation.
    int makeNewInvite(String valetSecretHash, Instant expirationTime, int useLimit,  int creatorId);
    // Retrieve an invite that is NOT: revoked, expired, or met use limit
    ValetInvite getActiveInviteById(int id);
    // Increment use count of an invite if it is still usable.
    boolean markUsed(int id);
    // Revoking an invite
    void revokeInvite(int id);
    // Delete invite
    int deleteInvite(int id);
    // Delete old invites
    int flushExpiredInvite();
    // Find all invites
    List<ValetInvite> findAllInvites(boolean isActive);
}
