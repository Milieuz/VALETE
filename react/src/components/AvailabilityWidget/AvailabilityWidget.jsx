import { useEffect, useState, useContext } from "react";
import { LotContext } from "../../context/LotContext";

function AvailabilityWidget() {
  const { lotId } = useContext(LotContext);

  const [data, setData] = useState({ availableCount: null, isFull: null });

  useEffect(() => {
    fetch(`http://localhost:9000/available/count?lotId=${lotId}`)
      .then(res => res.json())
      .then(count => setData(prev => ({ ...prev, availableCount: count })))
      .catch(err => console.error("Count fetch error:", err));

    fetch(`http://localhost:9000/available/full?lotId=${lotId}`)
      .then(res => res.json())
      .then(isFull => setData(prev => ({ ...prev, isFull })))
      .catch(err => console.error("Full fetch error:", err));
  }, [lotId]);

  return (
    <div>
      {/* Swapped: status message first */}
      <p>
        {data.isFull === null
          ? ""
          : data.isFull
            ? "Lot is full"
            : "Spots are available"}
      </p>

      <h2>
        {data.availableCount !== null
          ? `Available Spots: ${data.availableCount}`
          : "Loading..."}
      </h2>
    </div>
  );
}

export default AvailabilityWidget;
