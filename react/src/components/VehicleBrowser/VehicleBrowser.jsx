import { useEffect, useState } from "react";
import axios from "axios";
import styles from "./VehicleBrowser.module.css";

const COLORS = [
  "", "Black", "White", "Gray", "Silver", "Blue", "Red", "Green", "Yellow",
  "Orange", "Brown", "Gold", "Beige", "Purple", "Maroon", "Other"
];

export default function VehicleBrowser() {
  const [makes, setMakes] = useState([]);
  const [selectedMake, setSelectedMake] = useState("");
  const [models, setModels] = useState([]);
  const [selectedModel, setSelectedModel] = useState("");
  const [loading, setLoading] = useState(false);

  const [color, setColor] = useState("");
  const [plate, setPlate] = useState("");
  const [vin, setVin] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    axios.get("http://localhost:9000/vehicles")
      .then(response => setMakes(response.data))
      .catch(error => console.error("Error loading makes:", error));
  }, []);

  useEffect(() => {
    if (selectedMake) {
      setLoading(true);
      axios.get(`http://localhost:9000/vehicles/${selectedMake}/models`)
        .then(response => setModels(response.data))
        .catch(error => console.error("Error loading models:", error))
        .finally(() => setLoading(false));
    } else {
      setModels([]);
    }
  }, [selectedMake]);

  const validate = () => {
    if (!selectedMake) return "Please choose a make.";
    if (!selectedModel) return "Please choose a model.";
    if (!color) return "Please choose a color.";
    if (!plate) return "Please enter a license plate.";
    if (vin && vin.length !== 17) return "VIN must be exactly 17 characters.";
    return "";
  };

  const handleSubmit = async () => {
    const message = validate();
    if (message) {
      setError(message);
      return;
    }
    setError("");
  
    const payload = {
      makeName: selectedMake,
      modelName: selectedModel,
      color,
      licensePlate: plate.toUpperCase().trim(),
      vin: vin ? vin.toUpperCase().trim() : null
    };
  
    try {
      setLoading(true);
      const res = await axios.post("http://localhost:9000/vehicles/save", payload, {
        headers: {
          "Content-Type": "application/json"
          // Authorization or JWT goes here
        }
      });
  
      console.log("Saved vehicle:", res.data);
      alert("Vehicle saved!");
      window.location.reload();
  
      // Clear form or leave make/model
      setSelectedMake("");
      setModels([]);
      setSelectedModel("");
      setColor("");
      setPlate("");
      setVin("");
    } catch (err) {
      console.error("Error saving vehicle:", err);
      setError("Could not save vehicle. Please try again.");
    } finally {
      setLoading(false);
    }
  };
  

  return (
    <div className={styles.wrapper}>
      <h1 className={styles.title}>Vehicle Make/Model Browser<br />🏍️  🚐  🚛  🚗  🏎️  🚕  🚙  🚚  🛵  🛻</h1>

      <label className={styles.label}>Select a Make:</label>
      <select
        className={styles.select}
        value={selectedMake}
        onChange={e => {
          setSelectedMake(e.target.value);
          setSelectedModel("");
        }}
      >
        <option value="">-- Choose a Make --</option>
        {makes.map(make => (
          <option key={make.makeId || make.Make_ID} value={make.makeName || make.Make_Name}>
            {make.makeName || make.Make_Name}
          </option>
        ))}
      </select>

      {loading ? (
        <p className={styles.loading}>Loading models...</p>
      ) : selectedMake && (
        <>
          <label className={styles.label}>Select a Model:</label>
          <select
            className={styles.select}
            value={selectedModel}
            onChange={e => setSelectedModel(e.target.value)}
          >
            <option value="">-- Choose a Model --</option>
            {models.map((model, idx) => (
              <option key={idx} value={model.modelName || model.Model_Name}>
                {model.modelName || model.Model_Name}
              </option>
            ))}
          </select>

          <label className={styles.label}>Color:</label>
          <select
            className={styles.select}
            value={color}
            onChange={e => setColor(e.target.value)}
          >
            {COLORS.map(c => (
              <option key={c || 'none'} value={c}>
                {c || "-- Choose a Color --"}
              </option>
            ))}
          </select>

          <label className={styles.label}>License Plate:</label>
          <input
            className={styles.input}
            value={plate}
            onChange={e => setPlate(e.target.value.toUpperCase())}
            placeholder="ABC 123"
            maxLength={10}
          />

          <label className={styles.label}>VIN (17 chars):</label>
          <input
            className={styles.input}
            value={vin}
            onChange={e => setVin(e.target.value.toUpperCase().replace(/[IOQ]/g, ""))}
            placeholder="1HGCM82633A004352"
            maxLength={17}
          />

          {error && <div className={styles.error}>{error}</div>}

          <button className={styles.button} onClick={handleSubmit}>
            Save Vehicle
          </button>
        </>
      )}
    </div>
  );
}
