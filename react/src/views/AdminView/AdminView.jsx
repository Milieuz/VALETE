import { useState, useEffect } from 'react';
import { listInvites, createInvite, revokeInvite, deleteInvite, isExpired } from '../../services/InviteService';
import styles from "./AdminView.module.css";

/* Some Notes on API endpoints (appended to base URL imported from .env)
 * GET invites ==>          /admin/valet-invites    Filtered by active/inactive requests 
 * POST new invite ==>      /admin/valet-invites    RequestBody of InviteRequest, Return Body of NewlyCreatedInvite
 * POST revoke invite ==>   /admin/valet-invites/{id}/revoke
 * DELETE invites ==>       /admin/valet=invites/{id} 
 */

export default function AdminView() {

    const [invites, setInvites] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const [requestValidPeriod, setRequestValidPeriod] = useState(60 * 24 * 7);
    const [useLimit, setUseLimit] = useState(1);

    async function loadInvites(active = true) {
        setIsLoading(true);
        setError("");
        try {
            const inviteRows = await listInvites({ active });
            setInvites(inviteRows);
        } catch (error) {
            setError(error?.response?.data?.message || error.message || "Failed to load invites");
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => { loadInvites(true); }, []);

    async function onCreate(event) {
        event.preventDefault();

        setError('');
        try {
            const createdInvite = await createInvite({
                requestValidPeriod: Number(requestValidPeriod),
                useLimit: Number(useLimit),
            });
            setInvites((previous) => [createdInvite, ...previous]);
        } catch (error) {
            setError(error?.response?.data?.message || error.message || " Failed to create a new invite");
        }
    }

    async function onRevoke(id) {
        setError('');
        try {
            await revokeInvite(id);
            setInvites((previous) => previous.map((response) => (response.id === id ? { ...response, isRevoked: true } : response)));
        } catch (error) {
            setError(error?.response?.data?.message || error.message || "Failed to revoke invite");
        }
    }

    async function onDelete(id) {
        setError('');
        try {
            await deleteInvite(id);
            setInvites((previous) => previous.filter((response) => response.id !== id));
        } catch (error) {
            setError(error?.response?.data?.message || error.message || " Failed to delete the invite");
        }
    }

    const status = (response) => {
        if (response.isRevoked) {
            return "Revoked";
        }
        if (isExpired(response.expiresAt)) {
            return "Expired";
        }
        if (response.useCount >= response.useLimit) {
            return "Used";
        }
        return "Active";
    };

    return (
        <div className={styles.contentWrapper}>
            <div className={styles.container}>
                <h1 className={styles.title}>Admin Valet Invite Management</h1>
                <div className={styles.card}>
                    <form onSubmit={onCreate} className={styles.row}>
                        <label className={styles.label}>Expires In/Valid Period (minutes): </label>
                        <input
                            className={styles.input}
                            type="number"
                            min={1}
                            max={60 * 24 * 7}
                            value={requestValidPeriod}
                            onChange={(event) => setRequestValidPeriod(event.target.value)}
                            required
                        />
                        <label className={styles.label}>Use Limit</label>
                        <input
                            className={styles.input}
                            type="number"
                            min={1}
                            max={50}
                            value={useLimit}
                            onChange={(event) => setUseLimit(event.target.value)}
                            required
                        />
                        <button className={styles.button} type="submit">Create an Invite Code</button>
                    </form>
                    <p className={styles.tip}>** Codes displayed only once on creation (valetCode). Copy&Save Or Share ASAP</p>
                </div>

                {error && <div className={styles.error}> {error} </div>}

                <div className={styles.card}>
                    {isLoading ? (
                        <div className={styles.empty}>...Loading...</div>
                    ) : invites.length === 0 ? (
                        <div className={styles.empty}>No invites to load.</div>
                    ) : (
                        <table className={styles.table}>
                            <thead>
                                <tr>
                                    <th>Invite Code</th>
                                    <th>Invite Status</th>
                                    <th>Use</th>
                                    <th>Issued On</th>
                                    <th>Expires On</th>
                                    <th className={styles.right}>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {invites.map((inv) => {
                                    const invStatus = status(inv);
                                    const isRevocable = invStatus === 'Active';
                                    return (
                                        <tr key={inv.id}>
                                            <td className={styles.mono}>{inv.id}</td>
                                            <td>
                                                <span className={`${styles.tag} ${status === 'Active' ?
                                                    styles.tagActive
                                                    : status === 'Used' ?
                                                        styles.tagUsed
                                                        : status === 'Revoked' ?
                                                            styles.tagRevoked
                                                            : styles.tagExpired
                                                    }`}>
                                                    {invStatus}
                                                </span>
                                            </td>
                                            <td>{inv.useCount}/{inv.useLimit}</td>
                                            <td>{inv.createdAt ? new Date(inv.createdAt).toLocaleString() : "N/A"}</td>
                                            <td>{inv.expiresAt ? new Date(inv.expiresAt).toLocaleString() : "N/A"}</td>
                                            <td>
                                                {inv.valetCode ? (
                                                    <button className={styles.copy}
                                                        onClick={() => navigator.clipboard.writeText(inv.valetCode)}
                                                    >Copy Code
                                                    </button>
                                                ) : (
                                                    <span className={styles.suppress}>(N/A)</span>
                                                )}
                                            </td>
                                            <td className={styles.right}>
                                                <div className={styles.actionOptionsRow}>
                                                    <button className={styles.revoker} onClick={() => onRevoke(inv.id)}>Revoke</button>
                                                    <button className={styles.deleter} onClick={() => onDelete(inv.id)}>Delete</button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
}