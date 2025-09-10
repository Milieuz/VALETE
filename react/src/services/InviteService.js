import axios from 'axios';

function auth(){
    const token = localStorage.getItem("token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

function normalizeInviteRow(inv){
    return {
        id: inv.id ?? inv.inviteId ?? inv.invite_id ?? null,
        expiresAt: inv.expiresAt ?? inv.expirationTime ?? inv.expiration_time ?? null,
        createdAt: inv.createdAt ?? inv.creationTime ?? inv. creation_time ?? null,
        useLimit: inv.useLImit ?? inv.use_limit ?? 1,
        useCount: inv.useCount ?? inv.use_count ?? 0,
        isRevoked: (inv.isRevoked ?? inv.revoked ?? inv.is_revoked ?? false ) === true,
        valetCode: inv.valetCode ?? inv.valet_code ?? null,
    };
}

function normalizeNewlyCreatedInvite(inv){
    return normalizeInviteRow({
        id: inv.id,
        valetCode: inv.valetCode,
        expirationTime: inv.expirationTime,
        useLimit: inv.useLimit,
        creation_time: new Date().toISOString(),
    });
}

export async function listInvites({ active = true } = {}) {
    const res = await axios.get("/admin/valet-invites", {
        params: { active },
        headers: { ...auth() },
    });
    const data = Array.isArray(res.data) ? res.data : [];
    return data.map(normalizeInviteRow);
}

export async function createInvite({ requestValidPeriod, useLimit }) {
    const payload = { requestValidPeriod, useLimit };
    const res = await axios.post("/admin/valet-invites", payload, {
        headers: { ...auth() },
    });
    return normalizeNewlyCreatedInvite(res.data || {});
}

export async function revokeInvite(id) {
    await axios.post(`/admin/valet-invites/${id}/revoke`, null, {
        headers: { ...auth() },
    });
}

export async function deleteInvite(id){
    await axios.delete(`/admin/valet-invites/${id}`, {
        headers: { ...auth() },
    })
}

export function isExpired(expiresAt) {
    return expiresAt ? new Date(expiresAt) <= new Date() : false;
}