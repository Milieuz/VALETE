import { useEffect, useState, useContext } from 'react'
import { LotContext } from '../../context/LotContext'
import { UserContext } from '../../context/UserContext'
import styles from './ParkingBrowser.module.css'

export default function ParkingBrowser() {
  const { lotId } = useContext(LotContext)
  const { user } = useContext(UserContext)
  const roles = user?.authorities?.map(a => a.name) ?? []
  const canAddVehicle = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_VALET')

  const [spotStatus, setSpotStatus] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    fetch(`http://localhost:9000/parking-spots/status?lotId=${lotId}`)
      .then(res => {
        if (!res.ok) throw new Error(`Server returned ${res.status}`)
        return res.json()
      })
      .then(data => {
        if (cancelled) return
        const map = {}
        data.forEach(spot => {
          map[spot.spotId] = spot.available
        })
        setSpotStatus(map)
        setLoading(false)
      })
      .catch(() => {
        if (cancelled) return
        setError('Unable to load spots')
        setLoading(false)
      })

    return () => { cancelled = true }
  }, [lotId])

  const handleReserve = async spotId => {
    if (!user?.id) {
      alert('You must be logged in to reserve a spot.')
      return
    }
    const vehicleId = prompt(`Enter vehicle ID to reserve Spot ${spotId}:`)
    if (!vehicleId) return
    try {
      setBusy(spotId)
      const res = await fetch(
        `http://localhost:9000/sessions/checkin?spotId=${spotId}&userId=${user.id}&vehicleId=${vehicleId}`,
        { method: 'POST' }
      )
      if (!res.ok) {
        const txt = await res.text()
        throw new Error(txt || `Server returned ${res.status}`)
      }
      await res.json()
      setSpotStatus(prev => ({ ...prev, [spotId]: false }))
      alert(`Vehicle ${vehicleId} checked into spot ${spotId}`)
    } catch {
      alert('Failed to reserve this spot — please try again.')
    } finally {
      setBusy(null)
    }
  }

  const handleCheckout = async spotId => {
    const sessionId = prompt(`Enter the session ID for spot ${spotId}:`)
    if (!sessionId) return
    try {
      setBusy(spotId)
      const res = await fetch(
        `http://localhost:9000/valet/checkout/${sessionId}`,
        {
          method: 'POST',
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        }
      )
      if (!res.ok) {
        const txt = await res.text()
        throw new Error(txt || `Server returned ${res.status}`)
      }
      await res.json()
      setSpotStatus(prev => ({ ...prev, [spotId]: true }))
      alert(`Vehicle at spot ${spotId} has been checked out.`)
    } catch {
      alert('Failed to check out this car — please try again.')
    } finally {
      setBusy(null)
    }
  }

  const renderSpot = spotId => {
    const isAvailable = spotStatus[spotId]
    const className = `${styles.parkingSpot} ${isAvailable ? styles.spotAvailable : styles.spotOccupied}`

    return (
      <div key={spotId} id={`Spot_${spotId}`} className={className}>
        <div>{spotId}</div>

        {/* {isAvailable && canAddVehicle && (
          <button onClick={() => handleReserve(spotId)} disabled={busy === spotId}>
            {busy === spotId ? 'Reserving...' : 'Add Vehicle'}
          </button>
        )}

        {!isAvailable && (
          <button onClick={() => handleCheckout(spotId)} disabled={busy === spotId}>
            {busy === spotId ? 'Requesting Car Pickup...' : 'Request Car Pickup'}
          </button>
        )} */}
      </div>
    )
  }

  if (loading) return <div className={styles.parkingBrowser}><p>Loading spots...</p></div>
  if (error) return <div className={styles.parkingBrowser}><p>{error}</p></div>

  return (
    <div className={styles.parkingBrowser}>
      <div className={styles.parkingLot}>
        <div className={styles.parkingColumn}>
          {[1, 2, 3, 4, 5].map(renderSpot)}
        </div>
        <div className={styles.parkingColumn}>
          {[6, 7, 8, 9, 10].map(renderSpot)}
        </div>
      </div>
    </div>
  )
}
