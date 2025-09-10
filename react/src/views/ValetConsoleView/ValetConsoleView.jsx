import { useEffect, useState } from "react";
import axios from "axios";
import styles from "./ValetConsoleView.module.css";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:9000",
});
api.interceptors.request.use((config) => {
  const t = localStorage.getItem("token");
  if (t) config.headers.Authorization = `Bearer ${t}`;
  return config;
});

// ---- Pricing ----
const RATE_CENTS_PER_HOUR = 500; // $5.00/hr
//const minutes = Number(d?.minutesParked) || 0;

// formatting dollar value
const formatDollars = (cents) => (Math.max(0,Number(cents) || 0) / 100).toFixed(2);

// calculate charges
function calculateCharges(minutes, rate = RATE_CENTS_PER_HOUR) {
  const numMinutes = Math.max(0, Number(minutes) || 0);

  // Per-minute running total (rounded to nearest cent)
  const dueCents = Math.round((numMinutes * rate) / 60);

  // Rounded-up hourly total (ceil to next hour if minutes > 0)
  const roundedHours = Math.ceil(numMinutes / 60); // 0 -> 0, 1..60 -> 1, 61..120 -> 2, etc.
  const roundedCents = roundedHours * rate;

  return {
    dueCents,
    dueDollars: formatDollars(dueCents),
    roundedHours,
    roundedCents,
    roundedDollars: formatDollars(roundedCents),
  };
}

export default function ValetConsoleView() {
  const [tab, setTab] = useState("checkin");
  return (
    <div className={styles.contentWrapper}>
      <div className={styles.page}>
        <div className={styles.tabs}>
          <button onClick={()=>setTab("checkin")} className={tab==="checkin" ? styles.active : ""}>Check-In</button>
          <button onClick={()=>setTab("parked")}  className={tab==="parked"  ? styles.active : ""}>Parked Cars</button>
          <button onClick={()=>setTab("pickups")} className={tab==="pickups" ? styles.active : ""}>Pickup Queue</button>
        </div>

        {tab === "checkin" && <CheckInForm />}
        {tab === "parked"  && <ParkedList />}
        {tab === "pickups" && <PickupQueue />}
      </div>
    </div>
  );
}

/* ---------- Check-In ---------- */

function CheckInForm() {
  const [form, setForm] = useState({
    fullName: "", phoneNumber: "",
    year: "", makeName: "", modelName: "", color: "", licensePlate: "", vin: ""
  });
  const [slip, setSlip] = useState(null);
  const [msg, setMsg] = useState("");

  const onChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setMsg(""); setSlip(null);
    try {
      const payload = {
        fullName: form.fullName,
        phoneNumber: form.phoneNumber,
        year: form.year ? Number(form.year) : null,
        makeName: form.makeName,
        modelName: form.modelName,
        color: form.color,
        licensePlate: form.licensePlate,
        vin: form.vin || null
      };
      const { data } = await api.post("/valet/check-in", payload);
      setSlip(data);
    } catch (err) {
      const m = err?.response?.data?.message || "Check-in failed.";
      setMsg(m);
    }
  };

  return (
    <div className={styles.card}>
      <h2>Patron Check-In</h2>
      <form onSubmit={onSubmit} className={styles.form}>
        <div className={styles.row}>
          <label>Owner Full Name</label>
          <input name="fullName" value={form.fullName} onChange={onChange} required />
        </div>
        <div className={styles.row}>
          <label>Owner Phone</label>
          <input name="phoneNumber" value={form.phoneNumber} onChange={onChange} required />
        </div>

        <div className={styles.grid}>
          <div><label>Year</label><input name="year" value={form.year} onChange={onChange} /></div>
          <div><label>Make</label><input name="makeName" value={form.makeName} onChange={onChange} /></div>
          <div><label>Model</label><input name="modelName" value={form.modelName} onChange={onChange} /></div>
          <div><label>Color</label><input name="color" value={form.color} onChange={onChange} /></div>
          <div className={styles.span2}><label>Plate</label><input name="licensePlate" value={form.licensePlate} onChange={onChange} required /></div>
          <div className={styles.span2}><label>VIN (optional)</label><input name="vin" value={form.vin} onChange={onChange} /></div>
        </div>

        <button type="submit">Check In</button>
      </form>

      {msg && <p className={styles.error}>{msg}</p>}
      {slip && <ValetSlip slip={slip} />}
    </div>
  );
}

function ValetSlip({ slip }) {
  return (
    <div className={styles.slip}>
      <h3>Valet Slip</h3>
      <p><strong>Ticket #:</strong> {slip.ticketNumber}</p>
      <p><strong>Spot:</strong> {slip.spotNumber}</p>
      <p><strong>Plate:</strong> {slip.licensePlate}</p>
      <p><strong>Owner:</strong> {slip.ownerName} ({slip.phoneNumber})</p>
      <p><strong>Checked In:</strong> {new Date(slip.checkInTs).toLocaleString()}</p>
      <button onClick={()=>window.print()}>Print</button>
    </div>
  );
}

/* ---------- Parked Cars ---------- */
/** Utilities */
const normalizeTimestamp = (ts) => {
  if (ts == null) return 0;
  if (typeof ts === "number") return ts < 10_000_000_000 ? ts * 1000 : ts;
  if (typeof ts === "string") {
    let s = ts.replace(" ", "T").replace(/(\.\d{3})\d+/, "$1");
    if (!/[Zz]|[+\-]\d{2}:\d{2}$/.test(s)) s += "Z";
    const t = Date.parse(s);
    return Number.isFinite(t) ? t : 0;
  }
  if (typeof ts === "object" && ts.year) {
    const { year, monthValue = ts.month, dayOfMonth = ts.day, hour = 0, minute = 0, second = 0, nano = 0 } = ts;
    const ms = Math.floor(nano / 1e6);
    return Date.UTC(year, (monthValue - 1), dayOfMonth, hour, minute, second, ms);
  }
  return 0;
};
const natural = new Intl.Collator(undefined, { numeric: true, sensitivity: "base" });
const isDigits = (v) => typeof v === "string" && /^\d+$/.test(v);
const plateSan = (s = "") => s.toString().toLowerCase().replace(/[^a-z0-9]/g, "");
const parseInputDate = (s) => {
  if (!s) return null;
  const t = Date.parse(s.replace(" ", "T"));
  return Number.isFinite(t) ? t : null;
};

function ParkedList() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [q, setQ] = useState("");
  const [sortBy, setSortBy] = useState("spot"); // 'spot' | 'owner' | 'plate' | 'checkin'
  const [dir, setDir] = useState("asc");
  const [selected, setSelected] = useState(null);

  const PARKED_URL = "/valet/parked";

  // hydrate plates from ticket endpoint to guarantee same source as TicketDetails
  const hydratePlates = async (baseRows) => {
    const ids = baseRows.map(r => r.ticketId).filter(Boolean);
    const CONC = 6; // limit concurrency
    const plateMap = new Map();

    for (let i = 0; i < ids.length; i += CONC) {
      const slice = ids.slice(i, i + CONC);
      const results = await Promise.all(slice.map(async (id) => {
        try {
          const { data } = await api.get(`/valet/ticket/${id}`);
          const raw = data?.licensePlate ?? data?.plate ?? data?.license_plate ?? "";
          return [id, String(raw)];
        } catch {
          return [id, null];
        }
      }));
      for (const [id, plate] of results) plateMap.set(id, plate);
    }

    setRows(prev => prev.map(r => {
      const override = plateMap.get(r.ticketId);
      if (!override) return r;
      const raw = String(override);
      return { ...r, licensePlate: raw, plateSan: plateSan(raw) };
    }));
  };

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await api.get(PARKED_URL);

      // Base map from parked endpoint
      const mapped = (data || []).map((r) => {
        const spot  = r.spotNumber ?? r.spotId ?? r.spot ?? r.spot_id ?? "";
        const rawPlate =
          r.licensePlate ??
          r.license_plate ??
          r.licensePlateNumber ??
          r.plateNumber ??
          r.plate_number ??
          r.plate ??
          "";

        const start = r.startTime ?? r.checkInTs ?? r.checkinTs ?? r.check_in_ts ?? r.checkedInAt ?? r.createdAt ?? null;
        const checkInMs = normalizeTimestamp(start);
        const plateRaw = String(rawPlate || "");

        return {
          ticketId: r.ticketId ?? r.reservationId ?? r.id,
          spotNumber: String(spot),
          licensePlate: plateRaw,       // will be overwritten by hydratePlates
          plateSan: plateSan(plateRaw), // used for filter/sort
          ownerName: r.ownerName ?? r.fullName ?? r.name ?? "",
          makeName: r.make ?? r.makeName ?? "",
          modelName: r.model ?? r.modelName ?? "",
          checkInMs,
        };
      });

      setRows(mapped);
      // force cooperation: make list plates match TicketDetails source
      await hydratePlates(mapped);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, []);

  const filteredSorted = (() => {
    const needleRaw = q.trim().toLowerCase();

    // date tokens (work regardless of selected column)
    const afterMatch  = needleRaw.match(/after:([0-9/:T-]+(?:\s[0-9:]+)?)/i);
    const beforeMatch = needleRaw.match(/before:([0-9/:T-]+(?:\s[0-9:]+)?)/i);
    const afterMs  = parseInputDate(afterMatch?.[1]);
    const beforeMs = parseInputDate(beforeMatch?.[1]);

    const freeNeedle = needleRaw
      .replace(afterMatch?.[0] || "", "")
      .replace(beforeMatch?.[0] || "", "")
      .trim();

    const spotNeedle  = freeNeedle.replace(/^#/, "");
    const plateNeedle = plateSan(freeNeedle);

    let out = rows;

    if (needleRaw) {
      out = out.filter(r => {
        const owner = (r.ownerName || "").toLowerCase();
        const make  = (r.makeName  || "").toLowerCase();
        const model = (r.modelName || "").toLowerCase();
        const spot  = String(r.spotNumber || "").toLowerCase().replace(/^#/, "");
        const plate = r.plateSan || "";
        const t     = Number.isFinite(r.checkInMs) ? r.checkInMs : 0;

        let fieldMatch = true;
        if (freeNeedle) {
          switch (sortBy) {
            case "plate":
              fieldMatch = plate.includes(plateNeedle);
              break;
            case "spot":
              fieldMatch = spot.includes(spotNeedle);
              break;
            case "owner":
              fieldMatch = owner.includes(freeNeedle);
              break;
            case "checkin": {
              const dt = r.checkInMs ? new Date(r.checkInMs).toLocaleString().toLowerCase() : "";
              fieldMatch = dt.includes(freeNeedle);
              break;
            }
            default:
              fieldMatch =
                owner.includes(freeNeedle) ||
                make.includes(freeNeedle)  ||
                model.includes(freeNeedle) ||
                spot.includes(spotNeedle)  ||
                plate.includes(plateNeedle);
          }
        }

        const afterOk  = afterMs  ? t >= afterMs  : true;
        const beforeOk = beforeMs ? t <= beforeMs : true;

        return fieldMatch && afterOk && beforeOk;
      });
    }

    const cmp = (a, b) => {
      switch (sortBy) {
        case "spot": {
          const A = a.spotNumber || "", B = b.spotNumber || "";
          const res = (isDigits(A) && isDigits(B)) ? (Number(A) - Number(B)) : natural.compare(A, B);
          return dir === "asc" ? res : -res;
        }
        case "plate": {
          const A = a.plateSan || "", B = b.plateSan || "";
          const primary = A.localeCompare(B);
          if (primary !== 0) return dir === "asc" ? primary : -primary;
          const rawA = (a.licensePlate || "").toLowerCase();
          const rawB = (b.licensePlate || "").toLowerCase();
          const secondary = rawA.localeCompare(rawB);
          return dir === "asc" ? secondary : -secondary;
        }
        case "checkin": {
          const A = Number.isFinite(a.checkInMs) ? a.checkInMs : 0;
          const B = Number.isFinite(b.checkInMs) ? b.checkInMs : 0;
          const res = A - B;
          return dir === "asc" ? res : -res;
        }
        case "owner":
        default: {
          const A = a.ownerName || "", B = b.ownerName || "";
          const res = A.localeCompare(B, undefined, { sensitivity: "base" });
          return dir === "asc" ? res : -res;
        }
      }
    };

    return out.slice().sort(cmp);
  })();

  return (
    <div className={styles.card}>
      <h2>Parked Cars</h2>
      <p>Filter by column (Spot/Owner/Plate/Check-In Time).
        Time format: "M/DD/YYYY, H:MM:SS"
      </p><br />
      <div className={styles.toolbar}>
        <input
          placeholder="(Spot/Owner/Plate/Time)."
          value={q}
          onChange={(e)=>setQ(e.target.value)}
        />
        <select value={sortBy} onChange={(e)=>setSortBy(e.target.value)}>
          <option value="spot">Spot</option>
          <option value="owner">Owner</option>
          <option value="plate">Plate</option>
          <option value="checkin">Check-In Time</option>
        </select>
        <select value={dir} onChange={(e)=>setDir(e.target.value)}>
          <option value="asc">Asc</option>
          <option value="desc">Desc</option>
        </select>
        <button onClick={load} disabled={loading}>{loading ? "..." : "Refresh"}</button>
      </div>

      <ul className={styles.list}>
        {filteredSorted.map(r => (
          <li key={r.ticketId} onClick={()=>setSelected(r.ticketId)} className={styles.clickRow}>
            <span className={styles.badge}>#{r.spotNumber}</span>
            <span>{r.licensePlate}</span>
            <span>{r.ownerName}</span>
            <span>{r.makeName} {r.modelName}</span>
            <span className={styles.dim}>{r.checkInMs ? new Date(r.checkInMs).toLocaleString() : "—"}</span>
          </li>
        ))}
        {!loading && filteredSorted.length === 0 && (
          <li className={styles.dim} style={{padding:"12px 8px"}}>No parked cars match your filter.</li>
        )}
      </ul>

      {selected && <TicketDetails ticketId={selected} onClose={()=>setSelected(null)} />}
    </div>
  );
}

function TicketDetails({ ticketId, onClose }) {
  const [d, setD] = useState(null);

  useEffect(() => {
    (async () => {
      const { data } = await api.get(`/valet/ticket/${ticketId}`);
      setD(data);
    })();
  }, [ticketId]);

  if (!d) return null;

  const minutes = Number(d?.minutesParked) || 0;
  const { dueDollars, roundedHours, roundedDollars } = calculateCharges(minutes);

  return (
    <div className={styles.modal}>
      <div className={styles.modalBody}>
        <button className={styles.close} onClick={onClose}>×</button>
        <h3>Ticket #{d.ticketNumber}</h3>
        <p><strong>Owner:</strong> {d.ownerName} ({d.phoneNumber})</p>
        <p><strong>Vehicle:</strong> {(d.year ?? "")} {d.makeName} {d.modelName}, {d.color} — {d.licensePlate}</p>
        <p><strong>Spot:</strong> {d.spotNumber}</p>
        <p><strong>Checked In:</strong> {new Date(d.checkInTs).toLocaleString()}</p>

        {/* Per-minute running total */}
        <p>
          <strong>Running Total:</strong> {"$"}{dueDollars} ({minutes} min @ $5/hr)
        </p>

        {/* Rounded-up hourly total */}
        <p>
          <strong>Rounded Total (to hour):</strong> {"$"}{roundedDollars}
          {" "}{roundedHours > 0 ? `(${roundedHours} hr @ $5/hr)` : "(0 hr)"}
        </p>

        <div className={styles.rowBtns}>
          <button
            onClick={async () => {
              try{
                await api.post(`/valet/pickup/${ticketId}`, { notes: "Requested by valet" });
                alert("Pickup request created.");
                onClose();
              } catch (error) {
                console.error(error);
                alert(error?.response?.data?.message || "Failed to create a pickup request.");
              }
            }}
          >
            Create Pickup Request
          </button>

          <button
            onClick={async () => {
              try{
                const { data } = await api.post(`/valet/checkout/${ticketId}`);
                const finalCharge = data?.amountDueCents !== null ? 
                formatDollars(data.amountDueCents) : roundedDollars;
                alert(`Picked up. Final: $${finalCharge}`);
                onClose();
              } catch (error){
                console.error(error);
                alert(error?.response?.data?.message || "Failed to check out.");
              }
            }}
          >
            Mark Picked Up
          </button>
        </div>
      </div>
    </div>
  );
}

/* ---------- Pickup Queue ---------- */

function PickupQueue() {
  const [rows, setRows] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [completingId, setCompletingId] = useState(null);

  const load = async () => {
    setIsLoading(true);
    try{
      const { data } = await api.get("/valet/pickups");
      setRows(data);
    } finally{
      setIsLoading(false);
    }
  };
  useEffect(() => { load(); }, []);

  const complete = async (ticketId) => {
    try {
      setCompletingId(ticketId);
      const { data } = await api.post(`/valet/checkout/${ticketId}`);
      const finalCharge = data?.amountDueCents != null ? 
        formatDollars(data.amountDueCents) : "0.00";
      alert(`Picked up. Final Charge: $${finalCharge}`);
      await load();
    } catch (error) {
      console.error(error);
      alert(error?.response?.data?.message || "Failed to mark vehicle as picked up.");
    } finally {
      setCompletingId(null);
    }
  };

  return (
    <div className={styles.card}>
      <h2>Pickup Queue</h2>
      <ul className={styles.list}>
        {rows.map(r => (
          <li key={r.ticketId} className={`${styles.clickRow} ${styles.pickupRow}`}>
            <span className={styles.badge}>#{r.spotNumber}</span>
            <span>{r.licensePlate}</span>
            <span>{r.ownerName}</span>
            <span>{r.makeName} {r.modelName}</span>
            <span className={styles.dim}>Ticket {r.ticketNumber}</span>

            <div className={styles.pickupActions}>
              <button
                className={`${styles.pickupBtn} ${completingId === r.ticketId ? styles.isLoading : ""}`}
                onClick={() => complete(r.ticketId)}
                disabled={completingId === r.ticketId}
                aria-busy={completingId === r.ticketId}
              >
                {completingId === r.ticketId ? "Marking..." : "Mark Picked Up"}
              </button>

            </div>
          </li>
        ))}
        {!isLoading && rows.length === 0 && (
          <li className={styles.dim}>No open pickup requests.</li>
        )}
      </ul>
    </div>
  );
}
