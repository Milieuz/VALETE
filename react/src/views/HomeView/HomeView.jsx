import ParkingBrowser from "../../components/ParkingBrowser/ParkingBrowser";
import AvailabilityWidget from "../../components/AvailabilityWidget/AvailabilityWidget.jsx";
import LotSwitcher from "../../components/LotSwitcher/LotSwitcher.jsx";
import styles from './HomeView.module.css';
import bannerImg from "../../images/valetebannerrecolor (1).png";
export default function HomeView() {

  return (
    <div id="view-home">
      <div className={styles.contentWrapper}>
        <div>
          <h1 className={styles.homeMainHeading}>🅿️Parking Lot Valet🚦</h1>
          <br />
          <h2 className={styles.homeSubheading}>Welcome to the home page!</h2>
          <p>Parking Price: $5/hr</p>
          <ParkingBrowser />
          <AvailabilityWidget />
          <LotSwitcher />
        </div>
      </div>
    </div>
  );
}