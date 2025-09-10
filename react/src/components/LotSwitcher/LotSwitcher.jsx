import { useContext } from "react";
import { LotContext } from "../../context/LotContext";
import styles from "./LotSwitcher.module.css";

export default function LotSwitcher() {
  const { lotId, setLotId } = useContext(LotContext);

  return (
    <div className={styles.switcher}>
      <label htmlFor="lotSelect">🚗 Choose a Parking Lot:</label>
      <select
        id="lotSelect"
        value={lotId}
        onChange={e => setLotId(Number(e.target.value))}
      >
        {Array.from({ length: 10 }, (_, i) => (
          <option key={i + 1} value={i + 1}>
            Lot {i + 1}
          </option>
        ))}
      </select>
    </div>
  );
}
