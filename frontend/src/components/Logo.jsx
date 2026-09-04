import { LOGO_URL, LOGO_URL_WHITE } from "../lib/logo.js";

function Logo({ size = 40, bg, style }) {
  const src = bg === "#fff" ? LOGO_URL_WHITE : LOGO_URL;
  return <img src={src} alt="MOAMOA 로고" style={{ width: size, height: size, objectFit: "contain", ...style }} />;
}

export default Logo;
