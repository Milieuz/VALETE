import sideStyles from "./SideImages.module.css";
import leftImg from "../../images/valetstreetbannerro.png";
import rightImg from "../../images/rightsode.png";

export default function SideImages({ position }) {
  const src = position === "left" ? leftImg : rightImg;
  const sideClass =
    position === "left" ? sideStyles.sideLeft : sideStyles.sideRight;

  return <img src={src} alt={`${position} decoration`} className={`${sideStyles.sideImg} ${sideClass}`} />;
}