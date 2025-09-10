import { useContext } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { UserContext } from '../../context/UserContext';
import styles from './MainNav.module.css';

export default function MainNav() {
  const { user } = useContext(UserContext);

  const linkClass = ({ isActive }) =>
    [styles.navItem, isActive ? styles.active : ''].join(' ').trim();

  return (
    <header className={styles.header}>
      <nav className={styles.navList}>
        <NavLink to="/" className={linkClass}>
          Home
        </NavLink>

        {user ? (
          <>
            <NavLink to="/userProfile" className={linkClass}>
              Profile
            </NavLink>
            {user && user.authorities?.some(r => r.name === 'ROLE_VALET' || r.name === 'ROLE_ADMIN') && (
              <NavLink to="/valet" className={linkClass}>
                Valet
              </NavLink>
            )}
            {user && user.authorities?.some(r => r.name === 'ROLE_ADMIN') && (
              <NavLink to="/admin" className={linkClass}>
                Admin
              </NavLink>
            )}
            <Link to="/logout" className={[styles.navItem, styles.danger].join(' ')}>
              Logout
            </Link>
          </>
        ) : (
          <NavLink to="/login" className={[styles.navItem, styles.primary].join(' ')}>
            Login
          </NavLink>
        )}

        
      </nav>
    </header>
  );
}
