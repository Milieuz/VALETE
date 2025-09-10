import bannerImg from '../../images/valetebannerrecolor (1).png';
import styles from './Banner.module.css';

export default function Banner() {
  return (
    <div className={styles.banner}>
      <img src={bannerImg} alt="logo banner" />
    </div>
  );
}