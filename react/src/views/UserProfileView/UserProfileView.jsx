import { useContext, useEffect, useState } from "react";
import { UserContext } from "../../context/UserContext";
import VehicleBrowser from "../../components/VehicleBrowser/VehicleBrowser";
import PatronPickupView from "../PatronPickupView/PatronPickupView";
import axios from "axios";
import styles from "./UserProfileView.module.css";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:9000",
});
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default function UserProfileView() {
  const { user } = useContext(UserContext);

  const [vehicles, setVehicles] = useState([]);
  const [loadingVehicles, setLoadingVehicles] = useState(true);
  const [vehErr, setVehErr] = useState("");

  const [profile, setProfile] = useState({
    username: "",
    fullName: "",
    phoneNumber: ""
  });
  const [saving, setSaving] = useState(false);
  const [profileMsg, setProfileMsg] = useState("");

  useEffect(() => {
    let active = true;

    // Load profile
    (async () => {
      try {
        const { data } = await api.get("/users/me");
        if (!active) return;
        setProfile({
          username: data?.username ?? "",
          fullName: data?.fullName ?? "",
          phoneNumber: data?.phoneNumber ?? ""
        });
      } catch (e) {
        // ignore or show message
      }
    })();

    // Load vehicles
    (async () => {
      try {
        const { data } = await api.get("/vehicles/mine");
        if (active) setVehicles(data || []);
      } catch (e) {
        if (active) setVehErr("Failed to load vehicles.");
      } finally {
        if (active) setLoadingVehicles(false);
      }
    })();

    return () => { active = false; };
  }, []);

  const onChange = (e) => {
    const { name, value } = e.target;
    setProfile((p) => ({ ...p, [name]: value }));
  };

  const onSaveProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    setProfileMsg("");
    try {
      const payload = {
        fullName: profile.fullName || null,
        phoneNumber: profile.phoneNumber || null
      };
      const { data } = await api.put("/users/me", payload);
      // reflect returned values
      setProfile((p) => ({
        ...p,
        fullName: data?.fullName ?? "",
        phoneNumber: data?.phoneNumber ?? ""
      }));
      setProfileMsg("Profile saved ✔︎");
    } catch (err) {
      setProfileMsg("Save failed. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className={styles.contentWrapper}>
    <div>
      <h1 className={styles.profileHeading}>👤 User Profile 👤</h1>
      <h2 className={styles.profileSubheading}>Hello, {user?.username ?? profile.username ?? "guest"}!</h2>

      {/* <h2 className={styles.sectionHeading}>Your Profile:</h2> */}
      <form onSubmit={onSaveProfile} className={styles.profileForm}>
        <div className={styles.formRow}>
          <label>Full Name</label>
          <input
            type="text"
            name="fullName"
            value={profile.fullName}
            onChange={onChange}
            placeholder="Jane Q. Driver"
          />
        </div>

        <div className={styles.formRow}>
          <label>Phone Number</label>
          <input
            type="tel"
            name="phoneNumber"
            value={profile.phoneNumber}
            onChange={onChange}
            placeholder="(555) 123-4567"
          />
        </div>

        <button type="submit" disabled={saving}>
          {saving ? "Saving..." : "Save Profile"}
        </button>
        {profileMsg && <p className={styles.statusMsg}>{profileMsg}</p>}
      </form>

      {/* <hr style={{ margin: "1.5rem 0" }} /> */}

      <h2>Your Vehicles:</h2>
      {loadingVehicles && <p>Loading…</p>}
      {vehErr && <p style={{ color: "red" }}>{vehErr}</p>}
      {!loadingVehicles && !vehErr && vehicles.length === 0 && <p>No vehicles saved yet.</p>}
      {!loadingVehicles && !vehErr && vehicles.length > 0 && (
        <div className={styles.vehicleList}>
          <ol>
            {vehicles.map(v => (
              <li key={v.id}>
                {v.year ? `${v.year} ` : ""}{v.makeName} - {v.modelName}
                {v.color ? ` - ${v.color}` : ""}
                {v.licensePlate ? ` - Plate: ${v.licensePlate}` : ""}
              </li>
            ))}
          </ol>
        </div>
      )}

      {/* <hr style={{ margin: "1.5rem 0" }} /> */}

      <section style={{ marginTop: "2rem" }}>
        <h2>Your Vehicle Browser:</h2>
        <VehicleBrowser />
        <PatronPickupView />
      </section>
    </div>
    </div>
    </div>
  );
}
