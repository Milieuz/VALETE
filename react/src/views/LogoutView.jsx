import { useContext, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { UserContext } from '../context/UserContext';
import axios from 'axios';

export default function LogoutView() {
  const { setUser } = useContext(UserContext);

  function handleLogout() {
    // Remove auth data from local storage
    localStorage.removeItem('user');
    localStorage.removeItem('token');

    // Clear auth token from axios
    delete axios.defaults.headers.common['Authorization'];

    // Clear the auth context
    setUser(null);
  }

  useEffect(() => {
    handleLogout();
  });

  return <Navigate to="/login" />;
}
