import { useEffect, useRef, useState } from "react";
import axios from "axios";
import styles from "./PatronPickupView.module.css";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:9000",
});
api.interceptors.request.use((config) => {
  // use the same pattern as the console: include token if present, but not required
  const t = localStorage.getItem("token");
  if (t) config.headers.Authorization = `Bearer ${t}`;
  return config;
});

function dollars(cents) {
  if (cents == null) return "0.00";
  return (cents / 100).toFixed(2);
}

function parseTicket(input) {
  if (!input) return null;
  // Accept formats like "VL-123", "123", " vl 123 ", etc.
  const digits = String(input).replace(/[^0-9]/g, "");
  if (!digits) return null;
  return parseInt(digits, 10);
}

export default function PatronPickupView() {
  const [ticketInput, setTicketInput] = useState("");
  const [ticketId, setTicketId] = useState(null);
  const [estimate, setEstimate] = useState(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const intervalRef = useRef(null);

  const refreshEstimate = async (id) => {
    if (!id) return;
    try {
      const { data } = await api.get(`/patron/estimate/${id}`);
      setEstimate(data);
      setErr("");
    } catch (e) {
      setEstimate(null);
      setErr(e?.response?.data?.message || "No active session for this ticket.");
    }
  };

  const onRequestPickup = async () => {
    const id = parseTicket(ticketInput);
    if (!id) {
      setErr("Please enter a valid ticket number (e.g., VL-123 or 123).");
      return;
    }
    setLoading(true);
    try {
      await api.post(`/patron/pickup-request/${id}`);
      setTicketId(id);
      await refreshEstimate(id);
      setErr("");
    } catch (e) {
      setErr(e?.response?.data?.message || "Failed to request pickup.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!ticketId) return;
    // refresh every 30s
    intervalRef.current && clearInterval(intervalRef.current);
    intervalRef.current = setInterval(() => refreshEstimate(ticketId), 30000);
    return () => intervalRef.current && clearInterval(intervalRef.current);
  }, [ticketId]);

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Request My Car</h1>

      <div className={styles.card}>
        <label htmlFor="ticket" className={styles.label}>Valet Slip / Ticket #</label>
        <div className={styles.row}>
          <input
            id="ticket"
            className={styles.input}
            type="text"
            placeholder="e.g., VL-123"
            value={ticketInput}
            onChange={(e) => setTicketInput(e.target.value)}
          />
          <button className={styles.btn} onClick={onRequestPickup} disabled={loading}>
            {loading ? "Requesting..." : "Request Pickup"}
          </button>
        </div>

        {err && <p className={styles.err}>{err}</p>}

        {estimate && (
          <div className={styles.estimateBox}>
            <div className={styles.line}>
              <span>Ticket</span>
              <strong>VL-{estimate.ticketId ?? ticketId}</strong>
            </div>
            <div className={styles.line}>
              <span>Time Parked</span>
              <strong>{estimate.minutesParked} min</strong>
            </div>
            <div className={styles.line}>
              <span>Hourly Rate</span>
              <strong>${dollars(estimate.amountDueCents / Math.max(1, Math.ceil(estimate.minutesParked/60)))} / hr</strong>
            </div>
            <div className={styles.total}>
              <span>Current Total (rounded up by hour)</span>
              <strong>${dollars(estimate.amountDueCents)}</strong>
            </div>
            <small className={styles.note}>
            This estimate is not a binding quote. The final amount, including any applicable taxes, fees, or adjustments, will be determined at the Valet in accordance with posted rates.
            </small>
          </div>
        )}
      </div>
    </div>
  );
}
