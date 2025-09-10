import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../../services/AuthService';
import Notification from '../../components/Notification/Notification';

import styles from './RegisterView.module.css';

export default function RegisterView() {
  const navigate = useNavigate();

  const [notification, setNotification] = useState(null);

  // Setup state for the registration data
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('');
  const [secretCode, setSecretCode] = useState('');

  function handleSubmit(event) {
    event.preventDefault();


    // Validate the form data
    if (password !== confirmPassword) {
      // Passwords don't match, so display error notification
      setNotification({ type: 'error', message: 'Passwords do not match.' });
    } else {

      const payload = {
        username,
        password,
        confirmPassword,
        role,
        ...(role === 'valet' ? { secretCode } : {})
      };

      // If no errors, send data to server
      AuthService.register(payload)
        .then(() => {
          setNotification({ type: 'success', message: 'Registration successful' });
          navigate('/login');
        })
        .catch((error) => {
          // Check for a response message, but display a default if that doesn't exist
          const message = error.response?.data?.message || 'Registration failed.';
          setNotification({ type: 'error', message: message });
        });
    }
  }

  return (
    <>
    <div id="view-register">
      <h2>Register</h2>

      <Notification notification={notification} clearNotification={() => setNotification(null)} />

      <form onSubmit={handleSubmit}>
        <div className={styles.formControl}>
          <label htmlFor="username">Username:</label>
          <input
            type="text"
            id="username"
            value={username}
            size="50"
            required
            autoFocus
            autoComplete="username"
            onChange={(event) => setUsername(event.target.value)}
          />
        </div>

        <div className={styles.formControl}>
          <label htmlFor="password">Password:</label>
          <input
            type="password"
            id="password"
            value={password}
            size="50"
            required
            onChange={(event) => setPassword(event.target.value)}
          />
        </div>

        <div className={styles.formControl}>
          <label htmlFor="confirmPassword">Confirm Password:</label>
          <input
            type="password"
            id="confirmPassword"
            value={confirmPassword}
            size="50"
            required
            onChange={(event) => setConfirmPassword(event.target.value)}
          />
        </div>

        {/* 20250808 -- Additional dropdown menu to choose role 
            If valet role is chosen, a textbox pops up to ask for 'secret code' */}
        <div className={styles.radioContainer}>
          <label>User Role: </label>
          <label htmlFor="role">Patron</label>
          <input
            type="radio"
            name="rolechoice"
            id="patron"
            value="patron"
            size="50"
            required
            onChange={(event) => setRole(event.target.value)}
          />
          <label htmlFor="role">Valet</label>
          <input
            type="radio"
            name="rolechoice"
            id="valet"
            value="valet"
            size="50"
            required
            onChange={(event) => setRole(event.target.value)}
          />
          {/* <label htmlFor="role">Admin</label>
          <input
            type="radio"
            name="rolechoice"
            id="admin"
            value="admin"
            size="50"
            required
            onChange={(event) => setRole(event.target.value)}
          /> */}
        </div>
        
        {/* Conditional rendering of the secret code prompt only for valet role registration */}
        {role === 'valet' && (
          <div className={styles.formControl}>
            <label htmlFor="secretCode">Valet Registration Code:</label>
            <input
              type="password"
              id="secretCode"
              value={secretCode}
              required
              onChange={event => setSecretCode(event.target.value)} 
            />
          </div>
        )}

<button type="submit" className={`btn-primary ${styles.formButton}`}>Register</button>
        <Link to="/login" className={styles.registerLink}>Have an account? Log-in</Link>
      </form>
    </div>
    </>
  );
}
