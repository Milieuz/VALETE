import { Navigate } from 'react-router-dom';
import { useContext } from 'react';
import { UserContext } from '../context/UserContext';

export default function ProtectedRoute({ children, roles }) {
  const { user } = useContext(UserContext);

  if (!user) return <Navigate to="/login" replace />;

  const userRoles = (user.authorities || []).map(a => a.name); // e.g. ["ROLE_USER","ROLE_VALET"]
  if (roles?.length) {
    const allowed = roles.some(r => userRoles.includes(r));
    if (!allowed) return <Navigate to="/unauthorized" replace />;
  }
  return children;
}
